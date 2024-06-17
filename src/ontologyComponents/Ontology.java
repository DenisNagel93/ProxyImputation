package ontologyComponents;

import java.util.HashMap;
import java.util.HashSet;

public class Ontology {

    String name;
    HashSet<Triple> triples;
    HashMap<String,Concept> concepts;
    HashMap<String,Predicate> predicates;

    public Ontology(String name,HashSet<Triple> triples) {
        this.name = name;
        this.triples = triples;
    }

    public HashMap<String, Concept> getConcepts() {
        return concepts;
    }

    public HashSet<Triple> getTriples() {
        return triples;
    }

    public String getName() {
        return name;
    }

    public void setConcepts(HashMap<String, Concept> concepts) {
        this.concepts = concepts;
    }

    public void setPredicates(HashMap<String, Predicate> predicates) {
        this.predicates = predicates;
    }
}
