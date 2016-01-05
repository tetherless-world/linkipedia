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
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.document.Document;

public class VirtualDocument {
	
	public VirtualDocument(){
	}
	
	public static void createVirtualDocument(String file, String outputDir){
        try{
            FileInputStream fstream = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fstream);

			BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
//            Set<String> labels = new HashSet<String>();
            String subject = "";
            HashMap<String, Object> data = new HashMap<String, Object>();
            ArrayList<String> triples = new ArrayList<String> ();
            String contents = "";
            int count = 0;
            while ((line= br.readLine())!= null ){			            	
            	String [] spo = line.split(" ",3);
            	
            	if(spo.length < 3){
            		continue;
            	}
            	
            	if(!(spo[0].startsWith("<http")||spo[0].startsWith("_:")))
            		continue;
            	
            	if(!(spo[1].startsWith("<http")||spo[1].startsWith("_:")))
            		continue;	
            	
            	count ++;
            	if(!subject.equals(spo[0])){
            		if(!subject.equals("")){
            			data.put("url", subject);
            			data.put("triples", triples);
            			data.put("contents", contents);
//            			writeToFolder(file, outputDir);
//            			System.out.println(count+" adding "+subject);
//            			Document doc = getDoc(subject, data);
//            			writer.addDocument(doc);

            		}
            		subject = spo[0];
            		triples = new ArrayList<String>();
            		contents = "";
            	}
            	triples.add(spo[1]+" "+spo[2]);
            	if(!(spo[2].startsWith("<")||spo[2].startsWith("_"))){
            		contents += spo[2];
            	}
            }
        }catch(Exception e){
        	e.printStackTrace();
        }		
		
	}

}
