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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
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
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;

import edu.rpi.tw.linkipedia.search.indexing.DefaultAnalyzer;
import edu.rpi.tw.linkipedia.search.utils.Utils;

/**
 * For type query, pay attention to numbers at the end
 * @author zhengj3
 *
 */
public class WeightedQuery{

	static boolean debug = false;
	CustomScoreQuery finalQuery;
	static MaxPayloadFunction payloadFunctionMax;
	double contextWeightLamda = 1;
	double weightThreshold = -1;
	
	float labelBoost = 1;//10
	float contentBoost = 1;
	float relationBoost = 1;
	float typeBoost = 10;//5
	float defaultBoost = 1;//5
	float analyzedLabelBoost = 1;
	DictQueryExpansion dict_exp ;
	
//	Hashtable<String, String> myDict = new Hashtable<String, String>();
	
	public WeightedQuery(){
		payloadFunctionMax = new MaxPayloadFunction() ;
//		initDict("Cognate.txt");
		dict_exp = new DictQueryExpansion();
		
	}
//	
//	public void initDict(String filename){
//		try{
//			String sCurrentLine;
//			 
//			BufferedReader br = new BufferedReader(new FileReader(filename));
// 
//			while ((sCurrentLine = br.readLine()) != null) {
//				String [] parts = sCurrentLine.toLowerCase().split("\\|");
//				for(int i = 1; i < parts.length; i++){
//					myDict.put(parts[i].trim(), parts[0].trim());
//				}
//			}
//			
//			
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//	}
	private Query getDictExpandQuery(String label, ArrayList<String> weightedContext) throws ParseException{
		BooleanQuery luceneQuery = new BooleanQuery();
		ArrayList<String> dictLabels = dict_exp.getExpandQueryString(label);
		if(dictLabels == null)
			return luceneQuery;
		for(String dictLabel:dictLabels){
		if(dictLabel != null){
			Query dictlabelQuery = getSimpleLabelQuery(dictLabel);
			//Query dictcontentQuery = getContentQuery(dictLabel,weightedContext);
//			Query dictrelationQuery = getRelationQuery(dictLabel, weightedContext);	

			if(dictlabelQuery != null){
//				if(relationQuery == null && labelBoost == 1){
//					labelQuery.setBoost(10);
//				}
				dictlabelQuery.setBoost(labelBoost);
				luceneQuery.add(dictlabelQuery, BooleanClause.Occur.SHOULD);
			}
			
//			if(dictcontentQuery != null){
//				dictcontentQuery.setBoost(contentBoost);
//				luceneQuery.add(dictcontentQuery, BooleanClause.Occur.SHOULD);
//			}
			
//			if(dictrelationQuery != null){
//				//relationQuery.setBoost(2);
//				dictrelationQuery.setBoost(relationBoost);
//				luceneQuery.add(dictrelationQuery, BooleanClause.Occur.SHOULD);	
//			}

		}
		}
		return luceneQuery;

	}	
	public void setWeights(float labelWeight, float contentWeight, float relationWeight, float typeWeight,float defaultWeight){
		labelBoost = labelWeight;
		contentBoost = contentWeight;
		relationBoost = relationWeight;
		typeBoost = typeWeight;
		defaultBoost = defaultWeight;
		
		
//		System.out.println("labelBoost: "+labelBoost);
//		System.out.println("contentBoost: "+contentBoost);
//		System.out.println("relationBoost: "+relationBoost);
//		System.out.println("typeBoost: "+typeBoost);
//		System.out.println("defaultBoost: "+defaultBoost);

	}
	private Query getSimpleLabelQuery(String label)throws ParseException{
		//DisjunctionMaxQuery labelQuery = new DisjunctionMaxQuery(0);
		float weight = 1;
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
		
	
		String [] label_parts = label.split(" ");
		PhraseQuery pq = new PhraseQuery();
		for(int i = 0; i < label_parts.length; i++){
			pq.add(new Term("analyzedLabel", label_parts[i]));
		}
//		int gap = (new StandardAnalyzer(Version.LUCENE_47)).getPositionIncrementGap("analyzedLabel");
		pq.setSlop(10);
		labelQuery.add(pq,Occur.SHOULD);
		
//		QueryParser defaultLabelParser = new QueryParser(Version.LUCENE_47,"defaultLabel", analyzer);
//		Query defaultLabelQuery = defaultLabelParser.parse(label);
//		//defaultLabelQuery.setBoost(10);
//		labelQuery.add(defaultLabelQuery, BooleanClause.Occur.SHOULD);
		
		labelQuery.setBoost(weight);
		return labelQuery;		
	}

	private Query getLabelSpanQuery(String mylabel)throws ParseException{
		float weight = 1;
		
		if(!mylabel.equals("")){
			//DisjunctionMaxQuery labelQuery = new DisjunctionMaxQuery(0);		
			BooleanQuery labelQuery = new BooleanQuery();
			
			if(mylabel.contains("|")){
				String [] termWeight = mylabel.split("\\|");
				mylabel = termWeight[0];
				weight = Float.parseFloat(termWeight[1]);
			}
			
			for(String label:mylabel.split("\\*")){
			
			label = Utils.removeChars(label);
			Term term = new Term("label",label);
			//TermQuery termQuery = new TermQuery(term);
			PayloadTermQuery ptq = new PayloadTermQuery(term, payloadFunctionMax);
			//ptq.setBoost(5);
			labelQuery.add(ptq,Occur.SHOULD);
			
			

			//labelQuery.add(termQuery,Occur.SHOULD);
			
//			String dictLabel = getDictExpandQuery(label);
//			if(dictLabel != null){
//				Term dictTerm = new Term("label",dictLabel);
//				//TermQuery termQuery = new TermQuery(term);
//				PayloadTermQuery dictptq = new PayloadTermQuery(dictTerm, payloadFunctionMax);
//				//ptq.setBoost(5);
//				labelQuery.add(dictptq,Occur.SHOULD);
//			}
//			label = Utils.toPhrase(label);
//			Term pterm = new Term("label",label);
//			//TermQuery termQuery = new TermQuery(term);
//			PayloadTermQuery pptq = new PayloadTermQuery(pterm, payloadFunctionMax);
//			//ptq.setBoost(5);
//			labelQuery.add(pptq,Occur.SHOULD);
			
//			Term defaultTerm = new Term("defaultLabel",label);
//			//TermQuery defaultTermQuery = new TermQuery(defaultTerm);
//			PayloadTermQuery defaultTermptq = new PayloadTermQuery(defaultTerm, payloadFunctionMax);
//			//defaultTermptq.setBoost(10);
//			labelQuery.add(defaultTermptq,Occur.SHOULD);//, Occur.SHOULD
			//labelQuery.add(defaultTermQuery);
			
//			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
//			QueryParser parser = new QueryParser(Version.LUCENE_47,"analyzedLabel", analyzer);
//			parser.setDefaultOperator(Operator.AND);
//			Query parsedQuery = parser.parse(label);
//			//parsedQuery.setBoost((float) 0.1);
//			labelQuery.add(parsedQuery,Occur.SHOULD);
			

			
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
//			//SpanNearQuery [] snq = new SpanNearQuery[label_parts.length];
//			SpanQuery [] sq = new SpanQuery[label_parts.length];
//			//ArrayList<SpanNearQuery> span_terms = new ArrayList<SpanNearQuery>();
//			for(int i = 0; i < label_parts.length; i++){
//				SpanTermQuery stq = new SpanTermQuery(new Term("label", label_parts[i]));
//				sq[i] = stq;
//			}			
//			SpanNearQuery final_snq = new SpanNearQuery(sq,10,false);
//			labelQuery.add(final_snq,Occur.SHOULD);
//			
			String [] label_parts = label.split(" ");
			PhraseQuery pq = new PhraseQuery();
			for(int i = 0; i < label_parts.length; i++){
				pq.add(new Term("analyzedLabel", label_parts[i]));
			}
//			int gap = (new StandardAnalyzer(Version.LUCENE_47)).getPositionIncrementGap("analyzedLabel");
			pq.setSlop(10);
			labelQuery.add(pq,Occur.SHOULD);

			Query exp_label = getDictExpandQuery(label,null);
			labelQuery.add(exp_label,Occur.SHOULD);
			}
//			QueryParser defaultLabelParser = new QueryParser(Version.LUCENE_47,"defaultLabel", analyzer);
//			Query defaultLabelQuery = defaultLabelParser.parse(label);
//			//defaultLabelQuery.setBoost(10);
//			labelQuery.add(defaultLabelQuery, BooleanClause.Occur.SHOULD);
			
			labelQuery.setBoost(weight);
			return labelQuery;
		}
		return null;
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

			///Analyzer labelAnalyzer = new StandardAnalyzer(Version.LUCENE_47);
			///QueryParser labelParser = new QueryParser(Version.LUCENE_47,"label", labelAnalyzer);
			///labelParser.setDefaultOperator(Operator.AND);
			///Query labelParsedQuery = labelParser.parse(label);
			///labelQuery.add(labelParsedQuery,Occur.SHOULD);

			Term term = new Term("label",label);
			TermQuery termQuery = new TermQuery(term);
			//PayloadTermQuery ptq = new PayloadTermQuery(term, payloadFunctionMax);
			//ptq.setBoost(5);
			//labelQuery.add(ptq,Occur.SHOULD);
			labelQuery.add(termQuery,Occur.SHOULD);
			
			
//			Term defaultTerm = new Term("defaultLabel",label);
//			//TermQuery defaultTermQuery = new TermQuery(defaultTerm);
//			PayloadTermQuery defaultTermptq = new PayloadTermQuery(defaultTerm, payloadFunctionMax);
//			//defaultTermptq.setBoost(10);
//			labelQuery.add(defaultTermptq,Occur.SHOULD);//, Occur.SHOULD
			//labelQuery.add(defaultTermQuery);
			
			Analyzer analyzer = DefaultAnalyzer.getAnalyzer();
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
			Analyzer analyzer = DefaultAnalyzer.getAnalyzer();
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
	private Query getTypeQuery(String label){		
		
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
			
			return typesQuery;
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
		
		
		Query typeQuery = getTypeQuery(label);
		label = label.replaceAll("\\(.*\\)", "");
		if(debug)
		System.out.println("new label: "+label);
		BooleanQuery luceneQuery = new BooleanQuery();
		ArrayList<String> dictLabels = dict_exp.getExpandQueryString(label);
//		ArrayList<String> temp = new ArrayList<String>( Arrays.asList(contexts));
//		if(dictLabels != null){
//		for(String dictLabel:dictLabels){
//			temp.add(dictLabel);			
//		}	
//		}
//		contexts = temp.toArray(new String[temp.size()]);
		ArrayList<String> weightedContext = assignContextWeight(label, contexts, contexts.length);
	
		//System.out.println(label+" "+weightedContext.toString());
		
		Query labelQuery = getLabelQuery(label);	
		//Query labelQuery = getLabelSpanQuery(label);
		Query contentQuery = getContentQuery(label,weightedContext);
		Query relationQuery = getRelationQuery(label, weightedContext);


		
//		if(relationQuery!=null)
//			relationQuery.setBoost(100);
		
		if(typeQuery != null){
			typeQuery.setBoost(typeBoost);
			luceneQuery.add(typeQuery, BooleanClause.Occur.SHOULD);
		}
		
		if(labelQuery != null){
//			if(relationQuery == null && labelBoost == 1){
//				labelQuery.setBoost(10);
//			}
			labelQuery.setBoost(labelBoost);
			luceneQuery.add(labelQuery, BooleanClause.Occur.MUST);
		}
		
		if(contentQuery != null){
			contentQuery.setBoost(contentBoost);
			luceneQuery.add(contentQuery, BooleanClause.Occur.SHOULD);
		}
		
		if(relationQuery != null){
			//relationQuery.setBoost(2);
			relationQuery.setBoost(relationBoost);
			luceneQuery.add(relationQuery, BooleanClause.Occur.SHOULD);	
		}
		
//		System.out.println("parse: labelBoost: "+labelBoost);
//		System.out.println("parse: contentBoost: "+contentBoost);
//		System.out.println("parse: relationBoost: "+relationBoost);
//		System.out.println("parse: typeBoost: "+typeBoost);
//		System.out.println("parse: defaultBoost: "+defaultBoost);
		
//		Query dictQuery = getDictExpandQuery(label, weightedContext);
//		if(dictQuery != null){
//			luceneQuery.add(dictQuery, BooleanClause.Occur.SHOULD);	
//		}
		FunctionQuery boostQuery = new FunctionQuery(new FloatFieldSource("boost"));
		boostQuery.setBoost(defaultBoost);
		
		finalQuery = new CustomScoreQuery(luceneQuery, boostQuery);
		
		return finalQuery;
	}
	public Query getFuzzyQuery(String label, String [] contexts) throws ParseException{
		label = label.toLowerCase().trim();
		for(int i = 0 ; i < contexts.length; i++){
			contexts[i] = contexts[i].toLowerCase().trim();
		}
		
		
		Query typeQuery = getTypeQuery(label);
		label = label.replaceAll("\\(.*\\)", "");
		//System.out.println("new label: "+label);
		BooleanQuery luceneQuery = new BooleanQuery();
		ArrayList<String> weightedContext = assignContextWeight(label, contexts, contexts.length);
		
		//System.out.println(label+" "+weightedContext.toString());
		Term labelTerm = new Term("label", label);
		FuzzyQuery fuzzyLabelQuery = new FuzzyQuery(labelTerm);
		//Query labelQuery = getLabelQuery(label);	
		Query contentQuery = getContentQuery(label,weightedContext);
		Query relationQuery = getRelationQuery(label, weightedContext);

		
//		if(relationQuery!=null)
//			relationQuery.setBoost(100);
		
		if(typeQuery != null){
			//typeQuery.setBoost(15);
			luceneQuery.add(typeQuery, BooleanClause.Occur.MUST);
		}
		
		if(fuzzyLabelQuery != null){
//			if(relationQuery == null)
//				labelQuery.setBoost(10);
			luceneQuery.add(fuzzyLabelQuery, BooleanClause.Occur.MUST);
		}
		
		if(contentQuery != null){
			luceneQuery.add(contentQuery, BooleanClause.Occur.SHOULD);
		}
		
		if(relationQuery != null){
			//relationQuery.setBoost(2);
			luceneQuery.add(relationQuery, BooleanClause.Occur.SHOULD);	
		}
			
		
		FunctionQuery boostQuery = new FunctionQuery(new FloatFieldSource("boost"));
		
		finalQuery = new CustomScoreQuery(luceneQuery, boostQuery);
		
		return finalQuery;
	}

	public static boolean isRelated(IndexSearcher searcher, String label, String context){
		try{
			label = label.replaceAll("\\(.*\\)", "").replaceAll("\\|.*", "");
			context = context.replaceAll("\\(.*\\)", "").replaceAll("\\|.*", "");
			
		BooleanQuery luceneQuery = new BooleanQuery();
		WeightedQuery wquery = new WeightedQuery();
		Query labelQuery = wquery.getSimpleLabelQuery(label);
		

		
		Term contextTerm = new Term("related_object",context);
		TermQuery relateQuery = new TermQuery(contextTerm);
		
		BooleanQuery directRelate = new BooleanQuery();
		
		
		directRelate.add(labelQuery, BooleanClause.Occur.MUST);
		directRelate.add(relateQuery, BooleanClause.Occur.MUST);
		
		luceneQuery.add(directRelate,BooleanClause.Occur.SHOULD);
		
		
		BooleanQuery reverseRelate = new BooleanQuery();
		Query rLabelQuery = wquery.getSimpleLabelQuery(context);
		Term rContextTerm = new Term("related_object",label);
		TermQuery rRelateQuery = new TermQuery(rContextTerm);
		
		reverseRelate.add(rLabelQuery, BooleanClause.Occur.MUST);
		reverseRelate.add(rRelateQuery, BooleanClause.Occur.MUST);
		
		luceneQuery.add(reverseRelate,BooleanClause.Occur.SHOULD);
		
		TopDocs topDocs = searcher.search(luceneQuery, 1);
		ScoreDoc[] hits = topDocs.scoreDocs;
		if(debug)
		System.out.println(luceneQuery.toString()+" "+hits.length);
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
			str = str.replaceAll("\\W", " ");
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
				
				
				
			}
			*/
			contents += " "+str;
//			addedContext.add(str);
			
		}
		return contents;
	}


}
