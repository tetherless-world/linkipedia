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

package linkipedia;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import utils.ConceptItem;
import utils.ConceptMatcher;
import utils.QueryItem;
import org.json.JSONArray;
import org.json.JSONObject;

public class EsorService implements ConceptMatcher {

	private static Log log = LogFactory.getLog(EsorService.class);

	private static final String REST_URL = "http://esor.tw.rpi.edu/annotate/search";

	//temporary server for testing
	//private static final String REST_URL = "http://dataonetwc.tw.rpi.edu/linkipedia/search";


	public List<ConceptItem> getConcepts(String fullText) throws Exception {
		String query = parseTerm(fullText);
		String escapedSpaceQuery = escapeToSpace(query);
		String escapedCommaQuery = escapeToComma(query);

		if(escapedSpaceQuery == escapedCommaQuery){
			return getConcepts(new QueryItem(escapedSpaceQuery));
		}else{
			List<ConceptItem> res_escapedSpace = getConcepts(new QueryItem(escapedSpaceQuery));
			List<ConceptItem> res_escapedComma = getConcepts(new QueryItem(escapedCommaQuery));
			return mergeRes(res_escapedSpace, res_escapedComma);
		}
		//return getConcepts(new QueryItem(fullText));
	}

	public List<ConceptItem> getConcepts(QueryItem queryItem) throws Exception {
		return lookupEsor(queryItem);
	}


	private static List<ConceptItem> lookupEsor(QueryItem queryItem) throws Exception  {

		HttpClient client = HttpClients.createDefault();
		String uriStr = REST_URL + "?query=" + queryItem.toString();
		//System.out.println(uriStr);

		HttpGet method = new HttpGet(uriStr);
		method.setHeader("Accept", "application/json");
		HttpResponse response = client.execute(method);
		int code = response.getStatusLine().getStatusCode();
		if (2 != code / 100) {
				throw new Exception("response code " + code + " for resource at " + uriStr);
			}
		InputStream body = response.getEntity().getContent();
		String jsonStr = convertStreamToString(body);
		//System.out.println(jsonStr);

		JSONObject json = new JSONObject(jsonStr);
		String query = json.getString("query");
		JSONArray results = json.getJSONArray("results");


		//analysis the result and return
		ArrayList<ConceptItem> concepts = new ArrayList<ConceptItem>();
		for(int i = 0; i < results.length(); i++){
			JSONObject r = results.getJSONObject(i);
			String url = r.getString("url");
			String score = r.getString("score");


			if(url.length()>0) {
				if(url.contains("http")){
					ConceptItem c = new ConceptItem(new URI(url.substring(1, url.length() - 1)), Double.parseDouble(score));
					concepts.add(c);
				}
			}else{
				//System.out.println(queryItem+":NA");
			}
		}

		return concepts;
	}

	static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	private String parseTerm(String str) throws Exception{

		if(str.contains("(")){
			str =  str.substring(0, str.indexOf("("));
		}

		str = str.replaceAll("\\s+$", "");

		str = replaceFromSlashToSpace(replaceFromDotToSpace(replaceFromDashToSpace(str)));
		str = str.replace("%", " percent");
		str = insertSpaceBeforeCapital(str);
		str = URLEncoder.encode(str, "UTF-8").replaceAll("\\+", "%20");
		return str;
	}

	private String replaceFromDotToSpace(String str) {
		return str.replace(".", " ");
	}

	private String replaceFromSlashToSpace(String str){
		return str.replace("/", " ");
	}

	private String insertSpaceBeforeCapital(String str){
		char[] charArr = str.toCharArray();
		StringBuilder sb = new StringBuilder();
		for(int i = 0 ; i < charArr.length; i++){
			if(i>0 && charArr[i] >= 'A' && charArr[i] <= 'Z' && charArr[i-1] >= 'a'&& charArr[i-1] <= 'z')
				sb.append(" ");
			sb.append(charArr[i]);
		}
		return sb.toString();
	}

	private String replaceFromDashToSpace(String original){
		return original.replace("_", " ");
	}

	private String escapeToSpace(String original){
		return original.replace(" ", "%20");
	}

	private String escapeToComma(String original){
		return original.replace("%20", ",");
	}

	private List<ConceptItem> mergeRes(List<ConceptItem> res_escapedSpace, List<ConceptItem> res_escapedComma) {
		if(res_escapedSpace.size()==0) return res_escapedComma;
		if(res_escapedComma.size()==0) return res_escapedSpace;

		int indexS = 0;
		int indexC = 0;
		while(indexS < res_escapedSpace.size()){
			if(indexC < res_escapedComma.size() && res_escapedComma.get(indexC).getWeight() >= res_escapedSpace.get(indexS).getWeight()){
				res_escapedSpace.add(indexS, res_escapedComma.get(indexC));
				indexS++;
				indexC++;
			}else{
				indexS++;
			}
		}

		for(int i = indexC; i < res_escapedComma.size(); i++ ){
			res_escapedSpace.add(res_escapedComma.get(i));
		}

		return res_escapedSpace;
	}
}
