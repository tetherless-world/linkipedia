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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queries.function.valuesource.FloatFieldSource;
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
import org.apache.lucene.queries.BoostingQuery;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.rpi.tw.linkipedia.search.index.analyzer.EntropyAnalyzer;
import edu.rpi.tw.linkipedia.search.similarity.MySimilarity;
import edu.rpi.tw.linkipedia.search.utils.Annotation;
import edu.rpi.tw.linkipedia.search.indexing.DefaultAnalyzer;


public class SurfaceFormSearcher{
	
	IndexReader reader;
	IndexSearcher searcher;
	ArrayList<Annotation> results = new ArrayList<Annotation>();    
	MaxPayloadFunction payloadFunction = new MaxPayloadFunction() ;
	public SurfaceFormSearcher(String indexDir){
		 try {
				IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
				searcher = new IndexSearcher(reader);
			    searcher.setSimilarity(new MySimilarity());
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	public int lookup(String query, String context){
		ArrayList<String> results = new ArrayList<String>();    
		TopScoreDocCollector collector = TopScoreDocCollector.create(1, false);
		PhraseQuery luceneQuery = null;
		try {
			Term term = new Term("label",query);
			luceneQuery = new PhraseQuery();//parser.parse(query);
			luceneQuery.add(term);
			searcher.search(luceneQuery, collector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		int numTotalHits = collector.getTotalHits();
		return numTotalHits;
	}
	public ArrayList<Annotation> mysearch(String query) {
		TopScoreDocCollector collector = TopScoreDocCollector.create(10, false);
		results = new ArrayList<Annotation>();    
		try {
			BooleanQuery luceneQuery = new BooleanQuery();			
			Term term = new Term("label",query);
			//TermQuery termQuery = new TermQuery(term);
			PayloadTermQuery ptq = new PayloadTermQuery(term, payloadFunction);
			ptq.setBoost(10);
			Analyzer analyzer = DefaultAnalyzer.getAnalyzer();
			QueryParser parser = new QueryParser(Version.LUCENE_47,"analyzedLabel", analyzer);
			Query parsedQuery = parser.parse(query);
			
			//luceneQuery.add(termQuery, BooleanClause.Occur.SHOULD);
			luceneQuery.add(ptq, BooleanClause.Occur.SHOULD);
			luceneQuery.add(parsedQuery, BooleanClause.Occur.SHOULD);
			FunctionQuery boostQuery = new FunctionQuery(new FloatFieldSource("boost"));
			//boostQuery.setBoost(100);
			Query finalQuery = new CustomScoreQuery(luceneQuery, boostQuery);
			System.out.println(finalQuery);		
			searcher.search(finalQuery, collector);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		int numTotalHits = collector.getTotalHits();
		
		int start = 0;
		int end = Math.min(numTotalHits, 10);
		
		for (int i = start; i < end; i++) {	
			String thisResult = "";
			Document doc = null;
			try {
				doc = searcher.doc(hits[i].doc);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String url = doc.get("url");
			thisResult += url;//+" "+hits[i].score;		
			String [] triples = doc.getValues("triple");		
//			for(String label:labels){
//				thisResult += label+"\n";	
//			}
			Annotation annotation = new Annotation(query, url, hits[i].score);
			annotation.setTriples(new ArrayList<String>(Arrays.asList(triples)));
			results.add(annotation);		
		}
		return results;
	}
	/*
	public ArrayList<String> search(String query) {
		ArrayList<String> results = new ArrayList<String>();
		String [] queryParts = query.split(":");
		System.out.println(queryParts[0]+" "+queryParts[1]);
		Term term = new Term(queryParts[0],queryParts[1]);
		try{
			if(reader==null)
				System.out.println("null");
			TermDocs docs = reader.termDocs(term);
			int numDocs = reader.docFreq(term);
			if(numDocs == 0)
				return results;
			System.out.println(numDocs);
			while(docs.next()){
				String thisResult = "";
				int docId = docs.doc();
				Document doc = reader.document(docId);
				String url = doc.get("url");
				String [] labels = doc.getValues("label");
				thisResult += url+"\n";				
				for(String label:labels){
					thisResult += label+"\n";	
				}
				results.add(thisResult);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return results;
	}
	*/

}
