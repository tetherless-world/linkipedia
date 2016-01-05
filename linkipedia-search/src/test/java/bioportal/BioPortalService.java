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

/**
 * Created by xixiluo on 5/20/15.
 */

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import utils.ConceptItem;
import utils.ConceptMatcher;
import org.dataone.configuration.Settings;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.ontology.ConversionException;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import utils.QueryItem;

public class BioPortalService implements ConceptMatcher {

    private static Log log = LogFactory.getLog(BioPortalService.class);

    // for looking up concepts in BioPortal
    private String restUrl = null;
    private String apiKey = null;
    private String ontologies = null;

    public BioPortalService() {
        restUrl = Settings.getConfiguration().getString("annotator.matcher.bioportal.restUrl", "http://data.bioontology.org");
        apiKey = Settings.getConfiguration().getString("annotator.matcher.bioportal.apiKey", "24e4775e-54e0-11e0-9d7b-005056aa3316");
        ontologies = Settings.getConfiguration().getString("annotator.matcher.bioportal.ontologies", "D1-CARBON-FLUX,PROV-ONE,ENVO,CHEBI,DATA-CITE,DC-TERMS,OWL-TIME");

    }

    public List<ConceptItem> getConcepts(String text) throws Exception {
        List <ConceptItem> concepts = new ArrayList<ConceptItem>();
        List<Resource> resources = lookupAnnotationClasses(null, text, ontologies);
        int i = resources.size();
        for (Resource resource: resources) {
            double rank = i--/resources.size();
            ConceptItem concept = new ConceptItem(new URI(resource.getURI()), rank);
            concepts.add(concept);
        }
        return concepts;

    }

    public List<ConceptItem> getConcepts(QueryItem query) throws Exception {
        return null;
    }

    public List<ConceptItem> getConcepts(Map<String, String> queryItems) throws Exception {
        StringBuffer sb = new StringBuffer();
        for (String value: queryItems.values()) {
            sb.append(value);
            sb.append(" ");
        }
        return getConcepts(sb.toString());
    }

    /**
     * Look up possible concept from BioPortal annotation service.
     * @see "http://data.bioontology.org/documentation"
     * @param superClass
     * @param text
     * @return
     */
    private List<Resource> lookupAnnotationClasses(OntClass superClass, String text, String ontologies) {

        // no point calling the service
        if (text == null || text.length() == 0) {
            return null;
        }

        List<Resource> results = new ArrayList<Resource>();

        try {

            String urlParameters = "apikey=" + apiKey;
            urlParameters += "&format=xml";
            if (ontologies != null) {
                urlParameters += "&ontologies=" + ontologies;
            }
            urlParameters += "&text=" + URLEncoder.encode(text, "UTF-8");

            String url = restUrl + "/annotator?" + urlParameters ;
            URL restURL = new URL(url);
            InputStream is = restURL.openStream();
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            NodeList classNodeList = XPathAPI.selectNodeList(doc, "//annotation/annotatedClass/id");
            //NodeList classNodeList = XMLUtilities.getNodeListWithXPath(doc, "//annotation/annotatedClass/id");
            if (classNodeList != null && classNodeList.getLength() > 0) {
                for (int i = 0; i < classNodeList.getLength(); i++) {
                    String classURI = classNodeList.item(i).getFirstChild().getNodeValue();
                    log.info("annotator suggested: " + classURI);


                    if (superClass == null) {
                        // just add the suggestion to the list
                        results.add(ResourceFactory.createResource(classURI));
                    } else {
                        // check that it is a subclass of superClass
                        Resource subclass = superClass.getModel().getResource(classURI);
                        boolean isSubclass = false;
                        try {
                            isSubclass = superClass.hasSubClass(subclass);
                        } catch (ConversionException ce) {
                            log.warn("Skipping unknown subclass: " + classURI + " -- " + ce.getMessage() );
                            // try the next one
                            continue;
                        }
                        // now we can add this class
                        if (isSubclass) {
                            results.add(subclass);
                        }
                    }

                }

            }
        } catch (Exception e) {
            log.error("Could not lookup BioPortal annotation for text=" + text, e);
        }

        return results;
    }

}
