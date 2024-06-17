package datasetComponents;

import java.util.HashMap;
import java.util.HashSet;

//An individual record of a data set
public class Record {

    //Record's values for each attribute
    public HashMap<Attribute, String> entries;
    //Associated entities (Entity Linking)
    public HashSet<HashSet<String>> concepts;

    //Constructor
    public Record(HashMap<Attribute,String> entries) {
        this.entries = entries;
        this.concepts = new HashSet<>();
    }

    //Constructor (Join of two records)
    public Record(Record rA, Record rB) {
        this.entries = new HashMap<>();
        for (Attribute a : rA.getEntries().keySet()) {
            this.entries.put(a, rA.getEntry(a));
        }
        for (Attribute a : rB.getEntries().keySet()) {
            this.entries.put(a, rB.getEntry(a));
        }
    }

    public Record(Record rA, Record rB, Attribute toAdd) {
        this.entries = new HashMap<>();
        for (Attribute a : rA.getEntries().keySet()) {
            this.entries.put(a, rA.getEntry(a));
        }
        for (Attribute a : rB.getEntries().keySet()) {
            if (a.getTitle().toLowerCase().equals(toAdd.getTitle().toLowerCase())) {
                this.entries.put(toAdd, rB.getEntry(a));
            }
        }
    }

    //--------Getter/Setter--------

    public String getEntry(Attribute a) {
        return this.entries.get(a);
    }

    public HashMap<Attribute, String> getEntries()  {
        return this.entries;
    }

    public HashSet<HashSet<String>> getConcepts() {
        return concepts;
    }

}
