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

package edu.rpi.tw.linkipedia.search.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import edu.rpi.tw.linkipedia.search.main.helper.ReadIndex;
import edu.rpi.tw.linkipedia.search.nlp.NaturalLanguageProcessor;
import edu.rpi.tw.linkipedia.search.query.WeightedQuery;
import edu.rpi.tw.linkipedia.search.searching.EntitySearcher;
import edu.rpi.tw.linkipedia.search.similarity.MySimilarity;
import edu.rpi.tw.linkipedia.search.utils.Annotation;

public class ReadIndexServlet extends HttpServlet{
	
	  IndexSearcher searcher = null ;
	  
	  public ReadIndexServlet(String index){
			 try {
					IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
					searcher = new IndexSearcher(reader);
					searcher.setSimilarity(new MySimilarity());
			} catch (Exception e) {
				e.printStackTrace();
			} 
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
			String query = request.getParameter("url");
			query = "<"+query+">";
			ArrayList<String> contents = ReadIndex.readIndexByTerm(searcher, "url",query,"triple");

			String json_result = "{";
			json_result += "\"query\": \""+query+"\",";
			json_result += "\"triples\": [";
			String comma = "";
			for(int i = 0; i < contents.size(); i++){
				json_result += comma;
				String thisContent = contents.get(i).replaceAll("\\^\\^.*", "").replaceAll("\"","").replaceAll("\\\\", "");
				json_result += "{";
				json_result += "\"triple\": \""+thisContent+"\"";
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
