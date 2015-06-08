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

package entity.search.similarity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import entity.search.utils.Utils;

public class JaccardSimilarity{
	int highlevel = 2;
//	ArrayList<String> stopWords ;
//	ArrayList<String> garbagePattern;
	
	public JaccardSimilarity(){
//		stopWords = new ArrayList<String>(Arrays.asList((
////				"able," +
////				"about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because," +
////				"been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from," +
////				"get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just," +
////				"least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on," +
////				"only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the," +
////				"their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what," +
////				"when,where,which,while,who,whom,why,will,with,would,yet,you,your," +
//				"a,b,c,d,e,g,h,i,j,k,l,n,o,p,q,r,s,t,u,v,w,x,y,z,has").split(",")));
//		garbagePattern = new ArrayList<String>(Arrays.asList(("aa,bb,cc,dd,ee,ff,gg,hh,ii,jj,kk,oo,pp,qq,rr,uu,vv,ww," +
//				"xx,yy,zz").split(",")));
		
	}
	//assume the string passed in only contains number space and letter, and is ready for comparison.
	public static double computeSimilarity(String concept1,  String concept2) {
		if(concept1.equals(concept2))
			return 1;
		
		//System.out.println(concept2);
		concept1 = Utils.removeSingleLetter(concept1);
		concept2 = Utils.removeSingleLetter(concept2);
		
		//System.out.println(concept1+" "+concept2);
		
		
		String [] org_words1= concept1.trim().split("\\s+");
		String [] org_words2= concept2.trim().split("\\s+");
		
		double commons = 0 ; 
		for(int i = 0 ; i < org_words1.length; i ++){
			for(int j =0 ; j < org_words2.length; j++){
				if((org_words1[i]).equals(org_words2[j])){
					org_words2[j] = " ";
					commons= commons+2;
					break;
				}
			}

		}
		return commons/((double)(org_words1.length+org_words2.length));

	}



}
