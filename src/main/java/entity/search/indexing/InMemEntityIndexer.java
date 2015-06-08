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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import entity.search.similarity.MySimilarity;

public class InMemEntityIndexer {
	String sourceDirectory;
	String indexDirectory;
	HashMap<String, Set<String>> labels;
	Hashtable<String, Float> entityWeight;
	Hashtable<String, Float> propertyWeight;
	
	public InMemEntityIndexer(String source, String destination){
		sourceDirectory = source;
		indexDirectory = destination;
		propertyWeight = new Hashtable<String,Float>();
		entityWeight = new Hashtable<String,Float>();
		labels = new HashMap<String, Set<String>>();
	}
	
	public void createIndex(){
		try{

			  Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
			  IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_47, analyzer);
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
			            			writer.addDocument(doc);

			            		}
			            		subject = spo[0];
			            		triples = new ArrayList<String>();
			            		contents = "";
			            	}
			            	triples.add(spo[1]+" "+spo[2]);
			            	if(!(spo[2].startsWith("<")||spo[2].startsWith("_"))){
			            		contents += spo[2];
			            	}
			            }
			        }catch(Exception e){
			        	e.printStackTrace();
			        }
		      }
		    }
	}
	public void setEntityWeightFile(String weightFile){
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
	        	entityWeight.put(weight[0], Float.parseFloat(weight[1]));
	        }
	        in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private float getEntityWeight(String url){
		if(entityWeight.containsKey(url)){
			return entityWeight.get(url);
		}
		return 1;
	}
	public void setPropertyWeightFile(String weightFile){
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
	public void setEntityLabels(String labelFile){
		try{
			System.out.println("label file: "+labelFile);
	        FileInputStream fstream = new FileInputStream(labelFile);
	        DataInputStream in = new DataInputStream(fstream);
	        BufferedReader weightReader = new BufferedReader(new InputStreamReader(in));
	        String line;
	        while((line = weightReader.readLine())!=null){
	        	String [] spo = line.split(" ",3);
	        	if(spo.length < 3)
	        		continue;
	        	if(labels.containsKey(spo[0])){
	        		Set<String> myLabels = labels.get(spo[0]);
	        		addingLabels(myLabels, spo[2]);
	        		labels.put(spo[0], myLabels);
	        	}else{
	        		Set<String> myLabels = new HashSet<String>();
	        		addingLabels(myLabels, spo[2]);
	        		labels.put(spo[0], myLabels);    		
	        	}
	        }
	        in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
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
			noSingleLetter.add(removeSingleLetter(str));
		}
		hashSet.addAll(noSingleLetter);
	}
	private String removeSingleLetter(String label){
		label = label.replaceAll("(\\s+[a-z](?=\\s))"," ");
		return label;
	}
	private Document getDoc(String subject, HashMap<String, Object> data) throws CorruptIndexException, IOException{
		   Document doc = new Document();
		   String url = (String)data.get("url");
		   Set<String> myLabels = labels.get(url);
		   if(myLabels.isEmpty())
			   return doc;
		   
		   doc.add(new StringField("url", url, Field.Store.YES));
		   doc.add(new TextField("contents", (String)data.get("contents"), Field.Store.YES));		   
		   for(String thisLabel:myLabels){
			   doc.add(new StringField("label",thisLabel, Field.Store.YES));
			   doc.add(new TextField("analyzedLabel",thisLabel, Field.Store.NO));
		   }
		   ArrayList<String> triples = (ArrayList<String>) data.get("triples");
		   for(int i = 0 ; i < triples.size(); i++){
			   String triple = triples.get(i);
			   String [] po = triple.split(" ",2);
			   if(po.length < 2)
				   continue;
			   
			   //payload analyzer
			   if(po[1].startsWith("<") || po[1].startsWith("_")){
				   Set<String> objLabels = labels.get(po[1]);
				   for(String thisLabel:objLabels){
					   Field relatedObjectField = new StringField("related_object", thisLabel, Field.Store.YES);
					   Field object_original = new StringField("object_original", thisLabel+" || "+triple, Field.Store.YES);
					   float entropy = getPropertyWeight(po[0]);
					   relatedObjectField.setBoost(entropy);
					   doc.add(relatedObjectField);
					   doc.add(object_original);
				   }
			   }
		   }
		   
		   float weight = getEntityWeight(url);
		   System.out.println(url+" "+weight);
		   //doc.setBoost(weight);
		   return doc;
	}
}
