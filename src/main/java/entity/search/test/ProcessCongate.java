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
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class ProcessCongate {
	
	public static void main(String [] args){
		
		try{

			  //String queryFileName = "Dataset/EL2014TRAINING/MyQuery/query.csv";
			  String queryFileName = "Cognate.txt";
			  FileInputStream fstream = new FileInputStream(queryFileName);

			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  FileWriter ofstream = new FileWriter("NCOUNTRY");
			  BufferedWriter out = new BufferedWriter(ofstream);
			  
			  while ((strLine = br.readLine()) != null)   {
				  String [] parts = strLine.split("\\|");
				  for(String part:parts){
					  out.write(part+"\n");
				  }
			  }
			  in.close();
			  out.close();
			  
			 }catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
			}
		
	}
	

}
