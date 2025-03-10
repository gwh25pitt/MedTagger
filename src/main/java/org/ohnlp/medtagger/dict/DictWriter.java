/*******************************************************************************
 * Copyright: (c)  2013  Mayo Foundation for Medical Education and 
 *  Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 *  triple-shield Mayo logo are trademarks and service marks of MFMER.
 *  
 *  Except as contained in the copyright notice above, or as used to identify 
 *  MFMER as the author of this software, the trade names, trademarks, service
 *  marks, or product names of the copyright holder shall not be used in
 *  advertising, promotion or otherwise in connection with this software without
 *  prior written authorization of the copyright holder.
 *   
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *   
 *  http://www.apache.org/licenses/LICENSE-2.0 
 *   
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and 
 *  limitations under the License. 
 *******************************************************************************/
package org.ohnlp.medtagger.dict;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.resource.ResourceInitializationException;
import org.ohnlp.typesystem.type.syntax.BaseToken;
import org.ohnlp.typesystem.type.syntax.NumToken;
import org.ohnlp.typesystem.type.syntax.PunctuationToken;
import org.ohnlp.typesystem.type.syntax.WordToken;
import org.ohnlp.medtagger.type.DictContext;

public class DictWriter extends JCasAnnotator_ImplBase {

	public static final String PARAM_DICTOUTPUT = "DictOutput";
	private PrintWriter pwr = null;
	private HashSet<String> processed = new HashSet<String>();

	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		File dictOutput = new File(((String) aContext.getConfigParameterValue(PARAM_DICTOUTPUT)).trim());
		try {
			pwr = new PrintWriter(dictOutput);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
			JFSIndexRepository indexes = jCas.getJFSIndexRepository();
			Iterator<?> itr = indexes.getAnnotationIndex(BaseToken.type).iterator();
			String ret="";
			while (itr.hasNext()){
				BaseToken token = (BaseToken) itr.next();
				if(token instanceof WordToken){
					if(((WordToken)token).getCanonicalForm()!=null)
						ret+=((WordToken)token).getCanonicalForm()+"\t";
				}
				else if(token instanceof NumToken){
					ret+=((NumToken)token).getCoveredText()+"\t";
				} 
				else if(token instanceof PunctuationToken){
					String tktext=token.getCoveredText();
					if(tktext.equals(">") ||tktext.equals("<") || tktext.equals("="))
					ret+=tktext+"\t";
				} 
			}
			
			ret=ret.trim()+"|";
			if(ret.equals("|")) return;
			itr = indexes.getAnnotationIndex(DictContext.type).iterator();
			while (itr.hasNext()){
				DictContext source = ((DictContext)itr.next());
				ret+=source.getEntry();
			}
			
			//capturing multiple expansions where relevant
			if(processed.contains(ret)) return;
			else processed.add(ret);
			
			pwr.println(ret);
			pwr.flush();
	}


	@Override
	public void destroy() {
		super.destroy();
		pwr.close();
		System.out.println("Finished printed in the dictionary");
	}
}
