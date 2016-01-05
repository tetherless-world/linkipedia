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

package edu.rpi.tw.linkipedia.search.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import edu.rpi.tw.linkipedia.search.utils.Annotation;

public class LRUCache {
	private HashMap<String, DoubleLinkedListNode> map 
		= new HashMap<String, DoubleLinkedListNode>();
	private DoubleLinkedListNode head;
	private DoubleLinkedListNode end;
	private int capacity;
	private int len;
 
	
	//private HashMap<String, ArrayList<String>> context_map = new HashMap<String, ArrayList<String>>();
	
	public LRUCache(int capacity) {
		this.capacity = capacity;
		len = 0;
	}
 
	public ArrayList<Annotation> get(String key) {
		if (map.containsKey(key)) {
			DoubleLinkedListNode latest = map.get(key);
			removeNode(latest);
			setHead(latest);
			//context_map.put(key, new ArrayList(Arrays.asList(context)));
			return latest.val;
		} else {
			return null;
		}
	}
 
	public void removeNode(DoubleLinkedListNode node) {
		DoubleLinkedListNode cur = node;
		DoubleLinkedListNode pre = cur.pre;
		DoubleLinkedListNode post = cur.next;
 
		if (pre != null) {
			pre.next = post;
		} else {
			head = post;
		}
 
		if (post != null) {
			post.pre = pre;
		} else {
			end = pre;
		}
	}
 
	public void setHead(DoubleLinkedListNode node) {
		node.next = head;
		node.pre = null;
		if (head != null) {
			head.pre = node;
		}
 
		head = node;
		if (end == null) {
			end = node;
		}
	}
 
	public void set(String key, ArrayList<Annotation> value) {
		if (map.containsKey(key)) {
			DoubleLinkedListNode oldNode = map.get(key);
			oldNode.val = value;
			removeNode(oldNode);
			setHead(oldNode);
		} else {
			DoubleLinkedListNode newNode = 
				new DoubleLinkedListNode(key, value);
			if (len < capacity) {
				setHead(newNode);
				map.put(key, newNode);
				len++;
			} else {
				map.remove(end.key);
				end = end.pre;
				if (end != null) {
					end.next = null;
				}
 
				setHead(newNode);
				map.put(key, newNode);
			}
		}
	}
}
 
class DoubleLinkedListNode {
	public ArrayList<Annotation> val;
	public String key;
	public DoubleLinkedListNode pre;
	public DoubleLinkedListNode next;
 
	public DoubleLinkedListNode(String key, ArrayList<Annotation> value) {
		val = value;
		this.key = key;
	}
}