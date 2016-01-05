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

package edu.rpi.tw.linkipedia.search.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.rpi.tw.linkipedia.search.utils.Mention;
import edu.rpi.tw.linkipedia.search.utils.Pair;
import edu.rpi.tw.linkipedia.search.utils.Utils;

public class WikipediaParser {
	
	static Hashtable<String, Hashtable<String,Integer>> frequency_table = new Hashtable<String, Hashtable<String,Integer>>();
	static int count = 0;
	public static void main(String [] args){
		
		if(args.length < 2){
			System.out.println("input file, output file");
			return;
		}
		String input_filename = args[0];
		String output_filename = args[1];
		Utils.initLog(output_filename);
		processWikiDocs(new File(input_filename));

//		for(String surface:frequency_table.keySet()){
//			Hashtable<String,Integer> this_count = frequency_table.get(surface);
//			//System.out.println(surface+":");
//			double sum = 0;
//			for(String link:this_count.keySet()){
//				sum += this_count.get(link);
//			}
//			for(String link:this_count.keySet()){
////				if(((double)this_count.get(link)/sum) < 1){
////					System.out.println(surface+" $$$ "+link+" $$$ "+this_count.get(link)+" $$$ "+((double)this_count.get(link)/sum));
//					Utils.log(surface+" $$$ "+link+" $$$ "+((double)this_count.get(link)/sum)+"\n");
////				}
//			}
//		}
		Utils.closeLog();
	}
	public static void processWikiDocs(File inputDir){
		if (inputDir.isDirectory()) {
	        String[] files = inputDir.list();
	        if (files != null) {
	          for (int i = 0; i < files.length; i++) {
	        	  System.out.println(inputDir+"/"+files[i]);
	        	  processWikiDocs(new File(inputDir+"/"+files[i]));
	          }
	        }
      }else{
    	  System.out.println(count+" "+inputDir);
    	  count++;
    	  extractMention(inputDir.getPath());

    	  //countFrequency(inputDir.getPath());
      }
	}
	public static ArrayList<String> extractMention(String filename){
        try{
	    	File fXmlFile = new File(filename);
	    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    	Document doc = dBuilder.parse(fXmlFile);
	     
	    	//optional, but recommended
	    	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	    	doc.getDocumentElement().normalize();
	     
	    	//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
	     
	    	NodeList nList = doc.getElementsByTagName("page");
	     
	    	//System.out.println("----------------------------\n"+ nList.getLength());
	    	ArrayList<String> mentions = new ArrayList<String>();
	    	for (int temp = 0; temp < nList.getLength(); temp++) {
	     
	    		Node nNode = nList.item(temp);
	     	     
	    		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	     
	    			Element eElement = (Element) nNode;	     
	    			String text = eElement.getElementsByTagName("text").item(0).getTextContent();
	    			mentions = getLinks(text);
	    	    	  for(String mention:mentions){

	    					String [] parts = mention.split("\\|");
	    					String surface = "";
	    					String link = "";
	    					
	    					
	    					if(parts.length < 2){
	    						surface = parts[0].trim();
	    						link = parts[0].trim();
	    						if(surface.equals("")||surface.equals(" "))
	    							continue;
	    					}else{
	    						surface = parts[1].trim();
	    						link = parts[0].trim();
	    						if(surface.equals("")||surface.equals(" "))
	    							continue;
	    					}
	    					System.out.println(surface+"|"+link);
	    	    		  Utils.log(surface+"|"+link+"\n");
	    	    	  }
	    			
	    		}
	    	}
		
	    	return mentions;
		
        }catch(Exception e){
        	e.printStackTrace();
        }		
        return null;
	}
	public static void countFrequency(String filename){
		//Hashtable<String, Hashtable<String,Integer>> frequency_table = new Hashtable<String, Hashtable<String,Integer>>();
        try{
	    	File fXmlFile = new File(filename);
	    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    	Document doc = dBuilder.parse(fXmlFile);
	     
	    	//optional, but recommended
	    	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	    	doc.getDocumentElement().normalize();
	     
	    	//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
	     
	    	NodeList nList = doc.getElementsByTagName("page");
	     
	    	//System.out.println("----------------------------\n"+ nList.getLength());
	    	ArrayList<String> contents = new ArrayList<String>();
	    	for (int temp = 0; temp < nList.getLength(); temp++) {
	     
	    		Node nNode = nList.item(temp);
	     	     
	    		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	     
	    			Element eElement = (Element) nNode;	     
	    			String text = eElement.getElementsByTagName("text").item(0).getTextContent();
	    			contents.add(text);
	    			
	    		}
	    	}
		
		
		for(String content:contents){
		//String content = "[[Irish people|Irish]] [[hurling]] [[Manager (Gaelic games)|manager]][[Glanmire]][[Glanmire]][[Ireland|Irish]]";

		ArrayList<String> mentions = getLinks(content);
		
		for(String mention:mentions){
			String [] parts = mention.split("\\|");
			String surface = "";
			String link = "";
			
			
			if(parts.length < 2){
				surface = parts[0].trim();
				link = parts[0].trim();
				if(surface.equals("")||surface.equals(" "))
					continue;
			}else{
				surface = parts[1].trim();
				link = parts[0].trim();
				if(surface.equals("")||surface.equals(" "))
					continue;
			}
			//System.out.println(surface+" "+link);
			if(frequency_table.containsKey(surface)){
				Hashtable<String,Integer> link_count = frequency_table.get(surface);
				if(link_count.containsKey(link)){
					int my_count = link_count.get(link);
					link_count.put(link, my_count+1);
				}else{
					link_count.put(link, 1);
				}
				frequency_table.put(surface, link_count);
			}else{
				Hashtable<String,Integer> link_count = new Hashtable<String,Integer>();
				link_count.put(link, 1);
				frequency_table.put(surface, link_count);
			}
		}
		}
        }catch(Exception e){
        	e.printStackTrace();
        }
		//return frequency_table;
	}
	public static ArrayList<String> getLinks(String content){
		ArrayList<String> mentions = new ArrayList<String>();
		Matcher m = Pattern.compile("\\[\\[(.*?)\\]\\]")
		       .matcher(content);
		
		while (m.find()) {
			     //allMatches.add(m.group());
			if(m.group(1)!=null){
			   //System.out.println(m.group(1));
				mentions.add(m.group(1));
			}
		
		}
		   
	    return mentions;

	}
	
}
