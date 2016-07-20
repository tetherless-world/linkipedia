package aml;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Hashtable;

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

	//public static Ontology2Match source,target;
	public static Alignment lwcMaps;
	//public static double sim;
	
	private static CallStringMatcher strM;
	private static ParseString ps;
	private static CallCosSim tf;
	public static HashSet<Integer> targetSubClasses=new HashSet<>();
	//private static TfIdfMain tf;
	
	public static void main(String[] args) throws Exception
	{
		MyLinkipediaMatcher mm=new MyLinkipediaMatcher();
		
		HashSet<String> sourceName=mm.getSource();
		
		
		String targetPath="C:\\Users\\prakash\\Desktop\\sabitaInternship\\AgreementMakerLight\\src\\aml\\mergedOntologies.owl";
		int i=1;
		
		for(String sourcePath:sourceName)// for each ontology in the source  get the matches
		{
			Hashtable<String,ArrayList<String>> packageID_and_Class=new Hashtable<>();
			System.out.println("working on: "+i+ " dataset");
			System.out.println("this is the file name: "+sourcePath);
			
			
			ArrayList<String> classesReturned=new  ArrayList<>();
			Hashtable<String,String> highestScoringClass=new Hashtable<>();
			//for(String targetPath:targetName)//iterating through each target
			
				//System.out.println("this is the target ontology: "+targetPath);
				lwcMaps = new Alignment();
				strM=new CallStringMatcher();
				tf=new CallCosSim();
				double thresh = 0.1;
				Alignment mwmMaps, strMaps, lpmMaps, tfMaps = new Alignment();
				
				AML aml = AML.getInstance();
			//	Alignment refalign = new Alignment(referencePath);
			//	System.out.println("PropMaps count : "+refalign.size());
		
				try {
					aml.openOntologies(sourcePath, targetPath);
				} catch (OWLOntologyCreationException e) {
					// TODO Auto-generated catch block
					System.err.println("Error in opening the ontologies");
				}
				//aml.matchAuto();
				
				//To run pre-processing and Edit Distance Matchers
				strMaps = strM.myMatcher(sourcePath,targetPath);
				System.out.println("finished string matcher!!!!!!!!!!");
				/*
				 * in this part, we are trying to identify the class ids that are subclasses of measurement type. 
				 *
				 */
				System.out.println("*********");
				
				Set<Integer> targetKeys = CallStringMatcher.target.getClasses();
				RelationshipMap rmm=aml.getRelationshipMap();
				HashSet<Integer> sourceSubClasses=mm.getSuperClassForSource(rmm);//these are the id of the classes from source that are subclass of measurement type
				if(i==1)
				{
				 targetSubClasses=mm.getSuperClassForTarget(rmm);//classes from the target that are subclass of measurement type--doing it once since the target is always same
				}
				 
						
				//To run MultiWord Matcher
				MultiWordMatcher mwm = new MultiWordMatcher();
				mwmMaps = mwm.match(thresh);
				System.out.println("finished  multiword matcher");
				//To run LWC
				lwcMaps = LWCForLinkipedia.combine(strMaps, mwmMaps, 0.75,sourceSubClasses);
				//System.out.println("finished  LWC 1");
				//To run TF-IDF and Cosine Similarity
				tfMaps = tf.tfIdfMainParse(sourcePath,targetPath);
				System.out.println("finished  tf-idf");
				//To run LWC
				lwcMaps = LWCForLinkipedia.combine(lwcMaps, tfMaps, 0.75,sourceSubClasses);
				//System.out.println("finished  LWC 2");
				//To run Property Matcher
				LinkipediaPropertyMatcher lpm = new LinkipediaPropertyMatcher();
				lpmMaps = lpm.myPropertyMatcher(sourcePath,targetPath);
				System.out.println("finished  linkipedia property matcher");		
				//To run LWC
				lwcMaps = LWCForLinkipedia.combine(lwcMaps, lpmMaps, 0.75,sourceSubClasses);
				System.out.println("finished LWC ");
				//aml.setAlignment(personMaps);
				aml.setAlignment(lwcMaps);
				//System.out.println("this is the check: \n");
				//System.out.println("this is the size of lwcMaps: "+lwcMaps.size());
				
				//System.out.println("PropMaps count : "+personMaps.size());
			    //System.out.println("PropMaps count : "+lwcMaps.size());
				
				for(Mapping m:lwcMaps)
				{
			
					if(m.getSimilarity()>0)
					{
						//if((sourceSubClasses.contains(m.getSourceId()))&&(targetSubClasses.contains(m.getTargetId()))){
						
						if(highestScoringClass.keySet().contains(m.getSourceURI()))
						{
							String matchingClassAndScore=highestScoringClass.get(m.getSourceURI());
							//System.out.println(matchingClassAndScore);
							String matchingClass=matchingClassAndScore.split("@@")[2];
							double matchScore=Double.parseDouble(matchingClassAndScore.split("@@")[4]);
							if(m.getSimilarity()>matchScore)
							{
								matchingClass=m.getTargetURI();
								matchScore=m.getSimilarity();
								highestScoringClass.put(m.getSourceURI(),m.getSourceURI()+"@@"+CallStringMatcher.source.getName(m.getSourceId())+"@@"+matchingClass+"@@"+CallStringMatcher.target.getName(m.getTargetId())+"@@"+matchScore);
								//System.out.println("**********"+m.getSourceURI()+"@@"+CallStringMatcher.source.getName(m.getSourceId())+"@@"+matchingClass+"@@"+CallStringMatcher.target.getName(m.getTargetId())+"@@"+matchScore);
							}
							
							
						}
						else {
							highestScoringClass.put(m.getSourceURI(),m.getSourceURI()+"@@"+CallStringMatcher.source.getName(m.getSourceId())+"@@"+m.getTargetURI()+"@@"+CallStringMatcher.target.getName(m.getTargetId())+"@@"+m.getSimilarity());
						}
						
						
					}
									
				}
				
			for(String l:highestScoringClass.keySet())
			{
				String data=highestScoringClass.get(l);
				classesReturned.add(data);
				//System.out.println("***** data: "+l+"  the highest: "+data.split("@@")[0]);
				
			}
			
			packageID_and_Class.put(sourcePath, classesReturned);
			mm.printTheContent(packageID_and_Class);
			i++;
		}	
		
	
		System.out.println("finished writing!!");
		
	}
	
	
	public void printTheContent(Hashtable<String,ArrayList<String>> packageId_and_class)
	{
		String comma=",";
		String newline="\n";
		System.out.println("***********writing down!!");
		try {
			 BufferedWriter buf=new BufferedWriter(new FileWriter("C:\\Users\\prakash\\Desktop\\sabitaInternship\\owlFiles\\sab7_11_2016.csv",true));
			for(String k:packageId_and_class.keySet())
			{
				
				String nameOfPackage=k;
				//System.out.println("this is the package: "+nameOfPackage);
				ArrayList<String> classesMatched=packageId_and_class.get(k);
				if(classesMatched.size()==0)
				{
					buf.write(k); buf.write(newline);
				}
				else
				{
				for(String l:classesMatched)
				{
					//System.out.println("this is the matched class: "+l);
					String[] theComponents=l.split("@@");
					buf.write(k); buf.write(comma); buf.write(theComponents[0]);buf.write(comma); buf.write(theComponents[1]); buf.write(comma); buf.write(theComponents[2]);buf.write(comma); buf.write(theComponents[3]);buf.write(comma); buf.write(theComponents[4]); buf.write(newline);
					
					
				}
				}
				
				
			}
			
			buf.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("there is error in writing contents to the file");
		}
		
		
		
		
		
		
	}
 	public HashSet<String> getSource()
	{
		File sourcefolder = new File("C:\\Users\\prakash\\Desktop\\sabitaInternship\\owlFiles\\sourceCompleted");//path to the folder that has the source files
		File[] listOfFilesSource = sourcefolder.listFiles();

				
		HashSet<String> namesOfSource=new HashSet<>();
		
		
		    for (int i = 0; i < listOfFilesSource.length; i++) {
		      if (listOfFilesSource[i].isFile()) {
		    	  
		       namesOfSource.add(listOfFilesSource[i].getAbsolutePath());
		       
		      } 
		    }	
		    
		  return namesOfSource;  
	}
	public  HashSet<Integer> getSuperClassForSource(RelationshipMap rmm)
	{
		HashSet<Integer> sourceSubClasses=new HashSet<>();
		Set<Integer> sourceKeys = CallStringMatcher.source.getClasses();
		if(CallStringMatcher.id_of_measurement_type_source==-1) //there is no measurement_type class being mentioned in the ontology..so no chances of finding subclass
			return sourceSubClasses;
		for(Integer k:sourceKeys)
		{
			
			Set<Integer> supTem=rmm.getSuperClasses(k,true);//for each class from the source, I get its superClass
			if(supTem.contains(CallStringMatcher.id_of_measurement_type_source))
				{
				sourceSubClasses.add(k);
				
				}
		}
		return sourceSubClasses;
		
	}
	public HashSet<Integer> getSuperClassForTarget(RelationshipMap rmm)
	{
		HashSet<Integer> targetSubClasses=new HashSet<>();
		Set<Integer> targetKeys = CallStringMatcher.target.getClasses();
		if(CallStringMatcher.id_of_measurement_type_target==-1) //there is no measurement_type class being mentioned in the ontology..so no chances of finding subclass
			return targetSubClasses;
		for(Integer k:targetKeys)
		{
					
			Set<Integer> supTem=rmm.getSuperClasses(k,true);//for each class from the source, I get its superClass
			if(supTem.contains(CallStringMatcher.id_of_measurement_type_target))
				{
				targetSubClasses.add(k);
				
				}
		}
		return targetSubClasses;
		
	}
	

}
	
