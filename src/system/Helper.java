package system;

import datasetComponents.Attribute;
import datasetComponents.DataSet;
import datasetComponents.Record;
import io.Vars;

import java.io.*;
import java.nio.Buffer;
import java.util.*;

public class Helper {

    public static void writeContextCategories() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(Vars.getOutputPath() + "/contextAttributes.txt")));
        HashSet<String> contAtr = new HashSet<>();
        String line;
        while ((line = br.readLine()) != null) {
            contAtr.add(line);
        }
        br.close();

        BufferedWriter bw1 = new BufferedWriter(new FileWriter(new File(Vars.getOutputPath() + "/cat1.txt")));
        BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File(Vars.getOutputPath() + "/cat2.txt")));
        HashSet<DataSet> category1 = new HashSet<>();
        category1.addAll(Vars.atrIndex.get("Country"));
        category1.addAll(Vars.atrIndex.get("country"));
        HashSet<DataSet> year = new HashSet<>();
        year.addAll(Vars.atrIndex.get("Year"));
        year.addAll(Vars.atrIndex.get("year"));
        category1.retainAll(year);
        for (String s : contAtr) {
            category1.removeAll(Vars.atrIndex.get(s));
        }
        HashSet<DataSet> category2 = new HashSet<>(category1);
        category1.removeAll(Vars.atrIndex.get("Sex"));
        category2.retainAll(Vars.atrIndex.get("Sex"));

        for (DataSet d : category1) {
            bw1.write(d.getSrc().getName());
            for (Attribute a : d.getAttributes()) {
                if (!contAtr.contains(a.getTitle()) && !a.getTitle().equalsIgnoreCase("Year") && !a.getTitle().equalsIgnoreCase("Sex")
                        && !a.getTitle().equalsIgnoreCase("Country") && !a.getTitle().equalsIgnoreCase("Subnational region")
                        && !a.getTitle().equalsIgnoreCase("WHO region") && !a.getTitle().equalsIgnoreCase("UN region")
                        && !a.getTitle().equalsIgnoreCase("Comments") && !a.getTitle().equalsIgnoreCase("Data Source")
                        && !a.getTitle().equalsIgnoreCase("World Bank income group") && !a.getTitle().equalsIgnoreCase("UN SDG Region")) {
                    bw1.write("; " + a.getTitle());
                }
            }
            bw1.newLine();
        }
        for (DataSet d : category2) {
            bw2.write(d.getSrc().getName());
            for (Attribute a : d.getAttributes()) {
                if (!contAtr.contains(a.getTitle()) && !a.getTitle().equalsIgnoreCase("Year") && !a.getTitle().equalsIgnoreCase("Sex")
                        && !a.getTitle().equalsIgnoreCase("Country") && !a.getTitle().equalsIgnoreCase("Subnational region")
                        && !a.getTitle().equalsIgnoreCase("WHO region") && !a.getTitle().equalsIgnoreCase("UN region")
                        && !a.getTitle().equalsIgnoreCase("Comments") && !a.getTitle().equalsIgnoreCase("Data Source")
                        && !a.getTitle().equalsIgnoreCase("World Bank income group") && !a.getTitle().equalsIgnoreCase("UN SDG Region")) {
                    bw2.write("; " + a.getTitle());
                }
            }
            bw2.newLine();
        }

        bw1.close();
        bw2.close();
    }

    public static void findCommonAttributes() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(Vars.getOutputPath() + "/contextAttributes.txt"));
        for (String s : Vars.captionIndex.keySet()) {
            if (Vars.captionIndex.get(s).size() > 20) {
                bw.write(s);
                bw.newLine();
            }
        }
        bw.close();
    }

    public static void findDataSet() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Attribute Title: ");
        String s = sc.nextLine();
        if (Vars.atrIndex.containsKey(s)) {
            for (DataSet d : Vars.atrIndex.get(s)) {
                System.out.println("-- " + d.getTitle());
            }
        } else {
            System.out.println("-- No Data Set found!");
        }
    }

    public static File checkForFile(File in) {
        while (in.exists()) {
            if (in.getPath().contains("_alt_")) {
                in = new File(in.getPath().split("_alt")[0] + "_alt_"
                        + (Integer.parseInt(in.getPath().split("\\.")[0].split("_alt_")[1]) + 1) + "."
                        + in.getPath().split("\\.")[1]);
            } else {
                in = new File(in.getPath().split("\\.")[0] + "_alt_1."
                        + in.getPath().split("\\.")[1]);
            }
        }
        return in;
    }

    public static double computeAvg(Attribute a,HashSet<Record> records) {
        double sum = 0.0;
        int errors = 0;
        for (Record r : records) {
            try {
                sum = sum + Double.parseDouble(r.getEntry(a));
            } catch (Exception e) {
                errors++;
            }
        }
        return sum / (double)(records.size() - errors);
    }

    public static double prepareForDouble(String s) {
        s = s.replaceAll("\"","");
        String[] sub = s.split("\\[");
        return Double.parseDouble(sub[0]);
    }
}
