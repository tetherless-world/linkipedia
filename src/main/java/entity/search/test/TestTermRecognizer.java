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

import java.util.ArrayList;

public class TestTermRecognizer {

	static int gnext = 0;
	public static void main(String [] args){
		String text = "The fate of Lehman Brothers, the beleaguered investment bank, hung in the balance on Sunday as Federal Reserve officials and the leaders of major financial institutions continued to gather in emergency meetings trying to complete a plan to rescue the stricken bank.";
		ArrayList<String> dict = new ArrayList<String>();
		dict.add("a");
		dict.add("b c");
		dict.add("d");
		dict.add("e f g");
		ArrayList<String> ldict = new ArrayList<String>();
		ldict.add("a");
		ldict.add("a b");
		ldict.add("a b c");
		ldict.add("b");
		ldict.add("b c");
		ldict.add("d");
		ldict.add("e");
		ldict.add("e f");
		ldict.add("e f g");
		//ldict.add("");
		ArrayList<String> result = new ArrayList<String>();
		String [] terms = text.split(" ");
		//getPhrease(dict,ldict,result,"",0,terms);
		int end = 1;
		int start = 0;
		
		while(start < terms.length){
			System.out.println("Starting at: "+start+" ending at: "+end);
			
			if(inDict(ldict,start,end,terms)){
				if(end < terms.length){
					end++;
					continue;
				}
			}
			
			while((!inDict(dict,start,end,terms)) && end > start){
				end --;
			}
			
			if(start != end){
				String term = append(start,end,terms);
				result.add(term);
				start = end;
				end++;
			}else{
				start++;
				end++;
			}
		}
//		if(inDict(dict,start,end,terms)){
//			String term = append(start,end,terms);
//			result.add(term);
//		}
		for(String str:result){
			System.out.println("final: "+str);
		}
		
	}
	public static boolean inDict(ArrayList<String> dict, int start, int end, String [] terms){
		String term = append(start,end,terms);
		return dict.contains(term);
	}
	public static String append(int start, int end, String [] terms){
		StringBuilder sb = new StringBuilder();
		for(int i = start; i < end; i++){
			sb.append(terms[i]);
			sb.append(" ");
		}
		return sb.toString().trim();
	}
	/*
	public static int getPhrease(ArrayList<String> dict,ArrayList<String> ldict, ArrayList<String> result, String currentTerm, int next, String [] terms){
		
		if(next >= terms.length){
			System.out.println("returning: "+next);
			if(dict.contains(currentTerm)){
				result.add(currentTerm);
				System.out.println("adding "+currentTerm);
			}
			return next;
		}
		System.out.println("ct: "+currentTerm+" nt: "+next+" "+terms[next]);
		if(ldict.contains(currentTerm+terms[next])){
			int mynext = next+1;
			getPhrease(dict,ldict,result,currentTerm+terms[next],mynext,terms);
			//next = mynext;
		}

		if(dict.contains(currentTerm)){
			result.add(currentTerm);
			System.out.println("adding "+currentTerm);
			System.out.println("returning: "+next);
			return next;
			//return next;
			//next = getPhrease(dict,ldict,result,"",mynext,terms);
		}

		
		System.out.println("returning: "+next);
		return next;
		
	}
	*/
}
