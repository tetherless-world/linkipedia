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

import entity.search.query.WeightedQuery;
import entity.search.searching.EntityLinker;
import entity.search.searching.EntitySearcher;
import entity.search.similarity.MySimilarity;
import entity.search.similarity.ValidationRank;
import entity.search.utils.Annotation;
import entity.search.utils.Utils;

public class ParallelEntityLinkingWithValidationServlet extends HttpServlet{
	
	EntityLinker searcher = null ;
	static int currentIndex = 0;
	int validationSetNum = 5;
	private static final Object OBJ_LOCK = new Object(); 
	  //ValidationRank validator = null;
	  //IndexSearcher sfSearcher = null;
	  public ParallelEntityLinkingWithValidationServlet(String index){
		  searcher = new EntityLinker(index);
	  }
	  /*
	  public EntityLinkingWithValidationServlet(String index, String sfIndex){
		  searcher = new EntityLinker(index);
			 try {
					IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(sfIndex)));
					sfSearcher = new IndexSearcher(reader);
					sfSearcher.setSimilarity(new MySimilarity());
			} catch (Exception e) {
				e.printStackTrace();
			} 
		  validator = new ValidationRank(searcher.getSearcher(),sfSearcher);
	  }
	  */
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
			query = query.toLowerCase();
			String numResult = request.getParameter("numResult");
			if(query == null){
				out.write("Query is empty");
				return;
			}
			if(numResult == null){
				numResult = "1";
			}
		    String [] terms = query.split(",");
//		    ArrayList<String> contexts = new ArrayList<String>();
//		    for(int i = 0; i < terms.length; i++){
//		    	contexts.add(terms[i].trim());
//		    }

		    long start = System.currentTimeMillis();
		    
		    ArrayList<ArrayList<Annotation>> allAnnotations = new ArrayList<ArrayList<Annotation>>();
		    
		    
		    Hashtable<String, ArrayList<String>> context_table = new Hashtable<String, ArrayList<String>>();
		    ArrayList<String> context_terms = new ArrayList<String>(Arrays.asList(terms));
		    
		    //HashSet<String> relation_cache = new HashSet<String>();
		    for(int j = 0 ; j < terms.length; j++){	
			    //contexts.remove(terms[j]);
		    	context_table.put(terms[j], context_terms);
		    }
		    
		    System.out.println("Validation starts...");
		    ValidationRank validator = new ValidationRank(searcher.getSearcher());
		    ArrayList<ArrayList<Annotation>> results = new ArrayList<ArrayList<Annotation>>();
		    for(ArrayList<Annotation> anno:allAnnotations){
		    	ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		    	Annotation myAnnotation = validator.rank(anno, allAnnotations,context_table);
		    	annotations.add(myAnnotation);
		    	results.add(annotations);
		    }

			long end = System.currentTimeMillis();
			long time = end - start;
		    String json_result = getJsonResult(results,query,Integer.parseInt(numResult),time);
			out.println(json_result);
	      //out.close();
	  }
	  /*
		public void run() { 
			while(singleNodeAnnotate()){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}
	*/ 
	  public ArrayList<Annotation> singleNodeAnnotate(String [] terms){
		  int myIndex;
		  synchronized(OBJ_LOCK){
			  myIndex = currentIndex;
			  currentIndex++;
		  }
		  
		  ArrayList<String> currentRelatedContext = new ArrayList<String>();
	    	for(int k = 0 ; k < terms.length; k++){
	    		if(k==myIndex){
	    			currentRelatedContext.add(terms[k]);
	    		
	    		}else {
	    			if(WeightedQuery.isRelated(searcher.getSearcher(), terms[myIndex], terms[k].trim())){
	    				currentRelatedContext.add(terms[k]);
	    			}

	    		}
	    	
	    	}
	    	String[] contexts = currentRelatedContext.toArray(new String[currentRelatedContext.size()]);
	    	
			ArrayList<Annotation> annotations = searcher.mysearch(terms[myIndex], contexts, validationSetNum );
		  
			return annotations;
	    	

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

}
