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

package test.components.entity.search.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.rpi.tw.linkipedia.search.recognizer.EntityRecognizer;
import test.components.entity.search.ParallelEntityRecognizer;

public class ParallelEntityRecognizerTest {
	public static void main(String [] args) throws InterruptedException{
				
		String index = "Dataset/db_surface_form_base";
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
			//EntityRecognizer 
		    EntityRecognizer eg = new EntityRecognizer(index);
			eg.setThreshold(threshold);
			ParallelEntityRecognizer.setEntityRecognizer(eg);
		while(true){
			//ParallelEntityRecognizer.reset();
			String query = "";
			query = in.readLine();
		    if(query.equals("quit"))
		    	break;

		    long start = System.currentTimeMillis();
			
			ArrayList<String> sentences = eg.getSentences(query);
			//ParallelEntityRecognizer.setSentences(sentences);

			int maxThreadNum = Runtime.getRuntime().availableProcessors();
			if(sentences.size() < maxThreadNum)
				maxThreadNum = sentences.size();
			
			
			ParallelEntityRecognizer [] peg = new ParallelEntityRecognizer[maxThreadNum];
			System.out.println("Num Threads: "+maxThreadNum);
			/*
			for(int i = 0 ; i < maxThreadNum; i++){
				peg[i] = new ParallelEntityRecognizer();//(sentences.get(senIndex),eg);
				peg[i].start();
			}
			*/
			//ParallelEntityRecognizer pthread2 = new ParallelEntityRecognizer();//(sentences.get(senIndex),eg);

			//pthread1.start();
			//pthread2.start();
			for(int i = 0 ; i < maxThreadNum; i++){
				//peg[i] = new ParallelEntityRecognizer();//(sentences.get(senIndex),eg);
				peg[i].join();
			}
			//pthread1.join();
			//pthread2.join();

			long end = System.currentTimeMillis();

			ArrayList<String> global_mentions = new ArrayList<String>();//ParallelEntityRecognizer.getAllMentions();
			for(String m:global_mentions){
				System.out.println("mentions: "+m);
			}		
			System.out.println("total time: "+(end-start));
		}
		inReader.close();
		} catch (IOException e1) {

			e1.printStackTrace();
		}

		


	}
}
