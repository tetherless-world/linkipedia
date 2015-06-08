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

package entity.search.query;

import java.util.Hashtable;

/**
 * no to lower processing
 * @author zhengj3
 *
 */
public class Coreference {
	
	 // key: shorter mention, value: its coreferenced long mention
	
	public Coreference(String mentions){

	}

	public static Hashtable<String, String> coreferencing(String [] mentions){
		Hashtable<String, String> coreference_table = new Hashtable<String, String>();
		//String [] mentions = mentions_str.split(",");
		
		//need a query trim preprocessing
		for(int i = 0 ;i < mentions.length;i++){
			mentions[i]=mentions[i].trim();
		}
		
		for(int i = 0 ; i < mentions.length; i++){
			
			if(coreference_table.containsKey(mentions[i].toLowerCase()))
				continue;
			
			int distance = mentions.length;
			int currentDistance; //= distance;
			for(int j = 0; j < mentions.length; j++){
				if(i==j)
					continue;
				
				currentDistance = Math.abs(j - i);
				
				if(currentDistance > distance)
					break;
				
				if(isCoreferenceFor(mentions[j],mentions[i])){
					distance = currentDistance;
					coreference_table.put(mentions[i].toLowerCase(), mentions[j].toLowerCase());
				}
			}
			
		}	
		return coreference_table;
	}
	/**
	 *  is str1 coreference term for str2,  str1: George Washington, str2:Washington
	 */
	public static boolean isCoreferenceFor(String str1, String str2){
		
		
		
		if(str1.equals(str2))
			return false;
		
		if(str2.contains(" "))
			return false;
		
		String [] str1_parts = str1.split(" ");
		for(int i = 0; i < str1_parts.length; i++){
			if(str1_parts[i].equals(str2)){
				return true;
			}
		}
		

		
		return isCoreferenceForAbbr(str1,str2);
	}
	/**
	 *  is str1 coreference term for str2,  str1: George Washington, str2:GW
	 */
	public static boolean isCoreferenceForAbbr(String str1, String str2){
		str2 = str2.replaceAll("\\.","");
		
		String [] str1_parts = str1.split(" ");
		String str1_abbr = "";
		
		for(int i = 0 ; i < str1_parts.length; i++){
			if(str1_parts[i].length() > 0)
				str1_abbr += str1_parts[i].charAt(0);
		}
		
		return str1_abbr.equals(str2);
	}
	/**
	 * 
	 * @param str1 Noun
	 * @param str2 pronoun
	 * @return
	 */
	private static boolean isCoreferenceForPronoun(String str1, String str2){
		
		
		return false;
	}
	

}
