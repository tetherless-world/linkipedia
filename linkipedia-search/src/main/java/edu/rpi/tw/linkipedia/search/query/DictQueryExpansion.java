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

package edu.rpi.tw.linkipedia.search.query;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

public class DictQueryExpansion {
	
	Hashtable<String, ArrayList<String>> myDict = new Hashtable<String, ArrayList<String>>();
	HashSet<String> dictNames = new HashSet<String> ();
	public DictQueryExpansion(){
		//System.out.println("Dataset/EL2014TRAINING/Cognate_Base.txt");
		//initDict("Dataset/SystemData/Cognate_Base.txt");
		//initDict("Data/SystemData/Dictionary/Cognate_Base.txt");
	}
	public void initDict(String filename){
		try{
			String sCurrentLine;
			 
			BufferedReader br = new BufferedReader(new FileReader(filename));
 
			while ((sCurrentLine = br.readLine()) != null) {
				String [] parts = sCurrentLine.toLowerCase().split("\\|");
				for(int i = 1; i < parts.length; i++){
					String key = parts[i].trim().toLowerCase();
					
					if(parts[0].contains(parts[i]))
						continue;
					
					dictNames.add(parts[0].toLowerCase());
					
					if(myDict.containsKey(key)){
						ArrayList<String> currentMapping = myDict.get(key);
						currentMapping.add(parts[0].trim());
						myDict.put(key,currentMapping);
					}else{
						ArrayList<String> currentMapping = new ArrayList<String>();
						currentMapping.add(parts[0].trim());
						myDict.put(key,currentMapping);					
					}
				}
			}
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public ArrayList<String> getExpandQueryString(String query){
		//System.out.println(query);
		if(myDict.containsKey(query.toLowerCase())){
			return myDict.get(query);
		}
		return new ArrayList<String>();
	}
	public boolean inDict(String query){
		//System.out.println(myDict.keySet().size());
		return dictNames.contains(query.toLowerCase());
	}

}
