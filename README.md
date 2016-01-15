# Linkipedia

Linkipedia is an entity extraction and linking service that you can set up yourself against a set of ontologies and other RDF datasets you choose.
It will use the interlinks available in the RDF to score the overall informativeness of each term and use the context of the text you submit to find the closest matches.

## Installing a development Linkipedia in Linux or OS X

Linkipedia uses Maven for build management. Please install Maven 3 and a JDK of 1.7 or newer.
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
There's an example in `linkipedia/dataone/dataone-ontologies.sh`.
Run that script to download the ontologies, then run the following to index them:

```bash
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
