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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import entity.search.query.WeightedQuery;
import entity.search.searching.EntityLinker;
import entity.search.searching.EntitySearcher;
import entity.search.utils.Annotation;
import entity.search.utils.Utils;

public class EntityLinkingServlet extends HttpServlet{
	
	EntityLinker searcher = null ;
	  
	  public EntityLinkingServlet(String index){
		  searcher = new EntityLinker(index);
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
			String annotation_result = "";
			String outerComma = "";
		    long start = System.currentTimeMillis();
		    
		    for(int j = 0 ; j < terms.length; j++){	
			    //contexts.remove(terms[j]);
		    	ArrayList<String> currentRelatedContext = new ArrayList<String>();
		    	for(int k = 0 ; k < terms.length; k++){
		    		if(k==j){
		    			currentRelatedContext.add(terms[k]);
		    		}else if(WeightedQuery.isRelated(searcher.getSearcher(), terms[j], terms[k].trim())){
		    			currentRelatedContext.add(terms[k]);
		    		}
		    	
		    	}
		    	//need to be careful on this decision
//		    	if(currentRelatedContext.size() == 0)
//		    		continue;
		    	
		    	String[] contexts = currentRelatedContext.toArray(new String[currentRelatedContext.size()]);
		    	
		    	
				ArrayList<Annotation> annotations = searcher.mysearch(terms[j], contexts, Integer.parseInt(numResult));
				annotation_result += outerComma;
				annotation_result += "{";
				annotation_result += "\"entity_mention\": \""+terms[j] +"\",";
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
			long end = System.currentTimeMillis();
			long time = end - start;
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
}
