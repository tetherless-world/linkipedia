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

package edu.rpi.tw.linkipedia.search.main.helper;

import edu.rpi.tw.linkipedia.search.indexing.EntityIndexUpdater;
import edu.rpi.tw.linkipedia.search.indexing.EntityIndexer;
import edu.rpi.tw.linkipedia.search.indexing.InMemEntityIndexer;
import edu.rpi.tw.linkipedia.search.indexing.SurfaceFormIndexUpdater;
import edu.rpi.tw.linkipedia.search.indexing.SurfaceFormIndexer;

public class IndexData {
	
	public static void main(String [] args){
		if(args.length < 3){
			if(args.length == 1){
				if(args[0].equals("help")){
					System.out.println("Methods:");
					System.out.println("indexSurfaceForm");
				}
			}
			System.err.println("usage: indexData method inputDirectory/inputFile outputIndexName (additionalFile may required)");
			return;
		}
		String method = args[0];
		String inputfile = args[1];
		String outputfile = args[2];
		
		
		if(method.equals("indexSurfaceForm")){
			/*
			 * Assume both surface form file and entity weight file are sorted 
			 */
			//String weightfile="";
			if(args.length < 5){
				System.err.println("usage: indexData indexSurfaceForm inputDirectory/inputFile outputIndexName entityweightfile propertyweightfile");
				return;			
			}
			String entityweightfile = args[3];
			String propertyweightfile = args[4];
			
			SurfaceFormIndexer sfIndexer = new SurfaceFormIndexer(inputfile, outputfile);
			sfIndexer.setEntityWeightFile(entityweightfile);
			sfIndexer.setPropertyWeight(propertyweightfile);
			sfIndexer.createIndex();
		}else if(method.equals("indexEntity")){
			String weightfile="";
			if(args.length < 5){
				System.err.println("usage: indexData indexEntity inputDirectory outputIndexName surfaceFormIndex propertyWeight");
				return;			
			}	
			String surfaceIndex = args[3];
			weightfile = args[4];
			EntityIndexer entityIndexer = new EntityIndexer(inputfile, outputfile, surfaceIndex);
			entityIndexer.setWeightFile(weightfile);
			entityIndexer.createIndex();
		}else if(method.equals("inMemIndexEntity")){
			//need to modify, may delete, depends on how indexEntity works in terms of time
			if(args.length < 6){
				System.err.println("usage: indexData inMemIndexEntity inputDirectory outputIndexName sufaceFormFile entityWeight propertyWeight");
				return;			
			}	
			String sufaceFormFile = args[3];
			String entityWeight = args[4];
			String propertyWeight = args[5];
			InMemEntityIndexer entityIndexer = new InMemEntityIndexer(inputfile, outputfile);
			entityIndexer.setEntityLabels(sufaceFormFile);
			entityIndexer.setEntityWeightFile(entityWeight);
			entityIndexer.setPropertyWeightFile(propertyWeight);
			entityIndexer.createIndex();
		}
	}
	/*
	 * Assume both surface form file and entity weight file are sorted 
	 */
	public static void indexSurfaceForm(String inputfile, String outputfile, String entityweightfile, String propertyweightfile){
		SurfaceFormIndexer sfIndexer = new SurfaceFormIndexer(inputfile, outputfile);
		sfIndexer.setEntityWeightFile(entityweightfile);
		sfIndexer.setPropertyWeight(propertyweightfile);
		sfIndexer.createIndex();
	}
	public static void indexEntity(String inputfile, String outputfile, String surfaceIndex, String weightfile){
		EntityIndexer entityIndexer = new EntityIndexer(inputfile, outputfile, surfaceIndex);
		entityIndexer.setWeightFile(weightfile);
		entityIndexer.createIndex();
	}
	public static void updateEntityIndex(String inputfile, String entityIndex, String surfaceIndex, String propertyWeight, String entityWeight){
		SurfaceFormIndexUpdater sfUpdater = new SurfaceFormIndexUpdater(inputfile, surfaceIndex);
		sfUpdater.setEntityWeightFile(entityWeight);
		sfUpdater.setPropertyWeight(propertyWeight);
		sfUpdater.updateIndex();
		
		EntityIndexUpdater entityUpdater = new EntityIndexUpdater(inputfile, entityIndex, surfaceIndex);
		entityUpdater.setWeightFile(propertyWeight);
		entityUpdater.updateIndex();
	}
}
