package datasetComponents;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

//A single relational data set
public class DataSet {

    //List of attributes
    public ArrayList<Attribute> attributeList;
    //List of records
    public ArrayList<Record> recordList;
    //Set of Attributes
    public HashSet<Attribute> attributes;
    //Set of Records
    public HashSet<Record> records;
    //Data Set File
    File src;
    //Data Set title
    public String title;
    public HashMap<String,HashSet<Record>> recIndex;

    //Constructor (no attributes)
    public DataSet(File src) {
        this.records = new HashSet<>();
        this.recIndex = new HashMap<>();
        this.src = src;
        this.title = readTitle();
        this.recordList = new ArrayList<>();
    }

    //Constructor (with attributes)
    public DataSet(ArrayList<Attribute> attributeList) {
        this.attributeList = attributeList;
        this.records = new HashSet<>();
        this.recIndex = new HashMap<>();
        this.recordList = new ArrayList<>();
    }

    //Constructor (Empty Table)
    public DataSet(String title) {
        this.title = title;
    }

    public void addRecIndex(Record r, Collection<String> entries) {
        for (String s : entries) {
            String temp = s.split("\\[")[0].trim();
            if (this.recIndex.containsKey(temp)) {
                this.recIndex.get(temp).add(r);
            } else {
                HashSet<Record> recSet = new HashSet<>();
                recSet.add(r);
                this.recIndex.put(temp,recSet);
            }
        }
    }

    //Extracts data set title from file name
    public String readTitle() {
        String[] sub = this.getSrc().getName().split("\\.");
        StringBuilder temp = new StringBuilder(sub[0]);
        for (int i = 1; i < sub.length - 1; i++) {
            temp.append(".").append(sub[i]);
        }
        return temp.toString();
    }

    //Add a record to the data set
    public void addRecord(Record r) {
        this.records.add(r);
        this.recordList.add(r);
    }

    //Returns the attribute at the specified position
    public Attribute getAttribute(int index) {
        return this.attributeList.get(index);
    }

    //Returns the attribute with the specified title
    public Attribute getAttribute(String label) {
        Attribute out = null;
        for (Attribute dr : this.attributes) {
            String truncateA = dr.getTitle().replace("\"", "");
            String truncateB = label.replace("\"", "");
            if (truncateA.equals(truncateB)) {
                out = dr;
                break;
            }
        }
        return out;
    }

    //Computes candidates for join attributes for a given data set
    public HashMap<Attribute,Attribute> getJoinCandidates(DataSet d) {
        HashMap<Attribute,Attribute> joinCandidates = new HashMap<>();
        for (Attribute a : this.attributes) {
            for (Attribute aB : d.getAttributes()) {
                if (a.getTitle().equalsIgnoreCase(aB.getTitle())) {
                    joinCandidates.put(a,aB);
                }
            }
        }
        return joinCandidates;
    }

    //Computes the average value for a given numeric attribute
    public double computeAvg(Attribute a) {
        double sum = 0.0;
        int count = 0;
        for (Record r : this.getRecords()) {
            try {
                sum = sum + Double.parseDouble(r.getEntry(a));
                count++;
            } catch (Exception e) {
                return 0.0;
            }
        }
        return sum / (double)count;
    }

    //----------Getter/Setter----------

    public HashSet<Attribute> getAttributes() {
        return attributes;
    }

    public ArrayList<Attribute> getAttributeList() {
        return attributeList;
    }

    public ArrayList<Record> getRecordList() {
        return recordList;
    }

    public HashSet<Record> getRecords() {
        return records;
    }

    public File getSrc() {
        return src;
    }

    public String getTitle() {
        return title;
    }

    public HashMap<String, HashSet<Record>> getRecIndex() {
        return recIndex;
    }

    public void setAttributeList(ArrayList<Attribute> attributeList) {
        this.attributeList = attributeList;
    }

    public void setAttributes(HashSet<Attribute> attributes) {
        this.attributes = attributes;
    }

    public void setRecords(HashSet<Record> records) {
        this.records = records;
    }

    public void setRecordList(ArrayList<Record> recordList) {
        this.recordList = recordList;
    }

}
