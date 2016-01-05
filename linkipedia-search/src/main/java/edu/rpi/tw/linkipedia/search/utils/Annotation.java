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

public class Annotation implements Comparable<Annotation>{
	
	String keywords;
	String annotation;
	String contents;
	ArrayList<String> triples;
	float score;
	float context_score;
	
	public Annotation(String keywords, String annotation, float score){
		this.keywords = keywords;
		this.annotation = annotation;
		this.score = score;	
		this.contents = "";
		triples = new ArrayList<String>();
	}
	
	public void setScore(float score){
		this.score = score;
	}
	public void setTriples(ArrayList<String> triples){
		this.triples = triples;
	}
	public void setContent(String contents){
		this.contents = contents;
	}
	public String getContent(){
		return contents;
	}
	public ArrayList<String> getTriples(){
		return triples;
	}
	public String getAnnotation(){
		return annotation;
	}
	public String getKeyword(){
		return keywords;
	}
	public float getScore(){
		return score;
	}
	public void setContextScore(float score){
		context_score = score;
	}
	public float getContextScore(){
		return context_score;
	}
	public String toString(){
		return keywords+" "+annotation+" "+score;
	}

	public int compareTo(Annotation anno) {
		if(score > anno.score) return -1;
		else if(score == anno.score) return 0;
		else return 1;
	}
}
