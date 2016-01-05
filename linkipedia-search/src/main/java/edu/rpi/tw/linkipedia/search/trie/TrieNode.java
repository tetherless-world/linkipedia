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
import java.util.HashSet;
import java.util.List;

public class TrieNode
{
   private TrieNode parent;
   private HashSet<TrieNode> children;
   private boolean isLeaf;     //Quick way to check if any children exist
   private boolean isWord;     //Does this node represent the last character of a word
   private String word;     //The character this node represents

   /**
    * Constructor for top level root node.
    */
   public TrieNode()
   {
      children = new HashSet<TrieNode>();
      isLeaf = true;
      isWord = false;
   }

   /**
    * Constructor for child node.
    */
//   public TrieNode(char character)
//   {
//      this();
//      this.character = character;
//   }
   
   /**
    * Adds a word to this node. This method is called recursively and
    * adds child nodes for each successive letter in the word, therefore
    * recursive calls will be made with partial words.
    * @param word the word to add
    */
   protected void addWord(ArrayList<String> words, int index)
   {
	   if(index > words.size())
		   return;
	   
	   this.word = words.get(index);
	   TrieNode child = new TrieNode();
	   child.addWord(words, index+1);
	   children.add(child);

   }
   
   /**
    * Returns the child TrieNode representing the given char,
    * or null if no node exists.
    * @param c
    * @return
    */
   protected TrieNode getNode(String childword)
   {
      for(TrieNode child:children){
    	  if(child.getWord().equals(childword))
    		  return child;
      }
      return null;
   }
   
   /**
    * Returns a List of String objects which are lower in the
    * hierarchy that this node.
    * @return
    */
   protected List getWords()
   {
      //Create a list to return
      List list = new ArrayList();
      
      //If this node represents a word, add it
      if (isWord)
      {
      list.add(toString());
      }
      
      //If any children
      if (!isLeaf)
      {
      //Add any words belonging to any children
      for (TrieNode label:children)
      {
            list.addAll(label.getWords());
     }

}

return list; 

}



/**

* Gets the String that this node represents.

* For example, if this node represents the character t, whose parent

* represents the charater a, whose parent represents the character

* c, then the String would be "cat".

* @return

*/

//	public String toString()
//	
//	{
//	
//		if (parent == null)
//		
//		{
//		
//		     return "";
//		
//		}
//	
//		else
//		
//		{
//		
//		     //return parent.toString() + new String(new char[] {character});
//		
//		}
//	
//	} 
	public ArrayList<String> getString(TrieNode root){
		ArrayList<String> phrases = new ArrayList<String>();
		for(TrieNode child:root.getChildren()){
			ArrayList<String> childphrases = getString(child);
			for(String childphrase:childphrases){
				phrases.add(word+" "+childphrase);
			}
		}
		return phrases;
	}
	public HashSet<TrieNode> getChildren(){
		return children;
	}
	public String getWord(){
		return word;
	}

}