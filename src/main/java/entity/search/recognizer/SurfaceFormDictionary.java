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

package entity.search.recognizer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

/**
 * a more sophisticated dictionary structured needed
 * @author zhengj3
 *
 */
public class SurfaceFormDictionary {
	
	HashSet<String> surface_form_dict;
	HashSet<String> partial_surface_form_dict;
	public SurfaceFormDictionary(String filename){
		surface_form_dict = new HashSet<String>();
		partial_surface_form_dict = new HashSet<String>();
		System.out.println("Start loading to memory...");
		initialize(filename);
		System.out.println("Finish loading!!!");
	}
	
	public boolean isCompleteSurfaceForm(String mention){			
		return surface_form_dict.contains(mention);
	}
	
	public boolean isPossibleSurfaceForm(String mention){
		return partial_surface_form_dict.contains(mention);
	}
	private void initialize(String filename){
		try{
			
			System.out.println("surface file: "+filename);
	        FileInputStream fstream = new FileInputStream(filename);
	        DataInputStream in = new DataInputStream(fstream);
	        BufferedReader weightReader = new BufferedReader(new InputStreamReader(in));
	        String line;
	        int count = 0;
	        while((line = weightReader.readLine())!=null){
	        	String [] spo = line.split(" ",3);
	        	if(spo.length < 3)
	        		continue;
	        	if(count % 10000 ==0)
	        		System.out.println(count+" "+line);
	        	surface_form_dict.add(spo[2]);
	        	fillPartial(spo[2]);
	        	count++;
	        }
	        in.close();
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private void fillPartial(String name){
		String [] name_part = name.split(" ");
		StringBuilder labelBuilder = new StringBuilder();
		for(String label_part:name_part){
			labelBuilder.append(label_part);
			partial_surface_form_dict.add(labelBuilder.toString());
			labelBuilder.append(" ");
		}
	}
	
}
