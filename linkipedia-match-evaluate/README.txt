***** Information regarding the files in this folder *****

1. mergedOntologies.owl is the owl file created from the merged.nt. This is the "target" in the matching algorithm.

2. ntToOwl contains few lines of code that was used to convert from .nt to .owl using Jena.

3. getSourceOntology contains  the names of source files obtained from manual_annotations.txt. The script is used to take in each package_id, give it as input to the eml2owl.py and save the resulting owl file. While saving the file, I have replaced "/" that occur in the file name with "ZZZZ" because it was giving errors. Hence to get the actual package_id, replace "ZZZZ"  with "/".

4. sourceOntologies are the "source" for the matching algorithm. Y These were produced by the eml2owl.py (described in 3). You have to provide the path to this folder to the "source" variable.

5. Most of the paths that are given are absolute so you will have to change those accordingly.


----

PART 1: Matching the ontologies using AgreementMakerLight

1. Import the AgreementMakerLight project.

2. Run MyLinkipediaMatcher.java that can be found inside src/aml/. This java file requires two inputs: source ontology and target ontology.

2. Provide path to the folder that has source ontologies in the "getSource" method. The source ontologies are present in linkipedia-> linkipedia-match-evaluate->sourceOntologies  folder. 

3. Provide path to target ontology  in the "targetPath" variable. The target ontology is present in linkipedia->linkipedia-match-evaluate->mergedOntologies.owl file.

4. Provide path to the csv file where you will save the results in "printTheContent" method

Once this is done, all the matches found can be found in the csv file

-----

PART 2: Evaluating

1. Run the Evaluate.java file. This takes two inputs: a) manual annotation file b) csv file with the matches that have been found.

2. Manual annotation file can be obtained from  https://raw.githubusercontent.com/DataONEorg/semantic-query/master/lib/test_corpus_F_dev/manual_annotations.tsv.txt . Alternatively, it is also present inside the src/aml folder in AgreementMakerLight.

3. The csv file with matches is the same csv file that was obtained from PART 1.

When these files are provided and the program is run, it prints out the resulting precision, recall and fscore values.
