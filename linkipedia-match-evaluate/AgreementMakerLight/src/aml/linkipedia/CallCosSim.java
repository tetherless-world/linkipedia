package aml.linkipedia;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import aml.match.Alignment;
import aml.match.Mapping;
import aml.ontology.Ontology2Match;

public class CallCosSim {
    
	public static Ontology2Match source,target;
	public static Alignment csMaps;
	public static double sim;
	
	public static Alignment tfIdfMainParse(String sourcePath,String targetPath) throws OWLOntologyCreationException{
		double sim = 0.0d;
		csMaps = new Alignment();
		source = new Ontology2Match(sourcePath);
		target = new Ontology2Match(targetPath);
		
		String sName, tName;
		List<double[]> docsrcVector, doctgtVector = new ArrayList<double[]>();
		
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
				sim = calcCosSim(sName,tName); 
				csMaps.add(new Mapping(i,j,sim));
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
				sim = calcCosSim(sName,tName);
				csMaps.add(new Mapping(i,j,sim));
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
				sim = calcCosSim(sName,tName);
				csMaps.add(new Mapping(i,j,sim));
			}
		} 
		return csMaps;
 }
	
 public static double calcCosSim(String sName, String tName) {
		CalcCosSim cosSim = new CalcCosSim();
		double simLocal = cosSim.csScore(sName, tName);
		 
		return simLocal;
 }
   
}
