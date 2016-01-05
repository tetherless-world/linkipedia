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

package edu.rpi.tw.linkipedia.search.utils;

import java.util.ArrayList;

public class Mention {
	
	String mention;
	int start;
	int end;
	String coreference;
	ArrayList<Pair> context;
	//temp
	String docId;
	String sentence;
	String queryId;
	String type;

	public Mention(){context = new ArrayList<Pair>();sentence="";}
	
	public Mention(String sf){
		context = new ArrayList<Pair>();
		mention = sf;
		sentence = "";
		//Pair mentionContext = new Pair(sf,1);
		//context.add(mentionContext);
	}
	public void setType(String myType){
		type = myType;
	}
	public String getType(){
		return type;
	}
	//temp
	public void setDocId(String doc){
		docId = doc;
	}
	public String getDocId(){
		return docId;
	}
	public void setSentence(String sent){
		sentence = sent;
	}
	public String getSentence(){
		return sentence;
	}
	public String getQueryId(){
		return queryId;
	}
	public void setQueryId(String id){
		queryId = id;
	}
	
	
	public int getStart(){
		return start;
	}
	public int getEnd(){
		return end;
	}
	public void setMention(String m, int start, int end){
		mention = m;
		this.start = start;
		this.end = end;
	}
	
	public void setCoref(String coref){
		this.coreference = coref;
	}
	public String getCoref(){
		return coreference;
	}
	public String getMention(){
		return mention;
	}
	public void setContext(String context_mentions){
		clearContext();
		String contexts[]=context_mentions.split(";");
		if(contexts.length > 0)
			coreference = contexts[0];
		for(int i = 1 ; i < contexts.length; i++){
			addContext(contexts[i]);
		}
	}
	public void clearContext(){
		context.clear();
	}
	public void addContext(String context_mention, int weight){
		Pair mentionContext = new Pair(context_mention, weight);
		context.add(mentionContext);
	}
	
	public void addContext(String context_mention){
		Pair mentionContext = new Pair(context_mention, 1);
		context.add(mentionContext);
	}
	
	public ArrayList<Pair> getContext(){
		return context;
	}
	public String getContextString(){
		StringBuilder sb = new StringBuilder();
		String semi_colon = "";
		for(int i = 0 ; i < context.size(); i++){
			sb.append(semi_colon);
			Pair thisContext = context.get(i);
			sb.append(thisContext.getKey());//+"|"+thisContext.getValue());
			semi_colon=";";
		}
		return sb.toString().trim();		
	}
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(mention.replaceAll(",", " "));
		sb.append(",");
		sb.append(start);
		sb.append(",");
		sb.append(end);
		sb.append(",");
		if(coreference!=null && (!coreference.equals(""))){
			//System.err.println(mention+" -> "+coreference);
			sb.append(coreference);
		}else
			sb.append(mention);
		String semi_colon = ";";
		for(int i = 0 ; i < context.size(); i++){
			sb.append(semi_colon);
			Pair thisContext = context.get(i);
			sb.append(((String)thisContext.getKey()).replaceAll(","," ").replaceAll("\\s+", " "));//+"|"+thisContext.getValue());
			//semi_colon=";";
		}
		//sb.append(",||");
		//String temp = sentence.replaceAll(",",";").replaceAll("\n"," ");
//		sb.append(",<div>");
//		String mtemp = mention.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
//		sb.append(sentence.replaceAll(mtemp, "<b>"+mention+"<\\/b>").replaceAll("<b><b>", "<b>").replaceAll("</b></b>", "</b>"));
//		sb.append(" "+docId);
//		sb.append("</div>");
		return sb.toString().trim();
	}
}
