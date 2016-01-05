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

package edu.rpi.tw.linkipedia.search.test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.rpi.tw.linkipedia.search.utils.Mention;
import edu.rpi.tw.linkipedia.search.utils.Utils;

public class KBPNameExtractor {
	public static void main(String [] args){
		Hashtable<String, Query> queries = new Hashtable<String, Query> ();
		
		for(int i = 2009; i < 2014; i++){
		getKBPNames("../Data/All/query_"+i+".xml",
				"../Data/All/result_"+i+".tab",
				queries);
		}
		/**
		 * 1. rename files -> query_file_year, result_file_year
		 * for year = 2009 -> 2013
		 * call getKBPNames, return Hashtable<name, query>
		 * print GPE, ORG, PER
		 */
		
		String [] types = {"GPE","PER","ORG"};
		
		for(int i = 0 ; i < types.length; i++){
	    	Utils.initLog("KBP_"+types[i]);
	    	for(String key:queries.keySet()){
	    		Query q = queries.get(key);
	    		String type_str = q.getTypes();
	    		if(type_str.contains(types[i])){
	    			Utils.log(q+"\n");
	    		}
	    	}
	    	Utils.closeLog();
		}
    		
	}
	public static void getKBPNames(String query_file, String query_type_file,Hashtable<String, Query> queries){
		//ArrayList<Mention> reference = new ArrayList<Mention>();
		//Hashtable<String, Query> queries = new Hashtable<String, Query> ();
		Hashtable<String, String> query_type = readQueryType(query_type_file);
	    try {
	    	 
	    	File fXmlFile = new File(query_file);
	    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    	Document doc = dBuilder.parse(fXmlFile);
	    	doc.getDocumentElement().normalize();
	     
	    	NodeList nList = doc.getElementsByTagName("query");
	     
	    	for (int temp = 0; temp < nList.getLength(); temp++) {
	     
	    		Node nNode = nList.item(temp);
	     	     
	    		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	     
	    			Element eElement = (Element) nNode;
	    			String query_id = eElement.getAttribute("id");
	    			String name = ((Element) eElement.getElementsByTagName("name").item(0)).getTextContent();
	    			//System.out.println(name);
	    			if(queries.containsKey(name)){
	    				Query q = queries.get(name);
	    				String type = query_type.get(query_id);
	    				q.addType(type);
	    				int freq = q.getFreq() + 1;
	    				q.setFreq(freq);
	    				queries.put(name, q);
	    			}else{
	    				Query q = new Query();
	    				q.setFreq(1);
	    				q.setName(name);
	    				String type = query_type.get(query_id);
	    				q.addType(type);
	    				queries.put(name, q);
	    			}
	    		}
	    	}

	        } catch (Exception e) {
	    	e.printStackTrace();
	        }
	    
		
		
		//return queries;
	}
	public static Hashtable<String,String> readQueryType(String filename){
		Hashtable<String, String> query_type = new Hashtable<String, String>();
		try{
	        FileInputStream fstream = new FileInputStream(filename);
	        DataInputStream in = new DataInputStream(fstream);
	
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			String content = "";
			while((line = br.readLine())!=null){
				String [] parts = line.split("\t");
				query_type.put(parts[0],parts[2]);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return query_type;
	}
	static class Query{
		//ArrayList<String> query_ids;
		HashSet<String> types;
		String name;
		int freq;
		
		public Query(){
			types = new HashSet<String>();
			name = "";
			freq = 0;
		}
		public int getFreq(){
			return freq;
		}
		public void setName(String name){
			this.name = name;
		}
		public void setFreq(int freq){
			this.freq = freq;
		}
		public void addType(String type){
			this.types.add(type);
		}
		public String getTypes(){
			String typeString ="";
			for(String type:types){
				typeString+=type+",";
			}		
			return typeString;
		}
		public String toString(){
			String typeString ="";
			for(String type:types){
				typeString+=","+type;
			}
			return name+","+freq;//+typeString;
		}
		
	}
}
