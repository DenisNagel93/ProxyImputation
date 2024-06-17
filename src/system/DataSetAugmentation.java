package system;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import datasetComponents.Join;
import io.Parser;
import io.Vars;
import ui.applicationSelect;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import static io.WriteDataSet.WriteDataSetToFile;

public class DataSetAugmentation {

    static Join recentJoin = null;

    public static void augmentList(File f) throws IOException {
        long loadStartTime = System.nanoTime();
        for (File entry : f.listFiles()) {
            System.out.println("Processing file: " + entry.getName());
            Parser p = new Parser(entry,";");
            for (int i = 0; i < p.fileContent.size(); i++) {
                String appendix = p.fileContent.get(i).get(0);
                DataSet d = Vars.titleIndex.get(p.fileContent.get(i).get(1));
                ArrayList<DataSet> joinedTables = new ArrayList<>();
                ArrayList<DataSet> incompatibleTables = new ArrayList<>();
                joinedTables.add(d);
                for (int j = 2; j < p.fileContent.get(i).size(); j++) {
                    HashSet<DataSet> toAppend = Vars.atrIndex.get(p.fileContent.get(i).get(j).replaceAll(",",""));
                    String toIgnore = p.fileContent.get(i).get(j);
                    try {
                        for (DataSet d2 : toAppend) {
                            DataSet dn = annotateDataSet(d,d2,toIgnore);
                            if (dn.getRecords().size() - d.getRecords().size() == 0 && !recentJoin.hasFewerDim() && !recentJoin.isEmptyJoin()) {
                                d = dn;
                                joinedTables.add(d2);
                            } else {
                                incompatibleTables.add(d2);
                            }
                        }
                    } catch (NullPointerException e) {

                    }
                }
                WriteDataSetToFile(d,Helper.checkForFile(new File(Vars.getOutputPath() + "/Joins/" + d.getTitle() + "_" + appendix + ".csv")),";");
                writeJoinLog(Helper.checkForFile(new File(Vars.getOutputPath() + "/Joins/" + d.getTitle() + "_" + appendix + "_log.txt")),joinedTables,incompatibleTables);
            }
        }
        long loadRuntime = System.nanoTime() - loadStartTime;
        System.out.println("Time to Join: " + (double)loadRuntime / 1000000000 + " seconds");
    }

    public static void dialog(String path) throws IOException {
        while (true) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Filename of DataSet (type 'back' to go to main menu or 'exit' to close the application):");
            String input = sc.nextLine();
            if (input.equals("back")) {
                applicationSelect.selectOption(path);
                break;
            }
            if (input.equals("exit")) {
                break;
            } else {
                DataSet d = Vars.titleIndex.get(input);
                while (true) {
                    System.out.println("Attribute to append (type 'back' to go to main menu or 'exit' to close the application):");
                    String input2 = sc.nextLine();
                    if (input2.equals("back")) {
                        applicationSelect.selectOption(path);
                        break;
                    }
                    if (input2.equals("exit")) {
                        return;
                    } else {
                       HashSet<DataSet> toAppend = Vars.atrIndex.get(input2);
                       try {
                           for (DataSet d2 : toAppend) {
                               System.out.println("#Data Sets with Attribute: " + toAppend.size());
                               System.out.println("Join from Data Set: " + d2.getTitle());
                               DataSet dn = annotateDataSet(d,d2,input2);
                               System.out.println();
                               if (dn.getRecords().size() - d.getRecords().size() == 0 && !recentJoin.hasFewerDim() && !recentJoin.isEmptyJoin()) {
                                   WriteDataSetToFile(dn,new File(Vars.getOutputPath() + "/Joins/" + dn.getTitle() + ".csv"),";");
                                   d = dn;
                               } else {
                                   System.out.println("Tables are contextually incompatible!");
                                   System.out.println();
                                   if (recentJoin.hasFewerDim()) {
                                       System.out.println("--Fewer Contextual Dimensions");
                                   } else {
                                       if (recentJoin.isEmptyJoin()) {
                                           System.out.println("--Empty Join");
                                       } else {
                                           System.out.println("--Additional Records: " + (dn.getRecords().size() - d.getRecords().size()));
                                       }
                                   }
                               }
                               //break;
                           }
                       } catch (NullPointerException e) {
                           System.out.println("Attribute not found!");
                           System.out.println();
                       }
                    }
                }
            }
        }
    }

    public static void writeJoinLog(File out, ArrayList<DataSet> joins, ArrayList<DataSet> incomp) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(out));
        bw.write("----Joined----");
        bw.newLine();
        for (DataSet dj : joins) {
            bw.write(dj.getTitle());
            bw.newLine();
        }
        bw.newLine();
        bw.write("----Incompatible----");
        bw.newLine();
        for (DataSet di : incomp) {
            bw.write(di.getTitle());
            bw.newLine();
        }
        bw.close();
    }

    public static DataSet annotateDataSet(DataSet d, DataSet toAppend, String toIgnore) {
        Join j = new Join(d,toAppend,toIgnore);
        recentJoin = j;
        return j.getJoinedTable();
    }

}
