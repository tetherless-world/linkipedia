mkdir -p dataone-index/NTriple

rapper https://raw.githubusercontent.com/DataONEorg/sem-prov-ontologies/master/observation/d1-ECSO.owl -i rdfxml -o ntriples > dataone-index/NTriple/ecso.nt
rapper http://ecoinformatics.org/oboe/oboe.1.1/oboe.owl -i rdfxml -o ntriples > dataone-index/NTriple/oboe.nt
rapper http://ecoinformatics.org/oboe/oboe.1.1/oboe-core.owl -i rdfxml -o ntriples > dataone-index/NTriple/oboe-core.nt
rapper http://ecoinformatics.org/oboe/oboe.1.1/oboe-characteristics.owl -i rdfxml -o ntriples > dataone-index/NTriple/oboe-characteristics.nt
rapper http://ecoinformatics.org/oboe/oboe.1.1/oboe-standards.owl -i rdfxml -o ntriples > dataone-index/NTriple/oboe-standards.nt
rapper http://purl.dataone.org/odo/d1-generated-ECSO.owl -i rdfxml -o ntriples > dataone-index/NTriple/d1-generated-ECSO.nt
rapper http://purl.dataone.org/obo/ENVO_import.owl -i rdfxml -o ntriples > dataone-index/NTriple/ENVO_import.nt
rapper http://purl.dataone.org/obo/PATO_import.owl -i rdfxml -o ntriples > dataone-index/NTriple/PATO_import.nt
rapper http://purl.dataone.org/obo/UO_import.owl -i rdfxml -o ntriples > dataone-index/NTriple/UO_import.nt
rapper http://www.w3.org/TR/skos-reference/skos-owl1-dl.rdf -i rdfxml -o ntriples > dataone-index/NTriple/skos-owl1-dl.nt
