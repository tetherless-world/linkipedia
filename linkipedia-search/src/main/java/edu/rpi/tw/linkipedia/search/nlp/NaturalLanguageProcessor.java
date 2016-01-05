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

package edu.rpi.tw.linkipedia.search.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.LabeledWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class NaturalLanguageProcessor {
    StanfordCoreNLP pipeline;
    List<CoreMap> sentences;
    MaxentTagger tagger;
    String taggedString;
    public NaturalLanguageProcessor(){
	    Properties props = new Properties();
	    //props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
	    //props.put("annotators", "tokenize,ssplit,pos, parse");
	    props.put("annotators", "tokenize,ssplit");
	    pipeline = new StanfordCoreNLP(props);
    	//tagger = new MaxentTagger("models/bidirectional-distsim-wsj-0-18.tagger");
	}
	public void setText(String text){
		//taggedString= tagger.tagString(text);
//	    System.out.println(text);
	    Annotation document = new Annotation(text);
	    pipeline.annotate(document);	    
	    sentences = document.get(SentencesAnnotation.class);
	    //System.out.println("finish sentence annotate");
	}
	public ArrayList<String> getNounPhrases(){
//		ArrayList<String> phrase = new ArrayList<String>();
//		phrase.add(taggedString.replaceAll("/", "|"));
//		return phrase;
		ArrayList<String> phrases = new ArrayList<String> ();
		for(CoreMap sentence: sentences) {
			System.out.println(sentence);
		      Tree tree = sentence.get(TreeAnnotation.class);
		      phrases.add(printTree(tree));
		      //phrases.addAll(getNounPhraseFromParseTree(tree));
		}
		return phrases;
	}
	public ArrayList<String> getSentences(){
		ArrayList<String> sentences = new ArrayList<String> ();
		for(CoreMap sentence: this.sentences) {
		      sentences.add(sentence.toString());
		}
		return sentences;
	}
	public String printTree(Tree tree){
		List<LabeledWord> words = tree.labeledYield();

		String currentPhrase = "";
        for(LabeledWord word:words){
        	if(!(word.tag().toString().matches("[A-Z]+")))
        			continue;
        	currentPhrase+=word.word()+"|"+word.tag()+" ";
        }		
        System.out.println("Phrase: "+currentPhrase);
        return currentPhrase;
	}
	private List<String> getNounPhraseFromParseTree(Tree parse)
	{

	    List<String> phraseList=new ArrayList<String>();
	    for (Tree subtree: parse)
	    {
	      if(subtree.label().value().equals("NP"))
	      {
	    	String subtreeString = subtree.toString();
	    	if(subtreeString.lastIndexOf("(NP")!=subtreeString.indexOf("(NP"))
	    		continue;
	        //System.out.println(subtree);
	        List<LabeledWord> words = subtree.labeledYield();
	        String currentPhrase = "";
	        for(LabeledWord word:words){
	        	
	        	currentPhrase+=word.word()+"|"+word.tag()+" ";
	        }
	        currentPhrase = currentPhrase.trim();
	        phraseList.add(currentPhrase);
	      }
	    }

	    return phraseList;

	}

}
