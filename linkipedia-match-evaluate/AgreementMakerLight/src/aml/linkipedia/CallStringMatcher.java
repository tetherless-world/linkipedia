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

public class CallStringMatcher
{
	public static Ontology2Match source,target;
	public static double sim;
	private static ParseString ps;
	public static int id_of_measurement_type_source=-1;
	public static int id_of_measurement_type_target=-1;

	public static Alignment myMatcher(String sourcePath,String targetPath) throws OWLOntologyCreationException{
		double sim = 0.0d;
		Alignment strMaps = new Alignment();
		source = new Ontology2Match(sourcePath);
		target = new Ontology2Match(targetPath);
		
		String sName, tName;
		
		//Get class Names of the source and target
		Set<Integer> sourceKeys = source.getClasses();
		Set<Integer> targetKeys = target.getClasses();
		
		//Parse Class Names of Source and Target
		for(Integer i : sourceKeys)
		{
			for(Integer j : targetKeys)
			{		
				sName = source.getName(i);
				tName = target.getName(j);
				sim = ps.splitNames(sName,tName);
				//matchProperties
				strMaps.add(new Mapping(i,j,sim));
				//
				//updating the ids of the source and target measurement id
				if(sName.trim().equalsIgnoreCase("measurement type"))
					id_of_measurement_type_source=i;
				if(tName.trim().equalsIgnoreCase("measurement type"))
					id_of_measurement_type_target=j;
				
			}
		} 
		
		//Get Data Properties of the source and target
		Set<Integer> sDProp = source.getDataProperties();
		Set<Integer> tDProp = target.getDataProperties();
		
		//Parse Data Properties of Source and Target
		for(Integer i : sDProp)
		{
			for(Integer j : tDProp)
			{
				sName = source.getName(i);
				tName = target.getName(j);
				sim = ps.splitNames(sName,tName);
				strMaps.add(new Mapping(i,j,sim));
			}
		} 
		
		//Get Object Properties of the source and target
		Set<Integer> sOProp = source.getObjectProperties();
		Set<Integer> tOProp = target.getObjectProperties();
		
		//Parse Object Properties of Source and Target
		for(Integer i : sOProp)
		{
			for(Integer j : tOProp)
			{
				sName = source.getName(i);
				tName = target.getName(j);
				sim = ps.splitNames(sName,tName);
				strMaps.add(new Mapping(i,j,sim));
			}
		} 
		return strMaps;
	}
}