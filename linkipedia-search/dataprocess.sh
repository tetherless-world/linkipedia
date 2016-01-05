#!/bin/bash

INDEX=$1

LINKIPEDIA_JAR=$PWD/target/linkipedia-search-1.0-SNAPSHOT-jar-with-dependencies.jar

JAVA_OPTS='-Xms2560m -Xmx5120m'

LINKIPEDIA_CMD="java $JAVA_OPTS -jar $LINKIPEDIA_JAR"

echo "Go to NTriple folder"
pushd $INDEX/NTriple/
echo "cat  files to merged"
rm merged.nt
cat *.nt >> merged.nt
echo "sort  by subject"
sort -k 1 merged.nt > merged_sort1
echo "copy  to the directory with linkipedia"
cp merged_sort1 ../
echo "Go to the directory with linkipedia"
pushd ../
chmod 752 merged_sort1
chmod 754 merged_sort1
echo "getLabelsFromURL"

#$LINKIPEDIA_CMD getLabelsFromURL merged_sort1 merged_sort1_v1
#echo "Delete period “.” at the end of lines"
#sed -rne 's/(.+)[[:blank:]]+\.$/\1/gip' merged_sort1_v3_t3 > merged_sort1_v3_t4
sed  's/\.$//g' merged_sort1 > merged_sort1_v2
echo "addInverseProperty"
$LINKIPEDIA_CMD addInverseProperty merged_sort1_v2 merged_sort1_v30
echo "removeChars"
echo "===================================="
#java -jar Linkipedia.jar removeChars merged_sort1_v2   merged_sort1_v3
echo "delete sign ^^ and the content after"
sed "s/\\^\\^.*//g" merged_sort1_v30 >merged_sort1_v3_t1
echo "Delete @ and the content after"
sed "s/@.*//g" merged_sort1_v3_t1 > merged_sort1_v3_t2
echo "Delete double quote"
sed "s/\"//g" merged_sort1_v3_t2 > merged_sort1_v3_t3
echo "Delete period “.” at the end of lines"
#sed -rne 's/(.+)[[:blank:]]+\.$/\1/gip' merged_sort1_v3_t3 > merged_sort1_v3_t4
sed  's/\.$//g' merged_sort1_v3_t3 > merged_sort1_v3_t4
echo "Delete single quote"
sed "s/\'//g" merged_sort1_v3_t4 > merged_sort1_v3
#echo "Something related to http"
#sed "s/\((http)@<!:(.*)@=)&(:([^>]*$)@=)//g" merged_sort1_v3_t5 > merged_sort1_v3
echo "===================================="

echo "sort file by property"
LC_ALL='C' sort -k 2 merged_sort1_v3> merged_sort2
echo "Compute property entropy"
$LINKIPEDIA_CMD computePropertyEntropy merged_sort2 property_entropy_nan
sed 's/NaN$/1.0/' property_entropy_nan > property_entropy
echo "sort  file by object"
LC_ALL='C' sort -k 3 merged_sort1_v3 > merged_sort3
echo "Compute entity entropy"
$LINKIPEDIA_CMD computeEntityEntropyWeights merged_sort3 entity_entropy property_entropy
echo "grep surface form file"
grep -iE " <((http://www.geneontology.org/formats/oboInOwl#hasRelatedSynonym)|(http://www.geneontology.org/formats/oboInOwl#hasExactSynonym)|(http://www.geneontology.org/formats/oboInOwl#hasExactSynonym)|(http://www.w3.org/2000/01/rdf-schema#label)|(http://www.w3.org/2004/02/skos/core#altLabel)|(http://www.w3.org/2004/02/skos/core#prefLabel)|(http://tool.eal.org/urlName)|(http://purl.org/dc/elements/1.1/title)|(http://purl.org/dc/terms/title))> " merged_sort1_v3>surface_form_file
echo "Calculate surface form index"
$LINKIPEDIA_CMD indexSurfaceForm  surface_form_file surface_form_index entity_entropy property_entropy > surface_index_log
echo "Calculate knowledge index"
$LINKIPEDIA_CMD indexEntity merged_sort1_v3 knowledge_index surface_form_index property_entropy > knowledge_index_log
#echo "Start servlet"
#java -jar Linkipedia.jar startServlet knowledge_index surface_form_index 9100

popd

popd


