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
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilityMainTest {

	
	public static void main(String [] args){
		BufferedReader br = null;
		 
		try {
 
			String sCurrentLine;
 
			br = new BufferedReader(new FileReader("Dataset/EL2014TRAINING/tac_2014_kbp_english_EDL_training_queries.xml"));
 
			ArrayList<String> docIds = new ArrayList<String>();
			while ((sCurrentLine = br.readLine()) != null) {
				docIds.addAll(getDocIdList(sCurrentLine));
			}
			
			  FileWriter fstream = new FileWriter("Dataset/EL2014TRAINING/DOCID");
			  BufferedWriter out = new BufferedWriter(fstream);
			  for(String docid:docIds){
				  System.out.println("=="+docid);
				  out.write(docid+"\n");
			  }
			  out.flush();
			  out.close();
			  br.close();
			  
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}
	   public static ArrayList<String> getDocIdList(String content){
		   //Pattern p = Pattern.compile("<PERSON></PERSON>");
		   ArrayList<String> mentions = new ArrayList<String>();
		   Matcher m = Pattern.compile("<docid>(.*?)</docid>")
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
