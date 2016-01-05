sed -e 's~http://hbgd.org/~https://hbgd.tw.rpi.edu/~g' < /Users/jimmccusker/src/hbgdki_ontology/hbgdki.owl | rapper - -I https://hbgd.tw.rpi.edu/ -i rdfxml -o ntriples | grep -v debategraph > hbgd-index/NTriple/hbgdki.nt
curl -L 'http://owl.cs.manchester.ac.uk/converter/convert?ontology=https://raw.githubusercontent.com/ISA-tools/stato/dev/releases/1.2/stato.owl&format=RDF/XML' | iconv -f iso8859-1 -t utf-8 | rapper - -I 'http://www.w3.org/2002/07/owl#' -i rdfxml -o ntriples > hbgd-index/NTriple/stato.nt
curl -L 'http://downloads.dbpedia.org/2015-04/core-i18n/en/labels_en.nt.bz2' | bzip2 -d -c > hbgd-index/NTriple/dbpedia-titles.nt
curl -L 'http://downloads.dbpedia.org/2015-04/core-i18n/en/short-abstracts_en.nt.bz2' | bzip2 -d -c > hbgd-index/NTriple/dbpedia-abstracts.nt
curl -L 'ftp://ftp.nlm.nih.gov/online/mesh/mesh.nt.gz' | gunzip -c > hbgd-index/NTriple/mesh.nt
