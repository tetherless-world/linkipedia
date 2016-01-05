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

package edu.rpi.tw.linkipedia.search.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queries.function.valuesource.FloatFieldSource;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.sandbox.queries.regex.RegexQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.payloads.MaxPayloadFunction;
import org.apache.lucene.search.payloads.PayloadTermQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;

import edu.rpi.tw.linkipedia.search.utils.Utils;

/**
 * For type query, pay attention to numbers at the end
 * @author zhengj3
 *
 */
public class WeightedQueryFilterBackup{

	Query finalQuery;
	static MaxPayloadFunction payloadFunctionMax;
	double contextWeightLamda = 1;
	double weightThreshold = -1;
	double labelBoost = 1;
	double contentBoost = 1;
	double relationBoost = 1;
	double typeBoost = 1;
	double analyzedLabelBoost = 1;
	//double defaultLabelBoost = 1;
	
	public WeightedQueryFilterBackup(){
		payloadFunctionMax = new MaxPayloadFunction() ;
	}
	private Query getLabelQuery(String label) throws ParseException{
		float weight = 1;
	
		if(!label.equals("")){
			//DisjunctionMaxQuery labelQuery = new DisjunctionMaxQuery(0);		
			BooleanQuery labelQuery = new BooleanQuery();
			
			if(label.contains("|")){
				String [] termWeight = label.split("\\|");
				label = termWeight[0];
				weight = Float.parseFloat(termWeight[1]);
			}
			
			
			label = Utils.removeChars(label);
			Term term = new Term("label",label);
			//TermQuery termQuery = new TermQuery(term);
			PayloadTermQuery ptq = new PayloadTermQuery(term, payloadFunctionMax);
			//ptq.setBoost(5);
			labelQuery.add(ptq,Occur.SHOULD);
			//labelQuery.add(termQuery);
			
			
//			Term defaultTerm = new Term("defaultLabel",label);
//			//TermQuery defaultTermQuery = new TermQuery(defaultTerm);
//			PayloadTermQuery defaultTermptq = new PayloadTermQuery(defaultTerm, payloadFunctionMax);
//			//defaultTermptq.setBoost(10);
//			labelQuery.add(defaultTermptq,Occur.SHOULD);//, Occur.SHOULD
			//labelQuery.add(defaultTermQuery);
			
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
			QueryParser parser = new QueryParser(Version.LUCENE_47,"analyzedLabel", analyzer);
			parser.setDefaultOperator(Operator.AND);
			Query parsedQuery = parser.parse(label);
			//parsedQuery.setBoost((float) 0.1);
			labelQuery.add(parsedQuery,Occur.SHOULD);
			

			
//			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
//			QueryParser parser = new QueryParser(Version.LUCENE_47,"label", analyzer);
//			String label_re = "*"+label.replaceAll(" ", "*");
//			Query parsedQuery = parser.parse(label_re);
//			labelQuery.add(parsedQuery,Occur.SHOULD);
			
//			ComplexPhraseQueryParser cpqp = new ComplexPhraseQueryParser(Version.LUCENE_47, "label", analyzer);
//			String label_re = label.replaceAll(" ", "*");
//			Query parsedQuery = cpqp.parse(label_re);
//			labelQuery.add(parsedQuery,Occur.SHOULD);
			
	        //too slow
//			String rq_label = label.replaceAll(" ", ".*");
//			rq_label = ".*"+rq_label+".*";
//			RegexQuery rq = new RegexQuery(new Term("label",rq_label));
//			labelQuery.add(rq,Occur.SHOULD);
			
//			String [] label_parts = label.split(" ");
//			Term[] tTerms = new Term[label_parts.length];
//			for(int i = 0; i < label_parts.length; i++){
//				tTerms[i] = new Term("label", label_parts[i]);
//			}
//			MultiPhraseQuery multiPhrasequery = new MultiPhraseQuery();
//			multiPhrasequery.add( tTerms );
//			labelQuery.add(multiPhrasequery,Occur.SHOULD);
			
//			String [] label_parts = label.split(" ");
//			SpanNearQuery [] snq = new SpanNearQuery[label_parts.length];
//			//ArrayList<SpanNearQuery> span_terms = new ArrayList<SpanNearQuery>();
//			for(int i = 0; i < label_parts.length; i++){
//				SpanTermQuery [] stq = {new SpanTermQuery(new Term("label", label_parts[i]))};
//				snq[i] = new SpanNearQuery(stq,10,false);
//			}			
//			SpanNearQuery final_snq = new SpanNearQuery(snq,10,false);
//			labelQuery.add(final_snq,Occur.SHOULD);
//			
			
			
//			QueryParser defaultLabelParser = new QueryParser(Version.LUCENE_47,"defaultLabel", analyzer);
//			Query defaultLabelQuery = defaultLabelParser.parse(label);
//			//defaultLabelQuery.setBoost(10);
//			labelQuery.add(defaultLabelQuery, BooleanClause.Occur.SHOULD);
			
			labelQuery.setBoost(weight);
			return labelQuery;
		}
		return null;
	}
	private Query getContentQuery(String label, ArrayList<String> contexts) throws ParseException{
		if(contexts.size() > 0 && !label.equals("")){
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
			QueryParser contentParser = new QueryParser(Version.LUCENE_47,"contents", analyzer);
			String contents = generateContent(label,contexts);			
			Query contentQuery = contentParser.parse(contents);
			return contentQuery;
		}
		return null;	
	}
	/*
	private ArrayList<String> filterContext(String [] contexts){
		ArrayList<String> relatedContext = new ArrayList<String>();
		
		
		
		return relatedContext;
	}
	*/
	private CachingWrapperFilter getTypeFilter(String label){		
		
		if(label.contains("(") && label.contains(")") ){
			DisjunctionMaxQuery typesQuery = new DisjunctionMaxQuery(0);
			int typeStart = label.indexOf("(")+1;
			int typeEnd = label.indexOf(")");
			
			String type = label.substring(typeStart,typeEnd);
			
			String [] types = type.split(";");
			
			for(int i=0; i < types.length; i++){
				Term typeTerm = new Term("type",types[i].trim());
				Query typeQuery = new TermQuery(typeTerm);	
				//typeQuery.setBoost(15);
				typesQuery.add(typeQuery);					
			}
			QueryWrapperFilter qwf = new QueryWrapperFilter(typesQuery);
			return new CachingWrapperFilter(qwf);
		}else{
			return null;
		}
		
	}
	public ArrayList<String> assignContextWeight(String label, String [] contexts, int context_limit){

		ArrayList<String> weightedContext = new ArrayList<String>();
		for(int i = 0 ; i < contexts.length; i++){	
			String currentContext = contexts[i].replaceAll("\\(.*\\)", "");
			weightedContext.add(currentContext);
		}
		return weightedContext;
		/*
		int labelIndex = -1;
		boolean weightAssigned = false;
		for(int i = 0 ; i < contexts.length; i++){	
			//contexts[i]=contexts[i].toLowerCase();
			System.out.println("context: "+contexts[i]);
			String currentContext = contexts[i].replaceAll("\\(.*\\)", "");
			if(currentContext.equals(label)){
				labelIndex = i;
			}
			if(currentContext.contains("|")){
				weightAssigned = true;
				break;
			}
		}
		System.out.println(labelIndex);
		int startIndex = labelIndex - context_limit;
		if(startIndex < 0)
			startIndex = 0;
		
		int endIndex = labelIndex + context_limit;
		

		if(endIndex > contexts.length || labelIndex == -1)
			endIndex = contexts.length;
		
		
		for(int i = startIndex ; i < endIndex; i++){
			if(i == labelIndex)
				continue;
			if(!weightAssigned){
				int distance = Math.abs(labelIndex - i);
				double weight = contextWeightLamda*(double)contexts.length/((double)distance*distance);
				System.out.println("weight: "+weight);
				if(weight>weightThreshold){
					String currentContext = contexts[i].replaceAll("\\(.*\\)", "");
					weightedContext.add(currentContext);
					//weightedContext.add(contexts[i]+"|"+weight);
				}
			}

		}
		return weightedContext;
		*/
	}
	public Query getRelationQuery(String label, ArrayList<String> weightedContext){
		
		//System.out.println(label+" context size: "+weightedContext.size());
		
		if(weightedContext.size() < 1)
			return null;
		
		BooleanQuery relationQuery = new BooleanQuery();
		for(int i = 0 ; i < weightedContext.size(); i++){
			//System.out.println("value: "+weightedContext.get(i));
			String context = weightedContext.get(i);
			//String currentContext = contexts[i].replaceAll("\\(.*\\)", "");
			if(context.equals(label))
				continue;
			
			float weight = 1;
			if(context.contains("|")){
				String [] termWeight = context.split("\\|");
				context = termWeight[0];
				weight = Float.parseFloat(termWeight[1]);
			}
			
			//if(weight > weightThreshold){
			Term contextTerm = new Term("related_object",context);
			PayloadTermQuery contextTermQuery = new PayloadTermQuery(contextTerm, payloadFunctionMax);
			contextTermQuery.setBoost(weight);
			if(label.equals("")){
				relationQuery.add(contextTermQuery, BooleanClause.Occur.MUST);
			}else{
				relationQuery.add(contextTermQuery, BooleanClause.Occur.SHOULD);
			}
			//}
		}
		if(relationQuery.clauses().size()==0)
			return null;
		
		return relationQuery;
	}

	public Query parse(String label, String [] contexts) throws ParseException {
		
		label = label.toLowerCase().trim();
		for(int i = 0 ; i < contexts.length; i++){
			contexts[i] = contexts[i].toLowerCase().trim();
		}
		
		
		CachingWrapperFilter typeFilter = getTypeFilter(label);
		label = label.replaceAll("\\(.*\\)", "");
		//System.out.println("new label: "+label);
		BooleanQuery luceneQuery = new BooleanQuery();
		ArrayList<String> weightedContext = assignContextWeight(label, contexts, contexts.length);
		
		//System.out.println(label+" "+weightedContext.toString());
		
		Query labelQuery = getLabelQuery(label);	
		Query contentQuery = getContentQuery(label,weightedContext);
		Query relationQuery = getRelationQuery(label, weightedContext);

		
//		if(relationQuery!=null)
//			relationQuery.setBoost(100);
		


		
		if(labelQuery != null){

			luceneQuery.add(labelQuery, BooleanClause.Occur.MUST);
		}
		
		
		FunctionQuery boostQuery = new FunctionQuery(new FloatFieldSource("boost"));
		
		if(relationQuery == null && labelQuery!=null){
			labelQuery.setBoost(10);
			boostQuery.setBoost(5);
		}
		
		if(contentQuery != null && relationQuery != null){
			luceneQuery.add(contentQuery, BooleanClause.Occur.SHOULD);
		}
		
		if(relationQuery != null){
			//relationQuery.setBoost(2);
			luceneQuery.add(relationQuery, BooleanClause.Occur.SHOULD);	
		}
					

		Query query = new CustomScoreQuery(luceneQuery, boostQuery);
		
		if(typeFilter != null){
			finalQuery = new FilteredQuery(query, typeFilter);			
			return finalQuery;
		}
		return query;

	}
	public Query getFuzzyQuery(String label, String [] contexts) throws ParseException{
		label = label.toLowerCase().trim();

		for(int i = 0 ; i < contexts.length; i++){
			contexts[i] = contexts[i].toLowerCase().trim();
		}
		
		
		CachingWrapperFilter typeFilter = getTypeFilter(label);
		label = label.replaceAll("\\(.*\\)", "");

		ArrayList<String> weightedContext = assignContextWeight(label, contexts, contexts.length);
	
		Term labelTerm = new Term("label", label);
		FuzzyQuery fuzzyLabelQuery = new FuzzyQuery(labelTerm);
		BooleanQuery luceneQuery = new BooleanQuery();
		
		
		
		if(fuzzyLabelQuery != null){

			luceneQuery.add(fuzzyLabelQuery, BooleanClause.Occur.MUST);
		}
		Query contentQuery = getContentQuery(label,weightedContext);
		Query relationQuery = getRelationQuery(label, weightedContext);

		FunctionQuery boostQuery = new FunctionQuery(new FloatFieldSource("boost"));
		if(relationQuery == null && fuzzyLabelQuery!=null){
			fuzzyLabelQuery.setBoost(5);
			boostQuery.setBoost(5);
		}
		
		if(contentQuery != null && relationQuery != null){
			luceneQuery.add(contentQuery, BooleanClause.Occur.SHOULD);
		}
		
		if(relationQuery != null){
			//relationQuery.setBoost(2);
			luceneQuery.add(relationQuery, BooleanClause.Occur.SHOULD);	
		}
			
		
		
		Query query = new CustomScoreQuery(luceneQuery, boostQuery);
		
		if(typeFilter != null){
			finalQuery = new FilteredQuery(query, typeFilter);			
			return finalQuery;
		}
		return query;
	}

	public static boolean isRelated(IndexSearcher searcher, String label, String context){
		try{
		BooleanQuery luceneQuery = new BooleanQuery();
		WeightedQueryFilterBackup wquery = new WeightedQueryFilterBackup();
		Query labelQuery = wquery.getLabelQuery(label);
		
		context = context.replaceAll("\\(.*\\)", "").replaceAll("\\|.*", "");
		
		Term contextTerm = new Term("related_object",context);
		TermQuery relateQuery = new TermQuery(contextTerm);
		
		luceneQuery.add(labelQuery, BooleanClause.Occur.MUST);
		luceneQuery.add(relateQuery, BooleanClause.Occur.MUST);
		
		TopDocs topDocs = searcher.search(luceneQuery, 1);
		ScoreDoc[] hits = topDocs.scoreDocs;
		//System.out.println(luceneQuery.toString()+" "+hits.length);
			return hits.length == 1;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	public static boolean strictLabelRelate(IndexSearcher searcher, String label, String context){
		try{
		BooleanQuery luceneQuery = new BooleanQuery();
		//WeightedQuery wquery = new WeightedQuery();
		
		context = context.replaceAll("\\(.*\\)", "").replaceAll("\\|.*", "");
		
		Term term = new Term("label",label);
		PayloadTermQuery labelQuery = new PayloadTermQuery(term, payloadFunctionMax);
		
		//ptq.setBoost(5);
		//labelQuery.add(ptq);
		//Query labelQuery = wquery.getLabelQuery(label);
		
		Term contextTerm = new Term("related_object",context);
		TermQuery relateQuery = new TermQuery(contextTerm);
		
		luceneQuery.add(labelQuery, BooleanClause.Occur.MUST);
		luceneQuery.add(relateQuery, BooleanClause.Occur.MUST);
		
		TopDocs topDocs = searcher.search(luceneQuery, 1);
		ScoreDoc[] hits = topDocs.scoreDocs;
		//System.out.println(luceneQuery.toString()+" "+hits.length);
			return hits.length == 1;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	public static Query getCoOccurQuery(String label, String context){
		try{
			BooleanQuery luceneQuery = new BooleanQuery();
			//WeightedQuery wquery = new WeightedQuery();
			
			context = context.replaceAll("\\(.*\\)", "").replaceAll("\\|.*", "");
			
			Term term = new Term("related_object",label);
			TermQuery labelQuery = new TermQuery(term);
			
			
			Term contextTerm = new Term("related_object",context);
			TermQuery relateQuery = new TermQuery(contextTerm);
			
			luceneQuery.add(labelQuery, BooleanClause.Occur.MUST);
			luceneQuery.add(relateQuery, BooleanClause.Occur.MUST);
			
			return luceneQuery;
		}catch(Exception e){
		}
		return null;
	}
	public String toString(){
		if(finalQuery == null)return "";
		return finalQuery.toString();
	}
	private String generateContent(String label, ArrayList<String>contexts){
		String contents = "";
//		HashSet<String> addedContext = new HashSet<String>();
		for(String str:contexts){
//			if(addedContext.contains(str)){
//				continue;
//			}
			if(str.contains("|")){
				String [] termWeight = str.split("\\|");
				str = termWeight[0];
			}
			/*
			if(str.contains("(")){
				int typeStart = str.indexOf("(");
				int typeEnd = str.indexOf(")");
				
				//contents+=" "+label.substring(0,typeStart);
				
				String type = str.substring(typeStart+1,typeEnd);
				
				String [] types = type.split(";");
				
				str = " "+str.substring(0,typeStart);
				
				for(int i=0; i < types.length; i++){
					str += " "+types[i];
				}
				
				
				GPE
			}
			*/
			contents += " "+str;
//			addedContext.add(str);
			
		}
		return contents;
	}


}
