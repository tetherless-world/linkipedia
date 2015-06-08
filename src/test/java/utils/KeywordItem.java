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

package utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xixiluo on 1/12/15.
 */
public class KeywordItem {
    protected String keyword;
    protected List<String> types;

    public KeywordItem() {}

    public KeywordItem(String keyword) {
        this.keyword = keyword;
        types = new ArrayList<String>();
    }
    public KeywordItem(String keyword, List<String> types){
        this.keyword = keyword;
        this.types = types;
    }

    public void setKeyword(String keyword){
        this.keyword = keyword;
    }

    public String getKeyword(){
        return keyword;
    }

    public void setTypes(List<String> types){
        this.types = types;
    }

    public List<String> getTypes(){
        return types;
    }

    public void addType(String type){
        if(!types.contains(type)){
            types.add(type);
        }
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(keyword);

        boolean first = true;
        for(String type:types){
            if(first){
                sb.append("(");
                first = false;
            }else{
                sb.append(";");
            }

            sb.append(type);
        }

        if(!types.isEmpty()) sb.append(")");

        return sb.toString();
    }
}
