package io;

import ontologyComponents.Concept;
import ontologyComponents.Ontology;
import ontologyComponents.Predicate;
import ontologyComponents.Triple;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

public class LoadOntology {

    public static Ontology loadOntologyFromFile(File inputFile, String delim) {
        Parser ln = new Parser(inputFile,delim);
        HashMap<String,Concept> concepts = new HashMap<>();
        HashMap<String, Predicate> predicates = new HashMap<>();
        HashSet<Triple> triples = new HashSet<>();
        for (int i = 0; i < ln.fileContent.size(); i++) {
            Concept s,o;
            Predicate p;
            String sub = ln.fileContent.get(i).get(0);
            String pred = ln.fileContent.get(i).get(1);
            String obj = ln.fileContent.get(i).get(2);
            if (concepts.containsKey(sub)) {
                s = concepts.get(sub);
            } else {
                s = new Concept(sub,sub);
                concepts.put(sub,s);
            }
            if (concepts.containsKey(obj)) {
                o = concepts.get(obj);
            } else {
                o = new Concept(obj,obj);
                concepts.put(obj,o);
            }
            if (predicates.containsKey(pred)) {
                p = predicates.get(pred);
            } else {
                p = new Predicate(pred);
                predicates.put(pred,p);
            }
            if (p.getType().equals("partOf")) {
                o.addPart(s);
            }
            Triple t = new Triple(s,p,o);
            triples.add(t);
        }
        Ontology o = new Ontology(inputFile.getName(),triples);
        o.setConcepts(concepts);
        o.setPredicates(predicates);
        return o;
    }
}
