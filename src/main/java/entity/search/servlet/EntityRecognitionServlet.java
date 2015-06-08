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

import test.components.entity.search.ParallelEntityRecognizer;

import entity.search.query.WeightedQuery;
import entity.search.recognizer.EntityRecognizer;
import entity.search.searching.EntityLinker;
import entity.search.utils.Annotation;

public class EntityRecognitionServlet extends HttpServlet{
	
	EntityRecognizer eg = null ;
	  
	  public EntityRecognitionServlet(String index){
		  eg = new EntityRecognizer(index);
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
			//query = query.toLowerCase();
			ParallelEntityRecognizer.setEntityRecognizer(eg);
		//	ParallelEntityRecognizer.reset();
			if(query == null){
				out.write("Query is empty");
				return;
			}

		    long start = System.currentTimeMillis();
		   // ArrayList<String> global_mentions = eg.getEntityMentions(query);
		    
			ArrayList<String> sentences = eg.getSentences(query);
			//ParallelEntityRecognizer.setSentences(sentences);

			int maxThreadNum = Runtime.getRuntime().availableProcessors();
//			if(sentences.size() < maxThreadNum)
//				maxThreadNum = sentences.size();
			
			
			ParallelEntityRecognizer [] peg = new ParallelEntityRecognizer[maxThreadNum];
			System.out.println("Num Threads: "+maxThreadNum);
			for(int i = 0 ; i < maxThreadNum; i++){
				peg[i] = new ParallelEntityRecognizer();//(sentences.get(senIndex),eg);
				peg[i].start();
			}
			for(int i = 0 ; i < maxThreadNum; i++){
				try {
					peg[i].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}


			long end = System.currentTimeMillis();
		//	ArrayList<String> global_mentions = ParallelEntityRecognizer.getAllMentions();
			//ArrayList<String> global_mentions = ParallelEntityRecognizer.getAllMentions();

		    //long end = System.currentTimeMillis();
			long time = end-start;
			
		//	String mention_result = getJsonResult(global_mentions);
			
		    String json_result = "{";
			json_result += "\"query\": \""+query+"\",";
			json_result += "\"time\": \""+time+"\",";
			json_result += "\"results\": [";
		//	json_result += mention_result;
			json_result += "]}";
			out.println(json_result);
			

	  }
	  
	  private String getJsonResult(ArrayList<String> mentions){
		  String json_result = "";
//				  "{";
//		  json_result += "\"query\": \""+query+"\",";
//		  json_result += "\"results\": [";
		  String comma = "";
		  for(int i=0 ; i < mentions.size(); i++){
			  json_result += comma;
			  json_result += "{ \"mention\": \""+mentions.get(i)+"\" }";
			  comma = ",";
		  }	  
		 // json_result +="]}";
		  
		  return json_result;
	  }
	  
	  public void destroy()
	  {
	  }
}

