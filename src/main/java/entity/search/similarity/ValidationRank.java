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

package entity.search.similarity;

import java.io.IOException;
import java.util.ArrayList;
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

import entity.search.main.helper.ReadIndex;
import entity.search.query.WeightedQuery;
import entity.search.utils.*;

public class ValidationRank {
	IndexSearcher searcher;
	Hashtable<String, Float> co_cache = null;
	Hashtable<String, Float> dir_cache = null;
	int max_co_occur_threshold = 500;
	//IndexSearcher labelSearcher;
	public ValidationRank(IndexSearcher searcher){
		this.searcher = searcher;
		co_cache = new Hashtable<String, Float> ();
		dir_cache = new Hashtable<String, Float> ();
	}
	/*
	public ValidationRank(IndexSearcher searcher, IndexSearcher labelSearcher){
		this.searcher = searcher;
		this.labelSearcher = labelSearcher;
	}
	*/
	public float computeNameSimilarity(String url, String mention, String field){
		try {
			ArrayList<String> defaultLabels = ReadIndex.readIndexByTerm(searcher, "url", url, field);
			float maxScore = 0;
			for(int i = 0 ; i < defaultLabels.size(); i++){
				
				if(defaultLabels.get(i).matches(".*u\\d+.*\\s+.*")){
					continue;
				}
				String defaultLabel = defaultLabels.get(i).split("\\|")[0];
				float score = (float)JaccardSimilarity.computeSimilarity(defaultLabel,mention);
				if(score > maxScore){
					maxScore = score;
					//System.out.println(url+" default: "+defaultLabel+" "+maxScore);				
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
		float max_rank_score = -1;
		
		
		Annotation topRank = new Annotation("","",0);
		
		if(object.size() > 0)
			topRank = object.get(0);
		
		for(int i = 0 ; i < object.size(); i++){
			Annotation currentAnnotation = object.get(i);
			
			String url1 = currentAnnotation.getAnnotation();
			String url1_name = Utils.getDBpediaURLName(url1).toLowerCase();
			
			//String url_name_for_sim = Utils.getDBpediaURLName(url1).toLowerCase();
			float defaultNameSimilarity = (float)computeNameSimilarity(url1, currentAnnotation.getKeyword(),"defaultLabel");
			float urlNameSimilarity = (float)JaccardSimilarity.computeSimilarity(url1_name, currentAnnotation.getKeyword());
			//float labelNameSimilarity = (float)computeNameSimilarity(url1, currentAnnotation.getKeyword(),"label");
			//float nameStringSimilarity = (float)StringDistance.getSim(url1_name, currentAnnotation.getKeyword());
			System.out.println();
			float nameSimilarity = (defaultNameSimilarity+urlNameSimilarity)/2;
			if(defaultNameSimilarity == 1 || urlNameSimilarity==1){
				nameSimilarity=1;
			}
					
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
					float co_occur_count = getRelationCount(url1_name, url2_name);
					//float co_occur_count = getRelationCount(url1, url2);
					if(!is_relate_to_the_context){						
						//is_relate_to_the_context = isURLDirectRelated(url1, url2);
//						if(!is_relate_to_the_context){
//							is_relate_to_the_context = isURLDirectRelated(url2, url1);
//						}
						is_relate_to_the_context = isDirectRelated(url1_name, url2_name);
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
					System.out.println("watching is relate score: "+is_relate_score);
				}
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

				is_relate_score = (float) Math.log10(1+is_relate_score*9);//(is_relate_score/ base) * (is_relate_score/ base); 
				co_occur_score = (float) Math.log10(1+co_occur_score*9);//(co_occur_score/ base) * (co_occur_score/ base); 
				nameSimilarity = (float) Math.log10(1+nameSimilarity*9);//nameSimilarity * nameSimilarity;

	
				System.out.println("co occur score: "+co_occur_score);	
				System.out.println("is relate score: "+is_relate_score);
				System.out.println("name score: "+nameSimilarity);
				
				currentScore = (float) (((co_occur_score + nameSimilarity + is_relate_score)/3) * Math.log(1+currentAnnotation.getScore()));
				System.out.println("avg: "+((co_occur_score + nameSimilarity + is_relate_score)/3));
				//currentScore += currentAnnotation.getScore();
			}
			System.out.println("processing "+currentAnnotation+" -> new: "+currentScore);
			if(currentScore > max_rank_score){
				max_rank_score = currentScore;
				topRank = currentAnnotation;
				System.out.println("top: "+currentAnnotation+" new: "+currentScore);
			}
			
		}
		
		topRank.setScore(max_rank_score);		
		return topRank;
	}
	private boolean isDirectRelated(String url1, String url2){
		try{
			if(dir_cache.containsKey(url2+" "+url1)){
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
			System.out.println("direct: "+url1+" | "+url2+" : "+docs.scoreDocs.length);
			return docs.scoreDocs.length == 1;
		}catch(Exception e){
			e.printStackTrace();
		}	
		return false;
	}
	public float getRelationCount(String url1, String url2){
		try{
			if(co_cache.containsKey(url2+" "+url1)){
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
