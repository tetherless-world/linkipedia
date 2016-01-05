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

package edu.rpi.tw.linkipedia.search.searching;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queries.function.valuesource.FloatFieldSource;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.payloads.AveragePayloadFunction;
import org.apache.lucene.search.payloads.MaxPayloadFunction;
import org.apache.lucene.search.payloads.PayloadTermQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.rpi.tw.linkipedia.search.query.WeightedQuery;
import edu.rpi.tw.linkipedia.search.similarity.MySimilarity;
import edu.rpi.tw.linkipedia.search.utils.Annotation;

public class EntitySearcher {
	boolean debug = false;
	IndexReader reader;
	IndexSearcher searcher;  
	MaxPayloadFunction payloadFunctionMax = new MaxPayloadFunction() ;
	WeightedQuery wquery;
	public EntitySearcher(String indexDir){
		 try {
				IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
				searcher = new IndexSearcher(reader);
				searcher.setSimilarity(new MySimilarity());
				wquery = new WeightedQuery();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	public ArrayList<Annotation> mysearch(String label, String [] contexts, int numResult) {
		ArrayList<Annotation> results = new ArrayList<Annotation>();    
		TopScoreDocCollector collector = TopScoreDocCollector.create(numResult, false);
		
		try {

			Query finalQuery = wquery.parse(label, contexts);
			System.out.println("QUERY: "+finalQuery.toString()+"\n");
			searcher.search(finalQuery, collector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		int numTotalHits = collector.getTotalHits();
		
		int start = 0;
		int end = Math.min(numTotalHits, numResult);
		if(end == 0 ){
			return fuzzSearch(label,contexts,numResult);
		}
		for (int i = start; i < end; i++) {	
			//String thisResult = "";
			Document doc = null;
			try {
				doc = searcher.doc(hits[i].doc);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String url = doc.get("url");

			String [] labels = doc.getValues("label");
			String contents = "";
			for(int j = 0 ; j < labels.length; j++){
				contents+="label: "+labels[j]+"\n";
			}
			String boost = doc.get("boost");
			

			Annotation annotation = new Annotation(label, url, hits[i].score);
			annotation.setContent(contents);
			if(debug)
			System.out.println(annotation+" "+boost);
	//		annotation.setTriples(new ArrayList<String>(Arrays.asList(triples)));
			results.add(annotation);		
		}
		return results;
	}
	
	public ArrayList<Annotation> fuzzSearch(String label, String [] contexts, int numResult){
		ArrayList<Annotation> results = new ArrayList<Annotation>(); 
		TopScoreDocCollector collector = TopScoreDocCollector.create(numResult, false);
		 
		try {

			Query finalQuery = wquery.getFuzzyQuery(label, contexts);
			System.out.println("QUERY: "+finalQuery.toString()+"\n");
			searcher.search(finalQuery, collector);
			//for(int i = start; i < )
		} catch (Exception e) {
			e.printStackTrace();
		}
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		int numTotalHits = collector.getTotalHits();
		
		int start = 0;
		int end = Math.min(numTotalHits, numResult);
		if(end == 0 ){
			
			Annotation annotation = new Annotation(label, "", 0);
			results.add(annotation);
			return results;
		}
		for (int i = start; i < end; i++) {	
			//String thisResult = "";
			Document doc = null;
			try {
				doc = searcher.doc(hits[i].doc);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String [] labels = doc.getValues("defaultLabel");
			String url = doc.get("url");
			String boost = doc.get("boost");
			if(debug)
			System.out.println(url+" boost: "+boost+" score: "+hits[i].score);
//			for(int j = 0 ; j < labels.length; j++){
//				System.out.println("default label: "+labels[j]);
//			}
			Annotation annotation = new Annotation(label, url, hits[i].score);
	//		annotation.setTriples(new ArrayList<String>(Arrays.asList(triples)));
			results.add(annotation);		
		}

	return results;
}
}
