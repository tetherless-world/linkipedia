/******************************************************************************
* Copyright 2013-2016 LASIGE                                                  *
*                                                                             *
* Licensed under the Apache License, Version 2.0 (the "License"); you may     *
* not use this file except in compliance with the License. You may obtain a   *
* copy of the License at http://www.apache.org/licenses/LICENSE-2.0           *
*                                                                             *
* Unless required by applicable law or agreed to in writing, software         *
* distributed under the License is distributed on an "AS IS" BASIS,           *
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    *
* See the License for the specific language governing permissions and         *
* limitations under the License.                                              *
*                                                                             *
*******************************************************************************
* Test-runs AgreementMakerLight in Eclipse.                                   *
*                                                                             *
* @author Daniel Faria                                                        *
******************************************************************************/
package aml;

public class Test
{

//Main Method
	
	public static void main(String[] args) throws Exception
	{
		//Path to input ontology files (edit manually)
		String sourcePath = "D:/Extended E Drive/DataONE/Linkipedia_OntologyMatching/newowl.owl";
		String targetPath = "D:/Extended E Drive/DataONE/Linkipedia_OntologyMatching/ECSO3.owl";
		String referencePath = "";
		//Path to save output alignment (edit manually, or leave blank for no evaluation)
		String outputPath = "";
		
		
		AML aml = AML.getInstance();
		aml.openOntologies(sourcePath, targetPath);
		aml.matchAuto();
		
		if(!referencePath.equals(""))
		{
			aml.openReferenceAlignment(referencePath);
			aml.getReferenceAlignment();
			aml.evaluate();
			System.out.println(aml.getEvaluation());
		}
		if(!outputPath.equals(""))
			aml.saveAlignmentRDF(outputPath);
	}
}