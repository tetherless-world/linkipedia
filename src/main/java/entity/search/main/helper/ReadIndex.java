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

package entity.search.main.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;



import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;



public class ReadIndex {
	static boolean debug = false;
	static String  INDEX_DIR = "Dataset/entity_base_noPOstring";
	public static void main(String [] args){
		try{
			if(args.length < 1){
				System.out.println("index directory");
				return;
			}
				
			INDEX_DIR = args[0];
			
			
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(INDEX_DIR)));
			IndexSearcher searcher = new IndexSearcher(reader);
			System.out.println(reader.numDocs());
			while(true){
			    BufferedReader in = null;
			    String text = "";
			    try {
			    	in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
					text = in.readLine();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			    String [] mytext = text.split("\\|");
			    if(mytext.length > 1){
			    	text = mytext[0];
			    }
			    if(text.contains(":")){
			    	String [] fiedValue = text.split(":",2);
			    	readIndexByTerm(searcher, fiedValue[0], fiedValue[1], mytext[1]);//readIndexByTerm(reader,text);
			    }
			}

		}catch(Exception e){
			
			e.printStackTrace();
		}
	}
	public static void readIndex(String index){
		try{
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
			IndexSearcher searcher = new IndexSearcher(reader);
			while(true){
			    BufferedReader in = null;
			    String text = "";
			    try {
			    	in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
					text = in.readLine();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			    System.out.println("You entered: "+text);
			    String [] mytext = text.split("\\|");
			    if(mytext.length > 1){
			    	text = mytext[0];
			    }

			    if(text.contains(":")){
			    	String [] fieldValue = text.split(":",2);
			    	String filter = mytext[1];
			    	String value = fieldValue[1];

			    	ArrayList<String> contents = readIndexByTerm(searcher, fieldValue[0], value, filter);//readIndexByTerm(reader,text);
			    	for(String str:contents){
			    		System.out.println(str);
			    	}
			    }
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static ArrayList<String> readIndexByTerm(IndexSearcher searcher, String fieldString, String termString, String filter) throws CorruptIndexException, IOException{
		//System.out.println(fieldString+" "+termString);
		TermQuery query = new TermQuery(new Term(fieldString, termString));
		if(debug)
		System.out.println("your query |"+query.toString()+"|");
		TopDocs topDocs = searcher.search(query, 1);
		
		if(topDocs==null)
			return new ArrayList<String>();
		ScoreDoc[] hits = topDocs.scoreDocs;
		
//		System.out.println("matching: "+hits.length);
		ArrayList<String> contents = new ArrayList<String>();
		for(int i = 0 ; i < hits.length; i++){
			int docId = hits[i].doc;
			Document doc = searcher.doc(docId);
			
		 	List<IndexableField> fields = doc.getFields();
		 	for(int j = 0 ; j < fields.size(); j++){
		 		
		 		IndexableField field = fields.get(j);
		 		if(field.name().equals(filter)){
//		 			System.out.println(filter+": ("+field.stringValue()+")");
		 			contents.add(field.stringValue());
		 		}
//		 		if(!field.name().equals("triple"))
//		 		System.out.println(field.name()+" "+field.stringValue()+" "+filter);
		 		/*
		 		if(field.name().equals("label")||field.name().equals("boost")||field.stringValue().startsWith(filter+"|")){
		 				System.out.println(field.name()+": "+field.stringValue());
		 		}
		 		*/
		 	}
		}
		return contents;
		
		/*
		Term term = new Term(fieldString, termString);
		TermDocs docs = reader.termDocs(term);
		if(docs.next()){	
			int docId = docs.doc();
			Document doc = reader.document(docId);
			return doc;
		}
		*/
	}
	public static void printIndexByTerm(IndexSearcher searcher, String fieldString, String termString, String filter) throws CorruptIndexException, IOException{
		TermQuery query = new TermQuery(new Term(fieldString, termString));
		System.out.println("your query "+query.toString());
		TopDocs topDocs = searcher.search(query, 1);
		ScoreDoc[] hits = topDocs.scoreDocs;
		//System.out.println("matching: "+hits.length);
		//ArrayList<String> contents = new ArrayList<String>();
		for(int i = 0 ; i < hits.length; i++){
			int docId = hits[i].doc;
			Document doc = searcher.doc(docId);
			
		 	List<IndexableField> fields = doc.getFields();
		 	for(int j = 0 ; j < fields.size(); j++){
		 		
		 		IndexableField field = fields.get(j);
		 		if(field.name().equals(filter)){
		 			System.out.println(filter+": ("+field.stringValue()+")");
		 			//contents.add(field.stringValue());
		 		}
		 		//System.out.println(field.name()+" "+field.stringValue());
		 		/*
		 		if(field.name().equals("label")||field.name().equals("boost")||field.stringValue().startsWith(filter+"|")){
		 				System.out.println(field.name()+": "+field.stringValue());
		 		}
		 		*/
		 	}
		}
		//return contents;
		
		/*
		Term term = new Term(fieldString, termString);
		TermDocs docs = reader.termDocs(term);
		if(docs.next()){	
			int docId = docs.doc();
			Document doc = reader.document(docId);
			return doc;
		}
		*/
	}
//	public static void readIndex(IndexReader reader) throws CorruptIndexException, IOException{
//		int maxDoc = 100;
//		TermEnum terms = reader.terms(); 
//
//		while(terms.next()){
//			Term term = terms.term();
//			int numDocs = terms.docFreq();
//			TermDocs docs = reader.termDocs(term);
////			System.out.println(term.toString()+" "+(term.toString().contains("object:")));
//			if((!term.toString().contains(myterm) ))
//				continue;
//			
//			System.out.println("term: "+term.toString()+" : "+numDocs);
//			while(docs.next()){	
//				int docId = docs.doc();
//				Document doc = reader.document(docId);
//				
//			 	List<Field> fields = doc.getFields();
//			 	for(int i = 0 ; i < fields.size(); i++){
//			 		Field field = fields.get(i);	
//			 		System.out.println(field.name()+": "+field.stringValue());
//			 	}
//			}
//		}
//	}
	/*
	public static void readIndexByTerm(IndexReader reader, String myterm) throws CorruptIndexException, IOException{
		String [] termpart = myterm.split(":",2);
		Term term = new Term(termpart[0], termpart[1]);
		TermDocs docs = reader.termDocs(term);
		while(docs.next()){	
			int docId = docs.doc();
			Document doc = reader.document(docId);
			System.out.println(doc.get("url"));
		 	List<Field> fields = doc.getFields();
		 	for(int i = 0 ; i < fields.size(); i++){
		 		Field field = fields.get(i);	
		 		System.out.println(field.name()+": "+field.stringValue()+" with boost: "+field.getBoost());
		 	}
		}
	}

	*/

}
