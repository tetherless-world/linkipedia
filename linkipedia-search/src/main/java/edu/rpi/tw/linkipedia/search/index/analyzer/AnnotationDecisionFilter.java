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

package edu.rpi.tw.linkipedia.search.index.analyzer;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.IndexSearcher;

import edu.rpi.tw.linkipedia.search.main.helper.ReadIndex;
import edu.rpi.tw.linkipedia.search.utils.Annotation;

public class AnnotationDecisionFilter {

	IndexSearcher searcher;
	static String PERType [] = {"person","ethnic group"};
	static String GPEType [] = {"place","yago geo entity","location","populated place"};
	static String ORGType [] = {"organization","organisation"};
	static String EVEType [] = {"event"};
	static String TIMType [] = {"time period"};
	public static void main(String [] args){
		
	}
	
	public AnnotationDecisionFilter(IndexSearcher searcher){
		this.searcher = searcher;
	}

	
	public ArrayList<Annotation> filterByType(ArrayList<Annotation> result,String type){
		ArrayList<Annotation> finalResult = new ArrayList<Annotation>();
		
		if(type.equals("PER")){		
			for(int i = 0 ;i < result.size();i++){
				ArrayList<String> mytypes = getTypes(result.get(i).getAnnotation());
				if(isPER(mytypes)){
					finalResult.add(result.get(i));
				}
			}
		}else if(type.equals("GPE")){
			for(int i = 0 ;i < result.size();i++){
				ArrayList<String> mytypes = getTypes(result.get(i).getAnnotation());
				if(isGPE(mytypes)){
					finalResult.add(result.get(i));
				}
			}
		}else if(type.equals("ORG")){
			for(int i = 0 ;i < result.size();i++){
				ArrayList<String> mytypes = getTypes(result.get(i).getAnnotation());
				if(isORG(mytypes)){
					finalResult.add(result.get(i));
				}
			}
		}
		return null;
	}
	public boolean isOneOfType(ArrayList<String> types){
		
		return (isPER(types) || isGPE(types) || isEVE(types) || isORG(types) || isTIM(types));
	}
    public boolean isPER(ArrayList<String> types){
		for(int i = 0 ; i < PERType.length ; i++){
			for(int j = 0 ; j < types.size(); j++){
				if(PERType[i].equals(types.get(j).trim())){
					return true;
				}
			}
		}
		return false;
	}
    public boolean isTIM(ArrayList<String> types){
		for(int i = 0 ; i < TIMType.length ; i++){
			for(int j = 0 ; j < types.size(); j++){
				
				if(TIMType[i].equals(types.get(j).trim())){
					System.out.println("match 1: "+TIMType[i]+" 2: ("+types.get(j)+")");
					return true;
				}else{
					System.out.println("not match 1: "+TIMType[i]+" 2: ("+types.get(j)+")");
				}
			}
		}
		return false;
	}
    public boolean isGPE(ArrayList<String> types){
		for(int i = 0 ; i < GPEType.length ; i++){
			for(int j = 0 ; j < types.size(); j++){
				if(GPEType[i].equals(types.get(j).trim())){
					return true;
				}
			}
		}
		return false;
	}
    public boolean isEVE(ArrayList<String> types){
		for(int i = 0 ; i < EVEType.length ; i++){
			for(int j = 0 ; j < types.size(); j++){
				if(EVEType[i].equals(types.get(j).trim())){
					return true;
				}
			}
		}
		return false;
	}
    public boolean isORG(ArrayList<String> types){
		for(int i = 0 ; i < ORGType.length ; i++){
			for(int j = 0 ; j < types.size(); j++){
				if(ORGType[i].equals(types.get(j).trim())){
					return true;
				}
			}
		}
		return false;
	}
    public ArrayList<String> getGPOType(String url){
    	ArrayList<String> types = getTypes(url);
    	ArrayList<String> GPO = new ArrayList<String>();
    	if(isPER(types))
    		GPO.add("PER");
    	if(isORG(types))
    		GPO.add("ORG");
    	if(isGPE(types))
    		GPO.add("GPE");
    	return GPO;
    	
    }
    public ArrayList<String> getTypes(String url){
		try {
			ArrayList<String> types = ReadIndex.readIndexByTerm(searcher, "url", url, "type");
//			for(int i = 0 ; i < types.size(); i++){
//				System.out.println("type: "+url+" "+types.get(i));
//			}
			return types;
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
