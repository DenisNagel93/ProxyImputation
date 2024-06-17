package ontologyComponents;

import java.util.HashMap;
import java.util.HashSet;

public class Concept {

    String id;
    String name;
    HashMap<Concept, Integer> coOccurrences;
    int docCount;
    int maxCoOcc;
    HashSet<Concept> parts;

    public Concept(String id, String name) {
        this.id = id;
        this.name = name;
        this.coOccurrences = new HashMap<>();
        this.maxCoOcc = 0;
        this.docCount = 0;
        this.parts = new HashSet<>();
    }

    public void addPart(Concept c) {
        this.parts.add(c);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public HashMap<Concept, Integer> getCoOccurrences() {
        return coOccurrences;
    }

    public int getDocCount() {
        return docCount;
    }

    public int getMaxCoOcc() {
        return maxCoOcc;
    }

    public HashSet<Concept> getParts() {
        return parts;
    }

    public void setDocCount(int docCount) {
        this.docCount = docCount;
    }

    public void setMaxCoOcc(int maxCoOcc) {
        this.maxCoOcc = maxCoOcc;
    }
}
