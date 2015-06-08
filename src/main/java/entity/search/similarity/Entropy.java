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

package entity.search.similarity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Hashtable;

public class Entropy {

	public static void computeEntropyPageRank(String inputfile, String outputfile, String propertyWeightFile){
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
	        	
	        	
	        	
	        	//if(spo.length == 3 && (spo[2].startsWith("<") || spo[2].startsWith("_"))){
					if(propertyEntropy.containsKey(spo[1])){
						currentEntropy += propertyEntropy.get(spo[1]);
					}else{
						currentEntropy+=1;
					}
	        		obj = spo[2];
	        		break;
	        	//}
	        }
	        
	        //compute entropy and write to file
	        while((currentLine = objectList.readLine())!=null){
	        	System.out.println(currentLine);
	        	String [] spo = currentLine.split(" ",3);
	        	if(spo.length < 3)
	        		continue;
	        	if(spo[1]==null || spo[2]==null)
	        		continue;
//	        	if(!(spo[2].startsWith("<") || spo[2].startsWith("_")))
//	        		continue;
				int result = spo[2].compareTo(obj);
				if(result == 0){
					if(propertyEntropy.containsKey(spo[1])){
						currentEntropy += propertyEntropy.get(spo[1]);
					}else{
						currentEntropy+=1;
					}
				}else if(result > 0){
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
	/**
	 * Input file sort by object
	 * @param inputfile
	 * @param outputfile
	 * @param propertyWeightFile
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
	        int numObject = 0;
	        Hashtable<String, Float> frequencyTable = new Hashtable<String, Float>();
	        while((currentLine = objectList.readLine()) != null)   {     
	        	String [] spo = currentLine.split(" ",3);
	        	if(spo.length < 3)
	        		continue;
	        	
	        	
	        	
	        	//if(spo.length == 3 && (spo[2].startsWith("<") || spo[2].startsWith("_"))){
					if(propertyEntropy.containsKey(spo[1])){
						currentEntropy = propertyEntropy.get(spo[1]).floatValue()-1;
					}else{
						currentEntropy =1;
					}
	        		obj = spo[2];
	        		frequencyTable.put(spo[1],currentEntropy);
	        		numObject++;
	        		break;	        		
	        //	}
	        }
	        
	        //compute entropy and write to file
	        while((currentLine = objectList.readLine())!=null){
	        	//System.out.println(currentLine);
	        	String [] spo = currentLine.split(" ",3);
	        	if(spo.length < 3)
	        		continue;
	        	if(spo[1]==null || spo[2]==null)
	        		continue;
//	        	if(!(spo[2].startsWith("<") || spo[2].startsWith("_")))
//	        		continue;
				int result = spo[2].compareTo(obj);
				if(result == 0){
					
					if(propertyEntropy.containsKey(spo[1])){
						currentEntropy = propertyEntropy.get(spo[1]).floatValue()-1;
					}else{
						currentEntropy =1;
					}
					
					if(frequencyTable.keySet().contains(spo[1])){
						float count = frequencyTable.get(spo[1]) + currentEntropy;
						frequencyTable.put(spo[1],count);
					}else{
						frequencyTable.put(spo[1],currentEntropy);
					}
					numObject++;
				}else if(result > 0){
					float entropy = 0;
					if(numObject == 1){
						entropy = -1;
					}else{
					
						for(String key:frequencyTable.keySet()){
							System.out.println(key+" "+frequencyTable.get(key)+" "+numObject);
							double probability = frequencyTable.get(key)/(numObject*10);
							
							if(probability == 0){
								entropy += 0;
								continue;
							}
							
							entropy += probability * (Math.log(probability)/Math.log(numObject));///Math.log10(numObject));
						}
					}
					entropy = entropy * 10 * -1 + 1;
					//System.out.println(obj+" "+entropy);
					out.write(obj+" "+entropy+"\n");
					frequencyTable.clear();
					numObject = 0;
					currentEntropy = 1;
					obj = spo[2];
					
					if(propertyEntropy.containsKey(spo[1])){
						currentEntropy = propertyEntropy.get(spo[1]).floatValue()-1;
					}else{
						currentEntropy =1;
					}
					
					if(frequencyTable.keySet().contains(spo[1])){
						float count = frequencyTable.get(spo[1]) + currentEntropy;
						frequencyTable.put(spo[1],count);
					}else{
						frequencyTable.put(spo[1],currentEntropy);
					}
					
					numObject++;
				}
				
	        }
	        //finish reading file, write last weight
			float entropy = 0;
			if(numObject == 1){
				entropy = -1;
			}else{
			
				for(String key:frequencyTable.keySet()){
					System.out.println(key+" "+frequencyTable.get(key)+" "+numObject);
					double probability = frequencyTable.get(key)/(numObject*10);
					
					if(probability == 0){
						entropy += 0;
						continue;
					}
					
					entropy += probability * (Math.log(probability)/Math.log(numObject));///Math.log10(numObject));
				}
			}
			entropy = entropy * 10 * -1 + 1;
			out.write(obj+" "+entropy+"\n");
			out.flush();
			in.close();
			out.close();
		}catch(Exception e){
			e.printStackTrace(); 
		}
	}
	/**
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
					
					if(numObject == 1){
						entropy = -1;
					}else{
					
						for(String key:frequencyTable.keySet()){
							double probability = frequencyTable.get(key)/numObject;
							entropy += probability * (Math.log(probability)/Math.log(numObject));///Math.log10(numObject));
						}
					}
					entropy = (entropy * entropy * 19) + 1;
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
			if(numObject == 1){
				entropy = -1;
			}else{
				for(String key:frequencyTable.keySet()){
					double probability = frequencyTable.get(key)/numObject;
					entropy += probability * (Math.log(probability)/Math.log(numObject));///Math.log10(numObject));
				}
			}
			entropy = (entropy * entropy * 19) + 1;
			out.write(curProperty+" "+entropy+"\n");
			out.flush();
			in.close();
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}	
	}
}
