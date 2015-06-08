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

import java.net.URLDecoder;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

import entity.search.similarity.JaccardSimilarity;
import entity.search.utils.Utils;

public class HelloWorld{
	
	public static void main(String [] args){
		/*
		String s = URLDecoder.decode("http://dbpedia.org/resource/Ludv%C3%ADk_A%C5%A1kenazy");
		
		//<http://dbpedia.org/resource/Trev%C3%A9lez>
		
		  int index = s.lastIndexOf("/");
		  int nindex = s.lastIndexOf("#");
		  
		  if(nindex > index){
			  index = nindex;
		  }
		  
		  String urlName = s;
		  if(index != -1){
			  urlName = s.substring(index+1, s.length()-1);
		  }
		  
		  System.out.println(s+" <http://tool.eal.org/urlName> "+Utils.toPhrase(urlName)+" "+urlName);
		  */
		/*
		String urlName = "a b c e f a g g d|17.2837";
		
		   //String [] defaultLabelTerms = urlName.split(" ");
		   String [] defaultLabelTermWeight = urlName.split("\\|");
		   String thisTerm = "";
		   double defaultLabelweight = 1;
		   if(defaultLabelTermWeight.length > 1){
			   defaultLabelweight = Double.parseDouble(defaultLabelTermWeight[1]);
			   String [] defaultLabelTerms = defaultLabelTermWeight[0].split(" ");						
			   double margin = ((double)(1)/(double)defaultLabelTerms.length)*defaultLabelweight;
			   for(int i = 0 ; i < defaultLabelTerms.length;i++){
				   thisTerm += defaultLabelTerms[i];
				   String thisIndexTerm = thisTerm+"|"+ (margin*(i+1));
				   System.out.println(thisIndexTerm);
				   //System.out.println(thisIndexTerm);
				  // doc.add(new TextField("defaultLabel",thisIndexTerm, Field.Store.YES));
				   thisTerm += " ";
			   }
		   }
		   */
//		String term = "the fate of lehman brothers the beleaguered investment bank hung in the balance on sunday as federal reserve officials and the leaders of major financial institutions continued to gather in emergency meetings trying to complete a plan to rescue the stricken bank";
//		String [] terms = term.split(" ");
//		for(int i = 0 ; i < terms.length; i++)
//		System.out.println(Utils.filterStopWords(terms[i]));
		/*
		int typeStart = urlName.indexOf("(")+1;
		int typeEnd = urlName.indexOf(")");
		String type = urlName.substring(typeStart,typeEnd);
		String [] types = type.split(",");
		
		for(int i = 0 ; i < types.length; i++)
			System.out.println(types[i]);
		System.out.println(urlName.replaceAll("\\(.*\\)", ""));
		
		/*
		String str = "\u30FF";
		System.out.println(str+" "+str.matches("\\\\u.*"));
		*/
//		System.out.println(JaccardSimilarity.computeSimilarity("george", "george h w bush"));
//		
//		float weight = (float) (11.0/(float)Math.pow(2,1.0/10.0));
//		System.out.println(weight);
		
		//System.out.println(Utils.getDBpediaURLName("<http://dbpedia.org/resource/University_of_Amsterdam>"));
		String myurl = "Economy108366753";
		String urlName = URLDecoder.decode(myurl);
		urlName = Utils.toPhrase(urlName).replaceAll("\\d+$", "").toLowerCase().trim();
		  
		System.out.println(urlName);
		
		String spo = "a. b c ..";
		spo = spo.replaceAll(".$", "");
		System.out.println(spo);
		
		String mention = "[[:Cateogry:help]]";
		mention = mention.replaceAll("\\[","").replaceAll("\\]","");
		mention = mention.replaceAll("^:", "");
		System.out.println(mention);
		
		
		System.out.println(Utils.isAllUpper("EU"));
		
		System.out.println("a    b".replaceAll("\\s+"," "));
	}
	
}