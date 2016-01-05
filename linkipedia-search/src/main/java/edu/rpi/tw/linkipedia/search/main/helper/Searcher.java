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

package edu.rpi.tw.linkipedia.search.main.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.rpi.tw.linkipedia.search.nlp.NaturalLanguageProcessor;
import edu.rpi.tw.linkipedia.search.searching.EntityLinker;
import edu.rpi.tw.linkipedia.search.searching.EntitySearcher;
import edu.rpi.tw.linkipedia.search.searching.SurfaceFormSearcher;
import edu.rpi.tw.linkipedia.search.utils.Annotation;

public class Searcher {

	public static void main(String [] args){
		if(args.length < 2){
			if(args.length == 1){
				if(args[0].equals("help")){
					System.out.println("Action:");
					System.out.println("SurfaceFormSearch");
				}
			}
			System.err.println("usage: ELA.jar Action Parameters");
			return;
		}
		String method = args[0];

		
		if(method.equals("SurfaceFormSearch")){
			String index = args[1];
			SurfaceFormSearch(index);			
		}else if(method.equals("EntitySearch")){		
			String index = args[1];
			EntitySearch(index);
		}else if(method.equals("EntityLinking")){		
			String index = args[1];
			//EntityLinking(index);
		}
	}
	public static void SurfaceFormSearch(String index){
		try {
		SurfaceFormSearcher searcher = new SurfaceFormSearcher(index);
		InputStreamReader inReader = new InputStreamReader(System.in, "UTF-8");
	    BufferedReader in = new BufferedReader(inReader);
		while(true){			
			String query = "";
		    query = in.readLine();
		    if(query.equals("quit"))
		    	break;
			ArrayList<Annotation> annotations = searcher.mysearch(query);
			for(int i = 0; i < annotations.size(); i++){
				System.out.println(annotations.get(i));
			}
		}
		inReader.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public static void EntitySearch(String index){
	    try {
	    	EntitySearcher searcher = new EntitySearcher(index);
			InputStreamReader inReader = new InputStreamReader(System.in, "UTF-8");
		    BufferedReader in = new BufferedReader(inReader);
		while(true){
			//System.out.println("heloo");
			String query = "";
			query = in.readLine();
		    if(query.equals("quit"))
		    	break;
		    query = query.toLowerCase();
		    String [] terms = query.split(",");
//		    ArrayList<String> contexts = new ArrayList<String>();
//		    for(int i = 1; i < terms.length; i++){
//		    	contexts.add(terms[i].trim());
//		    }
			ArrayList<Annotation> annotations = searcher.mysearch(terms[0], terms, 10);
			for(int i = 0; i < annotations.size(); i++){
				System.out.println("url: "+annotations.get(i));
				//System.out.print(annotations.get(i).getContent());
			}
		}
		inReader.close();
		
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public static void EntityLinking(String index){
	    try {
	    	EntityLinker linker = new EntityLinker(index);
			InputStreamReader inReader = new InputStreamReader(System.in, "UTF-8");
		    BufferedReader in = new BufferedReader(inReader);
		    NaturalLanguageProcessor nlp = new NaturalLanguageProcessor();
		while(true){
			
			String query = "";
			query = in.readLine();
		    if(query.equals("quit"))
		    	break;
		    
		    nlp.setText(query);
		    //String [] terms = query.split(",");
		    ArrayList<String> phrases = nlp.getNounPhrases();
		    String [] terms = phrases.toArray(new String[phrases.size()]);
		    for(int i = 1; i < terms.length; i++){
		    	terms[i] = terms[i].trim();
		    }
		    for(int j = 0 ; j < terms.length; j++){
				ArrayList<Annotation> annotations = linker.mysearch(terms[j], terms, 10);
				for(int i = 0; i < annotations.size(); i++){
					System.out.println("url: "+annotations.get(i));
				}
				System.out.println();
		    }
		}
		inReader.close();
		
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
}
