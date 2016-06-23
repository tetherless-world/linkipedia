# Linkipedia

Linkipedia is an entity extraction and linking service that you can set up yourself against a set of ontologies and other RDF datasets you choose.
It will use the interlinks available in the RDF to score the overall informativeness of each term and use the context of the text you submit to find the closest matches.

You can read more about Linkipedia in our paper:

> Jin G Zheng, Daniel Howsmon, Boliang Zhang, Juergen Hahn, Deborah McGuinness, James Hendler and Heng Ji [Entity linking for biomedical literature](http://www.biomedcentral.com/1472-6947/15/S1/S4/) _BMC Medical Informatics and Decision Making_ 2015, **15**(Suppl 1):S4  doi:10.1186/1472-6947-15-S1-S4

## Installing a development Linkipedia in Linux or OS X

Linkipedia uses Maven for build management. Please install Maven 3 and a JDK of 1.7 or newer.

```bash
> sudo apt-get install maven

> sudo apt-get install default-jdk
```

After doing so, install the raptor utilities for RDF manipulation:

```bash
# OS X:
> brew install raptor

# Debian/Ubuntu/Mint:
> sudo apt-get install raptor2-utils
```

Next, check out this repository and cd into it, and build it:

```bash
> git clone https://github.com/tetherless-world/linkipedia.git
> cd linkipedia
> mvn package install
```

To load ontologies, they need to be stored in an index directory that contains a subdirectory called `NTriple`.
It's easiest to write a script that does the conversion work for you.
There's an example in `linkipedia/dataone/dataone-ontologies.py`. Run that script to download the ontologies.
```bash
> cd dataone
> python dataone-ontologies.py 
```
The system might prompt that certain modules are not present; install the required packages.

```bash
# for jupyter
> pip install --upgrade jupyter

# for pandas
> sudo apt-get install python-pandas

# for numpy
> sudo apt-get install python-numpy

# for nltk
> sudo pip install -U nltk
```

Then run the following to index them:

```bash
> cd ..
> cd linkipedia-search
> ./dataprocess.sh [path to index dir]
```

When that is done, you are almost ready to run the web service. First, from the main linkipedia directory, do:

```bash
> cd linkipedia-web
```

From there, edit the pom.xml file so that the systemProperty tag towards the end of the file points to the index you just created.
Make sure to use the subdirectory `knowledge_index` in it:

```xml
                <systemProperty>
                    <name>linkipedia.index</name>
                    <value>[path to index dir]/knowledge_index</value>
                </systemProperty>
```

Finally, run the service in developer mode using maven:

```bash
> mvn jetty:run
```

## Production Deployment

To deploy the service, set up a Jetty or Tomcat application container and copy `linkipedia-web/target/linkipedia-web.war` to the webapps directory.
Set the `linkipedia.index` system property on the container to point to your generated knowledge index as in the build instructions.

## Using the Linkipedia service

There are several endpoints one can use when using Linkipedia, but the most advanced one is the `annotate` endpoint.
When running Linkipedia in maven, the endpoint will be `http://localhost:8080/annotate/annotate/`.
This endpoint takes the following parameters:

* _query_: the query text to extract from.
* _numResults_: the number of results for each mention to return. Defaults to 1.
* _minScore_: the minimum score to return. Defaults to 5.
* _context_: the context to use when a query does not contain enough text itself. If the query is a noun phrase, it is valuable for this to be the sentence the noun phrase was extracted from.

The `annotate` endpoint performs the following operations:

1. Segment the query into sentences.
2. "Shingle" the sentences into word tuples from length 1 to 5. It uses the Lucene [ShingleFilter](https://lucene.apache.org/core/4_3_0/analyzers-common/org/apache/lucene/analysis/shingle/ShingleFilter.html) to do this.
3. For each word shingle, query the index to find matches using either the sentence or the provided context.
4. Add the matches based on the parameter requirements.
