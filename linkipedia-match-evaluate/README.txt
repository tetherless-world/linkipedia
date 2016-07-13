1. mergedOntologies.owl is the owl file created from the merged.nt. This is the "target" in the matching algorithm.

2. ntToOwl contains few lines of code that was used to convert from .nt to .owl using Jena.

3. getSourceOntology contains  the names of source files obtained from manual_annotations.txt. The script is used to take in each package_id, give it as input to the eml2owl.py and save the resulting owl file. While saving the file, I have replaced "/" that occur in the file name with "ZZZZ" because it was giving errors. Hence to get the actual package_id, replace "ZZZZ"  with "/".\

4. sourceOntologies are the "source" for the matching algorithm. Y These were produced by the eml2owl.py (described in 3). You have to provide the path to this folder to the "source" variable.

5. Most of the paths that are given are absolute so you will have to change those accordingly.


----

For AgreementMakerLight

1. Run MyLinkipediaMatcher.java

2. Provide the path to sourceOntologies in the "getSource" method

3. Provide path to mergedOntologies.owl in the "targetPath" variable

4. Provide path to the csv file where you will save the results in "printTheContent" method
