package execution;

import datasetComponents.Record;
import io.LoadNarratives;
import io.LoadOntology;
import io.Vars;
import matches.EventMatch;
import narrativeComponents.Entity;
import narrativeComponents.Event;
import narrativeComponents.Narrative;
import ontologyComponents.Concept;
import system.CompletenessCheck;
import system.EventMatching;
import system.EventRefinement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class ExtendedPipeline {

    public static void dialog() throws IOException {

        while (true) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Filename of Narrative Graph (type 'exit' to close the application):");
            String input = sc.nextLine();
            if (input.equals("exit")) {
                break;
            } else {
                Narrative n = LoadNarratives.loadNarrativeFromFile(new File(Vars.getNarrativePath() + "/" + input),Vars.getDelim());
                long loadStartTime = System.nanoTime();
                runPipeline(n,new File(Vars.getOutputPath() + "/CompletenessCheck/" + input.split("\\.")[0] + "_out.txt"));
                long loadRuntime = System.nanoTime() - loadStartTime;
                System.out.println("Done in " + (double)loadRuntime / 1000000000 + " s - outputfile created at specified path");
            }
        }
    }

    public static void runPipeline(Narrative n,File out) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(out));
        File f = new File(Vars.getOutputPath() + "/CompletenessCheck/" + out.getName().split("\\.")[0] + "_cont.txt");
        BufferedWriter bw1 = new BufferedWriter(new FileWriter(f));

        HashMap<Event, ArrayList<EventMatch>> matches = new HashMap<>();

        for (Event e : n.getEvents()) {
            bw.write("-- Event: " + e.getCaption());
            bw.newLine();
            bw1.write("-- Event: " + e.getCaption());
            bw1.newLine();
            EventMatching em = new EventMatching(e, Vars.getDataSets(), Vars.attributeEmbedding, Vars.titleEmbedding, Vars.getTEM());
            matches.put(e, em.getMatching());
            EventRefinement er = new EventRefinement(matches.get(e), Vars.factEmbedding, Vars.valueEmbedding, Vars.vtEmbedding, Vars.getTPM(), Vars.getTEM());

            for (EventMatch m : er.getEventMatching()) {
                CompletenessCheck cc = new CompletenessCheck(m, LoadOntology.loadOntologyFromFile(new File(Vars.getOntologyPath() + "/" + "Regions.txt"), Vars.getDelim()));
                bw.write("Data Set: " + m.getD().getTitle());
                bw.newLine();
                bw1.write("Data Set: " + m.getD().getTitle());
                bw1.newLine();
                for (Entity ent : cc.getMatchingRecords().keySet()) {
                    bw.write("---- Property: " + ent.getLabel());
                    bw.newLine();
                    if (cc.getConceptPartitions().containsKey(ent)) {
                        for (Integer i : cc.getConceptPartitions().get(ent).keySet()) {
                            bw.write("-> Level: " + i);
                            bw.newLine();
                            HashSet<Record> total = new HashSet<>();
                            for (Concept c : cc.getConceptPartitions().get(ent).get(i)) {
                                if (cc.getMatchingRecords().get(ent).containsKey(c.getName())) {
                                    bw.write("---> " + c.getName());
                                    bw.write(" -> #Records: " + cc.getMatchingRecords().get(ent).get(c.getName()).size());
                                    bw.newLine();
                                    total.addAll(cc.getMatchingRecords().get(ent).get(c.getName()));
                                } else {
                                    bw.write("---> " + c.getName());
                                    bw.write(" -> #Records: 0");
                                    bw.newLine();
                                }
                            }
                            bw.write("-> Level: " + i);
                            bw.write(" -> #Records: " + total.size());
                            bw.newLine();
                            bw.write(" -> Degree of Completeness: " + cc.getLevelCompleteness().get(ent).get(i));
                            bw.newLine();
                        }
                    } else {
                        if (cc.getMatchingRecords().get(ent).containsKey(ent.getLabel())) {
                            bw.write("---> " + ent.getLabel());
                            bw.write(" -> #Records: " + cc.getMatchingRecords().get(ent).get(ent.getLabel()).size());
                            bw.newLine();
                        } else {
                            bw.write("---> " + ent.getLabel());
                            bw.write(" -> #Records: 0");
                            bw.newLine();
                        }
                    }
                }
                for (Integer i : cc.getContextIndex().keySet()) {
                    bw1.write(" -" + i + ": " + cc.getContextIndex().get(i).getLabel());
                }
                bw1.newLine();
                for (String s : cc.getContextLevels().keySet()) {
                    System.out.println("Level: " + s);
                    System.out.println("Size: " + cc.getCoveredContexts().get(s).size());
                    System.out.println("Total: " + cc.getContextLevels().get(s).size());
                    double p = (double)cc.getCoveredContexts().get(s).size() / (double)cc.getContextLevels().get(s).size();
                    bw1.write("- Level: " + s + " -> Completeness: " +  p);
                    bw1.newLine();
                }

                bw1.newLine();
                bw.newLine();
            }
            bw.newLine();
        }
        bw.close();
        bw1.close();
    }

}
