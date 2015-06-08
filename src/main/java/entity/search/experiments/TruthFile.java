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

package entity.search.experiments;

import java.io.*;
import java.util.*;

import entity.search.utils.Annotation;

public class TruthFile {
	
	public static void main(String[] args) throws FileNotFoundException {
		 TruthFile truthFile = 
				 new TruthFile("EntityRecognitionDataset/truth2.txt");
		 HashMap<String, ArrayList<Annotation>> truthTables = truthFile.parse();
		 
		 truthFile.log("There are " + truthTables.size() + "truth files.");
		 Iterator itr1 = truthTables.entrySet().iterator();
		 while (itr1.hasNext()) {
			 Map.Entry truthTable = (Map.Entry) itr1.next();
//			 truthFile.log("\nFile \"" + truthTable.getKey() + "\":");
			 HashMap tableContents = (HashMap) truthTable.getValue(); 
			 Iterator itr2 = tableContents.entrySet().iterator();
			 while (itr2.hasNext()) {
				 Map.Entry tableEntry = (Map.Entry)itr2.next();
				 truthFile.log(tableEntry.getKey().toString().toLowerCase()
						 +",");// + ": " + tableEntry.getValue());
			 }
			 System.out.println();
		 }
	}
	
	public TruthFile(String fileName) {
		inputFile = new File(fileName);
	}
	
	public LinkedHashMap<String, ArrayList<Annotation>> parse() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			try {
				String line = null;
				String truthTableTitle = null;
				int truthTableNumber = 1;
				//LinkedHashMap<String, String> truthTable = new LinkedHashMap<String, String>();
				ArrayList<Annotation> truthTable = new ArrayList<Annotation>();
				while ((line = reader.readLine()) != null) {
					if ((line.contains("~~~"))) {
						String[] splits = line.split(" ");
						if (splits[1].equals("BOD")) {
							truthTableTitle = Integer.toString(truthTableNumber);
						}
						else {
							truthTableTitle = splits[1];
						}
						continue;
					}
					if (!(line.contains("###"))) {
						String[] splits = line.split("\t");
						//if (!(truthTable.containsKey(splits[0]))) {
							if (splits.length > 1 && !(splits[1].matches("!!!|---"))) {
								splits[1]=splits[1].replaceAll(" ", "_");
								String annotation = "<http://dbpedia.org/resource/"+splits[1]+">";
								splits[0]=splits[0].toLowerCase().replaceAll("\\."," ").replaceAll("-", " ").replaceAll("'", " ").replaceAll("\\s+", " ");
								Annotation anno = new Annotation(splits[0], annotation, 1);
								truthTable.add(anno);
						//	}
						}
					}
					else {
						truthTableNumber++;
						truthTables.put(truthTableTitle, truthTable);
						truthTable = new ArrayList<Annotation>();
					}
				}
			}
			finally {
				reader.close();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return truthTables;
	}
	
	public void log(Object obj) {
		System.out.print(obj);
	}
	
	private final File inputFile;
	private LinkedHashMap<String, ArrayList<Annotation>> truthTables = 
			new LinkedHashMap<String, ArrayList<Annotation>>();
} 
