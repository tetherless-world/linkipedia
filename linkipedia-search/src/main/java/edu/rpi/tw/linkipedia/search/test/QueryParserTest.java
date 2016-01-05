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

package edu.rpi.tw.linkipedia.search.test;

import org.apache.lucene.queryparser.classic.ParseException;

import edu.rpi.tw.linkipedia.search.query.WeightedQuery;

public class QueryParserTest {
	
	public static void main(String [] args) throws ParseException{
		WeightedQuery wquery = new WeightedQuery();
		String label = "un(organization;agent;political unit)|10";
		String [] contexts = {
				"us(country)|10",
				"biology|100","biology|100","biology|100","biology|100","biology|100",
				"un(organization;agent;political unit)|10",
		};
		wquery.parse(label, contexts);
		System.out.println(wquery);
	}

}
