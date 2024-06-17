package datasetComponents;

import matches.InstanceMatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

//A join between two data sets
public class Join {

    //The joined data sets (dA,dB) and the resulting table
    DataSet dA,dB,joinedTable;
    //Join Attributes
    HashMap<Attribute,Attribute> joinAttributes;
    //Mapping of join partners
    HashMap<Record,HashSet<Record>> joinMatches;
    //Attributes that are explicitly excluded from being considered as join attributes
    HashSet<Attribute> constraintsA,constraintsB;
    //Instance Match for each Data Set
    InstanceMatch iA,iB;
    boolean fewerDim;
    boolean emptyJoin;

    //Constructor(Given:Instance Matches)
    public Join(InstanceMatch iA, InstanceMatch iB, HashMap<Attribute,Attribute> joinAttributes, HashSet<Attribute> constraintsA, HashSet<Attribute> constraintsB) {
        this.iA = iA;
        this.iB = iB;
        this.dA = iA.getE().getD();
        this.dB = iB.getE().getD();
        this.joinAttributes = joinAttributes;
        this.constraintsA = constraintsA;
        this.constraintsB = constraintsB;
        this.joinAttributes = prepareJoinAttributes();
        this.joinMatches = computeJoin();
    }

    //Constructor(Given: Data Sets/No Constraints)
    public Join(DataSet dA, DataSet dB, HashMap<Attribute,Attribute> joinAttributes) {
        this.dA = dA;
        this.dB = dB;
        this.joinAttributes = joinAttributes;
        this.constraintsA = new HashSet<>();
        this.constraintsB = new HashSet<>();
        this.joinAttributes = prepareJoinAttributes();
        this.joinMatches = computeJoin();
    }

    //Constructor(Simple Join)
    public Join(DataSet dA, DataSet dB, String toIgnore) {
        this.dA = dA;
        this.dB = dB;
        this.joinAttributes = dA.getJoinCandidates(dB);
        this.fewerDim = false;
        this.emptyJoin = true;
        this.joinedTable = createHashJoinedTable(toIgnore);
    }

    //Removes constraint attributes from join candidates
    public HashMap<Attribute,Attribute> prepareJoinAttributes() {
        HashMap<Attribute,Attribute> attributes = new HashMap<>();
        for (Attribute a : joinAttributes.keySet()) {
            if (!constraintsA.contains(a) && !constraintsB.contains(joinAttributes.get(a)) /*&& !a.getTitle().equals("Comments")*/) {
                attributes.put(a,joinAttributes.get(a));
            }
        }
        return attributes;
    }

    public DataSet createHashJoinedTable(String toIgnore) {
        Attribute atrToAdd = null;
        String name;
        if (dA.getTitle().contains("_j_")) {
            name = dA.getTitle().split("\\.")[0].split("_j_")[0] + "_j_" + (Integer.parseInt(dA.getTitle().split("\\.")[0].split("_j_")[1]) + 1);
        } else {
            name = dA.getTitle().split("\\.")[0] + "_j_" + "1";
        }
        //DataSet j = new DataSet(dA.getTitle().split("\\.")[0] + "_j_" + dB.getTitle().split("\\.")[0].charAt(0));
        DataSet j = new DataSet(name);
        HashMap<String,Attribute> added = new HashMap<>();
        ArrayList<Attribute> attributeList = new ArrayList<>();
        HashSet<Attribute> attributes = new HashSet<>();
        for (Attribute a : dA.getAttributeList()) {
            attributeList.add(a);
            attributes.add(a);
            added.put(a.getTitle().toLowerCase(),a);
        }
        for (Attribute a : dB.getAttributeList()) {
            //if (!added.contains(a.getTitle().toLowerCase())) {
            if (a.getTitle().equals(toIgnore)) {
                if (!added.containsKey(a.getTitle().toLowerCase())) {
                    atrToAdd = a;
                    attributeList.add(a);
                    attributes.add(a);
                } else {
                    atrToAdd = added.get(a.getTitle().toLowerCase());
                }
            }
        }
        j.setAttributeList(attributeList);
        j.setAttributes(attributes);

        HashSet<Record> records = new HashSet<>();
        ArrayList<Record> recordList = new ArrayList<>();
        HashSet<Record> alreadyJoined = new HashSet<>();
        for (Record rA : dA.getRecords()) {

            HashSet<Record> toJoin = new HashSet<>(dB.getRecords());
            for (Attribute a : joinAttributes.keySet()) {
                if (!a.getTitle().equals("Comments") && !a.getTitle().equals(toIgnore)) {
                    if (dB.getRecIndex().containsKey(rA.getEntry(a).split("\\[")[0].trim())) {
                        toJoin.retainAll(dB.getRecIndex().get(rA.getEntry(a).split("\\[")[0].trim()));
                    } else {
                        toJoin.clear();
                    }
                }
            }
            if (!toJoin.isEmpty()) {
                boolean allRemoved = true;
                for (Record rB : toJoin) {
                    boolean keep = true;
                    for (Attribute a : joinAttributes.keySet()) {
                        if (!a.getTitle().equals("Comments") && !a.getTitle().equals(toIgnore)) {
                            if (!(rA.getEntry(a).split("\\[")[0].trim().equals(rB.getEntry(joinAttributes.get(a)).split("\\[")[0].trim()))) {
                                //System.out.println("->" + rA.getEntry(a).split("\\[")[0].trim() + " vs. " + rB.getEntry(joinAttributes.get(a)).split("\\[")[0].trim());
                                keep = false;
                            }
                        }
                    }
                    if (keep) {
                        //System.out.println("HALLO!!!!!!!!!!!");
                        allRemoved = false;
                        this.emptyJoin = false;
                        Record rn = new Record(rA,rB,atrToAdd);
                        records.add(rn);
                        recordList.add(rn);
                        if (alreadyJoined.contains(rB)) {
                            this.fewerDim = true;
                        } else {
                            alreadyJoined.add(rB);
                        }
                    }
                }
                if (allRemoved) {
                    records.add(rA);
                    recordList.add(rA);
                }
            } else {
                records.add(rA);
                recordList.add(rA);
            }

        }
        j.setRecordList(recordList);
        j.setRecords(records);
        return j;
    }

    //Creates join as a new data set
    public DataSet createJoinedTable(String toIgnore) {
        Attribute atrToAdd = null;
        String name;
        if (dA.getTitle().contains("_j_")) {
            name = dA.getTitle().split("\\.")[0].split("_j_")[0] + "_j_" + (Integer.parseInt(dA.getTitle().split("\\.")[0].split("_j_")[1]) + 1);
        } else {
            name = dA.getTitle().split("\\.")[0] + "_j_" + "1";
        }
        //DataSet j = new DataSet(dA.getTitle().split("\\.")[0] + "_j_" + dB.getTitle().split("\\.")[0].charAt(0));
        DataSet j = new DataSet(name);
        HashMap<String,Attribute> added = new HashMap<>();
        ArrayList<Attribute> attributeList = new ArrayList<>();
        HashSet<Attribute> attributes = new HashSet<>();
        for (Attribute a : dA.getAttributeList()) {
            attributeList.add(a);
            attributes.add(a);
            added.put(a.getTitle().toLowerCase(),a);
        }
        for (Attribute a : dB.getAttributeList()) {
            //if (!added.contains(a.getTitle().toLowerCase())) {
            if (a.getTitle().equals(toIgnore)) {
                if (!added.containsKey(a.getTitle().toLowerCase())) {
                    atrToAdd = a;
                    attributeList.add(a);
                    attributes.add(a);
                } else {
                    atrToAdd = added.get(a.getTitle().toLowerCase());
                }
            }
        }
        j.setAttributeList(attributeList);
        j.setAttributes(attributes);

        HashSet<Record> records = new HashSet<>();
        ArrayList<Record> recordList = new ArrayList<>();
        HashSet<Record> alreadyJoined = new HashSet<>();
        for (Record rA : dA.getRecords()) {
            boolean matched = false;
            for (Record rB : dB.getRecords()) {
                boolean compatible = true;
                for (Attribute a : joinAttributes.keySet()) {
                    if (!a.getTitle().equals("Comments") && !a.getTitle().equals(toIgnore)) {
                        try {
                            if (!(rA.getEntry(a).split("\\[")[0].trim().equals(rB.getEntry(joinAttributes.get(a)).split("\\[")[0].trim()))) {
                                compatible = false;
                            }
                        } catch (Exception ex) {
                            compatible = false;
                        }
                    }
                }
                if (compatible) {
                    this.emptyJoin = false;
                    matched = true;
                    //Record rn = new Record(rA,rB);
                    Record rn = new Record(rA,rB,atrToAdd);
                    records.add(rn);
                    recordList.add(rn);
                    if (alreadyJoined.contains(rB)) {
                        this.fewerDim = true;
                    } else {
                        alreadyJoined.add(rB);
                    }
                }
            }
            if (!matched) {
                records.add(rA);
                recordList.add(rA);
            }
        }
        j.setRecordList(recordList);
        j.setRecords(records);
        return j;
    }

    //Computes the Join partners
    public HashMap<Record,HashSet<Record>> computeJoin() {
        HashMap<Record,HashSet<Record>> matches = new HashMap<>();
        int countA = 0;
        int countB = 0;
        for (Record rA : iA.getEventReduction()) {
            HashSet<Record> joinPartner = new HashSet<>();
            countA++;
            for (Record rB : iB.getEventReduction()) {
                countB++;
                boolean compatible = true;
                for (Attribute a : joinAttributes.keySet()) {
                    try {
                        if (!(rA.getEntry(a).equals(rB.getEntry(joinAttributes.get(a))))) {
                            compatible = false;
                        }
                    } catch (Exception ex) {
                        compatible = false;
                    }
                }
                if (compatible) {
                    joinPartner.add(rB);
                }
                if (matches.containsKey(rB)) {
                    if (compatible) {
                        matches.get(rB).add(rA);
                    }
                } else {
                    HashSet<Record> joinPartnerB = new HashSet<>();
                    if (compatible) {
                        joinPartnerB.add(rA);
                        matches.put(rB,joinPartnerB);
                    }
                }
                if (countB == 1000) {
                    countB = 0;
                    break;
                }
            }
            matches.put(rA,joinPartner);
            if (countA == 1000) {
                break;
            }
        }
        return matches;
    }

    //--------Getter/Setter--------

    public DataSet getJoinedTable() {
        return joinedTable;
    }

    public HashMap<Record, HashSet<Record>> getJoinMatches() {
        return joinMatches;
    }

    public boolean hasFewerDim() {
        return fewerDim;
    }

    public boolean isEmptyJoin() {
        return emptyJoin;
    }
}
