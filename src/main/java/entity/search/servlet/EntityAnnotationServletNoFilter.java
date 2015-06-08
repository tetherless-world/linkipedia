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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import entity.search.index.analyzer.AnnotationDecisionFilter;
import entity.search.query.Coreference;
import entity.search.query.WeightedQuery;
import entity.search.searching.EntityAnnotator;
import entity.search.searching.EntityLinker;
import entity.search.searching.EntitySearcher;
import entity.search.similarity.ValidationRank;
import entity.search.similarity.ValidationRankSurface;
import entity.search.utils.Annotation;
import entity.search.utils.Utils;

public class EntityAnnotationServletNoFilter extends HttpServlet{
	
	EntityAnnotator annotator = null ;
	int validationSetNum = 7;
	  public EntityAnnotationServletNoFilter(String index, String surfaceIndex){
		  annotator = new EntityAnnotator(index);
		  annotator.setRecognizer(surfaceIndex);
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
			String query = request.getParameter("query");
			
			//Hashtable<String,String> coref_table = Coreference.coreferencing(query);
			
			
			//query = query.toLowerCase();
			String numResult = request.getParameter("numResult");
			if(query == null){
				out.write("Query is empty");
				return;
			}
			if(numResult == null){
				numResult = "1";
			}
		    long start = System.currentTimeMillis();
		    long mention_start = System.currentTimeMillis();
			ArrayList<String> mentions = annotator.getEntityMentions(query);
			String[] terms = mentions.toArray(new String[mentions.size()]);
			long mention_end = System.currentTimeMillis();
			System.out.println("Processing mentions took: "+(mention_end - mention_start));
//		    ArrayList<String> contexts = new ArrayList<String>();
//		    for(int i = 0; i < terms.length; i++){
//		    	contexts.add(terms[i].trim());
//		    }
		    
			long linking_start = System.currentTimeMillis();
		    ArrayList<ArrayList<Annotation>> allAnnotations = new ArrayList<ArrayList<Annotation>>();	    
		    Hashtable<String, ArrayList<String>> context_table = new Hashtable<String, ArrayList<String>>();
		    
//		    HashSet<String> uniq_terms = new HashSet<String>();
//		    for(int i = 0 ;i < terms.length; i++){
//		    	uniq_terms.add(terms[i]);
//		    }
		    
//		    ArrayList<String> context_terms = new ArrayList<String>(uniq_terms);
		    
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
		    			}else if(WeightedQuery.strictLabelRelate(annotator.getSearcher(), terms[j], terms[k].trim())){
		    				currentRelatedContext.add(terms[k]);
		    				relation_cache.add(terms[j]+terms[k]);
		    				relation_cache.add(terms[k]+terms[j]);
		    			}
		    		}
		    	
		    	}
		    	context_table.put(terms[j], new ArrayList<String>(currentRelatedContext));
		    	//context_table.put(terms[j], context_terms);
		    	String[] contexts = currentRelatedContext.toArray(new String[currentRelatedContext.size()]);
		    	
		    	
				ArrayList<Annotation> annotations = annotator.mysearch(terms[j], contexts, validationSetNum);
				allAnnotations.add(annotations);

		    }
		    long linking_end = System.currentTimeMillis();
		    System.out.println("Linking took: "+(linking_end - linking_start));
		    
		    System.out.println("Validation starts...");
		    ValidationRankSurface validator = new ValidationRankSurface(annotator.getSearcher());
		    ArrayList<ArrayList<Annotation>> results = new ArrayList<ArrayList<Annotation>>();
		    for(ArrayList<Annotation> anno:allAnnotations){
		    	long rank_single_start = System.currentTimeMillis();
		    	ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		    	Annotation myAnnotation = validator.rank(anno, allAnnotations,context_table);
		    	annotations.add(myAnnotation);
		    	results.add(annotations);
		    	long rank_single_end = System.currentTimeMillis();
		    	System.out.println("rerank "+anno+" took: "+(rank_single_end - rank_single_start));
		    }
/*		    
		    System.out.println("Filtering starts...");
		    AnnotationDecisionFilter filter = new AnnotationDecisionFilter(annotator.getSearcher());
		   
		    for(int j = 0 ; j < results.size();j++){
		    	ArrayList<Annotation> result = results.get(j);
				for(int i = 0 ;i < result.size();i++){
					ArrayList<String> mytypes = filter.getTypes(result.get(i).getAnnotation());
					if(!(filter.isOneOfType(mytypes))){
						result.remove(i);
					}
				}
				results.set(j, result);
		    }
		    */
			long end = System.currentTimeMillis();
			long time = end - start;
			//System.out.println();
		    String json_result = getJsonResult(results,query,Integer.parseInt(numResult),time);
			out.println(json_result);
	      //out.close();
	  }
	  public String getJsonResult(ArrayList<ArrayList<Annotation>> results, String query, int numReuslt, long time){
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
					annotation_result += "{";
					annotation_result += "\"url\": \""+current.getAnnotation()+"\",";
					annotation_result += "\"score\": \""+current.getScore()+"\"";
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
	  
	  /*
	  public void doGet(HttpServletRequest request,
	                    HttpServletResponse response)
	            throws ServletException, IOException
	  {
	      	response.setContentType("text/json");      
	      	PrintWriter out = response.getWriter();
			String query = request.getParameter("query");
			query = query.toLowerCase();
			String numResult = request.getParameter("numResult");
			if(query == null){
				out.write("Query is empty");
				return;
			}
			if(numResult == null){
				numResult = "1";
			}
			//think about put everything in entity annotator class
			
			ArrayList<String> terms = annotator.getEntityMentions(query);
			

			String annotation_result = "";
			String outerComma = "";
		    float start = System.currentTimeMillis();
		    
		    for(int j = 0 ; j < terms.size(); j++){	
			    //contexts.remove(terms[j]);
		    	ArrayList<String> currentRelatedContext = new ArrayList<String>();
		    	for(int k = 0 ; k < terms.size(); k++){
		    		if(k==j)
		    			continue;
		    		
		    		if(WeightedQuery.strictLabelRelate(annotator.getSearcher(), terms.get(j), terms.get(k).trim())){
		    			currentRelatedContext.add(terms.get(k));
		    		}
		    	
		    	}
		    	//need to be careful on this decision
		    	if(currentRelatedContext.size() == 0)
		    		continue;
		    	
		    	String[] contexts = currentRelatedContext.toArray(new String[currentRelatedContext.size()]);
		    	
		    	//System.out.println("this contexts: "+contexts.toString());
		    	
				ArrayList<Annotation> annotations = annotator.mysearch(terms.get(j), contexts, Integer.parseInt(numResult));
				//contexts.add(terms[j]);
				annotation_result += outerComma;
				annotation_result += "{";
				annotation_result += "\"entity_mention\": \""+terms.get(j) +"\",";
				annotation_result += "\"annotations\": [";
				String comma = "";
				for(int k = 0; k < annotations.size(); k++){
					Annotation current = annotations.get(k);
					annotation_result += comma;
					annotation_result += "{";
					annotation_result += "\"url\": \""+current.getAnnotation()+"\",";
					annotation_result += "\"score\": \""+current.getScore()+"\"";
					annotation_result += "}";
					comma = ",";
				}
				annotation_result += "]";
				annotation_result += "}";
				outerComma =",";
		    }
		    annotation_result +="";
			float end = System.currentTimeMillis();
			float time = end - start;
		    String json_result = "{";
			json_result += "\"query\": \""+query+"\",";
			json_result += "\"time\": \""+time+"\",";
			json_result += "\"results\": [";
			json_result += annotation_result;
			json_result += "]}";
			out.println(json_result);
	      //out.close();
	  }
	  
	  public void destroy()
	  {
	  }
	  */
}
