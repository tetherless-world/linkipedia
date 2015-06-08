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

package bioportal;

import esor.EsorService;
import utils.ConceptItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xixiluo on 5/20/15.
 */
public class BioPoralClient {
    public static void main(String[] args) throws Exception {
        BioPortalService bioS = new BioPortalService();

        EsorService esorS = new EsorService();

        String term = "Chlorophyll%20c3";

        List<ConceptItem> res_esor = esorS.getConcepts(term);

        List<ConceptItem> res_bio = bioS.getConcepts(term);


        /*for (int i = 0; i < res_bio.size(); i++) {
            ConceptItem c = res_bio.get(i);
            System.out.println(c.getUri());
            System.out.println(c.getWeight());
        }

        System.out.println("---------");*/

        for (int i = 0; i < res_esor.size(); i++) {
            ConceptItem c = res_esor.get(i);
            System.out.println(c.getUri());
            System.out.println(c.getWeight());
        }

    }
}
