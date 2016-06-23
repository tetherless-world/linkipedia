package aml.linkipedia;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import aml.match.Alignment;
import aml.match.Mapping;
import aml.match.PropertyMatcher;
import aml.ontology.Ontology2Match;
import aml.settings.EntityType;

public class LinkipediaPropertyMatcher {
    
	public static Ontology2Match source,target;
	public static Alignment propMaps,personMaps, myMaps;
	public static double sim;
	
	public Alignment myPropertyMatcher(String sourcePath,String targetPath) throws OWLOntologyCreationException{
		PropertyMatcher pm = new PropertyMatcher(true);
		
		personMaps = new Alignment();
		source = new Ontology2Match(sourcePath);
		target = new Ontology2Match(targetPath);
		
		Set<Integer> sourceKeys = source.getDataProperties();
		Set<Integer> targetKeys = target.getDataProperties();
		for(Integer i : sourceKeys)
		{
			for(Integer j : targetKeys)
			{
				double sim = pm.matchProperties(i,j,EntityType.DATA);
				if(sim >= 0.3)
					propMaps.add(new Mapping(i,j,sim));
			}
		}
		//Map object properties
		sourceKeys = source.getObjectProperties();
		targetKeys = target.getObjectProperties();
		for(Integer i : sourceKeys)
		{
			for(Integer j : targetKeys)
			{
				double sim = pm.matchProperties(i,j,EntityType.OBJECT);
				if(sim >= 0.3)
					propMaps.add(new Mapping(i,j,sim));
			}
		}
		return propMaps;
	}
}
