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

package org.ahocorasick.interval;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class IntervalTree {

    private IntervalNode rootNode = null;

    public IntervalTree(List<Intervalable> intervals) {
        this.rootNode = new IntervalNode(intervals);
    }

    public List<Intervalable> removeOverlaps(List<Intervalable> intervals) {

        // Sort the intervals on size, then left-most position
        Collections.sort(intervals, new IntervalableComparatorBySize());

        Set<Intervalable> removeIntervals = new TreeSet<Intervalable>();

        for (Intervalable interval : intervals) {
            // If the interval was already removed, ignore it
            if (removeIntervals.contains(interval)) {
                continue;
            }

            // Remove all overallping intervals
            removeIntervals.addAll(findOverlaps(interval));
        }

        // Remove all intervals that were overlapping
        for (Intervalable removeInterval : removeIntervals) {
            intervals.remove(removeInterval);
        }

        // Sort the intervals, now on left-most position only
        Collections.sort(intervals, new IntervalableComparatorByPosition());

        return intervals;
    }

    public List<Intervalable> findOverlaps(Intervalable interval) {
        return rootNode.findOverlaps(interval);
    }

}
