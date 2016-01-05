package edu.rpi.tw.linkipedia.search.indexing;

import java.lang.reflect.InvocationTargetException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

public class DefaultAnalyzer {

	public static Analyzer getAnalyzer() {
		return new StandardAnalyzer(Version.LUCENE_47);
	}
}
