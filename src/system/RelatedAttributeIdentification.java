package system;

import datasetComponents.Attribute;
import entityLinking.ConceptLink;
import io.Vars;
import ontologyComponents.Concept;
import ui.applicationSelect;

import java.io.*;
import java.util.*;

public class RelatedAttributeIdentification {

    public static String conceptPath = "/home/nagel/mesh_co_occurrences/attributes_annotated.txt";
    public static String coOccPath = "/home/nagel/mesh_co_occurrences/mesh_concepts_tf_idfs_sorted_tab.tsv";
    public static String docCountPath = "/home/nagel/mesh_co_occurrences/mesh_document_count.tsv";

    public static int totalDocCount = 34960700;

    public static HashMap<String, HashSet<Attribute>> conceptToAtrIndex = new HashMap<>();
    public static HashMap<Attribute, HashSet<Concept>> atrToConceptIndex = new HashMap<>();
    public static HashMap<String, Concept> conceptMap = new HashMap<>();
    public static HashSet<ConceptLink> clSet = new HashSet<>();

    public static void identifyRelated(String path) throws IOException {
        clSet = readConceptLinks(new File(conceptPath));
        loadCoOccurrences(new File(coOccPath));
        loadDocCount(new File(docCountPath));
        System.out.println("--------");
        for (String s : Vars.captionIndex.keySet()) {
            System.out.println(s);
        }
        System.out.println("--------");
        while (true) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Attribute to check (type 'back' to go to main menu or 'exit' to close the application):");
            String input = sc.nextLine();
            if (input.equals("back")) {
                applicationSelect.selectOption(path);
                break;
            }
            if (input.equals("exit")) {
                break;
            } else {
                for (Attribute a : Vars.captionIndex.get(input)) {
                    HashMap<Attribute, Double> scores = computeRelatedness(a);

                    LinkedHashMap<Attribute, Double> sorted = sortHashMapByValues(scores);

                    System.out.println("Attribute: " + a.getTitle());
                    for (Attribute aB : sorted.keySet()) {
                        if (sorted.get(aB) > 0.0) {
                            System.out.println("--" + aB.getTitle() + ": " + sorted.get(aB));
                        }
                    }
                    System.out.println();
                    break;
                }
            }
        }
    }

    public static LinkedHashMap<Attribute, Double> sortHashMapByValues(HashMap<Attribute, Double> passedMap) {
        List<Attribute> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Double> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues);

        LinkedHashMap<Attribute, Double> sortedMap = new LinkedHashMap<>();

        Iterator<Double> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Double val = valueIt.next();
            Iterator<Attribute> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Attribute key = keyIt.next();
                Double comp1 = passedMap.get(key);

                if (comp1.equals(val)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }

    public static HashMap<Attribute, Double> computeRelatedness(Attribute aA) {
        HashSet<Concept> linkedA = atrToConceptIndex.get(aA);
        HashMap<Attribute, Double> scores = new HashMap<>();
        for (Attribute aB : atrToConceptIndex.keySet()) {
            if (!aB.getTitle().equals("Comments")) {
                HashSet<Concept> linkedB = atrToConceptIndex.get(aB);
                if (linkedB.size() > 0) {
                    double atrScore = 0.0;
                    double atrCount = 0;
                    for (Concept cA : linkedA) {
                        double conScore = 0.0;
                        int conCount = 0;
                        for (Concept cB : linkedB) {
                            if (cA.getCoOccurrences().containsKey(cB)) {
                                conScore = conScore + (computeIdf(cB) * (cA.getCoOccurrences().get(cB) / cA.getMaxCoOcc()));
                                //conScore = conScore + ((0.5 * computeIdf(cB)) + (0.5 * (cA.getCoOccurrences().get(cB) / cA.getMaxCoOcc())));
                            }
                            if (cA == cB) {
                                conScore = conScore + 1.0;
                            }
                            conCount++;
                        }
                        if (conCount > 0) {
                            conScore = conScore / conCount;
                        }
                        atrScore = atrScore + conScore;
                        atrCount++;
                    }
                    if (atrCount > 0) {
                        atrScore = atrScore / atrCount;
                    }
                    scores.put(aB,atrScore);
                }
            }
        }
        return scores;
    }

    public static double computeIdf(Concept c) {
        return Math.log10(totalDocCount / c.getDocCount());
    }

    public static void loadDocCount(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        boolean header = true;
        while ((line = br.readLine()) != null) {
            if (!header) {
                String[] split = line.split("\\t");
                Concept cA = conceptMap.get("MESH:" + split[0]);
                int number = Integer.parseInt(split[2]);
                try {
                    cA.setDocCount(number);
                } catch (Exception ignored) {

                }
            } else {
                header = false;
            }
        }
        br.close();
    }

    public static void loadCoOccurrences(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        boolean header = true;
        while ((line = br.readLine()) != null) {
            if (!header) {
                String[] split = line.split("\\t");
                Concept cA = conceptMap.get(split[0]);
                Concept cB = conceptMap.get(split[2]);
                int number = Integer.parseInt(split[4]);
                try {
                    cA.getCoOccurrences().put(cB,number);
                    if (number > cA.getMaxCoOcc()) {
                        cA.setMaxCoOcc(number);
                    }
                } catch (Exception ignored) {

                }
            } else {
                header = false;
            }
        }
        br.close();
    }

    public static HashSet<ConceptLink> readConceptLinks(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        boolean inEntry = false;
        HashSet<Concept> cs = new HashSet<>();
        HashSet<Attribute> as = new HashSet<>();
        HashSet<ConceptLink> cls = new HashSet<>();
        while ((line = br.readLine()) != null) {
            if (!inEntry) {
                if (!line.equals("")) {
                    inEntry = true;
                    as = Vars.captionIndex.get(line);
                    cs = new HashSet<>();
                }
            } else {
                if (line.equals("")) {
                    inEntry = false;
                    try {
                        for (Attribute a : as) {
                            ConceptLink cl = new ConceptLink(a,cs);
                            cls.add(cl);
                            try {
                                atrToConceptIndex.put(a,cs);
                            } catch (Exception ignored) {

                            }
                        }
                    } catch (Exception ignored) {

                    }
                } else {
                    String[] split = line.split("\\t");
                    Concept c;
                    if (conceptMap.containsKey(split[1])) {
                        c = conceptMap.get(split[1]);
                    } else {
                        c = new Concept(split[1],split[2]);
                        conceptMap.put(split[1],c);
                    }
                    cs.add(c);
                    try {
                        conceptToAtrIndex.put(c.getId(),as);
                    } catch (Exception ignored) {

                    }
                }
            }
        }
        br.close();
        return cls;
    }

}
