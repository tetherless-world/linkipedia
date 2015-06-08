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

import entity.search.searching.EntitySearcher;
import entity.search.utils.Annotation;

public class EntitySearchServlet extends HttpServlet {
	
	  EntitySearcher searcher = null ;
	  
	  public EntitySearchServlet(String index){
		  searcher = new EntitySearcher(index);
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
				numResult = "20";
			}
		    String [] terms = query.split(",");
//		    ArrayList<String> contexts = new ArrayList<String>();
//		    for(int i = 1; i < terms.length; i++){
//		    	contexts.add(terms[i].trim());
//		    }
		    
		    float start = System.currentTimeMillis();
			ArrayList<Annotation> annotations = searcher.mysearch(terms[0], terms, Integer.parseInt(numResult));
			float end = System.currentTimeMillis();
			float time = end - start;
			String json_result = "{";
			json_result += "\"query\": \""+query+"\",";
			json_result += "\"time\": \""+time+"\",";
			json_result += "\"num_result\": \""+annotations.size()+"\",";
			json_result += "\"results\": [";
			String comma = "";
			for(int i = 0; i < annotations.size(); i++){
				json_result += comma;
				json_result += "{";
				json_result += "\"url\": \""+annotations.get(i).getAnnotation()+"\",";
				json_result += "\"score\": \""+annotations.get(i).getScore()+"\"";
				json_result += "}";
				comma = ",";
			}
			json_result += "]}";
	      // Set response content type
	      out.println(json_result);
	      //out.close();
	  }
	  
	  public void destroy()
	  {
	  }
}
