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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntervalNode {

    private enum Direction { LEFT, RIGHT }

    private IntervalNode left = null;
    private IntervalNode right = null;
    private int point;
    private List<Intervalable> intervals = new ArrayList<Intervalable>();

    public IntervalNode(List<Intervalable> intervals) {
        this.point = determineMedian(intervals);

        List<Intervalable> toLeft = new ArrayList<Intervalable>();
        List<Intervalable> toRight = new ArrayList<Intervalable>();

        for (Intervalable interval : intervals) {
            if (interval.getEnd() < this.point) {
                toLeft.add(interval);
            } else if (interval.getStart() > this.point) {
                toRight.add(interval);
            } else {
                this.intervals.add(interval);
            }
        }

        if (toLeft.size() > 0) {
            this.left = new IntervalNode(toLeft);
        }
        if (toRight.size() > 0) {
            this.right = new IntervalNode(toRight);
        }
    }

    public int determineMedian(List<Intervalable> intervals) {
        int start = -1;
        int end = -1;
        for (Intervalable interval : intervals) {
            int currentStart = interval.getStart();
            int currentEnd = interval.getEnd();
            if (start == -1 || currentStart < start) {
                start = currentStart;
            }
            if (end == -1 || currentEnd > end) {
                end = currentEnd;
            }
        }
        return (start + end) / 2;
    }

    public List<Intervalable> findOverlaps(Intervalable interval) {

        List<Intervalable> overlaps = new ArrayList<Intervalable>();

        if (this.point < interval.getStart()) { // Tends to the right
            addToOverlaps(interval, overlaps, findOverlappingRanges(this.right, interval));
            addToOverlaps(interval, overlaps, checkForOverlapsToTheRight(interval));
        } else if (this.point > interval.getEnd()) { // Tends to the left
            addToOverlaps(interval, overlaps, findOverlappingRanges(this.left, interval));
            addToOverlaps(interval, overlaps, checkForOverlapsToTheLeft(interval));
        } else { // Somewhere in the middle
            addToOverlaps(interval, overlaps, this.intervals);
            addToOverlaps(interval, overlaps, findOverlappingRanges(this.left, interval));
            addToOverlaps(interval, overlaps, findOverlappingRanges(this.right, interval));
        }

        return overlaps;
    }

    protected void addToOverlaps(Intervalable interval, List<Intervalable> overlaps, List<Intervalable> newOverlaps) {
        for (Intervalable currentInterval : newOverlaps) {
            if (!currentInterval.equals(interval)) {
                overlaps.add(currentInterval);
            }
        }
    }

    protected List<Intervalable> checkForOverlapsToTheLeft(Intervalable interval) {
        return checkForOverlaps(interval, Direction.LEFT);
    }

    protected List<Intervalable> checkForOverlapsToTheRight(Intervalable interval) {
        return checkForOverlaps(interval, Direction.RIGHT);
    }

    protected List<Intervalable> checkForOverlaps(Intervalable interval, Direction direction) {

        List<Intervalable> overlaps = new ArrayList<Intervalable>();
        for (Intervalable currentInterval : this.intervals) {
            switch (direction) {
                case LEFT :
                    if (currentInterval.getStart() <= interval.getEnd()) {
                        overlaps.add(currentInterval);
                    }
                    break;
                case RIGHT :
                    if (currentInterval.getEnd() >= interval.getStart()) {
                        overlaps.add(currentInterval);
                    }
                    break;
            }
        }
        return overlaps;
    }


    protected List<Intervalable> findOverlappingRanges(IntervalNode node, Intervalable interval) {
        if (node != null) {
            return node.findOverlaps(interval);
        }
        return Collections.emptyList();
    }

}
