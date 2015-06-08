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

package entity.search.main.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;


import entity.search.utils.Utils;

public class Preprocessor {
	
	/**
	 * A known bug: list of canadian football league seasons" . need to remove " .
	 * assume inputfile in n triple format //need to handle redirects and disambiguates
	 * @param inputfile
	 */
	public static void preprocessAll(String inputfile, String outputUnsortFile, String surfaceFormPropertiesFile){
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
		
		
		String outputfile = inputfile.replaceAll("\\.nt", "");
		System.out.println("Start Content Preprocssing");
		contentPreprocessing(inputfile, outputUnsortFile);
		//System.out.println("Sorting Files");
		//sortFiles(outputUnsortFile, outputfile+"_subjectSort.nt", outputfile+"_propertySort.nt", outputfile+"_objectSort.nt");
		System.out.println("Computing Entropies and Generate surface form file");
		computationPreprocessing(outputfile+"_subjectSort.nt", outputfile+"_propertySort.nt", outputfile+"_objectSort.nt", 
				outputfile+"_propertyEntropy.txt",outputfile+"_objectEntropy.txt",outputfile+"_surfaceForm.nt",surfaceFormPorperties);
	}
	/*
	 	private String removeChars(String [] spo){
		  StringBuilder newTripleSB = new StringBuilder();
		  if(spo[2].startsWith("\"")){
			  //need skip unicode
		      spo[2] = spo[2].replaceAll("\\^\\^.*", "");
			  spo[2] = spo[2].replaceAll("[^a-zA-Z0-9\\\\]", " ").replaceAll("\\s+", " ").trim();
			  newTripleSB.append(spo[0]);
			  newTripleSB.append(" ");
			  newTripleSB.append(spo[1]);
			  newTripleSB.append(" ");
			  newTripleSB.append(Utils.toPhrase(spo[2]));
			  newTripleSB.append("\n");
		  }else{
			  spo[2] = spo[2].substring(0,spo[1].length()-1).trim();
			  newTripleSB.append(spo[0]+" "+spo[1]+" "+spo[2]);
		  }
		  return newTripleSB.toString();
	}
	 */
	
	/**
	 * remove chars + add inverse property relation
	 * @param inputfile
	 * @param outputfile
	 */
	public static void contentPreprocessing(String inputfile, String outputfile){
		
		try{
			  FileInputStream fstream = new FileInputStream(inputfile);
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  
			  FileWriter ofstream = new FileWriter(outputfile);
			  BufferedWriter out = new BufferedWriter(ofstream);

			  String preEntity = "";
			  while ((strLine = br.readLine()) != null)   {
				  
				  if(strLine.startsWith("#"))
					  continue;
				  
				  strLine = strLine.replaceAll("@.*", "");
				  //removeChars
				  String [] spo = strLine.split(" ", 3);
				  if(spo.length < 3)
					  continue;

				  StringBuilder newTripleSB = new StringBuilder();
				  if(spo[2].startsWith("\"")){
					  //need skip unicode
				      spo[2] = spo[2].replaceAll("\\^\\^.*", "");
					  spo[2] = spo[2].replaceAll("[^\\p{L}\\p{M}A-Za-z0-9]", " ").replaceAll("\\s+", " ").trim();
					  newTripleSB.append(spo[0]);
					  newTripleSB.append(" ");
					  newTripleSB.append(spo[1]);
					  newTripleSB.append(" ");
					  newTripleSB.append(Utils.toPhrase(spo[2]));
					  //newTripleSB.append("\n");
				  }else{
					  if(spo[2].endsWith("."))
					  	spo[2] = spo[2].substring(0,spo[2].length()-1).trim();
					  newTripleSB.append(spo[0]+" "+spo[1]+" "+spo[2]);
				  }
				 // System.out.println(newTripleSB.toString());
				  if(newTripleSB.toString().length() > 0)
					  out.write(newTripleSB.toString()+"\n");
				  
				  //getLabelsFromURL
				  if(!preEntity.equals(spo[0])){
					  int index = spo[0].lastIndexOf("/");
					  int nindex = spo[0].lastIndexOf("#");
					  
					  if(nindex > index){
						  index = nindex;
					  }
					  
					  String urlName = spo[0];
					  if(index != -1){
						  urlName = spo[0].substring(index+1, spo[0].length()-1);
						  urlName = URLDecoder.decode(urlName);
					  }
					  preEntity = spo[0];
					  out.write(spo[0]+" <http://tool.eal.org/urlName> "+Utils.toPhrase(urlName)+"\n");
				  }
				  
				  //disambiguates and redirects
				  if(spo[1].contains("wikiPageDisambiguates") || spo[1].contains("wikiPageRedirects")){
					  int index = spo[0].lastIndexOf("/");
					  int nindex = spo[0].lastIndexOf("#");
					  
					  if(nindex > index){
						  index = nindex;
					  }
					  String urlName = spo[0];
					  if(index != -1 && index < spo[0].length() -1){
						  urlName = spo[0].substring(index+1, spo[0].length()-1);
						  urlName = URLDecoder.decode(urlName);
					  }	
					  out.write(spo[2]+" <http://tool.eal.org/redirectName> "+Utils.toPhrase(urlName)+"\n");
				  }
				  
				  //sameAs
				  if(spo[1].contains("sameAs")){
					  int index = spo[2].lastIndexOf("/");
					  int nindex = spo[2].lastIndexOf("#");
					  
					  if(nindex > index){
						  index = nindex;
					  }
					  String urlName = spo[2];
					  if(index != -1 && index < spo[2].length() -1){
						  urlName = spo[2].substring(index+1, spo[2].length()-1);
					  }	
					  out.write(spo[0]+" <http://tool.eal.org/sameAsName> "+Utils.toPhrase(urlName)+"\n");				  
				  }
				  
				  
				  //addInverseProperty
				  /*
				  String newtriple = newTripleSB.toString();
				  spo = newtriple.split(" ", 3);
				  if(spo.length < 3)
					  continue;
				  
				  if(spo[2].startsWith("<")){
					  StringBuilder inversePropertyTriple = new StringBuilder();
					  inversePropertyTriple.append(spo[2]);
					  inversePropertyTriple.append(" ");
					  String newProperty = spo[1].replace(">", "_InverseProperty>");
					  inversePropertyTriple.append(newProperty);
					  inversePropertyTriple.append(" ");
					  inversePropertyTriple.append(spo[0]);
					  //inversePropertyTriple.append("\n");
					  out.write(inversePropertyTriple.toString()+"\n");	
				  }
				  */  
				  
			  }
			  out.flush();
			  out.close();
			  in.close();

			    }catch (Exception e){
			  e.printStackTrace();
			  }
		
	}
	//sort by subject, property, object
	/**
	 * can't do redirecting, need new way to do redirect
	 * @param inputFile
	 * @param subjectSortFile
	 * @param propertySortFile
	 * @param objectSortFile
	 */
	public static void sortFiles(String inputFile, String subjectSortFile, String propertySortFile, String objectSortFile){
		Process p;
		try{
			p = Runtime.getRuntime().exec("sort -T . "+inputFile+" | uniq > " + subjectSortFile);
			p.waitFor();
			p = Runtime.getRuntime().exec("sort -T . -t ' ' -k 2 "+subjectSortFile+" | uniq > " + propertySortFile);
			p.waitFor();
			p = Runtime.getRuntime().exec("sort -T . -t ' ' -k 3 "+subjectSortFile+" | uniq > " + objectSortFile);
			p.waitFor();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void computationPreprocessing(String subjectSortFile, String propertySortFile, String objectSortFile, 
			String propertyEntropyOutput, String objectEntropyOutput, String surfaceFormOutput, ArrayList<String> surfaceFormProperties){
		getSurfaceFormData(subjectSortFile, surfaceFormProperties, surfaceFormOutput);
		computePropertyEntropy(propertySortFile, propertyEntropyOutput);
		computeEntityEntropyWeights(objectSortFile, objectEntropyOutput, propertyEntropyOutput);		
	}
	/**
	 * input file is sorted by subject, (getLabelsFromURL included)
	 * @param inputfile
	 * @param properties
	 * @param outputfile
	 */
	public static void getSurfaceFormData(String inputfile, ArrayList<String> properties, String outputfile){
		try{
			  FileInputStream fstream = new FileInputStream(inputfile);
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  
			  FileWriter ofstream = new FileWriter(outputfile);
			  BufferedWriter out = new BufferedWriter(ofstream);


			  while ((strLine = br.readLine()) != null)   {
//				  out.write(strLine+"\n");
				  String [] spo = strLine.split(" ", 3);
				  if(spo.length < 3)
					  continue;
				  
				  for(int i = 0 ; i < properties.size(); i++){
					  if(spo[1].toLowerCase().contains(properties.get(i))){
						  if(spo[2].startsWith("<")){
							  int index = spo[2].lastIndexOf("/");
							  int nindex = spo[2].lastIndexOf("#");
							  
							  if(nindex > index){
								  index = nindex;
							  }
							  String urlName = spo[2];
							  if(index != -1 && index < spo[2].length() -1){
								  urlName = spo[2].substring(index+1, spo[2].length()-1);
							  }	
							  out.write(spo[0]+" "+spo[1]+" "+Utils.toPhrase(urlName)+"\" .\n");
						  }else{
							  out.write(strLine+"\n");
						  }
						  break;
					  }
				  }
			  }
			  out.flush();
			  out.close();
			  in.close();
		}catch (Exception e){
			  e.printStackTrace();
		}
	}
	/*
	 * Assume input file format: N-triple without last dot
	 */
	public static void addInverseProperty(String inputfile, String outputfile){
		
		try{
			  FileInputStream fstream = new FileInputStream(inputfile);
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  
			  FileWriter ofstream = new FileWriter(outputfile);
			  BufferedWriter out = new BufferedWriter(ofstream);

			  
			  while ((strLine = br.readLine()) != null)   {
				  //strLine = strLine.replaceAll(".$", "").trim();
				  strLine = strLine.replaceAll(".$", ""); //delete by XixiLuo: this trim would delete ">" in the object
				  out.write(strLine+"\n");
				  String [] spo = strLine.split(" ", 3);
				  if(spo.length < 3)
					  continue;
				  StringBuilder newTripleSB = new StringBuilder();
				  if(spo[2].startsWith("<")){
					  //spo[2] = spo[2].replaceAll(".$", "").trim();
					  newTripleSB.append(spo[2]);
					  newTripleSB.append(" ");
					  String newProperty = spo[1].replace(">", "_InverseProperty>");
					  newTripleSB.append(newProperty);
					  newTripleSB.append(" ");
					  newTripleSB.append(spo[0]);
					  newTripleSB.append("\n");
				  }
				  out.write(newTripleSB.toString());
			  }
			  out.flush();
			  out.close();
			  in.close();
			    }catch (Exception e){
			  System.err.println("Error: " + e.getMessage());
			  }
	}
	/*
	public static void createEntityDoc(String inputfile, String outputDir){
		try{
			  FileInputStream fstream = new FileInputStream(inputfile);
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  
			  strLine = br.readLine();
			  //generate file name
			  int index = spo[0].lastIndexOf("/");
			  int nindex = spo[0].lastIndexOf("#");
			  
			  if(nindex > index){
				  index = nindex;
			  }
			  
			  String urlName = spo[0];
			  if(index != -1){
				  urlName = spo[0].substring(index+1, spo[0].length()-1);
			  }
			  
			  
			  
			  FileWriter ofstream = new FileWriter(outputfile);
			  BufferedWriter out = new BufferedWriter(ofstream);

			  
			  while ((strLine = br.readLine()) != null)   {		
				  
			  }
			  
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	*/
	public static void removeChars(String inputfile, String outputfile){
		try{
			  FileInputStream fstream = new FileInputStream(inputfile);
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  
			  FileWriter ofstream = new FileWriter(outputfile);
			  BufferedWriter out = new BufferedWriter(ofstream);

			  
			  while ((strLine = br.readLine()) != null)   {
				  //out.write(strLine+"\n");
				  String [] spo = strLine.split(" ", 3);
				  if(spo.length < 3)
					  continue;
				  StringBuilder newTripleSB = new StringBuilder();
				  if(spo[2].startsWith("\"")){
					  //need skip unicode
				      spo[2] = spo[2].replaceAll("\\^\\^.*", "");
					  spo[2] = spo[2].replaceAll("[^a-zA-Z0-9\\\\]", " ").replaceAll("\\s+", " ").trim();
					  newTripleSB.append(spo[0]);
					  newTripleSB.append(" ");
					  newTripleSB.append(spo[1]);
					  newTripleSB.append(" ");
					  newTripleSB.append(Utils.toPhrase(spo[2]));
					  newTripleSB.append("\n");
				  }else{
					  if(spo[2].endsWith("."))
						  	spo[2] = spo[2].substring(0,spo[2].length()-1).trim();
						  newTripleSB.append(spo[0]+" "+spo[1]+" "+spo[2]+"\n");
				  }

				  if(!spo[2].equals("")) { //added by Xueping Jiang, the subject with "" should not be included
					  out.write(newTripleSB.toString());
				  }
			  }
			  out.flush();
			  out.close();
			  in.close();
			    }catch (Exception e){
			  System.err.println("Error: " + e.getMessage());
			  }
	}
	/**
	 * input file sort by subject
	 * @param inputfile
	 * @param outputfile
	 */
	public static void getLabelsFromURL(String inputfile, String outputfile){
		try{
			  FileInputStream fstream = new FileInputStream(inputfile);
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  
			  FileWriter ofstream = new FileWriter(outputfile);
			  BufferedWriter out = new BufferedWriter(ofstream);

			  String preEntity = "";
			  while ((strLine = br.readLine()) != null)   {
				  out.write(strLine+"\n");
				  String [] spo = strLine.split(" ", 3);
				  if(spo.length < 3)
					  continue;
				  if(!preEntity.equals(spo[0])){
					  int index = spo[0].lastIndexOf("/");
					  int nindex = spo[0].lastIndexOf("#");
					  
					  if(nindex > index){
						  index = nindex;
					  }
					  
					  String urlName = spo[0];
					  if(index != -1){
						  urlName = spo[0].substring(index+1, spo[0].length()-1);
					  }
					  preEntity = spo[0];
					  out.write(spo[0]+" <http://tool.eal.org/urlName> \""+Utils.toPhrase(urlName)+"\" .\n");
				  }

			  }
			  out.flush();
			  in.close();
			  out.close();
			    }catch (Exception e){
			  System.err.println("Error: " + e.getMessage());
			  }
	}
	/*
	 * why some entity entropy is 1? -- possible bug
	 * file sorted by object, assume propertyWeight/entropy computed
	 */
	public static void computeEntityEntropyWeights(String inputfile, String outputfile, String propertyWeightFile){
		try{
	        FileInputStream fstream = new FileInputStream(inputfile);
	        DataInputStream in = new DataInputStream(fstream);
			BufferedReader objectList = new BufferedReader(new InputStreamReader(in));
			
			
	        FileInputStream fstream2 = new FileInputStream(propertyWeightFile);
	        DataInputStream in2 = new DataInputStream(fstream2);
			BufferedReader propertyWeight = new BufferedReader(new InputStreamReader(in2));
	        
			FileWriter ofstream = new FileWriter(outputfile);
			BufferedWriter out = new BufferedWriter(ofstream);
			  
			
			//read property entropy
	        Hashtable<String, Double> propertyEntropy = new Hashtable<String, Double>();
	        Hashtable<String, Integer> frequencyTable = new Hashtable<String, Integer>();
	        String pWeight = "";
	        while((pWeight = propertyWeight.readLine())!=null){
	        	String [] pw = pWeight.split(" ",2);
	        	if(pw.length < 2)
	        		continue;
	        	propertyEntropy.put(pw[0], Double.parseDouble(pw[1]));
	        }
	        in2.close();
	        
	        //read first triple where object is not string
	        String currentLine = "";
	        float currentEntropy = 1;
	        String obj = "";
	        while((currentLine = objectList.readLine()) != null)   {     
	        	String [] spo = currentLine.split(" ",3);
	        	if(spo.length < 3)
	        		continue;
	        	
	        	
	        	
	        	if(spo.length == 3 && (spo[2].startsWith("<") || spo[2].startsWith("_"))){
					if(propertyEntropy.containsKey(spo[1])){
						currentEntropy += propertyEntropy.get(spo[1]);
					}else{
						currentEntropy+=1;
					}
	        		obj = spo[2];
	        		break;
	        	}
	        }
	        
	        //compute entropy and write to file
	        while((currentLine = objectList.readLine())!=null){
	        	System.out.println(currentLine);
	        	String [] spo = currentLine.split(" ",3);
	        	if(spo.length < 3)
	        		continue;
	        	if(spo[1]==null || spo[2]==null)
	        		continue;
	        	if(!(spo[2].startsWith("<") || spo[2].startsWith("_")))
	        		continue;
				int result = spo[2].compareTo(obj);
				if(result == 0){
					if(propertyEntropy.containsKey(spo[1])){
						currentEntropy += propertyEntropy.get(spo[1]);
					}else{
						currentEntropy+=1;
					}
				}else if(result != 0){ //changed by Xueping to support Windows sort command line
					float weight = (float)(1 + Math.log(currentEntropy));
					out.write(obj+" "+weight+"\n");
					currentEntropy = 1;
					obj = spo[2];
				}	        	
	        }
	        //finish reading file, write last weight
			float weight = (float)(1 + Math.log(currentEntropy));
			out.write(obj+" "+weight+"\n");
			out.flush();
			in.close();
			out.close();
		}catch(Exception e){
			e.printStackTrace(); 
		}
	}
	/*
	 * Assume input file is sorted by property
	 */
	public static void computePropertyEntropy(String inputfile, String outputfile){
		try{
	        FileInputStream fstream = new FileInputStream(inputfile);
	        DataInputStream in = new DataInputStream(fstream);
			BufferedReader stmt = new BufferedReader(new InputStreamReader(in));
	        
			FileWriter ofstream = new FileWriter(outputfile);
			BufferedWriter out = new BufferedWriter(ofstream);
			String currentLine = "";
			String curProperty = "";
			Hashtable<String, Integer> frequencyTable = new Hashtable<String, Integer>();
			double numObject = 0;
			
			while((currentLine = stmt.readLine())!=null){
				String [] spo = currentLine.split(" ", 3);	
				if(spo.length < 3)
					continue;
				if(!spo[1].equals(curProperty)){
					double entropy = 0;
					for(String key:frequencyTable.keySet()){
						double probability = frequencyTable.get(key)/numObject;
						entropy += probability * (Math.log(probability));///Math.log10(numObject));
					}
					entropy = entropy * -1 + 1;
					out.write(curProperty+" "+entropy+"\n");
					frequencyTable.clear();
					numObject = 0;
					curProperty = spo[1];
				}
				String obj = spo[2];
				if(frequencyTable.keySet().contains(obj)){
					int count = frequencyTable.get(obj) + 1;
					frequencyTable.put(obj,count);
				}else{
					frequencyTable.put(obj,1);
				}
				numObject ++;
			}
			
			//finish reading file, write last property entropy
			double entropy = 0;
			for(String key:frequencyTable.keySet()){
				double probability = frequencyTable.get(key)/numObject;
				entropy += probability * (Math.log(probability));///Math.log10(numObject));
			}
			entropy = entropy * -1 + 1;
			out.write(curProperty+" "+entropy+"\n");
			out.flush();
			in.close();
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}	
	}
	/*
	 * Assume input file is a list of objects that other entities is pointed to
	 */
	public static void computeEntityWeights(String inputfile, String outputfile){
		
		try{
	        FileInputStream fstream = new FileInputStream(inputfile);
	        DataInputStream in = new DataInputStream(fstream);
			BufferedReader objectList = new BufferedReader(new InputStreamReader(in));
	        
			FileWriter ofstream = new FileWriter(outputfile);
			BufferedWriter out = new BufferedWriter(ofstream);
			  
	        String currentLine = "";
	        String obj = objectList.readLine();
	        int counts = 1;
	        
	        while((currentLine = objectList.readLine())!=null){
				int result = currentLine.compareTo(obj);
				if(result == 0){
					counts++;
				}else if(result != 0){ //changed by Xueping to support Windows sort command line
					float weight = (float)(1 + Math.log((float)counts));
					out.write(obj+" "+weight+"\n");
					counts = 1;
					obj = currentLine;
				}	        	
	        }
	        out.flush();
			in.close();
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	
	}
	/**
	 * Process owl:restrictions on someValuesFrom and allValuesFrom: experimental
	 * Process 1 level
	 */
	
	public static void processValueRestriction(String inputfile, String outputfile){
		//retrieve all blank node triples (if subject is blank node, put in hash table, if object is blank node put in loop array)
		//for triple t1 in loop array, find corresponding blank node in hash table, get triple t2s
		//split t1, get s1,
		//for triple t2 in t2s, create new triple t1.subject t2.property t2.object
		try{
	        FileInputStream fstream = new FileInputStream(inputfile);
	        DataInputStream in = new DataInputStream(fstream);
			BufferedReader input = new BufferedReader(new InputStreamReader(in));
	        
			FileWriter ofstream = new FileWriter(outputfile);
			BufferedWriter out = new BufferedWriter(ofstream);
			  
	        String currentLine = "";
	        
	        HashMap<String,ArrayList<String>> blankNodes = new HashMap<String, ArrayList<String>>();
	        ArrayList<String> replacingTriples = new ArrayList<String> ();
	        ArrayList<String> results = new ArrayList<String>();
	        while((currentLine = input.readLine())!=null){
	        	currentLine = currentLine.substring(0,currentLine.length()-1).trim();
        		String [] spo = currentLine.split(" ",3);
        		if(spo.length < 3)
        			continue;
	        	if(spo[0].startsWith("_:")){
	        		if(blankNodes.containsKey(spo[0])){
	        			ArrayList<String> currentTriples = blankNodes.get(spo[0]);
	        			currentTriples.add(currentLine);
	        			blankNodes.put(spo[0], currentTriples);
	        		}else{
	        			ArrayList<String> currentTriples = new ArrayList<String>();
	        			currentTriples.add(currentLine);
	        			blankNodes.put(spo[0], currentTriples);
	        		}
	        	}else if(spo[2].startsWith("_:")){
	        		replacingTriples.add(currentLine);       		
	        	}else{
	        		results.add(currentLine);   
	        	}
	        }

	        for(String triple:replacingTriples){
	        	String [] spo = triple.split(" ",3);
	        	if(spo.length < 3)
	        		continue;
	        	if(blankNodes.containsKey(spo[2])){
	        		ArrayList<String> blankNodeTriples = blankNodes.get(spo[2]);
	        		for(String btriple:blankNodeTriples){
	        			String [] blankSPO = btriple.split(" ", 3);
	        			if(blankSPO.length < 3)
	        				continue;
	        			String newTriple = spo[0]+" "+blankSPO[1]+" "+blankSPO[2];
	        			results.add(newTriple);
	        		}
	        	}
	        }
	        
	        Collections.sort(results);
	        
	        for(String result:results){
	        	out.write(result+"\n");
	        }
	        out.flush();
			in.close();
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
