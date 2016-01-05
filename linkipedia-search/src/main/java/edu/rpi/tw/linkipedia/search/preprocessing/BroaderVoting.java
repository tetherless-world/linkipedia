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

package edu.rpi.tw.linkipedia.search.preprocessing;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Set;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class BroaderVoting {
	public static void main(String [] args){
		
		try{
        	FileInputStream fstream = new FileInputStream("Dataset/skos_categories_en.nt");
    		Model m = ModelFactory.createDefaultModel();
    		m.read(fstream,"","N-TRIPLE");
    		Hashtable<String, ArrayList<String>> output = new Hashtable<String, ArrayList<String>>();   	
    		System.out.println("FINISH LOADING. PROCESSING NOW...");
    		getQueryResult(m,"<http://dbpedia.org/resource/Category:Data_management>");
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	public static void getQueryResult(Model model, String narrower)
	{
//		System.out.println(nlabel);
		String queryString = "SELECT DISTINCT ?broader WHERE {" +
//				"?narrower <http://www.w3.org/2004/02/skos/core#prefLabel> \"" + nlabel + "\". "+
				narrower +" <http://www.w3.org/2004/02/skos/core#broader>+ ?broader. " +
//				"?broader <http://www.w3.org/2004/02/skos/core#prefLabel> ?blabel. " +
				"}";
		QueryExecution qe = QueryExecutionFactory.create(queryString, model);
		Hashtable<String,ArrayList<String>> output = new Hashtable<String,ArrayList<String>>();
//		ArrayList<String> broaders = new ArrayList<String>();
		try {
			ResultSet queryResults = qe.execSelect();
			while(queryResults.hasNext()){
				QuerySolution qs = queryResults.next();
				String broader = qs.get("?broader").toString();//.replaceAll("@en", "");
				System.out.println(broader);
//				String narrower = qs.get("?nlabel").toString().replaceAll("@en", "");
//				broaders.add(broader);
//				if(output.keySet().contains(narrower)){
//					output.get(narrower).add(broader);	
//					System.out.print("n "+narrower+": ");
//					for(String broad:output.get(narrower)){
//						System.out.print(broad+" | ");
//					}
//					System.out.println();
//				}else{
//					ArrayList<String> broaderlist = new ArrayList<String> ();
//					broaderlist.add(broader);
//					output.put(narrower, broaderlist);
//					System.out.println("n: "+narrower+" "+broader);
//				}
			}
			/*
			out.write("{ ");
			for(String narrower:output.keySet()){
				out.write("\""+narrower+"\": [");
				for(String broader:output.get(narrower)){
					out.write("");
				}
				out.write("],");
			}
			out.write("}");
			*/
			qe.close();

		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
