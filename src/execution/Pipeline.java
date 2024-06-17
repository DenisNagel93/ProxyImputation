package execution;

import datasetComponents.DataSet;

import io.LoadNarratives;
import io.Vars;
import matches.EventMatch;
import matches.FactualBinding;
import matches.InstanceMatch;
import matches.NarrativeBinding;
import narrativeComponents.*;
import system.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Pipeline {

    static Narrative n;

    static File configFile;

    static HashMap<Narrative,HashSet<DataSet>> results = new HashMap<>();
    static HashMap<Event,ArrayList<EventMatch>> completeMatches = new HashMap<>();

    static HashMap<Narrative,HashMap<EventMatch,HashMap<EventMatch,HashSet<NarrativeBinding>>>> nBindings = new HashMap<>();
    static HashMap<Narrative,HashMap<EventMatch,HashSet<FactualBinding>>> fBindings = new HashMap<>();

    public static void runBiND() throws IOException {
        while (true) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Filename of Narrative Graph (type 'exit' to close the application):");
            String input = sc.nextLine();
            if (input.equals("exit")) {
                break;
            } else {
                n = LoadNarratives.loadNarrativeFromFile(new File(Vars.getNarrativePath() + "/" + input),Vars.getDelim());
                long loadStarttime = System.nanoTime();
                runPipeline();
                long loadRuntime = System.nanoTime() - loadStarttime;
                System.out.println("Done in " + (double)loadRuntime / 1000000000 + " s - outputfile created at specified path");
            }
        }
    }

    public static void runNoInput(String path, String narrative) throws IOException {
        configFile = new File(path + "/config.txt");
        Vars.initBiND(configFile);
        long loadStarttime = System.nanoTime();
        Vars.loadInput(new File(Vars.getDataPath()), new File(Vars.getNarrativePath()),Vars.getDelim());
        Vars.loadEmbeddings(",");
        long loadRuntime = System.nanoTime() - loadStarttime;
        System.out.println("Load Time: " + (double)loadRuntime / 1000000000 + " seconds");

        n = LoadNarratives.loadNarrativeFromFile(new File(Vars.getNarrativePath() + "/" + narrative),Vars.getDelim());
        long runStarttime = System.nanoTime();
        runPipeline();
        long runRuntime = System.nanoTime() - runStarttime;
        System.out.println("Done in " + (double)runRuntime / 1000000000 + " s - outputfile created at specified path");
    }

    public static void runPipeline() throws IOException {
        HashSet<DataSet> bindings = new HashSet<>();
        HashSet<NarrativeBinding> successful = new HashSet<>();
        HashMap<Event, ArrayList<EventMatch>> matches = new HashMap<>();
        HashSet<EventRefinement> candidates = new HashSet<>();
        for (Event e : n.getEvents()) {
            EventMatching em = new EventMatching(e,Vars.getDataSets(),Vars.attributeEmbedding,Vars.titleEmbedding,Vars.getTEM());
            matches.put(e, em.getMatching());
        }

        HashMap<Event, InstanceMatching> ims = new HashMap<>();
        for (Event e : matches.keySet()) {
            EventRefinement er = new EventRefinement(matches.get(e),Vars.factEmbedding,Vars.valueEmbedding,Vars.vtEmbedding,Vars.getTPM(),Vars.getTEM());
            candidates.add(er);
            InstanceMatching i = new InstanceMatching(er);
            ims.put(e, i);
            ArrayList<EventMatch> orderedList = new ArrayList<>();
            i.getMatching().sort(Collections.reverseOrder());
            for (InstanceMatch ri : i.getMatching()) {
                bindings.add(ri.getE().getD());
                orderedList.add(ri.getE());
            }
            completeMatches.put(e,orderedList);
        }

        printCandidateWitnesses(candidates);

        HashMap<Claim, FactChecking> fcs = new HashMap<>();
        for (Event e : matches.keySet()) {
            for (Claim cl : e.getClaims()) {
                FactChecking fc;
                if (cl.isBinary()) {
                    fc = new FactChecking(ims.get(cl.getNodeA()), ims.get(cl.getEventB()), cl);
                } else {
                    fc = new FactChecking(ims.get(cl.getNodeA()), cl);
                }
                fcs.put(cl,fc);
            }
        }

        HashMap<NarrativeRelation, RelationAssessment> ras = new HashMap<>();
        for (NarrativeRelation nr : n.getNarrativeRelations()) {
            RelationAssessment ra = new RelationAssessment(nr,ims.get(nr.getNodeA()),ims.get(nr.getNodeB()),new File(Vars.getEmbeddingPath() + "/DataEmbeddingScoresReduced.txt"),Vars.getTRA());
            ras.put(nr,ra);
            nBindings.put(n,ra.getMapping());
        }

        results.put(n,bindings);

        HashMap<EventMatch,HashSet<FactualBinding>> bindingResults = new HashMap<>();
        for (FactChecking fc : fcs.values()) {
            for (EventMatch em : fc.getResults().keySet()) {
                if (bindingResults.containsKey(em)) {
                    bindingResults.get(em).add(fc.getResults().get(em));
                } else {
                    HashSet<FactualBinding> fbSet = new HashSet<>();
                    fbSet.add(fc.getResults().get(em));
                    bindingResults.put(em,fbSet);
                }
            }
        }
        fBindings.put(n,bindingResults);

        for (NarrativeRelation nr : n.getNarrativeRelations()) {
            for (NarrativeBinding nb : ras.get(nr).getBindings()) {
                try {
                    if (nb.getScore().getScore() >= Vars.getTRA()) {
                        successful.add(nb);
                        successful.add(nb);
                    }
                } catch (Exception ignored) {

                }
            }
        }
        printOutputJson(n,matches,ims,ras,fcs,successful);
    }

    public static void printCandidateWitnesses(HashSet<EventRefinement> candidates) throws IOException {
        File outfile = new File(Vars.getOutputPath() + "/Witnesses/" + n.getSrc().getName());
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
        for (EventRefinement er : candidates) {
            for (EventMatch em : er.getEventMatching()) {
                bw.write(em.getE().getCaption() + "," + em.getA().getTitle() + "," + em.getD().getTitle());
                bw.newLine();
            }
            bw.newLine();
        }
        bw.close();
    }

    public static void printOutputJson(Narrative n,HashMap<Event,ArrayList<EventMatch>> matches,HashMap<Event, InstanceMatching> ims,
                                       HashMap<NarrativeRelation, RelationAssessment> ras,HashMap<Claim, FactChecking> fcs,HashSet<NarrativeBinding> successful) throws IOException {
        File outfile = new File(Vars.getOutputPath() + "/RelationAssessment/" + n.getSrc().getName());
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));

        bw.write("{\n");
        bw.write("  \"matching_results\": [\n");
        int count1 = 0;
        for (Event e : matches.keySet()) {
            if (count1 != 0) {
                bw.write(",\n");
            }
            count1++;
            bw.write("    {\n");
            bw.write("      \"event_id\": \"" + e.getLabel() + "\",\n");
            bw.write("      \"matches\": [\n");
            int count2 = 0;
            for (InstanceMatch m : ims.get(e).getMatching()) {
                if (count2 != 0) {
                    bw.write(",\n");
                }
                count2++;
                bw.write("        {\n");
                bw.write("          \"event_matching\": {\n");
                try {
                    bw.write("            \"original_score\":" + m.getE().getScore() + ",\n");
                    bw.write("            \"refined_score\":" + m.getE().getRefinedScore() + ",\n");
                    bw.write("            \"event_name\": \"" + m.getE().getE().getCaption() + "\",\n");
                    bw.write("            \"matched_attribute\": \"" + m.getE().getA().getTitle() + "\",\n");
                    bw.write("            \"data_set\": \"" + m.getE().getD().getTitle() + "\"\n");
                } catch (Exception ex) {
                    System.out.println("Empty Match");
                }
                bw.write("          },\n");
                bw.write("          \"property_matches\": [\n");
                int count3 = 0;
                for (EventProperty ep : e.getProperties()) {
                    try {
                        if (count3 != 0) {
                            bw.write(",\n");
                        }
                        count3++;
                        bw.write("            {\n");
                        bw.write("              \"score\":" + m.getE().getPm().get(ep).getScore() + ",\n");
                        bw.write("              \"property_name\": \"" + ep.getLabel() + "\",\n");
                        bw.write("              \"matched_attribute\": \"" + m.getE().getPm().get(ep).getA().getTitle() + "\",\n");
                        bw.write("              \"data_set\": \"" + m.getE().getPm().get(ep).getA().getDs().getTitle() + "\"\n");
                        bw.write("            }");
                    } catch (Exception ex) {
                        System.out.println("Empty Match");
                    }
                }
                bw.write("\n");
                bw.write("          ]\n");
                bw.write("        }");
            }
            bw.write("\n");
            bw.write("      ]\n");
            bw.write("    }");
        }
        bw.write("\n");
        bw.write("  ],\n");
        bw.write("  \"evaluation_results\": [\n");
        int count4 = 0;
        for (NarrativeRelation nr : n.getNarrativeRelations()) {
            for (NarrativeBinding nb : ras.get(nr).getBindings()) {
                try {
                    if (count4 != 0) {
                        bw.write(",\n");
                    }
                    count4++;
                    bw.write("    {\n");
                    bw.write("      \"attribute_A\": \"" + nb.getImA().getE().getA().getTitle() + "\",\n");
                    bw.write("      \"data_set_A\": \"" + nb.getImA().getE().getD().getTitle() + "\",\n");
                    bw.write("      \"claim\": \"" + nr.getLabel() + "\",\n");
                    bw.write("      \"attribute_B\": \"" + nb.getImB().getE().getA().getTitle() + "\",\n");
                    bw.write("      \"data_set_B\": \"" + nb.getImB().getE().getD().getTitle() + "\",\n");
                    bw.write("      \"score\": " + nb.getScore().getScore() + "\n");
                    bw.write("    }");
                } catch (Exception ex) {
                    System.out.println("Empty Binding");
                }
            }
        }
        bw.write("\n");
        bw.write("  ]\n");
        bw.write("}");
        bw.close();
    }

    public static void printOutput(Narrative n,HashMap<Event,ArrayList<EventMatch>> matches,HashMap<Event, InstanceMatching> ims,
                                   HashMap<NarrativeRelation, RelationAssessment> ras,HashMap<Claim, FactChecking> fcs,HashSet<NarrativeBinding> successful) throws IOException {
        File outfile = new File(Vars.getOutputPath() + "/RelationAssessment/" + n.getSrc().getName());
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));

        for (Event e : matches.keySet()) {
            bw.write(e.getLabel());
            bw.newLine();
            for (InstanceMatch m : ims.get(e).getMatching()) {
                try {
                    bw.write("(" + m.getE().getRefinedScore() + "/" + m.getE().getScore() + ") "
                            + e.getCaption()
                            + "-> " + m.getE().getA().getTitle()
                            + " (" + m.getE().getA().getDs().getSrc() + ")"
                            + " (" + m.getEventReduction().size() + ") ");
                    bw.newLine();
                } catch (Exception ex) {
                    bw.write(" -> No match found!");
                    bw.newLine();
                }
                for (EventProperty ep : e.getProperties()) {
                    if (!m.getE().getIntegrated().contains(ep)) {
                        bw.write("-" + ep.getLabel());
                        try {
                            bw.write("(" + m.getE().getPm().get(ep).getScore() + ") "
                                    + "-> " + m.getE().getPm().get(ep).getA().getTitle()
                                    + " (" + m.getE().getPm().get(ep).getA().getDs().getSrc()
                                    + ")");
                            bw.newLine();
                        } catch (Exception ex) {
                            bw.write(" -> No match found!");
                            bw.newLine();
                        }
                    }
                    for (FactualRelation fr : ep.getNodeB().getProperties()) {
                        bw.write("--" + fr.getLabel());
                        try {
                            bw.write("(" + m.getE().getFm().get(fr).getScore() + ") "
                                    + "-> " + m.getE().getFm().get(fr).getA().getTitle()
                                    + " (" + m.getE().getFm().get(fr).getA().getDs().getSrc() + ")");
                            bw.newLine();
                        } catch (Exception ex) {
                            bw.write(" -> No match found!");
                            bw.newLine();
                        }
                    }
                }
                for (Claim cl : e.getClaims()) {
                    if (cl.isBinary()) {
                        bw.write("-" + cl.getLabel());
                        try {
                            bw.write("(" + cl.getEventB().getLabel() + ") "
                                    + "-> " + fcs.get(cl).getResults().get(m.getE()).isValid());
                            bw.newLine();
                        } catch (Exception ex) {
                            bw.write(" -> Unknown!");
                            bw.newLine();
                        }
                    } else {
                        bw.write("-" + cl.getLabel());
                        try {
                            bw.write("(" + cl.getNodeB().getLabel() + ") "
                                    + "-> " + fcs.get(cl).getResults().get(m.getE()).isValid());
                            bw.newLine();
                        } catch (Exception ex) {
                            bw.write(" -> Unknown!");
                            bw.newLine();
                        }
                    }
                }
            }
            bw.newLine();
        }

        for (NarrativeRelation nr : n.getNarrativeRelations()) {
            for (NarrativeBinding nb : ras.get(nr).getBindings()) {
                try {
                    bw.write(nb.getImA().getE().getA().getTitle() + " (" + nb.getImA().getEventReduction().size()
                            + ") " + nr.getLabel() + " "
                            + nb.getImB().getE().getA().getTitle() + " (" + nb.getImB().getEventReduction().size()
                            + ") -> " + nb.getScore().getScore());
                    bw.newLine();
                } catch (Exception ex) {
                    bw.write("No successful binding!");
                    bw.newLine();
                }
            }
        }
        for (NarrativeBinding nb : successful) {
            bw.write(nb.getImA().getE().getD().getTitle() + " <--> " + nb.getImB().getE().getD().getTitle());
            bw.newLine();
        }
        bw.close();
    }

}
