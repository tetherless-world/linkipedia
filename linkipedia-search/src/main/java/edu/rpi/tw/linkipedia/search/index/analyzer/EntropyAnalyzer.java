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

package edu.rpi.tw.linkipedia.search.index.analyzer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Hashtable;

import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.UpperCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.payloads.DelimitedPayloadTokenFilter;
import org.apache.lucene.analysis.payloads.PayloadEncoder;
/*
import org.apache.lucene.analysis.payloads.DelimitedPayloadTokenFilter;
import org.apache.lucene.analysis.payloads.FloatEncoder;
import org.apache.lucene.analysis.payloads.PayloadEncoder;
*/
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
/*
public class EntropyAnalyzer extends Analyzer {
    private PayloadEncoder encoder;

    EntropyAnalyzer(PayloadEncoder encoder) {
      this.encoder = encoder;
    }

    public TokenStream tokenStream(String fieldName, Reader reader) {
      TokenStream result = new WhitespaceTokenizer(reader);
      result = new LowerCaseFilter(result);
      result = new DelimitedPayloadTokenFilter(result, '|', encoder);
      return result;
    }
  }
*/
import org.apache.lucene.util.Version;

public class EntropyAnalyzer extends Analyzer {
	
	private PayloadEncoder encoder;
	
	public EntropyAnalyzer(PayloadEncoder encoder) {
	      this.encoder = encoder;
	    }

	
	@Override
	protected TokenStreamComponents createComponents(String fieldName,
			Reader reader) {
	//	System.out.println("compoent");
		Tokenizer tk = new KeywordTokenizer(reader);
		TokenStream filter = new LowerCaseFilter(Version.LUCENE_47, tk);
		filter = new DelimitedPayloadTokenFilter(filter, '|', encoder);
		TokenStreamComponents components = new TokenStreamComponents(tk, filter);
		
		return components;
	}
	
	/*
	@Override
	   protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		System.out.println("testing: "+fieldName);
		     Tokenizer source = new WhitespaceTokenizer(Version.LUCENE_47, reader);
		     TokenStream filter = new UpperCaseFilter(Version.LUCENE_47,source);
		     return new TokenStreamComponents(source, filter);
		   }
*/
}