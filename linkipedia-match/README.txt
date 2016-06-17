                                              Ontology Matching for Linkipedia

Input: A source and a target ontology (as OWL).
Output: A set of mappings and their similarity scores.

Languages/Tools/APIs: Java, OWL API, WordNet.

Implemented Using: AgreementMakerLight (AML).

System Requirements:  Java 1.7 or higher, Eclipse Mars.

AML for Linkipedia - Installation Steps:
1. Download AML for Linkipedia from GitHub.
(linkipedia/linkipedia-match/matchers-aml/)
2. Unzip the folder to your local drive.
3. Open Eclipse, and follow the below:
File → Import → General → Existing Projects to Workspace → Next → Select root directory → Browse for the path where you saved AML for Linkipedia → Finish.
4. Open MyLinkipediaMatcher.java from the aml package. This contains the main() method for Ontology Matching for Linkipedia.
5. Change the sourcePath and targetPath to the point to the path where the source and target ontologies respectively are saved.
6. Comment/Uncomment the java code for whichever matcher you would like to run.

Terminologies:
Mapping: Correspondence between a source and a target node. Also known as alignment or mapping.
Concept: Classes in an ontology.
Ancestor(s): Super-class(es) of a given concept.
Descendent(s): Sub-class(es) of a given concept.
Similarity score: A metric that quantifies how similar a concept from the source ontology and a concept from the target ontology are.

String-based Ontology Matching Techniques:
1. Distance-based Algorithm: The label(or comment) of the source and target concepts are identified. Pre-processing is done on the labels of each node. 
  Pre-processing: Pre-processing includes removal of non-character items like numbers and punctuations. In this process, the non-content words such as “has”, “is”, etc. are also removed. If the label(or comment) is a combination of two or more words and they are separated by a hyphen or an underscore, the separators are removed and are replaced with a single space. They are also separated by capitalized words. A trimmed label is returned as a result of this step.
  After pre-processing is done, we consider the label of the source node and the label of the target node and find the relevance between the two labels. Relevance can be considered as the minimum number of operations that are required to transform one string into another. Once this is identified, based on the relevance of the two nodes, we set the similarity measure for that mapping.  Add mapping and corresponding similarity values to the initial list of mappings.

Algorithm splitNames(sName, tName):
	Input: Name of the source class/property sName, name of the target class/property tName.
  Output: Trimmed sName and tName.
sim ← 0
stopwords[] ← {"the","has","an","a","by","is","was","then","of","have","had","were","are","for"}
for i ← 0 to stopwords.length - 1 do
	if sName contains stopwords[i] then
		remove stopwords[i] from sName
	if tName contains stopwords[i] then
		remove stopwords[i] from tName
sName ← sName after removal of non-alphabet characters 
tName ← tName after removal of non-alphabet characters 
sSplits ← getSplitParts(sName)
tSplits ← getSplitParts(tName)
for t1 in tSplits do
	for s1 in sSplits do
		sim ← strSimilarity(s1, t1)
		if sim is lesser than 0.5 then
			sim ← wpDistance(s1, t1)
		if sim is greater than 0.5 then
			print s1, t1, sim

Algorithm getSplitParts(stName):
  Input: Name of the source/target stName
  Output: splitted parts of stName
stparts ← []
stParts[] ← split stName by “_”
stParts[] ← split stName by “-”
stParts[] ← split stName by upper case letters
return stParts[]

Algorithm strSimilarity(str1, str2):
	Input: Source string str1, Target string str2
  Output: string similarity
longerStr ← str1
shorterStr ← str2
if length of str1 is lesser than the length of str2 then
	longerStr ← str2
shorterStr ← str1
longerLength ← length of longerStr
if longerLength is equal to 0 then
	return 1.0
return (longerLength - LevEditDistance(longerStr, shorterStr)) / longerLength

Algorithm LevEditDistance(str1, str2):
	Input: Source string str1, Target string str2
  Output: Distance calculated using Levenshtein’s Edit Distance metric
for i ← 0 to length of str1 do
	oldvalue ← i
	for j ← 0 to length of str2 do
		if i equal to 0 then
			newCost[j] ← j
		else
			if j greater than 0 then
				newValue[j] ← newCost[j-1]
				if str1.charAt(i - 1) is not equal to str2.charAt(j - 1) then
					newValue ← min(min(newValue, oldValue),newCost[j]) + 1
		      newCost[j - 1] ← oldValue
		      oldValue ← newValue
	if i greater than 0 then
		newCost[str2.length()] ← oldValue
return newCost[str2.length()]

Algorithm wpDistance(str1, str2):
	Input: Source string str1, Target string str2
  Output: Distance calculated using Wu and Palmer Distance metric
simLocal ← 0
wpDist ← calcRelatednessOfWords(str1, str2)
if wpDist is greater than or equal to 1 then
  simLocal ← 1
else
	simLocal ← 0
return simLocal

2. Cosine Similarity based Algorithm: Treat concepts of the source and the target ontology, as virtual documents containing the information like concept descriptions. These containers of terms are transformed into TF-IDF vectors and the similarity is calculated using the cosine similarity metric. Add mapping and corresponding similarity values to the initial list of mappings.
Algorithm csScore(str1, str2):
	Input: Source string str1, Target string str2
  Output: Similarity calculated using Cosine Similarity metric
wSeqsName ← sName.split(" ")
wSeqtName ← tName.split(" ")
wFreqVect ← frequency (words in wSeqsName and wSeqtName)
distinctWSrcTgt ← frequency (distinct words in wSeqsName and wSeqtName)
VectAB ← 0
VectA_Sq ← 0
VectB_Sq ← 0
sim_score ← 0
for i ← 0 to size of distinctWSrcTgt do
	valST ← wFreqVect.get(distinctWSrcTgt.get(i))
  VectAB ← VectAB + (freq1 * freq2)
	VectA_Sq ← VectA_Sq + freq1 * freq1
	VectB_Sq ← VectB_Sq + freq2 * freq2
sim_score ← ((VectAB)/(sqrt(VectA_Sq)*sqrt(VectB_Sq)))
return sim_score

3. MultiWord Matcher: Since the ontologies we deal with are small, we customized the Multi-Word Matcher of AgreementMakerLight for our ontologies. It matches the closely related multi-word class and property names that have matching words and/or words with common WordNet synonyms or close hypernyms. First, Word Lexicon from the Lexicon of each ontology is obtained. Then the steps are detailed in the below algorithm:
Algorithm MultiWordMatcher(str1, str2):
	Input: Source string str1, Target string str2
  Output: Similarity
for sName in sourceLex do
  sWords ← sName.split(" ")
  if length of sWords is lesser than 2 OR length of sWords is greater than 3 then	
		continue			
	for tName in targetLex do		
		if sName is equal to tName then
			continue
		tWords ← tName.split(" ");
		if length of tWords is lesser than 2 OR if length of tWords is greater than 3 OR length of sWords and tWords are not equal then
			continue
		sim ← 0
		for i ← 0 to length of sWords do	
			sw ←  sWords[i]
			tw ←  tWords[i]
			if stopset contains sw OR stopset contains tw then
				continue
			if sw is equal to tw then
				sim ← sim + 1
				continue
			sList ← getAllWordForms(sw);
			tList ← getAllWordForms(tw);
			if sList contains tw OR tList contains sw then
				sim ← sim + 0.8
				continue
			if sourceWLex.getWordEC(sw) is less than 0.75 OR targetWLex.getWordEC(tw) is less than 0.75 then
        score ←wuPalmerScore(sw,tw);
        if score is greater than 0.5 then
	        sim ← sim + 0.5
    sim ← sim/length of sWords
    for srcID in sourceLex.getClasses(sName) do
      for tgtID in targetLex.getClasses(tName) do
        finalSim ←  sim * sourceLex.getCorrectedWeight(sName, srcId) * targetLex.getCorrectedWeight(tName,tgtId)
				if finalSim is less than threshold then
					finalSim ←  threshold
return finalSim

Property-based technique:
4.  Property Matcher: Consider the properties (object properties and data properties) of the source and target ontologies. If the domains or the ranges of the source and the target properties do not match, then the similarity is 0. Otherwise, it checks whether the ids are equal. If they are equal, then the similarity is 1. It also checks whether the lists of source and target values match. If so, then it calculates the Jaccard similarity. Otherwise, it uses WordNet to find the synonyms of each word. Jaccard similarity for the words in the source and the target is then calculated.
Algorithm MatchProperties(str1, str2):
	Input: Source string str1, Target string str2
  Output: Similarity based on the properties of the source and the target
sProp ← properties of source
tProp ← properties of target
sDomain ← domain of source property
tDomain ← domain of target property
sRange ← range of source property
tRange ← range of target property
sim ← 0
if sDomain is not equal to tDomain OR sRange is not equal to tRange then
	sim ← 0
for sName in sourceLexicon do
	for tName in targetLexicon do
		if sName is equal to tName then
			sim ← 1
sim ← 0
for sName in sourceLexicon do
	for tName in targetLexicon do
		newSim ← nameSimilarity(sName, tName)
		if newSim is greater than sim then
			sim ← newSim
return sim

Algorithm NameSimilarity(str1, str2):
	Input: Source property name str1, Target property name str2
  Output: Jaccard Similarity between the synonyms of str1 and str2
if str1 is equal to str2 then
	sim ← 1
sW ← str1.split(“ “)
sWords ← []
sSyns ← []
for w in sW do
	add w to sWords
	add w to sSyns 
	if the length of w is greater than 2 then
		add all noun word forms of w to sSyns
tW ← str2.split(“ “)
tWords ← []
tSyns ← []
for w in tW do
	add w to tWords
	add w to tSyns 
	if the length of w is greater than 2 then
		add all noun word forms of w to tSyns
wordSim ← Jaccard similarity (sWords, tWords) * 0.9
simString ← stringSimilarity(str1, str2)*0.9
sim ← 1 - ((1-wordSim) * (1-simString))
wordNetSim ← Jaccard similarity (sSyns, tSyns)
if wordNetSim is greater than sim then
	sim ← wordNetSim
return sim

Combining the alignments obtained from all the above matchers: The LWC matcher in AML can be used for combining the alignments obtained as a result of all the matchers. This method takes as input, alignments from two matchers at a time and a weight, and combines those alignments based on the linear weighted combination. It returns another set of alignments. This process can be repeated for all the matchers.

Algorithm LWC(M1Alignment, M2Alignment, Weight):
	Input: Alignment from matcher 1 M1Alignment, Alignment from matcher 2 M2Alignment, Weight with which the alignments are to be combined
  Output: Combined Alignments
for mapping m in M1Alignment do
	sim ← similarity of m*Weight + similarity of the source and target mapping from Matcher2 *(1-Weight)
	Add all the source, target, sim to combinedAlignments			
for mapping m in M2Alignment do
	if M1Alignment does not contain m then
    sim = similarity of m*(1-Weight)
		Add all the source, target, sim to combinedAlignments
return combinedAlignments

Future Work:
# Look into other existing matchers in AML.
# Would a structural matcher really help, given our ontologies are naive?
# Identify techniques to refine the mappings.
