package io;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import datasetComponents.Record;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

public class WriteDataSet {

    public static void WriteDataSetToFile(DataSet d, File out, String delim) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(out));
        for (Attribute a : d.getAttributeList()) {
            bw.write(a.getTitle());
            bw.write(delim);
        }
        bw.newLine();
        for (Record r : d.getRecordList()) {
            for (Attribute a : d.getAttributeList()) {
                if (r.getEntries().containsKey(a)) {
                    bw.write(r.getEntry(a));
                }
                bw.write(delim);
            }
            bw.newLine();
        }
        bw.close();
    }

    public static void printDataSetHeaders(HashSet<DataSet> datasets, File out) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(out));
        for (DataSet d : datasets) {
            bw.write(d.getTitle() + ":");
            for (Attribute a : d.getAttributeList()) {
                bw.write(a.getTitle() + ";");
            }
            bw.newLine();
        }
        bw.close();
    }
}
