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

package entity.search.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import entity.search.recognizer.DictionaryRecognizer;
import entity.search.recognizer.EntityRecognizer;


public class EntityRecognizerTest {

	public static void main(String [] args){
		String text = "the 48 contiguous states and washington, d.c. are in central north america between canada and mexico.";
		String index = "D:/Workspace/db_surface_form_base";
		//String index = "Dataset/db_surface_form_base";
		float threshold = 1;
		if(args.length >= 1){
			index = args[0];
		}
		
		if(args.length == 2){
			threshold = Float.parseFloat(args[1]);
		}
		
		try{
			InputStreamReader inReader = new InputStreamReader(System.in, "UTF-8");
		    BufferedReader in = new BufferedReader(inReader);
		    DictionaryRecognizer eg = new DictionaryRecognizer(index);
		    //EntityRecognizer eg = new EntityRecognizer(index);
			//eg.setThreshold(threshold);
		while(true){
			
			String query = "";
			query = in.readLine();
		    if(query.equals("quit"))
		    	break;
		    //query = query.toLowerCase();
			long start = System.currentTimeMillis();
			ArrayList<String> mentions = eg.getEntityMentions(query);//eg.getEntityMentions(query);
			long end = System.currentTimeMillis();
			for(String m:mentions){
				System.out.println("mentions: "+m);
			}		
			System.out.println("total time: "+(end-start));
		}
		inReader.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
}
