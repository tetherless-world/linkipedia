package aml;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import aml.AML;
import aml.linkipedia.*;
import aml.match.Alignment;
import aml.match.LexicalMatcher;
import aml.match.MultiWordMatcher;
import aml.match.Mapping;
import aml.match.LWC;
import aml.ontology.Individual;
import aml.ontology.ObjectProperty;
import aml.ontology.Ontology2Match;
import aml.ontology.RelationshipMap;
import aml.settings.EntityType;
import aml.settings.MappingRelation;
import aml.util.Similarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public class MyLinkipediaMatcher
{

	public static Ontology2Match source,target;
	public static Alignment lwcMaps;
	public static double sim;
	
	private static CallStringMatcher strM;
	private static ParseString ps;
	private static CallCosSim tf;
	//private static TfIdfMain tf;
	
	public static void main(String[] args) throws Exception
	{
		//Path to input ontology files (edit manually)
		String sourcePath = "D:/Extended E Drive/DataONE/Linkipedia_OntologyMatching/newowl.owl";
		String targetPath = "D:/Extended E Drive/DataONE/Linkipedia_OntologyMatching/ECSO3.owl";
		String referencePath = "";
		//Path to save output alignment (edit manually, or leave blank for no evaluation)
		String outputPath = "";
		
		lwcMaps = new Alignment();
		double thresh = 0.1;
		Alignment mwmMaps, strMaps, lpmMaps, tfMaps = new Alignment();
		
		AML aml = AML.getInstance();
	//	Alignment refalign = new Alignment(referencePath);
	//	System.out.println("PropMaps count : "+refalign.size());

		aml.openOntologies(sourcePath, targetPath);
		//aml.matchAuto();
		
		//To run pre-processing and Edit Distance Matchers
		strMaps = strM.myMatcher(sourcePath,targetPath);
				
		//To run MultiWord Matcher
		MultiWordMatcher mwm = new MultiWordMatcher();
		mwmMaps = mwm.match(thresh);
		
		//To run LWC
		lwcMaps = LWC.combine(strMaps, mwmMaps, 0.75);
		
		//To run TF-IDF and Cosine Similarity
		tfMaps = tf.tfIdfMainParse(sourcePath,targetPath);
		
		//To run LWC
		lwcMaps = LWC.combine(lwcMaps, tfMaps, 0.75);
		
		//To run Property Matcher
		LinkipediaPropertyMatcher lpm = new LinkipediaPropertyMatcher();
		lpmMaps = lpm.myPropertyMatcher(sourcePath,targetPath);
				
		//To run LWC
		lwcMaps = LWC.combine(lwcMaps, lpmMaps, 0.75);
		
		//aml.setAlignment(personMaps);
		aml.setAlignment(lwcMaps);
		
		//System.out.println("PropMaps count : "+personMaps.size());
	    //System.out.println("PropMaps count : "+lwcMaps.size());
		
		if(!referencePath.equals(""))
		{
			aml.openReferenceAlignment(referencePath);
			aml.getReferenceAlignment();
			aml.evaluate();
			System.out.println(aml.getEvaluation());
		}
		
		if(!outputPath.equals(""))
			aml.saveAlignmentRDF(outputPath);
	}
}