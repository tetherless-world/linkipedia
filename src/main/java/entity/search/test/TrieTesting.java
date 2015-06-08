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

package entity.search.test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.ahocorasick.trie.Token;
import org.ahocorasick.trie.Trie;
import org.apache.lucene.document.Document;

public class TrieTesting {
	
	public static void main(String [] args){
		
		String speech = "The cytoskeleton acts to organize and maintain the cell's shape; " +
				"anchors organelles in place; helps during endocytosis, the uptake of external " +
				"materials by a cell, and cytokinesis, the separation of daughter cells after " +
				"cell division; and moves parts of the cell in processes of growth and mobility.";
		
		System.out.println("Start Building Trie...");
		long start = System.currentTimeMillis();
		//Trie trie = buildTrie2("BioFinal/bio_surface_form_final.nt");
		Trie trie = buildTrie2("Dataset/surface_dict_uniq");
	    System.out.println("Finish Building Trie...");
	    long end = System.currentTimeMillis();
	    System.out.println("Finish Building Trie...");
	    System.out.println((end-start));
	    Collection<Token> tokens = trie.tokenize(speech);
	    StringBuffer output = new StringBuffer();
	    
	    for (Token token : tokens) {

	    	output.append(token.getFragment());
	    	output.append("|");

	    }
	    
	    System.out.println(output);
	}
	/*
	public HashSet<String> createMentionList(String filename){
		HashSet<String> dictionary = new HashSet<String>();
        try{
            FileInputStream fstream = new FileInputStream(filename);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            
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


            	if(count%5000 == 0){
            		System.out.println(count+" "+line);
            	}
            	dictionary.add(spo[2]);
            	String removeSingles = removeSingleLetter(spo[2]);
            	if(!removeSingles.equals(spo[0]))
            		dictionary.add(removeSingles);
            }

        }catch(Exception e){
        	e.printStackTrace();
        }		
	}
	*/
	private void addingLabels(Set<String> hashSet, String label){
		StringBuilder labelBuilder = new StringBuilder();
		label = label.toLowerCase();
		for(String label_part:label.split(" ")){
			labelBuilder.append(label_part);
			hashSet.add(labelBuilder.toString());
			labelBuilder.append(" ");
		}
		Set<String> noSingleLetter = new HashSet<String>();
		for(String str:hashSet){
			noSingleLetter.add(removeSingleLetter(str.replaceAll("\\|\\d+.*", "")));
		}
		hashSet.addAll(noSingleLetter);
	}
	public static Trie buildTrie2(String filename){
		 Trie trie = new Trie().removeOverlaps().onlyWholeWords().caseInsensitive();
	        try{
	            FileInputStream fstream = new FileInputStream(filename);
	            DataInputStream in = new DataInputStream(fstream);
	            BufferedReader br = new BufferedReader(new InputStreamReader(in));
	            String line;
	            int count = 0;
	            while ((line= br.readLine())!= null ){
	            				            	
	            	
	            	trie.addKeyword(line);
	            	if(count%1000 == 0)
	            		System.out.println(count+" "+line);
	            	count++;
	            }

	        }catch(Exception e){
	        	e.printStackTrace();
	        }
	        return trie;		
	}
	public static Trie buildTrie(String filename){
		 Trie trie = new Trie().removeOverlaps().onlyWholeWords().caseInsensitive();
        try{
            FileInputStream fstream = new FileInputStream(filename);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            
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


            	if(count%5000 == 0){
            		System.out.println(count+" "+line);
            	}
            	trie.addKeyword(spo[2]);
            	String removeSingles = removeSingleLetter(spo[2]);
            	if(!removeSingles.equals(spo[0]))
            		trie.addKeyword(removeSingles);
            }

        }catch(Exception e){
        	e.printStackTrace();
        }
        return trie;
	}
	public static String removeSingleLetter(String label){
		label = label.replaceAll("(\\s+[a-z](?=\\s))"," ");
		return label;
	}

}
