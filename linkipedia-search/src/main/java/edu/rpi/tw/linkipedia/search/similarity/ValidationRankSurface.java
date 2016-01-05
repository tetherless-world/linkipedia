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

package edu.rpi.tw.linkipedia.search.similarity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import edu.rpi.tw.linkipedia.search.main.helper.ReadIndex;
import edu.rpi.tw.linkipedia.search.query.DictQueryExpansion;
import edu.rpi.tw.linkipedia.search.query.WeightedQuery;
import edu.rpi.tw.linkipedia.search.utils.*;

public class ValidationRankSurface {
	boolean debug = false;
	IndexSearcher searcher;
	double NIL_threshold = 0;
//	Hashtable<String, Float> co_cache = null;
//	Hashtable<String, Float> dir_cache = null;
	int max_co_occur_threshold = 300;
	IndexSearcher labelSearcher;
	DictQueryExpansion query_expansion = new DictQueryExpansion();
	
	float co_occur_boost = 1;
	float name_sim_boost = 1;
	float dir_relate_boost = 1;
	
	public ValidationRankSurface(IndexSearcher searcher){
		this.searcher = searcher;
//		co_cache = new Hashtable<String, Float> ();
//		dir_cache = new Hashtable<String, Float> ();
	}
	
	public ValidationRankSurface(IndexSearcher searcher, IndexSearcher labelSearcher){
		this.searcher = searcher;
		this.labelSearcher = labelSearcher;
//		co_cache = new Hashtable<String, Float> ();
//		dir_cache = new Hashtable<String, Float> ();
	}
	public void setWeight(float co_occur_weight, float dir_relate_boost, float name_sim_boost){
		co_occur_boost = co_occur_weight;
		name_sim_boost = dir_relate_boost;
		dir_relate_boost = name_sim_boost;
	}
	
	public float computeNameSimilarity(String url, String mention, String field){
		try {
			
			ArrayList<String> defaultLabels = ReadIndex.readIndexByTerm(labelSearcher, "url", url, field);
			float maxScore = 0;
//			System.out.println("new enter "+field+" "+defaultLabels.size());
			
			for(int i = 0 ; i < defaultLabels.size(); i++){
				
				if(defaultLabels.get(i).matches(".*u\\d+.*\\s+.*")){
					continue;
				}
				//System.out.println("..."+defaultLabels.get(i)+" "+mention);
				String defaultLabel = defaultLabels.get(i).split("\\|")[0];
				for(String mymention:mention.split("\\*")){
					float score = (float)JaccardSimilarity.computeSimilarity(defaultLabel,mymention);
	
					if(score > maxScore){
						maxScore = score;
						//System.out.println(url+" default: "+defaultLabel+" "+maxScore);				
					}
				}
			}
			
			return maxScore;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 
	 * @param object: we want to rerank this
	 * @param contexts: all contexts
	 * @param context_table: related contexts to the object
	 * @return
	 */
	public Annotation rank(ArrayList<Annotation> object, ArrayList<ArrayList<Annotation>> contexts, Hashtable<String, ArrayList<String>> context_table){
		Hashtable<String, Float> co_cache = new Hashtable<String, Float>();
		Hashtable<String, Float> dir_cache = new Hashtable<String, Float>();
		
		float max_rank_score = -1;
		float final_co_occur = 0;
		float final_is_related = 0;
		
		Annotation topRank = new Annotation("","",0);
		
		if(object.size() > 0)
			topRank = object.get(0);
		
		for(int i = 0 ; i < object.size(); i++){
			Annotation currentAnnotation = object.get(i);
			
			String url1 = currentAnnotation.getAnnotation();
			String url1_name = Utils.getDBpediaURLName(url1).toLowerCase().replaceAll("_\\(.*?\\)", "");
			if(url1_name.startsWith("."))
				continue;
			//String url_name_for_sim = Utils.getDBpediaURLName(url1).toLowerCase();
			String mention = currentAnnotation.getKeyword().replaceAll("\\(.*\\)", "");
			ArrayList<String> expQueries = new ArrayList<String>();
			
			float labelNameSimilarity = 0;
			
			for(String mymention:mention.split("\\*")){
				expQueries.addAll(query_expansion.getExpandQueryString(mymention));
				float thislabelNameSimilariy = (float)computeNameSimilarity(url1,mymention,"label");
				if(thislabelNameSimilariy > labelNameSimilarity)
					labelNameSimilarity = thislabelNameSimilariy;
			}
			float defaultNameSimilarity = (float)computeNameSimilarity(url1, mention,"defaultLabel");
			
			if(expQueries != null){
			for(String expQuery:expQueries){
			float expdefaultNameSimilarity = (float)computeNameSimilarity(url1, expQuery,"defaultLabel");
			if(expdefaultNameSimilarity > defaultNameSimilarity)
				defaultNameSimilarity = expdefaultNameSimilarity;
			}
			}
			
			//need remove
//			if((!url1_name.equals("England")) && url1_name.contains("England")){
//				url1_name = "";
//			}
			
			float urlNameSimilarity = (float)JaccardSimilarity.computeSimilarity(url1_name, mention);
			
			for(String expQuery:expQueries){
			float expurlNameSimilarity = (float)JaccardSimilarity.computeSimilarity(url1_name, expQuery);
			if(expurlNameSimilarity > urlNameSimilarity)
				urlNameSimilarity = expurlNameSimilarity;
			}
			

			
			for(String expQuery:expQueries){
			float explabelNameSimilarity = (float)computeNameSimilarity(url1,expQuery,"label");
			if(explabelNameSimilarity > labelNameSimilarity)
				labelNameSimilarity = explabelNameSimilarity;
			}
			//float nameStringSimilarity = (float)StringDistance.getSim(url1_name, currentAnnotation.getKeyword());

//			System.out.println("???? "+url1+", "+mention+" vs "+currentAnnotation.getKeyword()+": "+defaultNameSimilarity+" "+urlNameSimilarity+" "+urlNameSimilarity);
			float nameSimilarity = (defaultNameSimilarity+urlNameSimilarity+urlNameSimilarity)/3;
			if(defaultNameSimilarity == 1 || urlNameSimilarity==1 || labelNameSimilarity ==1){
				nameSimilarity=1;
			}
			if(debug)
			System.out.println(url1_name+" "+currentAnnotation.getKeyword()+" "+nameSimilarity);
			if(nameSimilarity==0){
				if(debug)
				System.out.println("entered: "+url1_name+" "+currentAnnotation.getKeyword());
				nameSimilarity = (float)StringDistance.getSim(url1_name, currentAnnotation.getKeyword());
			}
			if(debug)	
			System.out.println("processing "+currentAnnotation+" nameSim: "+nameSimilarity);
			ArrayList<String> related_context = context_table.get(currentAnnotation.getKeyword());
			//System.out.println("related: context:"+related_context);
			
			float co_occur_score = 0;
			float is_relate_score = 0;
			//System.out.println("all context: "+contexts);
			
			float base = contexts.size() - 1;

			
			for(int j = 0 ; j < contexts.size();j++){

				
				ArrayList<Annotation> currentContext = contexts.get(j);
				float max_co_occur = 0;

				boolean is_relate_to_the_context = false;
				for(int k = 0 ; k < currentContext.size(); k++){
					if(currentContext.get(k).getKeyword().equals(currentAnnotation.getKeyword()))
						continue;
					
					if(!related_context.contains(currentContext.get(k).getKeyword())){
						//System.out.println("not relate: "+currentContext.get(k).getKeyword());
						continue;
					}
					String url2 = currentContext.get(k).getAnnotation();
					
					//in case both url1 and url2 are used to annotate some entity mentions: biology, cell (Cell_biology)
//					if(url1.equals(url2)){
//						base--;
//						continue;
//					}
					String url2_name = Utils.getDBpediaURLName(url2).toLowerCase();		
					float co_occur_count = getRelationCount(url1_name, url2_name,co_cache);
					//float co_occur_count = getRelationCount(url1, url2);
					if(!is_relate_to_the_context){						
						//is_relate_to_the_context = isURLDirectRelated(url1, url2);
//						if(!is_relate_to_the_context){
//							is_relate_to_the_context = isURLDirectRelated(url2, url1);
//						}
						is_relate_to_the_context = isDirectRelated(url1_name, url2_name,dir_cache);
					}
					
					//System.out.println("max: "+max_co_occur+" curr: "+co_occur_count);
					if(co_occur_count > max_co_occur){
						max_co_occur = co_occur_count;
						//System.out.println("max: "+max_co_occur+" curr: "+currentContext.get(k).getAnnotation());
					}
					if(max_co_occur == max_co_occur_threshold && is_relate_to_the_context){
						break;
					}
				}
				if(max_co_occur > 0 ){
					
					co_occur_score += (max_co_occur/(double)max_co_occur_threshold);
					//currentScore += 1+ (max_co_occur/1000 * Math.log(1+max_co_occur_annotation.getScore()));
					//System.out.println();
					//System.out.println(currentAnnotation.getAnnotation()+" "+max_co_occur+" new score: "+co_occur_score);
				}
				if(is_relate_to_the_context){
					is_relate_score++;
					if(debug)
					System.out.println("watching is relate score: "+is_relate_score);
				}
				if(debug)
				System.out.println("-------------");
				//System.out.println("max_co_occur: "+currentAnnotation.getAnnotation()+" "+max_co_occur_annotation.getAnnotation()+" "  +currentScore);
			}
			//currentScore = (float) (1+Math.log(currentScore));
			//System.out.println("processing "+currentAnnotation);
			//is_relate_score = is_relate_score / contexts.size();
			
//			System.out.println("base: "+contexts.size()+"-1="+base);
//			System.out.println("co occur score: "+co_occur_score+"^2/"+base+"^2="+((co_occur_score*co_occur_score) / (base * base)));	
//			System.out.println("is relate score: "+is_relate_score+"^2/"+base+"^2="+((is_relate_score*is_relate_score) / (base * base)));
//			System.out.println("name score: "+nameSimilarity);
			

			
//			is_relate_score = (is_relate_score*is_relate_score) / (base * base);
//			co_occur_score = (co_occur_score*co_occur_score) / (base * base);
//			nameSimilarity = nameSimilarity * nameSimilarity;
			float currentScore = 0;
			if(base == 0){
				currentScore = (float) (nameSimilarity*Math.log(1+currentAnnotation.getScore()));
			}else{
				//testing here
				if(is_relate_score > final_is_related){
					final_is_related = is_relate_score;
				}
				
				if(co_occur_score > final_co_occur){
					final_co_occur = co_occur_score;
				}
				
				is_relate_score = (float) Math.log10(1+(is_relate_score/base)*9);//(is_relate_score/ base) * (is_relate_score/ base); 
				co_occur_score = (float) Math.log10(1+(co_occur_score/base)*9);//(co_occur_score/ base) * (co_occur_score/ base); 
				nameSimilarity = (float) Math.log10(1+nameSimilarity*9);//nameSimilarity * nameSimilarity;
				
				//is_relate_score = Math.log(arg0)
				if(debug){
				System.out.println("co occur score: "+co_occur_score+" with boost: "+co_occur_boost);	
				System.out.println("is relate score: "+is_relate_score+" with boost: "+dir_relate_boost);
				System.out.println("name score: "+nameSimilarity+" with boost: "+name_sim_boost);
				System.out.println("Nil score: "+co_occur_score + is_relate_score);
				}
				
				
				if(co_occur_score + is_relate_score < NIL_threshold){
					currentScore = -1;
				}else{
					currentScore = (float) (((co_occur_score*co_occur_boost) + (nameSimilarity*name_sim_boost) + (is_relate_score*dir_relate_boost)/(co_occur_boost+name_sim_boost+dir_relate_boost)) * Math.log(1+currentAnnotation.getScore()));
				}
				
				
				
				if(debug)
				System.out.println("avg: "+((co_occur_score + nameSimilarity + is_relate_score)/3));
				//currentScore += currentAnnotation.getScore();
			}
			if(debug)
			System.out.println("processing "+currentAnnotation+" -> new: "+currentScore);
			if(currentScore > max_rank_score){
				max_rank_score = currentScore;
				topRank = currentAnnotation;
				if(debug)
				System.out.println("top: "+currentAnnotation+" new: "+currentScore);
			}
			
		}
//		double threshold =  Math.log(contexts.size()/5);
//		if(debug)
//		System.out.println("context size: "+contexts.size()+" co_occur: "+final_co_occur+" is related: "+final_is_related);
//		if(final_co_occur < threshold && final_is_related < threshold){
//			return new Annotation("","",0);
//		}
		
		topRank.setScore(max_rank_score);		
		return topRank;
	}
	public ArrayList<Annotation> rankTop(ArrayList<Annotation> object, ArrayList<ArrayList<Annotation>> contexts, Hashtable<String, ArrayList<String>> context_table){
		Hashtable<String, Float> co_cache = new Hashtable<String, Float>();
		Hashtable<String, Float> dir_cache = new Hashtable<String, Float>();
		
		float max_rank_score = -1;
		float final_co_occur = 0;
		float final_is_related = 0;
		
		ArrayList<Annotation> topLists = new ArrayList<Annotation>();
		
		
		for(int i = 0 ; i < object.size(); i++){
			Annotation currentAnnotation = object.get(i);
			
			String url1 = currentAnnotation.getAnnotation();
			String url1_name = Utils.getDBpediaURLName(url1).toLowerCase();
			if(url1_name.startsWith("."))
				continue;
			//String url_name_for_sim = Utils.getDBpediaURLName(url1).toLowerCase();
			String mention = currentAnnotation.getKeyword().replaceAll("\\(.*\\)", "");
			
			ArrayList<String> expQueries = query_expansion.getExpandQueryString(mention);

			float defaultNameSimilarity = (float)computeNameSimilarity(url1, mention,"defaultLabel");
			
			for(String expQuery:expQueries){
			float expdefaultNameSimilarity = (float)computeNameSimilarity(url1, expQuery,"defaultLabel");
			if(expdefaultNameSimilarity > defaultNameSimilarity)
				defaultNameSimilarity = expdefaultNameSimilarity;
			}
			
			float urlNameSimilarity = (float)JaccardSimilarity.computeSimilarity(url1_name, mention);
			
			for(String expQuery:expQueries){
			float expurlNameSimilarity = (float)JaccardSimilarity.computeSimilarity(url1_name, expQuery);
			if(expurlNameSimilarity > urlNameSimilarity)
				urlNameSimilarity = expurlNameSimilarity;
			}
			
			
			float labelNameSimilarity = (float)computeNameSimilarity(url1,mention,"label");
			
			for(String expQuery:expQueries){
			float explabelNameSimilarity = (float)computeNameSimilarity(url1,expQuery,"label");
			if(explabelNameSimilarity > labelNameSimilarity)
				labelNameSimilarity = explabelNameSimilarity;
			}
			
//			float defaultNameSimilarity = (float)computeNameSimilarity(url1, mention,"defaultLabel");
//			float urlNameSimilarity = (float)JaccardSimilarity.computeSimilarity(url1_name, mention);
//			float labelNameSimilarity = (float)computeNameSimilarity(url1,mention,"label");
			//float nameStringSimilarity = (float)StringDistance.getSim(url1_name, currentAnnotation.getKeyword());
			//System.out.println(url1+" vs "+currentAnnotation.getKeyword()+", "+mention+": "+defaultNameSimilarity+" "+urlNameSimilarity+" "+urlNameSimilarity);
			float nameSimilarity = (defaultNameSimilarity+urlNameSimilarity+urlNameSimilarity)/3;
			if(defaultNameSimilarity == 1 || urlNameSimilarity==1 || labelNameSimilarity ==1){
				nameSimilarity=1;
			}
			if(debug)
			System.out.println(url1_name+" "+currentAnnotation.getKeyword()+", "+mention+" "+nameSimilarity);
			if(nameSimilarity==0){
				if(debug)
				System.out.println("entered: "+url1_name+" "+currentAnnotation.getKeyword());
				nameSimilarity = (float)StringDistance.getSim(url1_name, currentAnnotation.getKeyword());
			}
			if(debug)	
			System.out.println("processing "+currentAnnotation+" nameSim: "+nameSimilarity);
			ArrayList<String> related_context = context_table.get(currentAnnotation.getKeyword());
			//System.out.println("related: context:"+related_context);
			
			float co_occur_score = 0;
			float is_relate_score = 0;
			//System.out.println("all context: "+contexts);
			
			float base = contexts.size() - 1;

			
			for(int j = 0 ; j < contexts.size();j++){

				
				ArrayList<Annotation> currentContext = contexts.get(j);
				float max_co_occur = 0;

				boolean is_relate_to_the_context = false;
				for(int k = 0 ; k < currentContext.size(); k++){
					if(currentContext.get(k).getKeyword().equals(currentAnnotation.getKeyword()))
						continue;
					
					if(!related_context.contains(currentContext.get(k).getKeyword())){
						//System.out.println("not relate: "+currentContext.get(k).getKeyword());
						continue;
					}
					String url2 = currentContext.get(k).getAnnotation();
					
					//in case both url1 and url2 are used to annotate some entity mentions: biology, cell (Cell_biology)
//					if(url1.equals(url2)){
//						base--;
//						continue;
//					}
					String url2_name = Utils.getDBpediaURLName(url2).toLowerCase();		
					float co_occur_count = getRelationCount(url1_name, url2_name,co_cache);
					//float co_occur_count = getRelationCount(url1, url2);
					if(!is_relate_to_the_context){						
						//is_relate_to_the_context = isURLDirectRelated(url1, url2);
//						if(!is_relate_to_the_context){
//							is_relate_to_the_context = isURLDirectRelated(url2, url1);
//						}
						is_relate_to_the_context = isDirectRelated(url1_name, url2_name,dir_cache);
					}
					
					//System.out.println("max: "+max_co_occur+" curr: "+co_occur_count);
					if(co_occur_count > max_co_occur){
						max_co_occur = co_occur_count;
						//System.out.println("max: "+max_co_occur+" curr: "+currentContext.get(k).getAnnotation());
					}
					if(max_co_occur == max_co_occur_threshold && is_relate_to_the_context){
						break;
					}
				}
				if(max_co_occur > 0 ){
					
					co_occur_score += (max_co_occur/(double)max_co_occur_threshold);
					//currentScore += 1+ (max_co_occur/1000 * Math.log(1+max_co_occur_annotation.getScore()));
					//System.out.println();
					//System.out.println(currentAnnotation.getAnnotation()+" "+max_co_occur+" new score: "+co_occur_score);
				}
				if(is_relate_to_the_context){
					is_relate_score++;
					if(debug)
					System.out.println("watching is relate score: "+is_relate_score);
				}
				if(debug)
				System.out.println("-------------");
				//System.out.println("max_co_occur: "+currentAnnotation.getAnnotation()+" "+max_co_occur_annotation.getAnnotation()+" "  +currentScore);
			}
			//currentScore = (float) (1+Math.log(currentScore));
			//System.out.println("processing "+currentAnnotation);
			//is_relate_score = is_relate_score / contexts.size();
			
//			System.out.println("base: "+contexts.size()+"-1="+base);
//			System.out.println("co occur score: "+co_occur_score+"^2/"+base+"^2="+((co_occur_score*co_occur_score) / (base * base)));	
//			System.out.println("is relate score: "+is_relate_score+"^2/"+base+"^2="+((is_relate_score*is_relate_score) / (base * base)));
//			System.out.println("name score: "+nameSimilarity);
			

			
//			is_relate_score = (is_relate_score*is_relate_score) / (base * base);
//			co_occur_score = (co_occur_score*co_occur_score) / (base * base);
//			nameSimilarity = nameSimilarity * nameSimilarity;
			float currentScore = 0;
			float context_score = 0;
			if(base == 0){
				currentScore = (float) (nameSimilarity*Math.log(1+currentAnnotation.getScore()));
				context_score = 1;
			}else{
				//testing here
				if(is_relate_score > final_is_related){
					final_is_related = is_relate_score;
				}
				
				if(co_occur_score > final_co_occur){
					final_co_occur = co_occur_score;
				}
				
				is_relate_score = (float) Math.log10(1+(is_relate_score/base)*9);//(is_relate_score/ base) * (is_relate_score/ base); 
				co_occur_score = (float) Math.log10(1+(co_occur_score/base)*9);//(co_occur_score/ base) * (co_occur_score/ base); 
				nameSimilarity = (float) Math.log10(1+nameSimilarity*9);//nameSimilarity * nameSimilarity;
				
				//is_relate_score = Math.log(arg0)
				if(debug){
				System.out.println("co occur score: "+co_occur_score+" with boost: "+co_occur_boost);	
				System.out.println("is relate score: "+is_relate_score+" with boost: "+dir_relate_boost);
				System.out.println("name score: "+nameSimilarity+" with boost: "+name_sim_boost);
//				System.out.println("Nil score: "+(co_occur_score + is_relate_score));
				}
				
				
//				if(co_occur_score + is_relate_score < NIL_threshold){
//					currentScore = -1;
//				}else{
					context_score = (co_occur_score + is_relate_score)/2;
					currentScore = (float) (((co_occur_score*co_occur_boost) + (nameSimilarity*name_sim_boost) + (is_relate_score*dir_relate_boost)/(co_occur_boost+name_sim_boost+dir_relate_boost)) * Math.log(1+currentAnnotation.getScore()));
//				}				
				if(debug)
				System.out.println("avg: "+((co_occur_score + nameSimilarity + is_relate_score)/3));
				//currentScore += currentAnnotation.getScore();
			}
			if(debug)
			System.out.println("processing "+currentAnnotation+" -> new: "+currentScore);
			
			currentAnnotation.setContextScore(context_score);
			
			currentAnnotation.setScore(currentScore);
			topLists.add(currentAnnotation);
			
	
		}
//		double threshold =  Math.log(contexts.size()/5);
//		if(debug)
//		System.out.println("context size: "+contexts.size()+" co_occur: "+final_co_occur+" is related: "+final_is_related);
//		if(final_co_occur < threshold && final_is_related < threshold){
//			return new Annotation("","",0);
//		}
		
//		topRank.setScore(max_rank_score);
		Collections.sort(topLists);
		
		return topLists;
	}
	private boolean isDirectRelated(String url1, String url2,Hashtable<String,Float> dir_cache){
		try{
			if(dir_cache.containsKey(url2+" "+url1)){
				if(debug)
				System.out.println("direct relate cache found: "+url2+" "+url1+" "+dir_cache.get(url2+" "+url1));
				return dir_cache.get(url2+" "+url1)==1;
			}				
//			if(cache.containsKey(url1+" "+url2)){
//				System.out.println("cache found 2: "+url2+" "+url1);
//				return cache.get(url1+" "+url2);
//			}
			
			BooleanQuery luceneQuery = new BooleanQuery();

			TermQuery termQuery = new TermQuery(new Term("defaultLabel",url1));
			//Query labelQuery = wquery.getLabelQuery(url1);
					
			Term contextTerm = new Term("related_object",url2);
			TermQuery relateQuery = new TermQuery(contextTerm);
			
			luceneQuery.add(termQuery, BooleanClause.Occur.MUST);
			luceneQuery.add(relateQuery, BooleanClause.Occur.MUST);
			
			
			TopDocs docs = searcher.search(luceneQuery, 1);	
			
			dir_cache.put(url1+" "+url2, (float)docs.scoreDocs.length);
			dir_cache.put(url2+" "+url1, (float)docs.scoreDocs.length);
			if(debug)
			System.out.println("direct: "+url1+" | "+url2+" : "+docs.scoreDocs.length);
			return docs.scoreDocs.length == 1;
		}catch(Exception e){
			e.printStackTrace();
		}	
		return false;
	}
	public float getRelationCount(String url1, String url2,Hashtable<String, Float> co_cache){
		try{
			if(co_cache.containsKey(url2+" "+url1)){
				if(debug)
				System.out.println("co occur cache found: "+url2+" "+url1+" "+co_cache.get(url2+" "+url1));
				return co_cache.get(url2+" "+url1);
			}				
//			if(cache.containsKey(url1+" "+url2)){
//				System.out.println("cache found 2: "+url2+" "+url1);
//				return cache.get(url1+" "+url2);
//			}
	
			Query query = getCoOccurQuery(url1,url2);
			//Query query = getCoOccurQuery(url1,url2);//WeightedQuery.getCoOccurQuery(url, url2);
			TopDocs docs = searcher.search(query, max_co_occur_threshold);
			co_cache.put(url1+" "+url2, (float)docs.scoreDocs.length);
			co_cache.put(url2+" "+url1, (float)docs.scoreDocs.length);
			if(debug)
			System.out.println("co occur: "+url1+" "+url2+" : "+docs.scoreDocs.length);
			return docs.scoreDocs.length;
		}catch(Exception e){
			e.printStackTrace();
		}
		return 0;
	}
	private Query getCoOccurQuery(String url1, String url2){
		try{
			BooleanQuery luceneQuery = new BooleanQuery();
			
			Term term = new Term("related_object",url1);
			TermQuery labelQuery = new TermQuery(term);
			
			
			Term contextTerm = new Term("related_object",url2);
			TermQuery relateQuery = new TermQuery(contextTerm);
			
			luceneQuery.add(labelQuery, BooleanClause.Occur.MUST);
			luceneQuery.add(relateQuery, BooleanClause.Occur.MUST);
			
			return luceneQuery;
		}catch(Exception e){
		}
		return null;
	}
	/**
	 * should allow lose analysis > strict
	 */
	/*
	private boolean isURLDirectRelated(String url1, String url2){
		try{
			if(dir_cache.containsKey(url1+" "+url2)){
				System.out.println("direct relate cache found: "+url2+" "+url1+" "+(dir_cache.get(url1+" "+url2)));
				return dir_cache.get(url1+" "+url2)==1;
			}				
//			if(cache.containsKey(url1+" "+url2)){
//				System.out.println("cache found 2: "+url2+" "+url1);
//				return cache.get(url1+" "+url2);
//			}
			
			BooleanQuery luceneQuery = new BooleanQuery();

			TermQuery termQuery = new TermQuery(new Term("url",url1));
			//Query labelQuery = wquery.getLabelQuery(url1);
					
			Term contextTerm = new Term("related_object_URL",url2);
			TermQuery relateQuery = new TermQuery(contextTerm);
			
			luceneQuery.add(termQuery, BooleanClause.Occur.MUST);
			luceneQuery.add(relateQuery, BooleanClause.Occur.MUST);
			
			
			TopDocs docs = searcher.search(luceneQuery, 1);	
			
//			dir_cache.put(url1+" "+url2, (float)docs.scoreDocs.length);
//			dir_cache.put(url2+" "+url1, (float)docs.scoreDocs.length);
			if(docs.scoreDocs.length == 1){
				dir_cache.put(url1+" "+url2, (float)docs.scoreDocs.length);
				//dir_cache.put(url2+" "+url1, (float)docs.scoreDocs.length);
				System.out.println("direct: "+url1+" | "+url2+" : "+docs.scoreDocs.length);
			}
			return docs.scoreDocs.length == 1;
		}catch(Exception e){
			e.printStackTrace(); 
		}	
		return false;
	}
	private Query getURLCoOccurQuery(String url1, String url2){
		try{
			BooleanQuery luceneQuery = new BooleanQuery();
			
			Term term = new Term("related_object_URL",url1);
			TermQuery labelQuery = new TermQuery(term);
			
			
			Term contextTerm = new Term("related_object_URL",url2);
			TermQuery relateQuery = new TermQuery(contextTerm);
			
			luceneQuery.add(labelQuery, BooleanClause.Occur.MUST);
			luceneQuery.add(relateQuery, BooleanClause.Occur.MUST);
			
			return luceneQuery;
		}catch(Exception e){
		}
		return null;
	}
	*/
}
