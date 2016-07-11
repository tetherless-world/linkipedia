package aml.linkipedia;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.StopWordRemover;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import edu.smu.tspell.wordnet.WordNetDatabase;

import aml.match.Alignment;
import aml.match.Mapping;
import aml.ontology.Individual;
import aml.ontology.ObjectProperty;
import aml.ontology.Ontology2Match;
import aml.ontology.RelationshipMap;
import aml.settings.EntityType;
import aml.settings.MappingRelation;
import aml.settings.MappingStatus;
import aml.util.Similarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public class ParseString
{

	public static Ontology2Match source,target;
	public static double sim;

	public static double splitNames(String sName, String tName){
		
		double sim = 0.0d;
		//String sName = source.getName(sourceId);
		//String tName = target.getName(targetId);
		
		//Removing Stop-words
		String stopWords[]={"the","has","an","a","by","is","was","then","of","have","had","were","are","for"};
		for(int i=0;i<stopWords.length;i++){
		       if(sName.contains(stopWords[i])){
		           sName=sName.replaceAll(stopWords[i]+"\\s+", ""); //this will remove spaces at the end
		           sName=sName.replaceAll("  "," ");
		       }
		       if(tName.contains(stopWords[i])){
		           tName=tName.replaceAll(stopWords[i]+"\\s+", ""); //this will remove spaces at the end
		           tName=tName.replaceAll("  "," ");
		       }
		}

		//Remove Non-alphabet characters
		sName = sName.replaceAll("[^A-Za-z0-9]", " ");
		tName = tName.replaceAll("[^A-Za-z0-9]", " ");
		
		ArrayList<String> sSplits = getSplitParts(sName);
		ArrayList<String> tSplits = getSplitParts(tName);
		/*for (String s: sSplits){
			System.out.println("* "+s);
		}
		for (String t: tSplits){
			System.out.println("> "+t);
		}*/
		for (String t1: tSplits){
			for (String s1: sSplits){
				sim = strSimilarity(s1,t1);
//				if (sim > 0.3){
//					System.out.println("Mapping: "+s1+" <-> "+t1);
//					System.out.println("Similarity: "+sim);
//				}
				//sabita commented the below one 7-11-2016
			/*	if (sim < 0.5)
					sim = wpDistance(s1,t1);
				
				if (sim > 0.5){
					System.out.println("Mapping: "+s1+" <-> "+t1);
					System.out.println("Similarity: "+sim);
				//	myMaps.add(new Mapping(s1,t1,sim,MappingRelation.EQUIVALENCE));
					//finalMaps.add(new Mapping(s1,t1,sim));
				}*/
				
			}
		}
		return sim;
	}
	
	public  static double strSimilarity(String str1, String str2) {
	    String longerStr = str1, shorterStr = str2;
	    
	    if (str1.length() < str2.length()) { // longer should always have greater length
	      longerStr = str2; 
	      shorterStr = str1;
	    }
	    int longerLength = longerStr.length();
	    if (longerLength == 0) { 
	    	return 1.0; // str1 and str2 are of length 0
	    }
	    
	    //Calculate similarity using Levenshtein's edit distance
	    return (longerLength - LevEditDistance(longerStr, shorterStr)) / (double) longerLength;
	  }
	
	//Levenshtein's Distance
	 private static int LevEditDistance(String str1, String str2) {
		    str1 = str1.toLowerCase();
		    str2 = str2.toLowerCase();

		    int[] newCost = new int[str2.length() + 1];
		    for (int i = 0; i <= str1.length(); i++) {
		      int oldValue = i;
		      for (int j = 0; j <= str2.length(); j++) {
		        if (i == 0)
		        	newCost[j] = j;
		        else {
		          if (j > 0) {
		            int newValue = newCost[j - 1];
		            if (str1.charAt(i - 1) != str2.charAt(j - 1))
		              newValue = Math.min(Math.min(newValue, oldValue),
		            		  newCost[j]) + 1;
		            newCost[j - 1] = oldValue;
		            oldValue = newValue;
		          }
		        }
		      }
		      if (i > 0)
		    	  newCost[str2.length()] = oldValue;
		    }
		    return newCost[str2.length()];
	 }
	
	private static ArrayList<String> getSplitParts(String stName)
	{
		ArrayList<String> stparts = new ArrayList<String>();	
		String[] uc;
		//split name by "_"
		if(stName.contains("_"))
		{	
			String tu[]=stName.toLowerCase().split("_");
			for(String x:tu)
			{
				
				stparts.add(x);
			}
		}
		else
			//split name by "-"
			if(stName.contains("-"))
			{	
				String th[]=stName.toLowerCase().split("-");
				for(String x:th)
				{
					
					stparts.add(x);
				}
			}
		else
			if(!stName.equals(stName.toLowerCase()))
			{
				//split name by Upper Case
				uc=stName.split("(?=\\p{Upper})");	
				for(String x:uc)
				{
					stparts.add(x.toLowerCase());
				}
			}
			else
			{		
				stparts.add(stName);
			}
		return stparts;
	}
	
	//Calculate Wu and Palmer Distance
	public static double wpDistance(String str1,String str2)
	{
		double simLocal = 0.0d;
		final ILexicalDatabase db = new NictWordNet();
		WS4JConfiguration.getInstance().setMFS(true);
		double wpDist = new WuPalmer(db).calcRelatednessOfWords(str1, str2);
		//Calculate similarity using Wu and Palmer Distance
		if(wpDist >= 1.0d)
            simLocal = 1.0d;
		else
			simLocal = 0.0d;
		return simLocal;
		
	}
}