package aml;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

public class Evaluate {

	
	public Hashtable<String, ArrayList<String>> manipulateManual()
	{
		Hashtable<String, ArrayList<String>> manualInfo=new Hashtable<>();
		HashSet<String> nameOfPackage=new HashSet<>();
		ArrayList<String> allContentFromFile=new ArrayList<>();
		Evaluate h=new Evaluate();
		h.getContentOfFile(allContentFromFile,nameOfPackage);
		
		for(String p:allContentFromFile)
		{
			String[] theComponents=p.split("\t");
			if((theComponents.length==3)||(theComponents[3].trim().isEmpty()))
				continue;
			String package_id=theComponents[0];
			int class_id=Integer.parseInt(theComponents[3].trim());
			String edited_class_id="http://purl.dataone.org/odo/ECSO_"+ String.format("%08d" ,class_id);	
			if(manualInfo.keySet().contains(package_id))
			{
				ArrayList<String> t=manualInfo.get(package_id);
				t.add(edited_class_id);				
				
			}
			else
			{
				ArrayList<String> pp=new ArrayList<>();
				pp.add(edited_class_id);
				manualInfo.put(package_id, pp);
				
			}
			
		}
		
			
	return manualInfo;
	}
	public void getContentOfFile(ArrayList<String> entireContent, HashSet<String> packageName)
	{
		
		BufferedReader buf;
		try{
		buf = new BufferedReader(new FileReader("C:\\Users\\prakash\\Desktop\\sabitaInternship\\7_12_2016\\AgreementMakerLight\\src\\aml\\manual_annotations.tsv.txt"));
		String theSentence="";
		
		theSentence=buf.readLine();
		while((theSentence=buf.readLine())!=null)
		{
			
			entireContent.add(theSentence);
			String[] theParts=theSentence.split("\t");
			packageName.add(theParts[0]);
					
		}
		buf.close();
		}
		catch(Exception e)
		{
			System.err.println("there is problem in reading content from file");
		}
		
	}
	
	public Hashtable<String,ArrayList<String>> manipulateAutomatic()
	{
		Hashtable<String,ArrayList<String>> packageAndClasses=new Hashtable<>();
		try {
			BufferedReader buf=new BufferedReader(new FileReader("C:\\Users\\prakash\\Desktop\\sabitaInternship\\7_12_2016\\sab7_11_2016_v4.csv"));
			String theSentence="";
			while((theSentence=buf.readLine())!=null)
			{
				String[] theComponents=theSentence.split(",");
				double theSimilarity=Double.parseDouble(theComponents[5]);
				String packageId=theComponents[0];
				packageId=packageId.replace("ZZZZ","\\");
				packageId=packageId.replace("C:\\Users\\prakash\\Desktop\\sabitaInternship\\owlFiles\\source\\","");
				packageId=packageId.replace(".owl","");
				if(packageId.startsWith("doi_")) packageId=packageId.replaceFirst("doi_","doi:");
				if(packageId.startsWith("https_")) packageId=packageId.replaceFirst("https_","https:");
				if(packageId.startsWith("ark_")) packageId=packageId.replaceFirst("ark_","ark:");
				packageId=packageId.replace("\\","/");
				if(theSimilarity>0.2)
				{
					
					if(packageAndClasses.keySet().contains(packageId))
						{
							ArrayList<String> tempArray=packageAndClasses.get(packageId);
							tempArray.add(theComponents[3]);
							packageAndClasses.put(packageId, tempArray);
							
						}
					else
					{
						ArrayList<String> temp=new ArrayList<>();
						temp.add(theComponents[3]);
						packageAndClasses.put(packageId, temp);
					}
									
					
				}
			
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		
		return packageAndClasses;
	}
	
	public void checkAccuracy(Hashtable<String,ArrayList<String>> manualData, Hashtable<String,ArrayList<String>> automaticData)
	{
		ArrayList<String> contentFromManual=new ArrayList<>();
		ArrayList<String> contentFromManualTemp=new ArrayList<>();
		ArrayList<String> contentFromAutomatic=new ArrayList<>();
		double precision=0.0, recall=0.0,fscore=0.0,hits=0.0,miss=0.0;
		for(String h:automaticData.keySet())
		{
			//System.out.println("this is the package id: "+h);
		ArrayList<String> automaticArray=automaticData.get(h);
		if(!(manualData.keySet().contains(h)))
			continue;
		for(String j:automaticArray)
		{
			contentFromAutomatic.add(h+" "+j);
		}
		ArrayList<String> manualArray=manualData.get(h);
		for(String r:manualArray)
		{
			contentFromManual.add(h+" "+r);
			contentFromManualTemp.add(h+" "+r);
			
		}
		}
		//above I got all the contents of the automated annotations and the corresponding contents from the 
		//manual annotations in two ArrayLists. Now we will compare these two ArrayLists and get the  accuracy
		/*System.out.println("from automatic: \n");
		for(String k:contentFromAutomatic)
			System.out.println(k);
		
		System.out.println("from manual: \n");
		for(String k:contentFromManual)
			System.out.println(k);
		System.out.println("***********************************");
		*/
		for(String k:contentFromAutomatic)
		{
			if(contentFromManualTemp.contains(k))
				{
				hits++;
				contentFromManualTemp.remove(k);
			
		}}
			miss=contentFromManual.size()-contentFromAutomatic.size();
			precision=hits/contentFromAutomatic.size();
			recall=hits/contentFromManual.size();
			if((precision==0.0)&&(recall==0.0))
				fscore=0.0;
			else
			fscore=(2*precision*recall)/(precision +recall);
			System.out.println("this is the precision: "+precision);
			System.out.println("this is the recall: "+recall);
			System.out.println("this is the fscore: "+fscore);
		
	}
	
	
	
	public static void main(String[] args)
	{
		
		Evaluate fm=new Evaluate();
		Hashtable<String,ArrayList<String>> manualAnnotation=fm.manipulateManual();
		Hashtable<String,ArrayList<String>> automaticAnnotation=fm.manipulateAutomatic();
		/*System.out.println("checking the keys of the manaual:");
		for(String h:manualAnnotation.keySet())
		{
			System.out.println(h);
		}
		System.out.println("\n\n*** checking the keys of the automatic: ");
		for(String k:automaticAnnotation.keySet())
			System.out.println(k);
		System.out.println("finished !!!!");*/
		//System.out.println("this is the size of the hashtable: "+automaticAnnotation.size());
		fm.checkAccuracy(manualAnnotation,automaticAnnotation);
		
		
		
		
		
		
		
		
	}
	
}
