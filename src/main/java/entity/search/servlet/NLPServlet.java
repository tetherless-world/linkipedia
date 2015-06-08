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

import edu.stanford.nlp.ling.LabeledWord;
import entity.search.nlp.NaturalLanguageProcessor;
import entity.search.searching.EntityLinker;
import entity.search.searching.EntitySearcher;
import entity.search.utils.Annotation;

public class NLPServlet extends HttpServlet{
	
	NaturalLanguageProcessor processor = null ;
	  
	  public NLPServlet(){
		  processor = new NaturalLanguageProcessor();
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
			if(query == null){
				out.write("Query is empty");
				return;
			}
			
			processor.setText(query);
			ArrayList<String> phrases = processor.getNounPhrases();
			
			String comma = "";
			String json = "{ \"phrases\": [";
			for(String phrase:phrases){
				json += comma;
				String [] parts = phrase.split("\\|");
				if(parts.length < 2)
					continue;
				json += "{ \"word\": \""+parts[0]+"\", \"tag:\""+parts[1]+"\" }";
				comma = ",";
			}
			json += "]}";
			out.println(json);
	  }
	  
	  public void destroy()
	  {
	  }
}
