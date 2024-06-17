package system;

import datasetComponents.Record;
import matches.EventMatch;
import matches.PropertyMatch;
import narrativeComponents.Entity;
import ontologyComponents.Concept;
import ontologyComponents.Ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CompletenessCheck {

    EventMatch e;
    Ontology o;
    HashMap<Entity,HashMap<String, HashSet<Record>>> matchingRecords;
    HashMap<Entity,HashMap<Integer, HashSet<Concept>>> conceptPartitions;
    HashMap<Entity,HashMap<Integer,Double>> levelCompleteness;

    HashMap<HashSet<Record>,Integer> recordPartitions;

    HashMap<String,HashSet<ArrayList<String>>> contextLevels;
    HashMap<Integer,Entity> contextIndex;
    HashMap<String,HashSet<ArrayList<String>>> coveredContexts;

    public CompletenessCheck(EventMatch e, Ontology o) {
        this.e = e;
        this.o = o;
        this.conceptPartitions = new HashMap<>();
        this.matchingRecords = identifyMatchingRecords();
        this.levelCompleteness = checkCompleteness();
        //this.recordPartitions = new HashMap<>();
        /*for (PropertyMatch p : e.getPm().values()) {
            recordPartitions = buildPartitions(p.getEP().getNodeB());
        }*/
        this.contextIndex = new HashMap<>();
        this.contextLevels = new HashMap<>();
        buildContext();
        this.coveredContexts = checkContexts();
    }

    public HashMap<String,HashSet<ArrayList<String>>> checkContexts() {
        HashMap<String,HashSet<ArrayList<String>>> coveredLvl = new HashMap<>();
        for (String s : contextLevels.keySet()) {
            HashSet<ArrayList<String>> covered = new HashSet<>();
            for (ArrayList<String> c : contextLevels.get(s)) {
                if (matchingRecords.containsKey(contextIndex.get(0))) {
                    //System.out.println(contextIndex.get(0).getLabel() + " has Records!");
                    if (matchingRecords.get(contextIndex.get(0)).containsKey(c.get(0))) {
                        //System.out.println(c.get(0) + " has also Records!");
                        HashSet<Record> rs = new HashSet<>(matchingRecords.get(contextIndex.get(0)).get(c.get(0)));
                        //System.out.println("--" + rs.size() + " Records");
                        if (c.size() > 1) {
                            for (int i = 1; i < c.size(); i++) {
                                boolean empty = true;
                                if (matchingRecords.containsKey(contextIndex.get(i))) {
                                    //System.out.println(contextIndex.get(i).getLabel() + " has Records!");
                                    if (matchingRecords.get(contextIndex.get(i)).containsKey(c.get(i))) {
                                        //System.out.println(c.get(i) + " has also Records!");
                                        rs.retainAll(matchingRecords.get(contextIndex.get(i)).get(c.get(i)));
                                        //System.out.println("--" + rs.size() + " Records remain");
                                        empty = false;
                                    }
                                }
                                if (empty) {
                                    rs.clear();
                                }
                            }
                        }
                        if (!rs.isEmpty()) {
                            covered.add(c);
                        }
                    }
                }
            }
            coveredLvl.put(s,covered);
        }
        return coveredLvl;
    }

    public void buildContext() {
        int ind = 0;
        for (PropertyMatch p : e.getPm().values()) {
            Entity ent = p.getEP().getNodeB();
            contextIndex.put(ind, ent);
            if (ind == 0) {
                if (conceptPartitions.containsKey(ent)) {

                    HashSet<ArrayList<String>> base = new HashSet<>();
                    ArrayList<String> lb = new ArrayList<>();
                    lb.add(0,entityLinking(ent).getName());
                    base.add(lb);
                    contextLevels.put("0",base);

                    for (Integer i : conceptPartitions.get(ent).keySet()) {
                        HashSet<ArrayList<String>> cont = new HashSet<>();
                        for (Concept c : conceptPartitions.get(ent).get(i)) {
                            ArrayList<String> l = new ArrayList<>();
                            l.add(0,c.getName());
                            cont.add(l);
                        }
                        String lvl = i.toString();
                        contextLevels.put(lvl,cont);
                    }
                } else {
                    HashSet<ArrayList<String>> cont = new HashSet<>();
                    ArrayList<String> l = new ArrayList<>();
                    l.add(0,ent.getLabel());
                    cont.add(l);
                    contextLevels.put("0",cont);
                }
            } else {
                HashMap<String,HashSet<ArrayList<String>>> newContextLevels = new HashMap<>();
                if (conceptPartitions.containsKey(ent)) {
                    for (String s : contextLevels.keySet()) {

                        HashSet<ArrayList<String>> bnc = new HashSet<>();
                        for (ArrayList<String> ol : contextLevels.get(s)) {
                            ArrayList<String> nl = new ArrayList<>(ol);
                            nl.add(ind,entityLinking(ent).getName());
                            bnc.add(nl);
                        }
                        String blvl = s + "0";
                        newContextLevels.put(blvl,bnc);

                        for (Integer i : conceptPartitions.get(ent).keySet()) {
                            HashSet<ArrayList<String>> nc = new HashSet<>();
                            for (ArrayList<String> ol : contextLevels.get(s)) {
                                for (Concept c : conceptPartitions.get(ent).get(i)) {
                                    ArrayList<String> nl = new ArrayList<>(ol);
                                    nl.add(ind,c.getName());
                                    nc.add(nl);
                                }
                            }
                            String lvl = s + i.toString();
                            newContextLevels.put(lvl,nc);
                        }
                    }

                } else {
                    for (String s : contextLevels.keySet()) {
                        HashSet<ArrayList<String>> nc = new HashSet<>();
                        for (ArrayList<String> ol : contextLevels.get(s)) {
                            ol.add(ind,ent.getLabel());
                            nc.add(ol);
                        }
                        String lvl = s + "0";
                        newContextLevels.put(lvl,nc);
                    }
                }
                contextLevels = newContextLevels;
            }
            ind++;
        }


    }

    public HashMap<Entity,HashMap<Integer,Double>> checkCompleteness() {
        HashMap<Entity,HashMap<Integer,Double>> lcs = new HashMap<>();
        for(PropertyMatch p : e.getPm().values()) {
            Entity ent = p.getEP().getNodeB();
            HashMap<Integer,Double> lc = new HashMap<>();
            if (conceptPartitions.containsKey(ent)) {
                for (Integer i : conceptPartitions.get(ent).keySet()) {
                    int count = 0;
                    for (Concept c : conceptPartitions.get(ent).get(i)) {
                        if (matchingRecords.get(ent).containsKey(c.getName())) {
                            count++;
                        }
                    }
                    int total = conceptPartitions.get(ent).get(i).size();
                    lc.put(i,(double)count / (double)total);
                }
                lcs.put(ent,lc);
            }
        }
        return lcs;
    }

    public HashMap<HashSet<Record>,Integer> buildPartitions(Entity ent) {
        HashMap<HashSet<Record>,Integer> newPartitions = new HashMap<>();
        if (recordPartitions.keySet().isEmpty()) {
            if (conceptPartitions.containsKey(ent)) {
                for (Integer i : conceptPartitions.get(ent).keySet()) {
                    HashSet<Record> subrs = new HashSet<>();
                    for (Concept c : conceptPartitions.get(ent).get(i)) {
                        subrs.addAll(matchingRecords.get(ent).get(c.getName()));
                    }
                    newPartitions.put(subrs,conceptPartitions.get(ent).get(i).size());
                }
            } else {
                HashSet<Record> subrs = new HashSet<>(matchingRecords.get(ent).get(ent.getLabel()));
                newPartitions.put(subrs,1);
            }
        } else {
            for (HashSet<Record> rs : recordPartitions.keySet()) {
                if (conceptPartitions.containsKey(ent)) {
                    for (Integer i : conceptPartitions.get(ent).keySet()) {
                        HashSet<Record> subrs = new HashSet<>();
                        int count = 0;
                        for (Concept c : conceptPartitions.get(ent).get(i)) {
                            if (matchingRecords.get(ent).containsKey(c.getName())) {
                                HashSet<Record> added = new HashSet<>(matchingRecords.get(ent).get(c.getName()));
                                added.retainAll(rs);
                                if (!added.isEmpty()) {
                                    count++;
                                }
                                subrs.addAll(added);
                            }
                        }
                        newPartitions.put(subrs,recordPartitions.get(rs) * count);
                    }
                } else {
                    HashSet<Record> subrs = new HashSet<>(matchingRecords.get(ent).get(ent.getLabel()));
                    subrs.retainAll(rs);
                    newPartitions.put(subrs,recordPartitions.get(rs));
                }
            }
        }
        return newPartitions;
    }

    public Concept entityLinking(Entity e) {
        for (Concept c : o.getConcepts().values()) {
            if (c.getName().toLowerCase().contains(e.getLabel().toLowerCase())) {
                return c;
            }
        }
        return null;
    }

    public HashMap<Integer,HashSet<Concept>> partsOfConcept(Concept c) {
        HashMap<Concept,Integer> toCheck = new HashMap<>();
        toCheck.put(c,0);
        HashSet<Concept> base = new HashSet<>();
        base.add(c);
        HashMap<Integer,HashSet<Concept>> parts = new HashMap<>();
        parts.put(0,base);
        while (!toCheck.isEmpty()) {
            Concept f = toCheck.keySet().iterator().next();
            for (Concept p : f.getParts()) {
                if (parts.containsKey(toCheck.get(f) + 1)) {
                    parts.get(toCheck.get(f) + 1).add(p);
                } else {
                    HashSet<Concept> level = new HashSet<>();
                    level.add(p);
                    parts.put(toCheck.get(f) + 1,level);
                }
                toCheck.put(p,toCheck.get(f) + 1);
            }
            toCheck.remove(f);
        }
        //System.out.println("Contains " + parts.size() + " parts");
        return parts;
    }

    public HashMap<Entity,HashMap<String,HashSet<Record>>> identifyMatchingRecords() {
        HashMap<Entity,HashMap<String,HashSet<Record>>> mr = new HashMap<>();
        for (PropertyMatch p : e.getPm().values()) {
            //System.out.println("Property: " + p.getEP().getNodeB().getLabel());
            HashMap<String,HashSet<Record>> emr = new HashMap<>();
            mr.put(p.getEP().getNodeB(),emr);
            Concept c = entityLinking(p.getEP().getNodeB());
            if (c != null) {
                //System.out.println("Linked to: " + c.getName());
                conceptPartitions.put(p.getEP().getNodeB(),partsOfConcept(c));
            }
        }
        for (Record r : e.getD().getRecords()) {
            for (PropertyMatch p : e.getPm().values()) {
                if (conceptPartitions.containsKey(p.getEP().getNodeB())) {
                    for (HashSet<Concept> hc : conceptPartitions.get(p.getEP().getNodeB()).values()) {
                        for (Concept c : hc) {
                            //System.out.println("Compare: " + r.getEntry(p.getA()).replace("\"","").trim().toLowerCase() + "vs." + c.getName().trim().toLowerCase());
                            if (r.getEntry(p.getA()).replace("\"","").trim().toLowerCase().contains(c.getName().trim().toLowerCase())
                            || c.getName().trim().toLowerCase().contains(r.getEntry(p.getA()).replace("\"","").trim().toLowerCase())) {
                                if (mr.get(p.getEP().getNodeB()).containsKey(c.getName())) {
                                    mr.get(p.getEP().getNodeB()).get(c.getName()).add(r);
                                } else {
                                    HashSet<Record> rs = new HashSet<>();
                                    rs.add(r);
                                    mr.get(p.getEP().getNodeB()).put(c.getName(),rs);
                                }
                            }
                        }
                    }
                } else {
                    if (r.getEntry(p.getA()).replace("\"","").trim().toLowerCase().contains(p.getEP().getNodeB().getLabel().trim().toLowerCase())
                    || p.getEP().getNodeB().getLabel().trim().toLowerCase().contains(r.getEntry(p.getA()).replace("\"","").trim().toLowerCase())) {
                        if (mr.get(p.getEP().getNodeB()).containsKey(p.getEP().getNodeB().getLabel())) {
                            mr.get(p.getEP().getNodeB()).get(p.getEP().getNodeB().getLabel()).add(r);
                        } else {
                            HashSet<Record> rs = new HashSet<>();
                            rs.add(r);
                            mr.get(p.getEP().getNodeB()).put(p.getEP().getNodeB().getLabel(),rs);
                        }
                    }
                }
            }
        }
        return mr;
    }

    /*public static HashSet<Record> findRecords(EventMatch em) {
        HashSet<Record> result = new HashSet<>();
        for (Record r : em.getD().getRecords()) {
            boolean addR = true;
            for (PropertyMatch p : em.getPm().values()) {
                boolean addP = false;
                try {
                    if (p.getEP().getNodeB().hasOperator()) {
                        switch (p.getEP().getNodeB().getOperator()) {
                            case "<" :
                                if (Double.parseDouble(r.getEntry(p.getA()).replace("\"","").trim()) < Double.parseDouble(p.getEP().getNodeB().getLabel().trim())) {
                                    addP = true;
                                }
                                break;
                            case ">" :
                                if (Double.parseDouble(r.getEntry(p.getA()).replace("\"","").trim()) > Double.parseDouble(p.getEP().getNodeB().getLabel().trim())) {
                                    addP = true;
                                }
                                break;
                            case "not" :
                                if (!r.getEntry(p.getA()).replace("\"","").trim().contains(p.getEP().getNodeB().getLabel().trim())) {
                                    addP = true;
                                }
                                break;
                            case "or" :
                                String[] options = p.getEP().getNodeB().getLabel().trim().split(":");
                                if (r.getEntry(p.getA()).replace("\"","").trim().contains(options[0])
                                        || r.getEntry(p.getA()).replace("\"","").trim().contains(options[1])) {
                                    addP = true;
                                }
                                break;
                            default:
                                break;
                        }
                    } else {
                        try {
                            double v1 = Double.parseDouble(r.getEntry(p.getA()).replace("\"","").trim());
                            double v2 = Double.parseDouble(p.getEP().getNodeB().getLabel().trim());
                            if (v1 == v2) {
                                addP = true;
                            }
                        } catch (Exception e) {
                            if (r.getEntry(p.getA()).replace("\"","").trim().contains(p.getEP().getNodeB().getLabel().trim())) {
                                addP = true;
                            }
                        }
                    }
                } catch (Exception e) {
                    //Remove try later!!
                }
                if (!addP) {
                    addR = false;
                    try {
                        Double.parseDouble(p.getEP().getNodeB().getLabel().trim());
                    } catch (Exception e) {
                        if (p.getDS().getTitle().contains(p.getEP().getNodeB().getLabel().trim())) {
                            addR = true;
                            //Change Matched Attribute to Title
                        }
                    }

                }
            }
            if (addR) {
                result.add(r);
            }
        }
        return result;
    }*/

    public HashMap<Entity, HashMap<Integer, HashSet<Concept>>> getConceptPartitions() {
        return conceptPartitions;
    }

    public HashMap<Entity, HashMap<String, HashSet<Record>>> getMatchingRecords() {
        return matchingRecords;
    }

    public HashMap<Entity, HashMap<Integer, Double>> getLevelCompleteness() {
        return levelCompleteness;
    }

    public HashMap<String, HashSet<ArrayList<String>>> getContextLevels() {
        return contextLevels;
    }

    public HashMap<String, HashSet<ArrayList<String>>> getCoveredContexts() {
        return coveredContexts;
    }

    public HashMap<Integer, Entity> getContextIndex() {
        return contextIndex;
    }
}
