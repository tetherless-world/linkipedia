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

package entity.search.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import entity.search.cache.LRUCache;
import entity.search.index.analyzer.AnnotationDecisionFilter;
import entity.search.query.Coreference;
import entity.search.query.WeightedQuery;
import entity.search.searching.EntityLinker;
import entity.search.searching.EntitySearcher;
import entity.search.similarity.MySimilarity;
import entity.search.similarity.ValidationRank;
import entity.search.similarity.ValidationRankSurface;
import entity.search.utils.Annotation;
import entity.search.utils.Utils;

public class EntityLinkingWithValidationServletSurfaceSingleTop extends HttpServlet{
	
	EntityLinker searcher = null ;
	 int validationSetNum = 10;
	  //ValidationRank validator = null;
	  IndexSearcher sfSearcher = null;
	  LRUCache cache = null;

	  public EntityLinkingWithValidationServletSurfaceSingleTop(String index){
		  searcher = new EntityLinker(index);
		  cache = new LRUCache(1000);

	  }
	  
	  public EntityLinkingWithValidationServletSurfaceSingleTop(String index, String sfIndex){
		  searcher = new EntityLinker(index);
		  cache = new LRUCache(1000);
			 try {
					IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(sfIndex)));
					sfSearcher = new IndexSearcher(reader);
					sfSearcher.setSimilarity(new MySimilarity());
			} catch (Exception e) {
				e.printStackTrace();
			} 
		  //validator = new ValidationRank(searcher.getSearcher(),sfSearcher);
	  }
	  
	  public void init() throws ServletException
	  {
	  }
	 
	  public void doGet(HttpServletRequest request,
	                    HttpServletResponse response)
	            throws ServletException, IOException
	  {
	      	response.setContentType("text/json");      
	      	PrintWriter out = response.getWriter();
			String query = request.getParameter("query").replaceAll(",+",",").replaceAll("\\s+"," ");
			//System.out.println("===="+query);
			
			
			String labelWeight = request.getParameter("labelWeight");
			String relationWeight = request.getParameter("relationWeight");
			String defaultWeight = request.getParameter("defaultWeight");
			String typeWeight = request.getParameter("typeWeight");
			String contentWeight = request.getParameter("contentWeight");
			String dirRelateWeight = request.getParameter("dirRelateWeight");
			String coOccurWeight = request.getParameter("coOccurWeight");
			String nameWeight = request.getParameter("nameWeight");
			
			
			//float mylabelWeight = 10;
			float mylabelWeight = 1;
			if(labelWeight!=null)
				mylabelWeight = Float.parseFloat(labelWeight);
			
			//float myrelationWeight = 3;
			float myrelationWeight = 1;
			if(relationWeight!=null)
				myrelationWeight = Float.parseFloat(relationWeight);
			
			//float mydefaultWeight = 6;
			float mydefaultWeight = 1;
			if(defaultWeight!=null)
				mydefaultWeight = Float.parseFloat(defaultWeight);
			
			//float mytypeWeight = 5;
			float mytypeWeight = 1;
			if(typeWeight!=null)
				mytypeWeight = Float.parseFloat(typeWeight);
			
			float mycontentWeight = 1;
			if(contentWeight!=null)
				mycontentWeight = Float.parseFloat(contentWeight);
			
			float mydirRelateWeight = 1;
			if(dirRelateWeight!=null)
				mydirRelateWeight = Float.parseFloat(dirRelateWeight);
			
			float mycoOccurWeight = 1;
			if(coOccurWeight!=null)
				mycoOccurWeight = Float.parseFloat(coOccurWeight);
			
			float mynameWeight = 1;
			if(nameWeight!=null)
				mynameWeight = Float.parseFloat(nameWeight);			
			
//			System.out.println("mylabelWeight: "+mylabelWeight);
//			System.out.println("mycontentWeight: "+mycontentWeight);
//			System.out.println("myrelationWeight: "+myrelationWeight);
//			System.out.println("mytypeWeight: "+mytypeWeight);
//			System.out.println("mydefaultWeight: "+mydefaultWeight);
//			System.out.println("mycoOccurWeight: "+mycoOccurWeight);
//			System.out.println("mydirRelateWeight: "+mydirRelateWeight);
//			System.out.println("mynameWeight: "+mynameWeight);
			searcher.setWeights(mylabelWeight, mycontentWeight, myrelationWeight, mytypeWeight, mydefaultWeight);
			
			
			//Hashtable<String,String> coref_table = Coreference.coreferencing(query);
			
		    ArrayList<ArrayList<Annotation>> results = new ArrayList<ArrayList<Annotation>>();

			query = query.toLowerCase();
			String numResult = request.getParameter("numResult");
			if(query == null){
				out.write("Query is empty");
				return;
			}
			if(numResult == null){
				numResult = "10";
			}
		    String [] terms = query.split(",");
//		    ArrayList<String> contexts = new ArrayList<String>();
//		    for(int i = 0; i < terms.length; i++){
//		    	contexts.add(terms[i].trim());
//		    }

		    long start = System.currentTimeMillis();
		    
		    long linking_start = System.currentTimeMillis();
		    ArrayList<ArrayList<Annotation>> allAnnotations = new ArrayList<ArrayList<Annotation>>();
		   
		    
		    Hashtable<String, ArrayList<String>> context_table = new Hashtable<String, ArrayList<String>>();
		    
		    HashSet<String> uniq_terms = new HashSet<String>();
		    for(int i = 0 ;i < terms.length; i++){
		    	uniq_terms.add(terms[i]);
		    }
		    
		    ArrayList<String> context_terms = new ArrayList<String>(uniq_terms);
		    
		    HashSet<String> relation_cache = new HashSet<String>();
		    
		    if(0 >= terms.length){
		    	out.println("Empty query");
		    	return;
		    }
		    

		    
		    for(int j = 0 ; j < terms.length; j++){	
					
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

		    	context_table.put(terms[j], context_terms);
		    	String[] contexts = currentRelatedContext.toArray(new String[currentRelatedContext.size()]);
			    //check if terms[j] is cached, if so return, otherwise process
		    	ArrayList<Annotation> annotations = searcher.mysearch(terms[j], contexts, validationSetNum);
//		    	ArrayList<Annotation> annotations = null;//searcher.mysearch(terms[j], contexts, validationSetNum);
//				if((annotations = cache.get(terms[j])) == null){
//					annotations = searcher.mysearch(terms[j], contexts, validationSetNum);
//					cache.set(terms[j], annotations);
//				}
				allAnnotations.add(annotations);
		    }
		    
		    long linking_end = System.currentTimeMillis();
		    System.out.println("Linking took: "+(linking_end - linking_start));
		    
		    long validation_start = System.currentTimeMillis();
		    System.out.println("Validation starts...");
		    ValidationRankSurface validator = new ValidationRankSurface(searcher.getSearcher(),sfSearcher);
		    validator.setWeight(mycoOccurWeight, mydirRelateWeight, mynameWeight);
		   
		    
		    

		    for(ArrayList<Annotation> anno:allAnnotations){
		 	    	
		    	if(anno.size() == 0 || !anno.get(0).getKeyword().equals(terms[0])){
		    		continue;
		    	}
		    	
		    	ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		    	ArrayList<Annotation> myAnnotation = validator.rankTop(anno, allAnnotations,context_table);
		    	annotations.addAll(myAnnotation);
		    	results.add(annotations);
		    }
		    long validation_end = System.currentTimeMillis();
		    System.out.println("Validation took: "+(validation_end - validation_start));
		    
			long end = System.currentTimeMillis();
			long time = end - start;
			
			AnnotationDecisionFilter filter = new AnnotationDecisionFilter(searcher.getSearcher());
			
			
		    String json_result = getJsonResult(results,query,Integer.parseInt(numResult),time,filter);		    
			out.println(json_result);
			
		    //String json_result = getJsonResult(results,query,Integer.parseInt(numResult),time);
			//out.println(json_result);
	      //out.close();
	  }
	  public String getJsonResult(ArrayList<ArrayList<Annotation>> results, String query, int numReuslt, long time,AnnotationDecisionFilter filter){
			String annotation_result = "";
			String outerComma = "";
		    

			for(ArrayList<Annotation> annotations:results){
				
				if(annotations.size() == 0)
					continue;
				
				annotation_result += outerComma;
				annotation_result += "{";
				annotation_result += "\"entity_mention\": \""+annotations.get(0).getKeyword() +"\",";
				annotation_result += "\"annotations\": [";
				String comma = "";
				for(int k = 0; k < annotations.size() && k < numReuslt; k++){
					Annotation current = annotations.get(k);
					annotation_result += comma;
					ArrayList<String> types = filter.getGPOType(current.getAnnotation());
					
					
					annotation_result += "{";
					annotation_result += "\"url\": \""+current.getAnnotation()+"\",";
					annotation_result += "\"score\": \""+current.getScore()+"\",";
					annotation_result += "\"context_score\": \""+current.getContextScore()+"\",";

					
					if(types.size() == 0){
						annotation_result += "\"types\": [{ \"type\": \"NIL\"}]";
					}else{
						annotation_result += "\"types\": [";
						String type_comma = "";
						for(String type:types){
							annotation_result += type_comma;
							annotation_result += "{ \"type\": \""+type+"\" }";
							type_comma = ",";
						}
						annotation_result += "]";
					}
					annotation_result += "}";
					comma = ",";
				}
				annotation_result += "]";
				annotation_result += "}";
				outerComma =",";
				
				
			}
		    annotation_result +="";
			//long end = System.currentTimeMillis();
			//long time = end - start;
		    String json_result = "{";
			json_result += "\"query\": \""+query+"\",";
			json_result += "\"time\": \""+time+"\",";
			json_result += "\"results\": [";
			json_result += annotation_result;
			json_result += "]}";
			return json_result;
	  }
	  public void destroy()
	  {
	  }
}
