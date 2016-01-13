package edu.rpi.tw.linkipedia.web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SentenceTokenizer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.google.gson.Gson;

import edu.rpi.tw.linkipedia.search.cache.LRUCache;
import edu.rpi.tw.linkipedia.search.index.analyzer.AnnotationDecisionFilter;
import edu.rpi.tw.linkipedia.search.query.WeightedQuery;
import edu.rpi.tw.linkipedia.search.searching.EntityLinker;
import edu.rpi.tw.linkipedia.search.searching.EntitySearcher;
import edu.rpi.tw.linkipedia.search.similarity.MySimilarity;
import edu.rpi.tw.linkipedia.search.similarity.ValidationRankSurface;
import edu.rpi.tw.linkipedia.search.utils.Annotation;

/**
 * Root resource
 */
@Path("/")
public class EntitySearchResource {

	public class ResultList {
		public String query;
		public float time;
		public int num_result = 0;
		public List<Object> results = new ArrayList<Object>();
	}

	public class Result {

		public String url;
		public float score;
		public String entity_mention;
	}

	public class Entity {

		public ArrayList<Result> annotations;
		public String entity_mention;

	}

	static private EntitySearcher searcher;
	static private LRUCache cache;
	static private IndexSearcher sfSearcher;
	static private EntityLinker linker;
	
	private int validationSetNum = 5;


	public EntitySearchResource() {
		if (searcher == null) {
			searcher = new EntitySearcher(System.getProperty("linkipedia.index", "linkipedia.index"));
			linker = new EntityLinker(System.getProperty("linkipedia.index", "linkipedia.index"));
			cache = new LRUCache(1000);
			try {
				IndexReader reader = DirectoryReader
				        .open(FSDirectory.open(new File(System.getProperty("linkipedia.index", "linkipedia.index"))));
				sfSearcher = new IndexSearcher(reader);
				sfSearcher.setSimilarity(new MySimilarity());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}

	/**
	 * Method handling HTTP GET requests. The returned object will be sent to
	 * the client as "text/plain" media type.
	 *
	 * @return String that will be returned as a text/plain response.
	 */
	@Path("search")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response search(@QueryParam("query") String query,
	        @DefaultValue("20") @QueryParam("numResult") int numResult) {
		if (query == null) return emptyQuery();
		query = query.replaceAll(",+", ",").replaceAll("\\s+", " ").toLowerCase();
		String[] terms = query.split(",");
		if (terms.length == 0) return emptyQuery();

		ResultList results = new ResultList();
		float start = System.currentTimeMillis();
		ArrayList<Annotation> annotations = searcher.mysearch(terms[0], terms, numResult);
		float end = System.currentTimeMillis();
		results.time = end - start;
		results.query = query;
		results.num_result = annotations.size();

		for (Annotation a : annotations) {
			Result result = new Result();
			result.url = a.getAnnotation().replaceAll("^<","").replaceAll(">$", "");
			result.entity_mention = a.getKeyword();
			result.score = a.getScore();
			results.results.add(result);
		}
		return Response.ok(new Gson().toJson(results), MediaType.APPLICATION_JSON).build();
	}

	protected Response emptyQuery() {
		return Response.status(Response.Status.BAD_REQUEST).entity("Query is empty.").build();
	}
	
	@Path("vlinking3")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response vlinking3(@QueryParam("query") String query,
	        @DefaultValue("10") @QueryParam("labelWeight") float labelWeight,
	        @DefaultValue("3") @QueryParam("relationWeight") float relationWeight,
	        @DefaultValue("6") @QueryParam("defaultWeight") float defaultWeight,
	        @DefaultValue("5") @QueryParam("typeWeight") float typeWeight,
	        @DefaultValue("1") @QueryParam("contentWeight") float contentWeight,
	        @DefaultValue("1") @QueryParam("dirRelateWeight") float dirRelateWeight,
	        @DefaultValue("1") @QueryParam("coOccurWeight") float coOccurWeight,
	        @DefaultValue("1") @QueryParam("nameWeight") float nameWeight,
	        @DefaultValue("1") @QueryParam("numResult") int numResult) {
		ResponseBuilder response = Response.ok();
		response.type(MediaType.APPLICATION_JSON);

		if (query == null) return emptyQuery();

		query = query.replaceAll(",+", ",").replaceAll("\\s+", " ").toLowerCase();

		String[] terms = query.split(",");
		if (terms.length == 0) return emptyQuery();

		long start = System.currentTimeMillis();

		long linking_start = System.currentTimeMillis();
		ArrayList<ArrayList<Annotation>> allAnnotations = new ArrayList<ArrayList<Annotation>>();

		Hashtable<String, ArrayList<String>> context_table = new Hashtable<String, ArrayList<String>>();

		HashSet<String> uniq_terms = new HashSet<String>(Arrays.asList(terms));

		ArrayList<String> context_terms = new ArrayList<String>(uniq_terms);

		HashSet<String> relation_cache = new HashSet<String>();

		for (int j = 0; j < terms.length; j++) {

			HashSet<String> currentRelatedContext = new HashSet<String>();
			for (int k = 0; k < terms.length; k++) {
				if (k == j) {
					currentRelatedContext.add(terms[k]);

				} else {
					String relation_cache_key = terms[j] + terms[k];
					if (relation_cache.contains(relation_cache_key)) {
						// System.out.println("relation cache found:
						// "+relation_cache_key);
						currentRelatedContext.add(terms[k]);
					} else if (WeightedQuery.isRelated(linker.getSearcher(), terms[j], terms[k].trim())) {
						currentRelatedContext.add(terms[k]);
						relation_cache.add(terms[j] + terms[k]);
						relation_cache.add(terms[k] + terms[j]);
					}
				}

			}

			context_table.put(terms[j], context_terms);
			String[] contexts = currentRelatedContext.toArray(new String[currentRelatedContext.size()]);
			// check if terms[j] is cached, if so return, otherwise process
			ArrayList<Annotation> annotations = linker.mysearch(terms[j], contexts, validationSetNum);
			// ArrayList<Annotation> annotations =
			// null;//searcher.mysearch(terms[j], contexts, validationSetNum);
			// if((annotations = cache.get(terms[j])) == null){
			// annotations = searcher.mysearch(terms[j], contexts,
			// validationSetNum);
			// cache.set(terms[j], annotations);
			// }
			allAnnotations.add(annotations);
		}

		long linking_end = System.currentTimeMillis();
		System.out.println("Linking took: " + (linking_end - linking_start));

		long validation_start = System.currentTimeMillis();
		System.out.println("Validation starts...");
		ValidationRankSurface validator = new ValidationRankSurface(linker.getSearcher(), sfSearcher);
		validator.setWeight(coOccurWeight, dirRelateWeight, nameWeight);

	    ArrayList<ArrayList<Annotation>> results = new ArrayList<ArrayList<Annotation>>();
		
		for (ArrayList<Annotation> anno : allAnnotations) {

			if (anno.size() == 0 || !anno.get(0).getKeyword().equals(terms[0])) {
				continue;
			}

			ArrayList<Annotation> annotations = new ArrayList<Annotation>();
			Annotation myAnnotation = validator.rank(anno, allAnnotations, context_table);
			annotations.add(myAnnotation);
			results.add(annotations);
		}
		long validation_end = System.currentTimeMillis();
		System.out.println("Validation took: " + (validation_end - validation_start));

		long end = System.currentTimeMillis();
		long time = end - start;

		AnnotationDecisionFilter filter = new AnnotationDecisionFilter(linker.getSearcher());

		String json_result = getJsonResult(results, query, numResult, time, filter);
		return response.entity(json_result).type(MediaType.APPLICATION_JSON).build();
	}

	public String getJsonResult(ArrayList<ArrayList<Annotation>> results, String query, int numReuslt, long time,
	        AnnotationDecisionFilter filter) {
		String annotation_result = "";
		String outerComma = "";

		for (ArrayList<Annotation> annotations : results) {
			Entity entity = new Entity();
			
			if (annotations.size() == 0)
				continue;

			annotation_result += outerComma;
			annotation_result += "{";
			entity.entity_mention = annotations.get(0).getKeyword().replaceAll("\"", "");
			entity.annotations = new ArrayList<Result>();
			annotation_result += "\"annotations\": [";
			String comma = "";
			for (int k = 0; k < annotations.size() && k < numReuslt; k++) {
				Annotation current = annotations.get(k);
				annotation_result += comma;
				annotation_result += "{";
				annotation_result += "\"url\": \"" + current.getAnnotation() + "\",";
				annotation_result += "\"score\": \"" + current.getScore() + "\",";

				ArrayList<String> types = filter.getGPOType(current.getAnnotation());
				if (types.size() == 0) {
					annotation_result += "\"types\": [{ \"type\": \"NIL\"}]";
				} else {
					annotation_result += "\"types\": [";
					String type_comma = "";
					for (String type : types) {
						annotation_result += type_comma;
						annotation_result += "{ \"type\": \"" + type + "\" }";
						type_comma = ",";
					}
					annotation_result += "]";
				}
				annotation_result += "}";
				comma = ",";
			}
			annotation_result += "]";
			annotation_result += "}";
			outerComma = ",";

		}
		annotation_result += "";
		// long end = System.currentTimeMillis();
		// long time = end - start;
		String json_result = "{";
		json_result += "\"query\": \"" + query.replaceAll("\"", "") + "\",";
		json_result += "\"time\": \"" + time + "\",";
		json_result += "\"results\": [";
		json_result += annotation_result;
		json_result += "]}";
		return json_result;
	}
	
	protected ResultList link(String[] terms, int numResult) {
		ResultList results = new ResultList();
		for (String term : terms){	
			ArrayList<String> currentRelatedContext = new ArrayList<String>();
			for (String context : terms){
				if (term == context) {
					currentRelatedContext.add(context);
				} else if (WeightedQuery.isRelated(linker.getSearcher(), term, context.trim())){
					currentRelatedContext.add(context);
				}
  	
			}
			//need to be careful on this decision
			//  	if(currentRelatedContext.size() == 0)
			//  		continue;
  	
			String[] contexts = currentRelatedContext.toArray(new String[currentRelatedContext.size()]);
  	
			Entity entity = new Entity();
			entity.entity_mention = term;
			entity.annotations = new ArrayList<Result>();
			ArrayList<Annotation> annotations = searcher.mysearch(term, contexts, numResult);
			for (Annotation current : annotations) {
				Result result = new Result();
				result.url = current.getAnnotation().replaceAll("^<","").replaceAll(">$", "");
				result.score = current.getScore();
				entity.annotations.add(result);
			}
			results.results.add(entity);
		}
		return results;
	}
	
	@Path("linking")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response linking(@QueryParam("query") String query,
	        @DefaultValue("1") @QueryParam("numResult") int numResult) {
		if (query == null) return emptyQuery();
		query = query.replaceAll(",+", ",").replaceAll("\\s+", " ").toLowerCase();
		String[] terms = query.split(",");
		if (terms.length == 0) return emptyQuery();

		long start = System.currentTimeMillis();

		ResultList results = this.link(terms, numResult);
		
		long end = System.currentTimeMillis();
		long time = end - start;
		results.query = query;
		results.time = time;
		
		return Response.ok(new Gson().toJson(results), MediaType.APPLICATION_JSON).build();
	}
	
	protected List<String> getSentences(String text) {
		SentenceTokenizer st = new SentenceTokenizer(new StringReader(text));
		List<String> sentences = new ArrayList<String>();
		
		try {
			st.reset();
			while (st.incrementToken()) {
				sentences.add(st.getAttribute(CharTermAttribute.class).toString());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				st.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return sentences;
	}
	
	protected List<String> tokenize(String text, int maxShingleSize) {
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
		List<String> result = new ArrayList<String>();
		try {
			TokenStream stream = analyzer.tokenStream("text", new StringReader(text));
			stream.reset();
			if (maxShingleSize > 1) {
				stream = new ShingleFilter(stream, maxShingleSize);
				((ShingleFilter)stream).setOutputUnigrams(true);
			}
			while (stream.incrementToken()) {
				result.add(stream.getAttribute(CharTermAttribute.class).toString());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			analyzer.close();
		}
		return result;
	}
	
	static String splitCamelCase(String s) {
		String regex = String.format("%s|%s",
				"(?<=[A-Z])(?=[A-Z][a-z])",
		        "(?<=[^A-Z])(?=[A-Z])"
		        //"(?<=[A-Za-z])(?=[^A-Za-z])"
		     );
		return s.replaceAll(regex," ");
	}
	
	@Path("annotate")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response annotate(@QueryParam("query") String query,
	        @DefaultValue("1") @QueryParam("numResult") int numResult,
	        @DefaultValue("6") @QueryParam("minScore") int minScore,
	        @DefaultValue("") @QueryParam("context") String context) {
		if (query == null) return emptyQuery();
		
		query = splitCamelCase(query);
		List<String> sentences = getSentences(query);
		
		long start = System.currentTimeMillis();

		ResultList results = new ResultList();

		for (String sentence : sentences) {
			if (context.length() == 0) {
				context = sentence;
			}
			List<String> contextList = tokenize(context,1);
			List<String> terms = tokenize(sentence, 5);
			
			Set<String> done = new HashSet<String>();
			
			for (String term : terms){
				
				term = term.replaceAll("_", "").replaceAll("\\s+", " ").trim();
				if (term.length() < 2) continue;
				if (done.contains(term)) {
					continue;
				}
				done.add(term);
				ArrayList<String> currentRelatedContext = new ArrayList<String>();
				for (String c : contextList){
					if (term == c) {
						currentRelatedContext.add(c);
					} else if (WeightedQuery.isRelated(linker.getSearcher(), term, c.trim())){
						currentRelatedContext.add(c);
					}
	  	
				}
				//need to be careful on this decision
				//  	if(currentRelatedContext.size() == 0)
				//  		continue;
	  	
				String[] contexts = currentRelatedContext.toArray(new String[currentRelatedContext.size()]);
	  	
				Entity entity = new Entity();
				entity.entity_mention = term;
				entity.annotations = new ArrayList<Result>();
				ArrayList<Annotation> annotations = searcher.mysearch(term, contexts, numResult);

				Map<String, Float> added = new HashMap<String, Float>();
				for (Annotation current : annotations) {
					if (current.getScore() < minScore) continue;
					String url = current.getAnnotation().replaceAll("^<","").replaceAll(">$", "");
					if (!added.containsKey(url) || added.get(url) < current.getScore()) {
						added.put(url, current.getScore());
					}
				}
				
				for (Entry<String, Float> entry : added.entrySet()) {
					Result result = new Result();
					result.score = entry.getValue();
					result.url = entry.getKey();
					entity.annotations.add(result);
				}
				if (entity.annotations.size() > 0)
					results.results.add(entity);
			}
		}
		long end = System.currentTimeMillis();
		long time = end - start;
		results.query = query;
		results.time = time;
		return Response.ok(new Gson().toJson(results), MediaType.APPLICATION_JSON).build();
	}
}
