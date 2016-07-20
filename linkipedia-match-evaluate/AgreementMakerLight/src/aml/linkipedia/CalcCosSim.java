package aml.linkipedia;

import java.util.Hashtable;
import java.util.LinkedList;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import aml.match.Alignment;
import aml.match.Mapping;
import aml.ontology.Ontology2Match;

public class CalcCosSim{
	 
 public static Ontology2Match source,target;
 
 public double csScore(String sName, String tName){
	 
	 String [] wSeqsName = sName.split(" ");
	 String [] wSeqtName = tName.split(" ");
	 
	 Hashtable<String, FreqValues> wFreqVect = new Hashtable<String, FreqValues>();
	 LinkedList<String> distinctWSrcTgt = new LinkedList<String>();

	 double sim;
   
	 //Find word frequency vector for source name
	 for(int i=0;i<wSeqsName.length;i++){
		 String temp = wSeqsName[i].trim();
		 if(temp.length()>0){
			 if(wFreqVect.containsKey(temp)){
				 FreqValues valSrc = wFreqVect.get(temp);
				 int freq1 = valSrc.freq1+1;
				 int freq2 = valSrc.freq2;
				 valSrc.updateValues(freq1, freq2);
				 wFreqVect.put(temp, valSrc);
			 }
			 else{
				 FreqValues valSrc = new FreqValues(1, 0);
				 wFreqVect.put(temp, valSrc);
				 distinctWSrcTgt.add(temp);
			 }
		 }
	 }
   
	 //Find word frequency vector for target name
	 for(int i=0;i<wSeqtName.length;i++){
		 String temp = wSeqtName[i].trim();
		 if(temp.length()>0){
			 if(wFreqVect.containsKey(temp)){
				 FreqValues valTgt = wFreqVect.get(temp);
				 int freq1 = valTgt.freq1;
				 int freq2 = valTgt.freq2+1;
				 valTgt.updateValues(freq1, freq2);
				 wFreqVect.put(temp, valTgt);
			 }
			 else{
				 FreqValues valTgt = new FreqValues(0, 1);
				 wFreqVect.put(temp, valTgt);
				 distinctWSrcTgt.add(temp);
			 }
		 }
	 }
	 sim = findSim(wFreqVect, distinctWSrcTgt);
	 return sim;
 }
 
 //Compute Cosine Similarity
 public double findSim(Hashtable<String, FreqValues> wFreqVect, LinkedList<String> distinctWSrcTgt){
   
	 double VectAB = 0.0d;
	 double VectA_Sq = 0.0d;
	 double VectB_Sq = 0.0d;
	 
	 double sim_score=0.0d;
   
	 for(int i=0;i<distinctWSrcTgt.size();i++)
	 {
		 FreqValues valST = wFreqVect.get(distinctWSrcTgt.get(i));
   
		 double freq1 = (double)valST.freq1;
		 double freq2 = (double)valST.freq2;
		 
		 VectAB = VectAB + (freq1 * freq2);
    
		 VectA_Sq = VectA_Sq + freq1 * freq1;
		 VectB_Sq = VectB_Sq + freq2 * freq2;
	 }
	 
	 sim_score = ((VectAB)/(Math.sqrt(VectA_Sq)*Math.sqrt(VectB_Sq)));
   
	 return sim_score;
 }
 
}