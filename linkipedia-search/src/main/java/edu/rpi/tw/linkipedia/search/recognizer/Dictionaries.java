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

package edu.rpi.tw.linkipedia.search.recognizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import ds.tree.*;

/**
 * this dictionary reads dicts from data resource, 
 * and we can use it to query phrases
 * @author che
 *
 */
public class Dictionaries 
{	
	static public Dictionaries singleton_dictionaries;
	
//	static public Dictionaries getSingleton()
//	{
//		if(singleton_dictionaries == null)
//		{
//			try 
//			{
//				singleton_dictionaries = new Dictionaries();
//			} 
//			catch (FileNotFoundException e) 
//			{
//				e.printStackTrace();
//			} 
//			catch (IOException e) 
//			{
//				e.printStackTrace();
//			}
//		}
//		return singleton_dictionaries;
//	}
		
	// make a dictionary data structure
	// key is "phrase/word" to be searched, value is type
	private Map<String, RadixTree<String>> dictionary;
	
	public Dictionaries(String dic_path) throws FileNotFoundException, IOException
	{
		// get properties
		File dict_file;
		dict_file = new File(dic_path);
		
		setDictionary(new HashMap<String, RadixTree<String>>());
		initializeDict(dict_file);
	}

	/**
	 * get the dictionary path from default property file 
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
//	protected File getDictPath() throws FileNotFoundException, IOException {
//		File dict_file;
//		String currentPath = System.getProperty(MyProperties.NER_HOME);
//		String dict_path = MyProperties.getSingleton().getDictionaryPath();
//		dict_file = new File(currentPath + File.separator + dict_path);
//		return dict_file;
//	}

	/**
	 * read the dictionary to memory data structure
	 * @param dictFile
	 */
	protected void initializeDict(File dictFile) 
	{
		System.out.println("loading dicts ... ");
		File[] files = dictFile.listFiles();
		
		Stack<File> children = new Stack<File>();
		children.addAll(Arrays.asList(files));
		
		while(!children.empty())
		{
			File child = children.pop();
			if(child.isFile() && !child.isHidden())
			{
				String file_name = child.getName();
				String dict_name = file_name;
				System.out.println("file: "+dict_name);
				if(file_name.indexOf('.') > 0)
				{
					dict_name = file_name.substring(0, file_name.indexOf('.'));
				}
				
				// insert the words in this file into dictionary as key, and file name as value
				try 
				{
					RadixTree<String> tree = new RadixTreeImpl<String>();
					this.insertFile(tree, child, dict_name);
					this.getDictionary().put(dict_name, tree);
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
			else
			{
				File[] sub_files = child.listFiles();
				children.addAll(Arrays.asList(sub_files));
			}
		}
	}
	
	/**
	 * get Radix tree from document in which each line corresponds to one entry 
	 * @param child
	 * @return
	 * @throws IOException 
	 */
	protected static RadixTree<String> insertFile(RadixTree<String> tree, File child, String dict_name) throws IOException 
	{
		BufferedReader reader = new BufferedReader(new FileReader(child));
		String line = "";
		while((line = reader.readLine()) != null)
		{
			// convert tokens into lowercase
			line = line.trim().toLowerCase();
			if(line.equals(""))
			{
				continue;
			}
			if(!tree.contains(line))
			{
//				if(dict_name.equals("surface_dict_uniq")){
//					
//					System.out.println(line);
//				}else
				//System.out.println(line);
				tree.insert(line, dict_name);
			}
		}
		reader.close();
		return tree;
	}

	// indicats that a token is a valid prefix in this dictionary
	static public final String HAS_PREFIX = "HAS_PREFIX"; 
	
	/**
	 * search the type of given token
	 * @param token
	 * @return return type if dictionary contains it, otherwise, 
	 * if it's a valid prefix, return HAS_PREFIX, otherwise, return null  
	 */
	public String searchToken(String key, String token)
	{
		//System.out.println(key+" "+token);
		RadixTree<String> tree = this.dictionary.get(key);
		if(tree == null)
		{
			
			return null;
		}
		//System.out.println("found tree");
		token = token.toLowerCase();
		//System.out.println("searching "+token);
		String type = tree.find(token);
		
		return type;
	}

	private String searchPrefix(String key, String token){
		RadixTree<String> tree = this.dictionary.get(key);
		ArrayList<String> keys_has_prefix = tree.searchPrefix(token, 1);
			//System.out.println("found: "+keys_has_prefix.size());
		String type = null;
		if(keys_has_prefix.size() > 0)
		{
			type = "HAS_PREFIX";
		}
		return type;
	}
	void setDictionary(Map<String, RadixTree<String>> dictionary) 
	{
		this.dictionary = dictionary;
	}

	public Map<String, RadixTree<String>> getDictionary() 
	{
		return dictionary;
	}
	public String search(String query){
		query = query.toLowerCase();
		Set<String> types = dictionary.keySet();
		for(String type:types){
			String myType = searchToken(type,query);
			if(myType!=null){
				return myType;
			}
		}
		return searchPrefix(query);
	}
	public String searchPrefix(String query){
		query = query.toLowerCase();
		Set<String> types = dictionary.keySet();
		for(String type:types){
			String myType = searchPrefix(type,query);
			if(myType!=null){
				return myType;
			}
		}
		return null;
	}
	public boolean hasTerm(String query){
		query = query.toLowerCase();
		Set<String> types = dictionary.keySet();
		for(String type:types){
			String myType = searchToken(type,query);
			if(myType!=null){
//				System.out.println("valid: "+query+" "+myType);
				return true;
			}
		}
//		System.out.println("not valid: "+query);
		return false;
	}
	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		String dict_path = "Dataset/Dictionary/GPO";
		Dictionaries dicts = new Dictionaries(dict_path);
		//String type = dicts.searchToken("COUNTRY", "Janpanese");
		String type = dicts.search("new");
//		System.out.println(type);
	}

}
