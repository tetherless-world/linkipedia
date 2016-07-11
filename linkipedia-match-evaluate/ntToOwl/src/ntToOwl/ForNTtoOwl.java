package ntToOwl;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

public class ForNTtoOwl {

	public static void main(String[] args)
	{
		//Log.setLog4j("log4j.properties");
		Model m=ModelFactory.createDefaultModel();
		//m.read("C:/Users/prakash/Desktop/sabitaInternship/owlFiles/merged.nt", "NTRIPLES");
		//String NS="http://example.com/test/";
		RDFDataMgr.read(m, "C:/Users/prakash/Desktop/sabitaInternship/owlFiles/merged.nt") ;
		//Resource r=m.createResource(NS+"r");
		//Property p=m.createProperty(NS+"p");
		DataOutputStream stream;
		try {
			stream = new DataOutputStream(new FileOutputStream("C:/Users/prakash/Desktop/sabitaInternship/owlFiles/mergedSabita.owl"));
			RDFDataMgr.write(stream, m, RDFFormat.RDFXML_ABBREV) ;
			stream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//r.addProperty(p,"hello world",XSDDatatype.XSDstring);
 catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//m.write("C:/Users/prakash/Desktop/sabitaInternship/owlFiles/mergedSabita.owl","RDFXML");
		
		
		
	}
}
