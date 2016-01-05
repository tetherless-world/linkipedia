/**
 * Linkipedia, Copyright (c) 2015 Tetherless World Constellation 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.rpi.tw.linkipedia.search.main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.rpi.tw.linkipedia.search.experiments.RecognizerExperiment;
import edu.rpi.tw.linkipedia.search.main.helper.EntityServletContext;
import edu.rpi.tw.linkipedia.search.main.helper.IndexData;
import edu.rpi.tw.linkipedia.search.main.helper.Preprocessor;
import edu.rpi.tw.linkipedia.search.main.helper.ReadIndex;
import edu.rpi.tw.linkipedia.search.main.helper.Searcher;
import edu.rpi.tw.linkipedia.search.recognizer.EntityRecognizer;
import edu.rpi.tw.linkipedia.search.similarity.Entropy;



public class EAL {

	public static void main(String [] args){
		
		//need remove
		
		if(args.length < 1){
			System.err.println("usage: ELA.jar Action check help");
			return;
		}

		String method = args[0];
		//method = "ReadIndex";
		if(method.equals("help")){
			System.out.println("Action:");
			System.out.println("SurfaceFormSearch");
			System.out.println("EntitySearch");
			System.out.println("EntityLinking");
			System.out.println("ReadIndex");
			System.out.println("indexSurfaceForm");
			System.out.println("indexEntity");
			System.out.println("preprocessAll");
			System.out.println("addInverseProperty");
			System.out.println("removeChars");
			System.out.println("getLabelsFromURL");
			System.out.println("computeEntityWeights");
			System.out.println("computeEntityEntropyWeights");
			System.out.println("computePropertyEntropy");
			System.out.println("processValueRestriction");
			System.out.println("processValueRestrictionAll");
			System.out.println("startServlet");
			System.out.println("getSurfaceFormData");
			System.out.println("updateEntityIndex");
			
		}else if(method.equals("SurfaceFormSearch")){
			if(args.length < 2){
				System.err.println("usage: SurfaceFormSearch index");				
			}
			String index = args[1];
			Searcher.SurfaceFormSearch(index);			
		}else if(method.equals("UpdateKB")){
			if(args.length < 5){
				System.err.println("usage: UpdateEntityIndex inputDirectory entityIndexName surfaceFormIndex propertyWeight entityWeight");
				System.err.println("usage: inputFile are in ntriple format, sorted by subject");
				return;			
			}	
			String inputDir = args[1];
			String entityIndex = args[2];
			String surfaceFormIndex = args[3];
			String propertyWeight = args[4];
			String entityWeight = args[5];
			IndexData.updateEntityIndex(inputDir, entityIndex, surfaceFormIndex, propertyWeight, entityWeight);			
		}else if(method.equals("EntitySearch")){	
			if(args.length < 2){
				System.err.println("usage: EntitySearch index");				
			}
			String index = args[1];
			Searcher.EntitySearch(index);
		}else if(method.equals("EntityLinking")){	
			if(args.length < 2){
				System.err.println("usage: EntityLinking index");				
			}
			String index = args[1];
			Searcher.EntityLinking(index);
		}else if(method.equals("ReadIndex")){
			if(args.length < 1){
				System.err.println("usage: ReadIndex index");
				return;
			}
			String index = args[1];
			ReadIndex.readIndex(index);
		}else if(method.equals("indexSurfaceForm")){
			if(args.length < 5){
				System.err.println("usage: indexSurfaceForm inputDirectory/inputFile outputIndexName entityweightfile propertyweightfile");
				System.err.println("usage: inputFile are in ntriple format, preprocessed using preprocessors");
				return;			
			}
			String inputfile = args[1];
			String outputfile = args[2];
			String entityweightfile = args[3];
			String propertyweightfile = args[4];
			IndexData.indexSurfaceForm(inputfile, outputfile, entityweightfile, propertyweightfile);
		}else if(method.equals("indexEntity")){
			if(args.length < 5){
				System.err.println("usage: indexEntity inputDirectory outputIndexName surfaceFormIndex propertyWeight");
				System.err.println("usage: inputFile are in ntriple format, sorted by subject");
				return;			
			}	
			String inputfile = args[1];
			String outputfile = args[2];
			String surfaceFormIndex = args[3];
			String propertyWeight = args[4];
			IndexData.indexEntity(inputfile, outputfile, surfaceFormIndex, propertyWeight);
		}else if(method.equals("getSurfaceFormData")){
			if(args.length < 4){
				System.err.println("usage: getSurfaceFormData inputfile output_unsort_file surfaceFormPropertiesFile");
				System.err.println("input file (ntriple format)");
				return;			
			}	
			String inputfile = args[1];
			String outputUnsort = args[2];
			String surfaceFormPropertiesFile = args[3];
			
			ArrayList<String> surfaceFormPorperties = new ArrayList<String>();
			try{

				FileInputStream fstream = new FileInputStream(surfaceFormPropertiesFile);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;
				while ((strLine = br.readLine()) != null)   {
					surfaceFormPorperties.add(strLine);
					System.out.println("Get surface form property: "+strLine);
				}			
			}catch(Exception e){
				e.printStackTrace();
			}
			
			Preprocessor.getSurfaceFormData(inputfile, surfaceFormPorperties, outputUnsort);		
		}else if(method.equals("preprocessAll")){
			if(args.length < 4){
				System.err.println("usage: preprocessAll inputfile output_unsort_file surfaceFormPropertiesFile");
				System.err.println("input file (ntriple format)");
				return;			
			}	
			String inputfile = args[1];
			String outputUnsort = args[2];
			String surfaceFormPropertiesFile = args[3];
			Preprocessor.preprocessAll(inputfile, outputUnsort, surfaceFormPropertiesFile);
		}else if(method.equals("addInverseProperty")){
			if(args.length < 3){
				System.err.println("usage: addInverseProperty inputfile outputfile");
				System.err.println("input file (ntriple format)");
				return;			
			}	
			String inputfile = args[1];
			String outputfile = args[2];
			Preprocessor.addInverseProperty(inputfile, outputfile);
		}else if(method.equals("removeChars")){
			if(args.length < 3){
				System.err.println("usage: removeChars inputfile outputfile");
				System.err.println("input file (ntriple format)");
				return;			
			}		
			String inputfile = args[1];
			String outputfile = args[2];
			Preprocessor.removeChars(inputfile, outputfile);
		}else if(method.equals("getLabelsFromURL")){
			if(args.length < 3){
				System.err.println("usage: getLabelsFromURL inputfile outputfile");
				System.err.println("input file (ntriple format)");
				return;			
			}	
			String inputfile = args[1];
			String outputfile = args[2];
			Preprocessor.getLabelsFromURL(inputfile, outputfile);
		}else if(method.equals("computeEntityWeights")){
			if(args.length < 3){
				System.err.println("usage: computeEntityWeights inputfile outputfile (use computeEntityEntropyWeight if propertyWeightfile is obtained using computePropertyEntropy)");
				System.err.println("usage: input file is a list of objects (not ntriple file) that other entities is pointed to");
				return;			
			}	
			String inputfile = args[1];
			String outputfile = args[2];
			Preprocessor.computeEntityWeights(inputfile, outputfile);
		}else if(method.equals("computeEntityEntropyWeights")){
			if(args.length < 4){
				System.err.println("usage: computeEntityEntropyWeights inputfile outputfile propertyWeightfile");
				System.err.println("usage: input file (ntriple format) is sorted by object, assume propertyWeight/entropy computed");
				return;			
			}	
			String inputfile = args[1];
			String outputfile = args[2];
			String weightFile = args[3];
			Preprocessor.computeEntityEntropyWeights(inputfile, outputfile, weightFile);
		}else if(method.equals("computePropertyEntropy")){
			if(args.length < 3){
				System.err.println("usage: computePropertyEntropy inputfile outputfile");
				System.err.println("input file (ntriple format) is sorted by property");
				return;			
			}	
			String inputfile = args[1];
			String outputfile = args[2];
			Preprocessor.computePropertyEntropy(inputfile, outputfile);
		}else if(method.equals("computeNormalizedPropertyEntropy")){
			if(args.length < 3){
				System.err.println("usage: computePropertyEntropy inputfile outputfile");
				System.err.println("input file (ntriple format) is sorted by property");
				return;			
			}	
			String inputfile = args[1];
			String outputfile = args[2];
			Entropy.computePropertyEntropy(inputfile, outputfile);
		}else if(method.equals("processValueRestriction")){
			if(args.length < 3){
				System.err.println("usage: processValueRestriction inputfile outputfile");
				System.err.println("input file (ntriple format)");
				return;			
			}	
			String inputfile = args[1];
			String outputfile = args[2];
			Preprocessor.processValueRestriction(inputfile, outputfile);
		}else if(method.equals("processValueRestrictionAll")){
			if(args.length < 3){
				System.err.println("usage: processValueRestrictionAll inputfolder outputfolder");
				System.err.println("input file (ntriple format)");
				return;			
			}	
			String inputfile = args[1];
			String outputfile = args[2];
			File folder = new File(inputfile);
			
			String currentDir = "";
			if(inputfile.endsWith("/")){
				currentDir = inputfile;
			}else{
				currentDir = inputfile+"/";
			}
			
			String outputDir = "";
			if(outputfile.endsWith("/")){
				outputDir = outputfile;
			}else{
				outputDir = outputfile+"/";
			}
			int count = 1;
//			boolean processing = false;
		    for (final File fileEntry : folder.listFiles()) {   	
		    	String filename = fileEntry.getName();
		    	System.out.println(count+" "+filename);
		    	count++;
//		    	if(filename.equals("RXNORM.ttl.nt")){
//		    		processing = true;
//		    	}
//		    	if(processing){
		    		Preprocessor.processValueRestriction(currentDir+filename, outputDir+filename+"-new.nt");
//		    	}
		    }
		}else if(method.equals("startServlet")){
			if(args.length < 3){
				System.err.println("usage: startServlet index service_base port(optional)");
				return;
			}
			String index = args[1];	
			String surfaceFormIndex = args[2];
			int port = 8080;
			
			if(args.length == 4){
				port = Integer.parseInt(args[3]);
			}	
			EntityServletContext.startServlet(index, surfaceFormIndex, port);
		}else if(method.equals("runExperiment")){//temp
			if(args.length < 4){
				System.err.println("usage: runExperiment index inputfile outputfile");
				return;
			}		
			String index = args[1];
			String inputfile = args[2];
			String outputfile = args[3];
			
			RecognizerExperiment.runExperiment(index, inputfile, outputfile);
		}else if(method.equals("entityRecognize")){
			if(args.length < 1){
				System.err.println("usage: entityRecognize index");
				return;
			}
			String index = args[1];
			EntityRecognizer er = new EntityRecognizer(index);
			while(true){
			    BufferedReader in = null;
			    String text = "";
			    try {
			    	in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
					text = in.readLine();
					ArrayList<String> mentions = er.getEntityMentions(text);
					for(int i = 0 ; i < mentions.size(); i++){
						System.out.println(mentions.get(i));
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		}
		else{
			System.err.println("Your command is not recognized.");
		}
	}

}
