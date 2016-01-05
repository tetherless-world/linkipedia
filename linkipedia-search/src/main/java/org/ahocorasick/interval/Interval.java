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

public class Interval implements Intervalable {

    private int start;
    private int end;

    public Interval(final int start, final int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return this.start;
    }

    public int getEnd() {
        return this.end;
    }

    public int size() {
        return end - start + 1;
    }

    public boolean overlapsWith(Interval other) {
        return this.start <= other.getEnd() &&
               this.end >= other.getStart();
    }

    public boolean overlapsWith(int point) {
        return this.start <= point && point <= this.end;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Intervalable)) {
            return false;
        }
        Intervalable other = (Intervalable)o;
        return this.start == other.getStart() &&
               this.end == other.getEnd();
    }

    @Override
    public int hashCode() {
        return this.start % 100 + this.end % 100;
    }

    public int compareTo(Object o) {
        if (!(o instanceof Intervalable)) {
            return -1;
        }
        Intervalable other = (Intervalable)o;
        int comparison = this.start - other.getStart();
        return comparison != 0 ? comparison : this.end - other.getEnd();
    }

    @Override
    public String toString() {
        return this.start + ":" + this.end;
    }

}
