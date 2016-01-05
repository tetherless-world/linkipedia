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

package edu.rpi.tw.linkipedia.search.recognizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queries.function.valuesource.FloatFieldSource;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.payloads.MaxPayloadFunction;
import org.apache.lucene.search.payloads.PayloadFunction;
import org.apache.lucene.search.payloads.PayloadTermQuery;
import org.apache.lucene.store.FSDirectory;

import edu.rpi.tw.linkipedia.search.nlp.NaturalLanguageProcessor;
import edu.rpi.tw.linkipedia.search.similarity.MySimilarity;
import edu.rpi.tw.linkipedia.search.utils.Utils;

public class EntityRecognizer {
	
    NaturalLanguageProcessor nlp;
    IndexSearcher searcher;
    float threshold = 15;
	PayloadFunction payloadFunction  = new MaxPayloadFunction() ;
	Dictionaries dicts;
	
    public EntityRecognizer(String index){
    	//need change
    	
    	
    	
    	nlp = new NaturalLanguageProcessor();
		IndexReader reader;
		try {
			dicts = new Dictionaries("Dataset/Dictionary/Dicts_Blender_2");
			reader = DirectoryReader.open(FSDirectory.open(new File(index)));
			searcher = new IndexSearcher(reader);
			searcher.setSimilarity(new MySimilarity());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    public EntityRecognizer(IndexSearcher searcher){
    	this.searcher = searcher;
    	nlp = new NaturalLanguageProcessor();
    	
    	try {
			dicts = new Dictionaries("Dataset/Dictionary/Dicts_Blender_2");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    /*
    public ArrayList<String> getEntityMentions(String sentences){
    	ArrayList<String> mentions = new ArrayList<String> ();    	
    	nlp.setText(sentences);
    	ArrayList<String> phrases = nlp.getNounPhrases();
    	
    	for(String phrase:phrases){
    		mentions.addAll(getPhraseMentions(phrase));
    	}
    	
    	return mentions;
    }
    */
    
    public ArrayList<String> getEntityMentions(String text){
    	ArrayList<String> mentions = new ArrayList<String> ();  
    	//System.out.println(text);
    	nlp.setText(text);
    	//nlp.printTree();getSentences
    	ArrayList<String> sentences = nlp.getNounPhrases();
    	//ArrayList<String> sentences = nlp.getSentences();
    	for(String sentence:sentences){
    		/*
    		if(sentence.length() > 2)
    			sentence = sentence.replaceAll("[^a-zA-Z0-9\\\\]", " ").replaceAll("\\s+", " ").trim();
    		*/
    		//System.out.println("current: "+sentence);
    		mentions.addAll(getPhraseMentions(sentence));
    	}
    	
    	return mentions;
    }
    public ArrayList<String> getSentences(String sentences){
    	//System.out.println("Processing: ");
    	nlp.setText(sentences);
    	return nlp.getSentences();
    }
    //recursive implementation of getPhraseMentions
    public ArrayList<String> getPhraseMentions(String phrase){
    	//phrase = Utils.removeChars(phrase);
		ArrayList<String> mentions = new ArrayList<String>();
		//System.out.println("reg process "+phrase);
		try {
			String [] terms = phrase.split(" ");
			
			int end = 1;
			int start = 0;
			
			while(start < terms.length){
				
				
				String tryTerm = append(start,end,terms);
				
				if(tryTerm.equals("a") || tryTerm.equals("an")){
					start++;
					end = start + 1;
					continue;
				}
				
				//System.out.println("Starting at: "+start+" ending at: "+end+" "+tryTerm);
				//check if dictionary have term that has prefix tryTerm
				if(!tryTerm.equals("") && lookup(tryTerm)){
					if(end < terms.length){
						end++;
						continue;
					}
				}
				
				//if no term with such prefix, check if we have terms that is exactly the same as tryTerm--
				boolean eligible = validatable(start,end,terms);
				if(eligible){
				
					tryTerm = append(start,end,terms);			
					while(end > start && eligible && (!validateTerm(tryTerm, start, end, terms))){
						end --;
						tryTerm = append(start,end,terms);
						eligible = validatable(start,end,terms);
					}
				}
				if(eligible && start != end){
					String term = append(start,end,terms);
					
					
					//term = Utils.filterStopWords(term);
					if(term.length() > 0)
						mentions.add(term);
					
					//mentions.add(term);
					start = end;
					end++;
				}else{
					//System.out.println("current start: "+start+" current end: "+end);
					start++;
					end = start + 1;
				}
			}	
			return mentions;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mentions;
		
    }
    private boolean validatable(int start, int end, String [] terms){
    	
    	while(start < end){
    		if(terms[start].contains("|N")||terms[start].contains("|CD")){
    			//System.out.println("is validate: "+terms[start]);
    			return true;
    		}
    		start++;
    	}
    	return false;
    	
    	//System.out.println("is validate: "+terms[end-1]+" with "+start+" to "+end);
    }
	public String append(int start, int end, String [] terms){
		StringBuilder sb = new StringBuilder();
		
		if(end - start == 1){
			String [] part = terms[start].split("\\|");
			//System.out.println("current word: "+terms[start]+" "+part.length);
			if(part.length != 2 || ((! Utils.isAllUpper(part[0])) && Utils.filterStopWords(part[0]).equals("")) )
				return "";
			
			//surface form already remove chars
			part[0]=Utils.removeChars(part[0]);
			
			sb.append(part[0]);

			return sb.toString().trim();
		}
		
		for(int i = start; i < end && i < terms.length; i++){
			String [] part = terms[i].split("\\|");
			if(part.length < 2)
				continue;
			part[0]=Utils.removeChars(part[0]);
			sb.append(part[0]);
			sb.append(" ");
		}
		return sb.toString().trim();
	}
    private boolean validateTerm(String query, int start, int end, String [] terms){
		try {
			
			if(dicts.hasTerm(query)){
				System.out.println(query+" validate by dicts");
				return true;
			}else{
				System.out.println(query+" validate by dicts FAILED");
			}
			
			
			
			
			if(!(terms[end-1].contains("|N")))
				return false;
			
//			if(terms[end-1].contains("|NNP"))
//				return true;
			
			query = query.toLowerCase();
			query = query.trim();

			Term labelTerm = new Term("label",query);
			PayloadTermQuery ptq = new PayloadTermQuery(labelTerm, payloadFunction);

			//FunctionQuery boostQuery = new FunctionQuery(new FloatFieldSource("boost"));
			//boostQuery.setBoost(100);ptq
			//Query finalQuery = new CustomScoreQuery(ptq, boostQuery);
			
			
			TopDocs topDocs = searcher.search(ptq, 1);
			ScoreDoc[] hits = topDocs.scoreDocs;
			//System.out.println("validate: "+ptq.toString()+" "+hits.length);
			if(hits.length > 0){
				int docId = hits[0].doc;
				Document doc = searcher.doc(docId);
				String url = doc.get("url");
				System.out.println("validate: "+ptq.toString()+" got "+url);
			}
				
			return hits.length == 1;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
    }
	private boolean lookup(String query){  
		
		
		//try dictionary
		String type = dicts.search(query);
		if(type!=null){
			System.out.println(query+" search by dicts");
			return true;
		}else{
			System.out.println(query+" search by dicts FAILED");
		}
		
		BooleanQuery luceneQuery = new BooleanQuery();
		try {
			query = query.toLowerCase();
			query = query.trim();
			//System.out.println("query: "+query);
			Term term = new Term("lookUpLabel",query);
			TermQuery termQuery = new TermQuery(term);//parser.parse(query);
			Term labelTerm = new Term("label",query);
			PayloadTermQuery ptq = new PayloadTermQuery(labelTerm, payloadFunction);
			luceneQuery.add(termQuery, Occur.SHOULD);
			luceneQuery.add(ptq, Occur.SHOULD);
			//FunctionQuery boostQuery = new FunctionQuery(new FloatFieldSource("boost"));
			//Query finalQuery = new CustomScoreQuery(luceneQuery, boostQuery);

			TopDocs topDocs = searcher.search(luceneQuery, 1);
			ScoreDoc[] hits = topDocs.scoreDocs;
//			if(hits.length == 0){
//				System.out.println("look up: "+luceneQuery.toString()+" failed");
//			}
			//System.out.println("look up: "+luceneQuery.toString()+" "+hits.length);
			return hits.length == 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
		/*
		Document doc = null;
		try {
			doc = searcher.doc(hits[0].doc);
			weight = hits[0].score;
			System.out.println(query+": "+doc.get("url")+" "+weight);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return weight;
		*/

	}
	public void setThreshold(float threshold) {
		this.threshold = threshold;	
	}
}
