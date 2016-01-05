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

package edu.rpi.tw.linkipedia.search.experiments;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import edu.rpi.tw.linkipedia.search.searching.EntityLinker;
import edu.rpi.tw.linkipedia.search.utils.Annotation;

public class RecognizerExperiment {
	

	public static void main(String [] args) throws Exception{
//		if(args.length < 2){
//			System.err.println("Experiment index inputfile");
//			return;
//		}
		 String index = "Dataset/entity_index";//args[0];
		 String inputfile = "EntityRecognitionDataset/truth1.txt";//args[1];
//		 TruthFile truthFile = new TruthFile(inputfile);
		 runExperiment(index,inputfile,"truth1result");
	}
	public static void runExperiment(String index, String inputfile, String outputfile){
		try{
		FileWriter ofstream = new FileWriter(outputfile);
		BufferedWriter out = new BufferedWriter(ofstream);

		 TruthFile truthFile = new TruthFile(inputfile);
		 HashMap<String, ArrayList<Annotation>> truthTables = truthFile.parse();
		 EntityLinker linker = new EntityLinker(index);
//		 truthFile.log("There are " + truthTables.size() + "truth files.");
		 double totalCount = 0;
		 double correctCount = 0;
		 ArrayList<String> termArray = new ArrayList<String>();
		 for(String key:truthTables.keySet()){
			 //current file truth
			 ArrayList<Annotation> currentTruth = truthTables.get(key);
			 
			 //get terms
			 for(Annotation anno:currentTruth){
				 String thisTerm = anno.getKeyword();
				 termArray.add(thisTerm);
//				 System.out.println(thisTerm);
//				 System.out.println(anno.getAnnotation());
			 }
			 String [] terms = termArray.toArray(new String[termArray.size()]);
			 for(int i = 1; i < terms.length; i++){
				 terms[i] = terms[i].trim();
			 }
			 
			 //search each term
			 for(int j = 0 ; j < terms.length; j++){
				 totalCount++;
				 ArrayList<Annotation> myannotations = linker.mysearch(terms[j], terms, 1);
				 
				 //validate answer
				 for(int i = 0; i < myannotations.size(); i++){
					 boolean foundTruth = false;
					 for(int k = 0 ; k < currentTruth.size(); k++){
						 System.out.println(currentTruth.get(k).getKeyword()+" vs "+myannotations.get(i).getKeyword());
							if(currentTruth.get(k).getKeyword().equals(myannotations.get(i).getKeyword())){
								//System.out.println(annotations.get(i).getKeyword()+" "+annotations.get(i).getAnnotation());
								if(currentTruth.get(k).getAnnotation().equals(myannotations.get(i).getAnnotation())){
									out.write(myannotations.get(i).getKeyword()+" "+myannotations.get(i).getAnnotation()+" 1\n");
									correctCount++;
								}else{
									out.write(myannotations.get(i).getKeyword()+" "+myannotations.get(i).getAnnotation()+" 2\n");
									//correctCount++;
								}
								
								foundTruth=true;
								break;
							}
						}
						if(!foundTruth){
							System.err.println(myannotations.get(i).getKeyword()+" "+myannotations.get(i).getAnnotation());
							out.write(myannotations.get(i).getKeyword()+" "+myannotations.get(i).getAnnotation()+" 0\n");
						}
					}
			    }
		 }
		 System.out.println(""+(correctCount/totalCount)+"\n");
		 out.write(""+(correctCount/totalCount)+"\n");
		 out.flush();
		 out.close();
		 /*
		 Iterator itr1 = truthTables.entrySet().iterator();
		 EntityLinker linker = new EntityLinker(index);
		 int totalCount = 0;
		 int correctCount = 0;
		 while (itr1.hasNext()) {
			 Map.Entry truthTable = (Map.Entry) itr1.next();
//			 truthFile.log("\nFile \"" + truthTable.getKey() + "\":");
			 HashMap tableContents = (HashMap) truthTable.getValue();
			 Iterator itr2 = tableContents.entrySet().iterator();
			 
			 ArrayList<String> termArray = new ArrayList<String>();
			 while (itr2.hasNext()) {
				 Map.Entry tableEntry = (Map.Entry)itr2.next();
				 String thisTerm = (String) tableEntry.getKey();
				 thisTerm = thisTerm.toLowerCase();
				 truthFile.log(thisTerm);// + ": " + tableEntry.getValue());
				 termArray.add(thisTerm);
			 }
			 
			 String [] terms = termArray.toArray(new String[termArray.size()]);
			    for(int i = 1; i < terms.length; i++){
			    	terms[i] = terms[i].trim();
			    }
			    for(int j = 0 ; j < terms.length; j++){
			    	totalCount++;
					ArrayList<Annotation> annotations = linker.mysearch(terms[j], terms, 1);
					for(int i = 0; i < annotations.size(); i++){
						//out.write(annotations.get(i)+"\n");
						if(tableContents.get(terms[j]).equals(annotations.get(i).getAnnotation())){
							System.out.println(annotations.get(i).getKeyword()+" "+annotations.get(i).getAnnotation());
							correctCount++;
						}else{
							System.err.println(annotations.get(i).getKeyword()+" "+annotations.get(i).getAnnotation());
						}
					}
			    }
			    
		 }*/
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/*
	public static void runExperiment(String [] terms, String outputFile, String index) throws Exception{
		FileWriter ofstream = new FileWriter(outputFile);
		BufferedWriter out = new BufferedWriter(ofstream);
		EntityLinker linker = new EntityLinker(index);
	    for(int i = 1; i < terms.length; i++){
	    	terms[i] = terms[i].trim();
	    }
	    for(int j = 0 ; j < terms.length; j++){
			ArrayList<Annotation> annotations = linker.mysearch(terms[j], terms, 1);
			for(int i = 0; i < annotations.size(); i++){
				out.write(annotations.get(i).toString());
				out.write("\n");
			}
//			System.out.println();
	    }
	    out.close();
	}
	*/
}
