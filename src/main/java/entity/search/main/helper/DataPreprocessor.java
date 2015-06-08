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

package entity.search.main.helper;

import java.io.File;

/**
 * Deprecated
 * @author zhengj3
 *
 */

public class DataPreprocessor {
	
	public static void main(String [] args){
		if(args.length < 3){
			if(args.length == 1){
				if(args[0].equals("help")){
					System.out.println("Methods:");
					System.out.println("addInverseProperty");
					System.out.println("removeChars");
					System.out.println("getLabelsFromURL");
					System.out.println("computeEntityWeights");
					System.out.println("computePropertyEntropy");
				}
			}
			System.err.println("usage: DataPreprocess method inputfile outputfile");
			return;
		}
		String method = args[0];
		String inputfile = args[1];
		String outputfile = args[2];
		
		
		if(method.equals("addInverseProperty")){
			Preprocessor.addInverseProperty(inputfile, outputfile);
		}else if(method.equals("removeChars")){
			Preprocessor.removeChars(inputfile, outputfile);
		}else if(method.equals("getLabelsFromURL")){
			Preprocessor.getLabelsFromURL(inputfile, outputfile);
		}else if(method.equals("computeEntityWeights")){
			Preprocessor.computeEntityWeights(inputfile, outputfile);
		}else if(method.equals("computeEntityEntropyWeights")){
			if(args.length < 4){
				System.err.println("usage: DataPreprocess computeEntityEntropyWeights inputfile outputfile propertyWeightfile");
				return;
			}
			String weightFile = args[3];
			Preprocessor.computeEntityEntropyWeights(inputfile, outputfile, weightFile);
		}else if(method.equals("computePropertyEntropy")){
			Preprocessor.computePropertyEntropy(inputfile, outputfile);
		}else if(method.equals("processValueRestriction")){
			//loop through directory
			Preprocessor.processValueRestriction(inputfile, outputfile);
		}else if(method.equals("processValueRestrictionAll")){
			File folder = new File(inputfile);
			
			String currentDir = "";
			if(inputfile.endsWith("/")){
				currentDir = inputfile;
			}else{
				currentDir = inputfile+"/";
			}
			
			String outputDir = "";
			if(outputfile.endsWith("/")){
				outputDir = outputfile;
			}else{
				outputDir = outputfile+"/";
			}
			int count = 1;
			boolean processing = false;
		    for (final File fileEntry : folder.listFiles()) {   	
		    	String filename = fileEntry.getName();
		    	System.out.println(count+" "+filename);
		    	count++;
		    	if(filename.equals("RXNORM.ttl.nt")){
		    		processing = true;
		    	}
		    	if(processing){
		    		Preprocessor.processValueRestriction(currentDir+filename, outputDir+filename+"-new.nt");
		    	}
		    }
		}
	}

}
