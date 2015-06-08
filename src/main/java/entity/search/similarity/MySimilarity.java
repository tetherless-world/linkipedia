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

import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.util.BytesRef;


public class MySimilarity extends DefaultSimilarity{
	
	@Override
	public float lengthNorm(FieldInvertState state){
		return 1;
	}
	
	@Override
	public float scorePayload(int doc, int start, int end, BytesRef payload){
		if (payload != null){
			return PayloadHelper.decodeFloat(payload.bytes, payload.offset);
		}else
			return 1.0F;
		
//		return 1;
	}
	
	@Override
	public float idf(long docFreq, long numDocs){
		//return super.idf(docFreq, numDocs);
		return 1;
	}
	
	@Override
	public float tf(float freq){
		//return super.tf(freq);
		return 1;
	}
	
	@Override
	public float coord(int overlap, int maxOverlap) {
		//System.out.println("coord: "+super.coord(overlap,maxOverlap));		
	    return super.coord(overlap, maxOverlap);
		//return 1;
	}
	
	@Override
	public float queryNorm(float sumOfSquaredWeights){
		//return super.queryNorm(sumOfSquaredWeights);
		return 1;
	}
	

	
	@Override
	public float sloppyFreq(int distance) {
		//System.out.println("super sloppy: "+super.sloppyFreq(distance));
	    //return super.sloppyFreq(distance);
		return 1;
	}
	

}
