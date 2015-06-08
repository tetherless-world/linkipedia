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

package entity.search.indexing;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;


import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.payloads.FloatEncoder;
import org.apache.lucene.analysis.payloads.PayloadEncoder;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import entity.search.index.analyzer.EntropyAnalyzer;
import entity.search.similarity.MySimilarity;
import entity.search.utils.Utils;

public class SurfaceFormIndexer {

	String sourceDirectory;
	String indexDirectory;
	String currentLine;
	BufferedReader weightReader;
	Hashtable<String, Float> propertyWeight;
	
	public SurfaceFormIndexer(String source, String destination){
		sourceDirectory = source;
		indexDirectory = destination;
		propertyWeight = new Hashtable<String, Float>();
	}
	
	public void createIndex(){
		try{
			  Analyzer stdAnalyzer = new StandardAnalyzer(Version.LUCENE_47);
			  PayloadEncoder encoder = new FloatEncoder();
			  EntropyAnalyzer entropyAnalyzer = new EntropyAnalyzer(encoder);
			  Map<String, Analyzer> myAnalyzerMap = new HashMap<String, Analyzer>();
			  myAnalyzerMap.put("label", entropyAnalyzer);
			  myAnalyzerMap.put("analyzedLabel", stdAnalyzer);  
			  PerFieldAnalyzerWrapper MyAnalyzer = new PerFieldAnalyzerWrapper(stdAnalyzer,myAnalyzerMap);
			  IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_47, MyAnalyzer);
			  iwc.setSimilarity(new MySimilarity());
			  Directory dir = FSDirectory.open(new File(indexDirectory));
			  IndexWriter writer = new IndexWriter(dir, iwc);

		      System.out.println("Indexing to directory '" +indexDirectory+ "'...");
		      indexDocs(writer, new File(sourceDirectory));
		      System.out.println("Optimizing...");

		      writer.close();
		      System.out.println("Finished Indexing");

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private void indexDocs(IndexWriter writer, File file){
	    if (file.canRead()) {
		      if (file.isDirectory()) {
		        String[] files = file.list();
		        if (files != null) {
		          for (int i = 0; i < files.length; i++) {
		        	System.out.print(i+" ");
		            indexDocs(writer, new File(file, files[i]));
		          }
		        }
		      } else {
			        System.out.println("adding " + file);
			        try{
			            FileInputStream fstream = new FileInputStream(file);
			            DataInputStream in = new DataInputStream(fstream);
			            BufferedReader br = new BufferedReader(new InputStreamReader(in));
			            String line;
			            Set<String> labels = new HashSet<String>();
			            HashSet<String> defaultLabel = new HashSet<String>();
			            Set<String> lookUpLabels = new HashSet<String>();
			            String subject = "";
			            HashMap<String, Object> data = new HashMap<String, Object>();
			            
			            int count = 0;
			            while ((line= br.readLine())!= null ){
			            				            	
			            	String [] spo = line.split(" ",3);
			            	
			            	if(spo.length < 3){
			            		continue;
			            	}
			            	
			            	if(!(spo[0].startsWith("<http")||spo[0].startsWith("_:")))
			            		continue;
			            	
			            	if(!(spo[1].startsWith("<http")||spo[1].startsWith("_:")))
			            		continue;	
			            	
			            	count ++;
			            	if(!subject.equals(spo[0])){
			            		if(!subject.equals("")){
			            			data.put("url", subject);
			            			data.put("label", labels);
			            			data.put("defaultLabel", defaultLabel);
			            			data.put("lookUpLabel", lookUpLabels);
			            			System.out.println(count+" adding "+subject);
			            			Document doc = getDoc(data);
			            			writer.addDocument(doc);

			            		}
			            		subject = spo[0];
			            		defaultLabel = new HashSet<String>();
			            		labels = new HashSet<String>();
			            		lookUpLabels = new HashSet<String>();
			            	}
			            	if(spo[2].equals("")){
			            		continue;
			            	}
			            	/*
			            	if(spo[2].matches("u\\d+.*")){
			            		continue;
			            	}
			            	*/
			            	spo[2] = spo[2].toLowerCase();
			            	spo[2] = spo[2]+"|"+getPropertyWeight(spo[1]);
			            	
			            	if(spo[1].contains("urlName") || spo[1].contains("redirectName")){
			            		defaultLabel.add(spo[2]);
			            	}
			            	
			            	
			            	
			            	labels.add(spo[2]);
			            	String removeSingles = Utils.removeSingleLetter(spo[2]);
			            	if(!removeSingles.equals(spo[0]))
			            		labels.add(removeSingles);
			            	addingLabels(lookUpLabels,spo[2]);
			            	//labels.add(reviseString(spo[2]));
			            }
			            
			            //index last entity
	            		if(!subject.equals("")){
	            			data.put("url", subject);
	            			data.put("label", labels);
	            			data.put("defaultLabel", defaultLabel);
	            			data.put("lookUpLabel", lookUpLabels);
	            			System.out.println(count+" adding "+subject);
	            			Document doc = getDoc(data);
	            			writer.addDocument(doc);
	            		}
	            		
			        }catch(Exception e){
			        	e.printStackTrace();
			        }
		      }
		    }
	}
	private float getPropertyWeight(String url){
		if(propertyWeight.containsKey(url)){
			return propertyWeight.get(url);
		}
		return 1;
	}
	public void setPropertyWeight(String weightFile){
		try{
			System.out.println("weight file: "+weightFile+" end");
	        FileInputStream fstream = new FileInputStream(weightFile);
	        DataInputStream in = new DataInputStream(fstream);
	        BufferedReader weightReader = new BufferedReader(new InputStreamReader(in));
	        String line;
	        while((line = weightReader.readLine())!=null){
	        	String [] weight = line.split(" ",2);
	        	if(weight.length < 2)
	        		continue;
	        	propertyWeight.put(weight[0], Float.parseFloat(weight[1]));
	        }
	        in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void setEntityWeightFile(String weightFile){
		try{
			System.out.println("weight file: "+weightFile+" end");
	        FileInputStream fstream = new FileInputStream(weightFile);
	        DataInputStream in = new DataInputStream(fstream);
	        weightReader = new BufferedReader(new InputStreamReader(in));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private float getEntityWeight(String url){
		int counts = 1;
		try{
			if(currentLine!=null){
				String [] urlWeight = currentLine.split(" ", 2);
				if(urlWeight.length >= 2){
	
				if(urlWeight[0].equals(url)){
					float myWeight = Float.parseFloat(urlWeight[1]);
					System.out.println("found: "+url+" weight: "+myWeight);
					return myWeight;
				}
				int result = urlWeight[0].compareTo(url);
					if(result > 0){
						return 1;
					}
				
				}
			}
			
			while((currentLine = weightReader.readLine())!=null){
				String [] urlWeight = currentLine.split(" ", 2);
				if(urlWeight.length < 2)
					continue;
				//System.out.println(urlWeight[0]+" vs "+url);
			
				int result = urlWeight[0].compareTo(url);
				if(result == 0){
					float myWeight = Float.parseFloat(urlWeight[1]);
					System.out.println("found: "+url+" weight: "+myWeight);
					return myWeight;
				}else if(result > 0){
					break;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		float myWeight = (float)(1 + Math.log(counts));
		System.out.println("not found: "+url+" weight: "+myWeight);
		return myWeight;		
	}
	private void addingLabels(Set<String> hashSet, String label){
		StringBuilder labelBuilder = new StringBuilder();
		label = label.toLowerCase();
		for(String label_part:label.split(" ")){
			labelBuilder.append(label_part);
			hashSet.add(labelBuilder.toString());
			labelBuilder.append(" ");
		}
		Set<String> noSingleLetter = new HashSet<String>();
		for(String str:hashSet){
			noSingleLetter.add(Utils.removeSingleLetter(str.replaceAll("\\|\\d+.*", "")));
		}
		hashSet.addAll(noSingleLetter);
	}
	private Document getDoc(HashMap<String, Object> data){
		   Document doc = new Document();
		   doc.add(new StringField("url", (String)data.get("url"), Field.Store.YES));
		   //System.out.println("url:"+((String)data.get("url"))+"|");
		   HashSet<String> labels = (HashSet<String>)data.get("label");
		   HashSet<String> lookUpLabels = (HashSet<String>)data.get("lookUpLabel");
		   
		   HashSet<String> defaultLabels = (HashSet<String>)data.get("defaultLabel");
		   
		   for(String defaultLabel:defaultLabels){
			   doc.add(new TextField("defaultLabel",defaultLabel, Field.Store.YES));
		   }

		   for(String label:labels){
//			   System.out.println("label "+label);
			   doc.add(new TextField("label",label, Field.Store.YES));  //Analyzed using EntropyAnalyzer
			   String [] analyzedLabel = label.split("\\|");
	//		   System.out.println("analyzedlabel "+analyzedLabel[0]);
			   doc.add(new TextField("analyzedLabel",analyzedLabel[0], Field.Store.NO)); //Analyzed using stdAnalyzer
		   }
		   
		   for(String lookUpLabel:lookUpLabels){
			   //System.out.println(lookUpLabel);
			   doc.add(new StringField("lookUpLabel",lookUpLabel, Field.Store.NO)); //not analyzed
		   }
		   

		   float weight = getEntityWeight((String)data.get("url"));
		   doc.add(new FloatField("boost",weight, Field.Store.YES));
		   return doc;
	}
}
