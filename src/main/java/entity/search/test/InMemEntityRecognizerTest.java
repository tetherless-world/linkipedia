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

import java.util.ArrayList;

import entity.search.recognizer.InMemEntityRecognizer;

public class InMemEntityRecognizerTest {
	
	public static void main(String [] args){
		
		String text = "The fate of Lehman Brothers, the beleaguered investment bank, hung in the balance on Sunday as Federal Reserve officials and the leaders of major financial institutions continued to gather in emergency meetings trying to complete a plan to rescue the stricken bank.";
		
		InMemEntityRecognizer rec = new InMemEntityRecognizer("Dataset/dbpedia_wikipedia_final_label.nt");
		
		ArrayList<String> mentions = rec.getEntityMentions(text);
		
		for(int i = 0; i < mentions.size(); i++){
			System.out.println(mentions.get(i));
		}
		
	}

}
