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

package edu.rpi.tw.linkipedia.search.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.payloads.MaxPayloadFunction;

import edu.rpi.tw.linkipedia.search.query.WeightedQuery;
import edu.rpi.tw.linkipedia.search.recognizer.EntityRecognizer;
import edu.rpi.tw.linkipedia.search.searching.EntityLinker;
import edu.rpi.tw.linkipedia.search.utils.Annotation;


public class ParallelEntityLinker extends Thread{
	
	static EntityLinker searcher;
	MaxPayloadFunction payloadFunctionMax = new MaxPayloadFunction() ;
	WeightedQuery wquery;
	static int currentTermIndex = 0;
	static String [] terms;
	static ArrayList<Annotation> globalAnnotation = new ArrayList<Annotation>();
	
	public static void main(String [] args){
		
		searcher= new EntityLinker("");
		String [] terms = {"a","b","c","d"};
		
	    ArrayList<ArrayList<Annotation>> allAnnotations = new ArrayList<ArrayList<Annotation>>();

	    HashSet<String> relation_cache = new HashSet<String>();
	    
	    for(int j = 0 ; j < terms.length; j++){	
		    //contexts.remove(terms[j]);
	    	HashSet<String> currentRelatedContext = new HashSet<String>();
	    	for(int k = 0 ; k < terms.length; k++){
	    		if(k==j){
	    			currentRelatedContext.add(terms[k]);
	    		
	    		}else {
	    			String relation_cache_key = terms[j]+terms[k];
	    			if(relation_cache.contains(relation_cache_key)){
	    				//System.out.println("relation cache found: "+relation_cache_key);
	    				currentRelatedContext.add(terms[k]);
	    			}else if(WeightedQuery.isRelated(searcher.getSearcher(), terms[j], terms[k].trim())){
	    				currentRelatedContext.add(terms[k]);
	    				relation_cache.add(terms[j]+terms[k]);
	    				relation_cache.add(terms[k]+terms[j]);
	    			}
	    		}
	    	
	    	}

	    	String[] contexts = currentRelatedContext.toArray(new String[currentRelatedContext.size()]);
	    	
	    	
			ArrayList<Annotation> annotations = searcher.mysearch(terms[j], contexts, 7);
			allAnnotations.add(annotations);

	    }
	}
	
	public ParallelEntityLinker(){

	}

	private int localTermIndex;
	private HashSet<String> currentRelatedContext = new HashSet<String>();
	public void run() { 
		//System.out.println(sentence);
		//String currentSentence = 
		while(getCurrentTerm()){
			
		for(int k = 0 ; k < terms.length; k++){
    		if(k==localTermIndex){
    			currentRelatedContext.add(terms[k]);
    		
    		}else {
    			String relation_cache_key = terms[localTermIndex]+terms[k];
    			if(checkCache(relation_cache_key)){
    				//System.out.println("relation cache found: "+relation_cache_key);
    				currentRelatedContext.add(terms[k]);
    			}else if(WeightedQuery.isRelated(searcher.getSearcher(), terms[localTermIndex].trim(), terms[k].trim())){
    				currentRelatedContext.add(terms[k]);
    				updateCache(terms[localTermIndex]+terms[k]);
    				updateCache(terms[k]+terms[localTermIndex]);
    			}
    		}
		}
		
		
			//globalAnnotation.put(terms[localTermIndex], context_terms);
	    	String[] contexts = currentRelatedContext.toArray(new String[currentRelatedContext.size()]);
	    	
	    	
			ArrayList<Annotation> annotations = searcher.mysearch(terms[localTermIndex], contexts, 7);
			
			addToGlobal(annotations);
		}
	} 
	public synchronized void updateCache(String term){
		
	}
	public synchronized boolean checkCache(String key){
		return false;
	}
//	public ArrayList<String> getMentions(){
//		return mentions;
//	}
	public synchronized boolean getCurrentTerm(){
		if(currentTermIndex == terms.length)
			return false;
		localTermIndex = currentTermIndex;
		System.out.println(getName()+" assign "+terms[currentTermIndex]);
		currentTermIndex++;
		return true;
		//return mySentence;
	}
	public synchronized void addToGlobal(ArrayList<Annotation> annotations){
		globalAnnotation.addAll(annotations);
	}

}
