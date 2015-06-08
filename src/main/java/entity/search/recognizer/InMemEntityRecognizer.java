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

package entity.search.recognizer;


import java.util.ArrayList;
import entity.search.nlp.NaturalLanguageProcessor;
import entity.search.utils.Utils;

public class InMemEntityRecognizer {
	
    NaturalLanguageProcessor nlp;
    SurfaceFormDictionary dict;
    public InMemEntityRecognizer(String filename){
    	dict = new SurfaceFormDictionary(filename);
    	nlp = new NaturalLanguageProcessor();
    }
    
    public ArrayList<String> getEntityMentions(String text){
    	ArrayList<String> mentions = new ArrayList<String> ();  
    	System.out.println(text);
    	nlp.setText(text);

    	ArrayList<String> sentences = nlp.getNounPhrases();
    	
    	for(String sentence:sentences){

    		mentions.addAll(getPhraseMentions(sentence));
    	}
    	
    	return mentions;
    }

    //recursive implementation of getPhraseMentions
    private ArrayList<String> getPhraseMentions(String phrase){
		ArrayList<String> mentions = new ArrayList<String>();
		
		try {
			String [] terms = phrase.split(" ");
			
			int end = 1;
			int start = 0;
			
			while(start < terms.length){
				System.out.println("Starting at: "+start+" ending at: "+end);
				String tryTerm = append(start,end,terms);
				if(dict.isPossibleSurfaceForm(tryTerm)){
					if(end < terms.length){
						end++;
						tryTerm = append(start,end,terms);
						continue;
					}
				}
				
				tryTerm = append(start,end,terms);
				while((!dict.isCompleteSurfaceForm(tryTerm)) && end > start){
					end --;
					tryTerm = append(start,end,terms);
				}
				
				if(start != end){
					String term = append(start,end,terms);
					
					
					term = Utils.filterStopWords(term);
					if(term.length() > 0)
						mentions.add(term);
					
					mentions.add(term);
					start = end;
					end++;
				}else{
					start++;
					end = start + 2;
				}
			}	

			return mentions;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mentions;
		
    }
	public String append(int start, int end, String [] terms){
		StringBuilder sb = new StringBuilder();
		
		if(end - start == 1){
			String [] part = terms[start].split("\\|");
			
			if(part.length != 2)
				return "";
			
			part[0]=Utils.removeChars(part[0]);
			
			if(part[1].startsWith("N") || part[0].contains(" ")){
				sb.append(part[0]);
				sb.append(" ");
			}
			return sb.toString();
		}
		
		for(int i = start; i < end && i < terms.length; i++){
			String [] part = terms[i].split("\\|");
			if(part.length < 2)
				continue;

			sb.append(part[0]);
			sb.append(" ");
		}
		return sb.toString().trim();
	}



    private ArrayList<String> getSentences(String sentences){
    	System.out.println("Processing: ");
    	nlp.setText(sentences);
    	return nlp.getSentences();
    }
}
