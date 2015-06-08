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

package entity.search.preprocessing;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;


public class FormatConverter {
	static Model m;
	public static void main(String [] args){
		File folder = new File("Dataset/BioDataset");
		int count = 1;
	    for (final File fileEntry : folder.listFiles()) {   	
	    	String filename = fileEntry.getName();

	    	if(filename.endsWith("owl") || filename.endsWith("rdf")){
		    	System.out.println(count + " "+filename);
		    	count++;
	    		convert("Dataset/BioDataset/"+filename, "Dataset/BioDataset/NTriple/"+filename+".nt", "RDF/XML", "N-TRIPLE");
	    	}else if(filename.endsWith("ttl")){
		    	System.out.println(count + " "+filename);
		    	count++;
	    		convert("Dataset/BioDataset/"+filename, "Dataset/BioDataset/NTriple/"+filename+".nt", "TTL", "N-TRIPLE");	    		
	    	}
	    }
	}
	public static void convert(String inputfile, String outputfile, String inputFormat, String outputFormat){
        try{
        	FileInputStream fstream = new FileInputStream(inputfile);
    		m = ModelFactory.createDefaultModel();
    		m.read(fstream,"",inputFormat);
    		OutputStream output = new FileOutputStream(outputfile);
    		m.write(output,outputFormat);

        }catch (Exception e){
            System.err.println("Error: " + e.getMessage());
        }
	}
	public void listFilesForFolder(final File folder) {

	}

}
