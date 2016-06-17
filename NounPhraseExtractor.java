import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

public class NounPhraseExtractor {

	private Tokenizer tokenizer;
	private POSTaggerME pos_tagger;
	
	private String tokenizer_model_file = "en-token.bin";
	private String pos_model_file = "en-pos-maxent.bin";
	
	public NounPhraseExtractor(){
		try {
			InputStream posMdelIn = new FileInputStream(pos_model_file);
			POSModel model = new POSModel(posMdelIn);
			pos_tagger = new POSTaggerME(model);
			posMdelIn.close();
			
			InputStream modelIn = new FileInputStream(tokenizer_model_file);
			//InputStream modelIn= this.getClass().getClassLoader().getResourceAsStream(tokenizer_model_file);
			TokenizerModel token_model = new TokenizerModel(modelIn);
			tokenizer = new TokenizerME(token_model);
			modelIn.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getNounPhrase(String text){
		ArrayList<String> noun_phrases = new ArrayList<String>();
		String token_strs [] = tokenizer.tokenize(text);
		String token_pos [] = pos_tagger.tag(token_strs);
		String current_noun_phrase = "";
		for(int i = 0 ; i < token_strs.length; i++){
//			System.out.print(token_strs[i]+"|"+token_pos[i]+" ");
			if(token_pos[i].startsWith("N")){
				current_noun_phrase = current_noun_phrase + token_strs[i]+" ";
			}else{
				if(!current_noun_phrase.equals("")){
					noun_phrases.add(current_noun_phrase.trim());
					current_noun_phrase = "";
				}
			}
		}
		if(!current_noun_phrase.equals("")){
			noun_phrases.add(current_noun_phrase.trim());
		}
		return noun_phrases;
	}
	
	public static void main(String [] args){
		String text = "San Francisco Fed President John Williams told reporters after a conference at University of California Berkeley's Clausen Center.";
		NounPhraseExtractor extractor = new NounPhraseExtractor();
		ArrayList<String> noun_phrases = extractor.getNounPhrase(text);
		for(String np:noun_phrases){
			System.out.println(np);
		}
	}
	
}
