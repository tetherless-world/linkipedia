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

package edu.rpi.tw.linkipedia.search.trie;

import java.util.ArrayList;
import java.util.List;

public class Trie
{
	
   private TrieNode root;
   
   /**
    * Constructor
    */
   public Trie()
   {
      root = new TrieNode();
   }
   
   /**
    * Adds a word to the Trie
    * @param word
    */
   public void addWord(ArrayList<String> words)
   {
      root.addWord(words,0);
   }
   
   public boolean checkPhrase(String phrase){
	   return false;
   }
   
   /**
    * Get the words in the Trie with the given
    * prefix
    * @param prefix
    * @return a List containing String objects containing the words in
    *         the Trie with the given prefix.
    */
//   public List getWords(String prefix)
//   {
//	   
//      //Find the node which represents the last letter of the prefix
//      TrieNode lastNode = root;
//      for (int i=0; i<prefix.length(); i++)
//      {
//      lastNode = lastNode.getNode(prefix.charAt(i));
//      
//      //If no node matches, then no words exist, return empty list
//      if (lastNode == null) return new ArrayList();      
//      }
//      
//      //Return the words which eminate from the last node
//      return lastNode.getWords();
//      
//   }
}