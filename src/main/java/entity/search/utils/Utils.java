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

package entity.search.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

public class Utils {

	static BufferedWriter out;
	static ArrayList<String> stopWords = new ArrayList<String>(Arrays.asList((
	"able," +
	"about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because," +
	"been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from," +
	"get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just," +
	"least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on," +
	"only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the," +
	"their,them,then,there,these,they,this,tis,to,too,twas,wants,was,we,were,what," +
	"when,where,which,while,who,whom,why,will,with,would,yet,you,your,has,between").split(",")));
	
	public static void log(String line){
		try{
			out.write(line);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void initLog(){
		try{
			FileWriter fstream = new FileWriter("log");
			out = new BufferedWriter(fstream);			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void initLog(String filename){
		try{
			FileWriter fstream = new FileWriter(filename);
			out = new BufferedWriter(fstream);			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void closeLog(){
		try{
			out.flush();
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static String toPhrase(String title) {
		//title = handleUnicode(title);
		String [] splits = title.split("_");
		if(splits.length == 1){
			splits = title.split(" ");
		}
		
		String ret = "";
		
		for(String str:splits){
		
			if(isAllUpper(str)){
				ret+=" "+str;
				continue;
			}
			if(str.startsWith("\\u")){
				ret+=" "+str;
				continue;
			}
			if(str.startsWith("%")){
				ret+=str+" ";
				continue;
			}
						
			String current_word = "";
			boolean preNum = false;
			for (int i = 0; i < str.length(); ++i) {		
			    char c = str.charAt(i);
			    
			    if (Character.isUpperCase(c) ){
			    	
			    	if(current_word.length() > 0){
			    		ret+=" "+current_word;
			    		current_word = "";
			    	}
			    	
			    	current_word += ""+c;

			    }else{
			    	if(Character.isDigit(c)){
				    	if(!preNum && current_word.length() > 0){
				    		ret+=" "+current_word;
				    		current_word = "";
				    	}
			    		preNum = true;
			    	}else{
			    		preNum = false;
			    	}
			    	current_word += ""+c;
			    }   
			}
			ret+=" "+current_word;
		}
		return ret.replaceAll("[^\\p{L}\\p{M}A-Za-z0-9]", " ").replaceAll("\\s+", " ").trim();
	}
	/*
	private static String handleUnicode(String myString){
		String str = myString.split(" ")[0];
		str = str.replace("\\","");
		String[] arr = str.split("u");
		String text = "";
		for(int i = 1; i < arr.length; i++){
		    int hexVal = Integer.parseInt(arr[i], 16);
		    text += (char)hexVal;
		}
		return text;
	}
	*/
	public static boolean isAllUpper(String s) {
	    for(char c : s.toCharArray()) {
	       if(!(Character.isUpperCase(c) || Character.isDigit(c))) {
	           return false;
	        }
	    }
	    return true;
	}
	public static void countFrequency(ArrayList<String> descriptions, HashMap<String, Integer> freqCount){

			  String strLine = "";

			  for (int i = 0 ; i < descriptions.size(); i++)   {
				  strLine = descriptions.get(i);
				  String [] spo = strLine.split(" ", 3);
				  if(spo.length < 3)
					  continue;
				  if(spo[2].startsWith("\"")){
				      spo[2] = spo[2].replaceAll("\\^\\^.*", "");
					  spo[2] = spo[2].replaceAll("[^a-zA-Z0-9\\\\]", " ").replaceAll("\\s+", " ").trim();
					  String [] values = spo[2].split(" ");
					  for(String value:values){
						  if(freqCount.containsKey(value)){
							  freqCount.put(value, freqCount.get(value)+1);
						  }else{
							  freqCount.put(value, 1);
						  }
					  }
				  }
			  }
	}

	public static String getURLName(String url){
		  int index = url.lastIndexOf("/");
		  int nindex = url.lastIndexOf("#");
		  
		  if(nindex > index){
			  index = nindex;
		  }
		  
		  String urlName = url;
		  if(index != -1){
			  urlName = url.substring(index+1, url.length()-1);
			  urlName = URLDecoder.decode(urlName);
		  }

		  return Utils.toPhrase(urlName);
	}
	
	public static String getDBpediaURLName(String url){
		String name = url.replaceAll("<http://dbpedia.org/resource/", "").replaceAll(">", "");
		name = name.replaceAll("<http://en.wikipedia.org/wiki/", "").replaceAll(">", "");
		name = name.replaceAll("<http://dbpedia.org/class/yago/", "").replaceAll(">", "");
		
		if(name.startsWith("<http"))
			name = getURLName(url);
		
		if(name.startsWith("."))
			return name;
		return Utils.toPhrase(name);
	}
	
	public static Document readIndexByTerm(IndexSearcher searcher, String fieldString, String termString) throws CorruptIndexException, IOException{
		TermQuery query = new TermQuery(new Term(fieldString, termString));
		//System.out.println(query.toString());
		TopDocs topDocs = searcher.search(query, 1);
		ScoreDoc[] hits = topDocs.scoreDocs;
		for(int i = 0 ; i < hits.length; i++){
			int docId = hits[i].doc;
			return searcher.doc(docId);
		}
		
		/*
		Term term = new Term(fieldString, termString);
		TermDocs docs = reader.termDocs(term);
		if(docs.next()){	
			int docId = docs.doc();
			Document doc = reader.document(docId);
			return doc;
		}
		*/
		return null;
	}
	/*
	 * need to think about what to remove
	 */
	public static String removeChars(String str){
		return str.replaceAll("[^\\p{L}\\p{M}A-Za-z0-9]", " ").replaceAll("\\s+", " ").trim();
	}
	
	public static String filterStopWords(String str){
		for(int i = 0 ; i < stopWords.size(); i++){
			str = str.replaceAll("^(?i)"+stopWords.get(i)+"\\s|(?i)\\s"+stopWords.get(i)+"$|^(?i)"+stopWords.get(i)+"$", "").trim();
		}
		return str;
	}
	public static String removeSingleLetter(String label){
		label = label.replaceAll("(\\s+[a-z](?=\\s))"," ");
		label = label.replaceAll("\\s+", " ");
		return label;
	}
}
