package entityLinking;

import datasetComponents.Attribute;
import ontologyComponents.Concept;

import java.util.HashSet;

public class ConceptLink {

    Attribute a;
    HashSet<Concept> concepts;

    public ConceptLink(Attribute a, HashSet<Concept> concepts) {
        this.a = a;
        this.concepts = concepts;
    }

    public Attribute getA() {
        return a;
    }

    public HashSet<Concept> getConcepts() {
        return concepts;
    }
}
