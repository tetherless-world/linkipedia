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

package edu.rpi.tw.linkipedia.search.indexing;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
//import java.util.HashSet;
import java.util.Hashtable;
//import java.util.List;
//import java.util.Set;

import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.payloads.FloatEncoder;
import org.apache.lucene.analysis.payloads.PayloadEncoder;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.rpi.tw.linkipedia.search.index.analyzer.EntropyAnalyzer;
import edu.rpi.tw.linkipedia.search.similarity.MySimilarity;
import edu.rpi.tw.linkipedia.search.utils.Utils;
import edu.rpi.tw.linkipedia.search.indexing.DefaultAnalyzer;

public class EntityIndexUpdater {
	String sourceDirectory;
	String indexDirectory;
	Hashtable<String, Float> propertyWeight;
	IndexSearcher searcher;
	
	public EntityIndexUpdater(String source, String destination, String sfIndex){
		sourceDirectory = source;
		indexDirectory = destination;
		propertyWeight = new Hashtable<String,Float>();
		try{
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(sfIndex)));
			searcher = new IndexSearcher(reader);
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void updateIndex(){
		try{
			
			  Analyzer stdAnalyzer = DefaultAnalyzer.getAnalyzer();
			  PayloadEncoder encoder = new FloatEncoder();
			  EntropyAnalyzer entropyAnalyzer = new EntropyAnalyzer(encoder);
			  Map<String, Analyzer> myAnalyzerMap = new HashMap<String, Analyzer>();
			  myAnalyzerMap.put("related_object", entropyAnalyzer);
			  myAnalyzerMap.put("label", entropyAnalyzer);
			  myAnalyzerMap.put("defaultLabel", entropyAnalyzer);
			  myAnalyzerMap.put("analyzedLabel", stdAnalyzer);  
			  PerFieldAnalyzerWrapper MyAnalyzer = new PerFieldAnalyzerWrapper(stdAnalyzer,myAnalyzerMap);
			  IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_47, MyAnalyzer);
			  iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			  iwc.setRAMBufferSizeMB(4096);
			  iwc.setMaxThreadStates(36);
			  iwc.setSimilarity(new MySimilarity());
			  Directory dir = FSDirectory.open(new File(indexDirectory));
			  IndexWriter writer = new IndexWriter(dir, iwc);
		      System.out.println("Update directory '" +indexDirectory+ "'...");
		      indexDocs(writer, new File(sourceDirectory));
		      System.out.println("Optimizing...");
		      writer.close();
		      System.out.println("Finished Updating");

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
//			            Set<String> labels = new HashSet<String>();
			            String subject = "";
			            HashMap<String, Object> data = new HashMap<String, Object>();
			            ArrayList<String> triples = new ArrayList<String> ();
			            String contents = "";
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
			            			data.put("triples", triples);
			            			data.put("contents", contents);
			            			System.out.println(count+" adding "+subject);
			            			Document doc = getDoc(subject, data);
			            			writer.updateDocument(new Term("url",subject), doc);

			            		}
			            		subject = spo[0];
			            		triples = new ArrayList<String>();
			            		contents = "";
			            	}
			            	triples.add(spo[1]+" "+spo[2]);
			            	//spo[2] = spo[2].toLowerCase();
			            	if(!(spo[2].startsWith("<")||spo[2].startsWith("_"))){
			            		contents += spo[2];
			            	}
			            }
			            //last entity
	            		if(!subject.equals("")){
	            			data.put("url", subject);
	            			data.put("triples", triples);
	            			data.put("contents", contents);
	            			System.out.println(count+" adding "+subject);
	            			Document doc = getDoc(subject, data);
	            			writer.updateDocument(new Term("url",subject), doc);
	            		}
			            
			        }catch(Exception e){
			        	e.printStackTrace();
			        }
		      }
		    }
	}

	public void setWeightFile(String weightFile){
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
	private float getPropertyWeight(String url){
		if(propertyWeight.containsKey(url)){
			return propertyWeight.get(url);
		}
		return 1;
	}
	/*
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
			noSingleLetter.add(removeSingleLetter(str));
		}
		hashSet.addAll(noSingleLetter);
	}
	
	private String removeSingleLetter(String label){
		label = label.replaceAll("(\\s+[a-z](?=\\s))"," ");
		return label;
	}
	*/
	private Document getDoc(String subject, HashMap<String, Object> data) throws CorruptIndexException, IOException{
		   Document doc = new Document();
		   //Text
		   doc.add(new StringField("url", (String)data.get("url"), Field.Store.YES));
		   doc.add(new TextField("contents", (String)data.get("contents"), Field.Store.NO));
		   //get label field from surfaceform index and add to doc;
		   //System.out.println((String)data.get("url"));
		   Document surfaceFormDoc = Utils.readIndexByTerm(searcher, "url", subject);
		   String weight = null;
		   if(surfaceFormDoc != null){
			   IndexableField[] labelFields = surfaceFormDoc.getFields("label");
			   for(int i = 0 ; i < labelFields.length; i++){
				   //System.out.println("label: "+labelFields[i].stringValue());
				   doc.add(new TextField("label",labelFields[i].stringValue(), Field.Store.YES));
				   doc.add(new TextField("analyzedLabel",labelFields[i].stringValue(), Field.Store.NO));
			   }
			   //System.out.println("default: "+surfaceFormDoc.get("defaultLabel"));
			   IndexableField[] defaultLabelFields = surfaceFormDoc.getFields("defaultLabel");
			   
			   for(int i = 0 ; i < defaultLabelFields.length; i++){
				   doc.add(new TextField("defaultLabel",defaultLabelFields[i].stringValue(), Field.Store.YES));
			   }
//			   String [] defaultLabelTermWeight = surfaceFormDoc.get("defaultLabel").split("\\|");
//			   String thisTerm = "";
//			   double defaultLabelweight = 1;
//			   if(defaultLabelTermWeight.length > 1){
//				   defaultLabelweight = Double.parseDouble(defaultLabelTermWeight[1]);
//				   String [] defaultLabelTerms = defaultLabelTermWeight[0].split(" ");						
//				   double margin = ((double)(1)/(double)defaultLabelTerms.length)*defaultLabelweight;
//				   for(int i = 0 ; i < defaultLabelTerms.length;i++){
//					   thisTerm += defaultLabelTerms[i];
//					   String thisIndexTerm = thisTerm+"|"+ (margin*(i+1));
//					  // System.out.println(thisIndexTerm);
//					   //System.out.println(thisIndexTerm);
//					   doc.add(new TextField("defaultLabel",thisIndexTerm, Field.Store.YES));
//					   thisTerm += " ";
//				   }
//			   }
			   weight = surfaceFormDoc.get("boost");
		   }else{
			   System.out.println("not in surface index: "+(String)data.get("url"));
		   }
		   
		   //analyze object, get surface form and weight
		   ArrayList<String> triples = (ArrayList<String>) data.get("triples");
		   for(int i = 0 ; i < triples.size(); i++){
			   String triple = triples.get(i);
			   doc.add(new StringField("triple", triple, Field.Store.YES));
			   String [] po = triple.split(" ",2);
			   if(po.length < 2)
				   continue;
			   
			   
			   //add entropy information
			   doc.add(new StringField("related_object_URL", po[1], Field.Store.NO));
			   //add type condition
			   if(po[0].contains("syntax-ns#type")){
				   
					  int index = po[1].lastIndexOf("/");
					  int nindex = po[1].lastIndexOf("#");
					  
					  if(nindex > index){
						  index = nindex;
					  }
					  
					  //problem with type indexing: if there are numbers after type string
					  String urlName = po[1];
					  if(index != -1){
						  urlName = po[1].substring(index+1, po[1].length()-1);
						  urlName = URLDecoder.decode(urlName);
						  urlName = Utils.toPhrase(urlName).replaceAll("\\d+$", "").toLowerCase().trim();
					  }
				   
				   Field typeField = new StringField("type",urlName, Field.Store.YES);
				   //float typeWeight = propertyWeight.get(po[0]);
				   //System.out.println("type: "+urlName);
				   //typeField.setBoost(typeWeight);
				   doc.add(typeField);
			   }
			   
			   //payload analyzer
			   if(po[1].startsWith("<") || po[1].startsWith("_")){
				   //get surface form of object
				   //System.out.println("look up: "+po[1]);
				   Document objectSurfaceFormDoc = Utils.readIndexByTerm(searcher, "url", po[1]);
				   if(objectSurfaceFormDoc != null){
					   
					   IndexableField[] objlabelFields = objectSurfaceFormDoc.getFields("label");

					   for(int j = 0 ; j < objlabelFields.length; j++){
						   IndexableField objSFField = objlabelFields[j];
						   
						   String thisLabel = objSFField.stringValue().trim();
						   //System.out.println(po[1]+" has label : "+thisLabel);
						   //Field object_original = new StringField("object_original", objSFField.stringValue()+" || "+triple, Field.Store.NO);
						   //get property entropy and set boost or payload
						  
						   float relateObjectWeight = Float.parseFloat(objectSurfaceFormDoc.get("boost"));
						   
						   float relatePropertyWeight = 1;
						   if(propertyWeight.containsKey(po[0])){
							   relatePropertyWeight = propertyWeight.get(po[0]);
						   }

						   
						   
						   String [] objLabelandWeight = thisLabel.split("\\|");
						   float relateLabelWeight = 1;
						   
						   if(objLabelandWeight.length > 1){
							   relateLabelWeight = Float.parseFloat(objLabelandWeight[1]);
						   }
						   
						   //think about how relate final weight works
						   Float relateFinalWeight = (float) (1 + Math.log(relateObjectWeight * relatePropertyWeight * relateLabelWeight));
						   thisLabel = objLabelandWeight[0]+"|"+relateFinalWeight;								
						   Field relatedObjectField = new TextField("related_object", thisLabel, Field.Store.NO);
						   //System.out.println("related_object: "+thisLabel);
						   
//						   float entropy = getPropertyWeight(po[0]);
//						   relatedObjectField.setBoost(entropy);
						   doc.add(relatedObjectField);
						   //doc.add(object_original);
					   }
				   }
			   }else{
				   float relatePropertyWeight = 1;
				   if(propertyWeight.containsKey(po[0])){
					   relatePropertyWeight = propertyWeight.get(po[0]);
				   }
				   String thisLabel = Utils.toPhrase(po[1])+"|"+relatePropertyWeight;								
				   Field relatedObjectField = new TextField("related_object", thisLabel, Field.Store.NO);
				   //System.out.println("non url object: "+thisLabel);
				  // Field relatedObjectField = new TextField("related_object", Utils.toPhrase(po[1]), Field.Store.NO);
				   doc.add(relatedObjectField);
			   }
		   }
		   //doc.add(new NumericDocValuesField("boost", 1L));
		   
		   if(weight != null){
			   System.out.println("in weight: "+weight);
			   doc.add(new FloatField("boost",Float.parseFloat(weight), Field.Store.YES));	
		   }else{
			   System.out.println("weight: "+weight);
			   doc.add(new FloatField("boost",1, Field.Store.YES));
		   }
		   return doc;
	}


}
