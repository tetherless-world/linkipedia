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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;

import edu.rpi.tw.linkipedia.search.utils.Utils;

public class CategoryHierarchy {
	
	public static void main(String [] args){
		CategoryHierarchy catHier = new CategoryHierarchy();
		catHier.initialize("Dataset/skos_categories_broader.nt");
		System.out.println("Finish init");
		String [] cats2 = { 
				"<http://dbpedia.org/resource/Category:JSON>",
				"<http://dbpedia.org/resource/Category:Lightweight_markup_languages>",
				"<http://dbpedia.org/resource/Category:Data_serialization_formats>",
				"<http://dbpedia.org/resource/Category:XML_markup_languages>",
		};
		String [] cats1 = {
				"<http://dbpedia.org/resource/Category:Parallel_computing>",
				"<http://dbpedia.org/resource/Category:Google_software>",
				"<http://dbpedia.org/resource/Category:Distributed_computing_architecture>",				
		};
		//String myCat2 = "<http://dbpedia.org/resource/category:Lightweight_markup_languages>";
		//String myCat = "<http://dbpedia.org/resource/category:Parallel_computing>";
		
		HashSet<String> broaderCat = new HashSet<String> ();
		for(String cat:cats1){
			broaderCat.addAll(catHier.getCategories(cat));
		}
		for(String cat:cats2){
			System.out.println("level 1: "+cat);
			String msca = catHier.mostCommonCategory(cat, broaderCat);
			System.out.println(msca);
		}
		
		broaderCat.clear();
		for(String cat:cats2){
			broaderCat.addAll(catHier.getCategories(cat));
		}
		for(String cat:cats1){
			System.out.println("level 1: "+cat);
			String msca = catHier.mostCommonCategory(cat, broaderCat);
			System.out.println(msca);
		}
		/*
		HashSet<String> broaderCat2 = catHier.getCategories(myCat2);
		System.out.println(myCat);
		Utils.initLog();
		for(String cat:broaderCat){
			if(broaderCat2.contains(cat))
				Utils.log(cat+"\n");
		}
		System.out.println();
		*/
	}
	
	HashMap<String, HashSet<String> > categoryHierarchy;
	public CategoryHierarchy(){
		categoryHierarchy = new HashMap<String, HashSet<String> > ();
	}

	public void initialize(String filename){
		try{
			  FileInputStream fstream = new FileInputStream(filename);
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  
			  while((strLine = br.readLine()) != null){
				  
				  String [] spo = strLine.split(" ", 4);
				 // System.out.println(spo[0]);
				  if(categoryHierarchy.containsKey(spo[0])){
					  HashSet<String> mySet = categoryHierarchy.get(spo[0]);
					  mySet.add(spo[2]);
					  categoryHierarchy.put(spo[0], mySet);
				  }else{
					  HashSet<String> mySet = new HashSet<String>();
					  mySet.add(spo[2]);
					  categoryHierarchy.put(spo[0], mySet);
				  }
			  }
			  
			  
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public HashSet<String> getCategories(String key){
		HashSet<String> categories = new HashSet<String>();
		Queue<String> queue = new PriorityQueue<String>();
		queue.add(key);
		
		while(!queue.isEmpty()){
			String currrent = queue.poll();
			HashSet<String> myCat = categoryHierarchy.get(currrent);
			if(myCat == null)
				continue;
			for(String cat:myCat){
				if(!categories.contains(cat)){
					queue.add(cat);
					categories.add(cat);
				}
				if(cat.equals("<http://dbpedia.org/resource/Category:Web_2.0>")){
					System.out.println(currrent+" has broader "+cat);
				}
			}
		}
		
		
		return categories;
	}
	public String mostCommonCategory(String key, HashSet<String> catList){
		HashSet<String> categories = new HashSet<String>();
		Queue<String> queue = new PriorityQueue<String>();
		queue.add(key);
		
		while(!queue.isEmpty()){
			String currrent = queue.poll();
			HashSet<String> myCat = categoryHierarchy.get(currrent);
			if(myCat == null)
				continue;
			for(String cat:myCat){
				if(catList.contains(cat))
					return cat;
				if(!categories.contains(cat)){
					queue.add(cat);
					categories.add(cat);
				}
			}
		}
		return "";
	}
}
