package ui;

import evaluation.EvaluationTest;
import execution.ExtendedPipeline;
import execution.Pipeline;
import io.Vars;
import system.DataSetAugmentation;
import system.Helper;
import system.RelatedAttributeIdentification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Scanner;

public class applicationSelect {

    static File configFile;

    public static void selectOption(String path) throws IOException {
        //initializeApplication(path);
        Scanner sc = new Scanner(System.in);
        System.out.println();
        System.out.println("Select Application Mode:");
        System.out.println();
        System.out.println("--0: Run BiND");
        System.out.println("--------Evaluation--------");
        System.out.println("--1: Complete Pipeline");
        System.out.println("--2: Partial Execution");
        System.out.println("-------------------------");
        System.out.println("--3: Configure BiND (Currently not implemented)");
        System.out.println("--4: Prepare Embeddings");
        System.out.println("--5: Exit");
        System.out.println();
        System.out.println("--------Completeness Check--------");
        System.out.println("--6: User Dialog");
        System.out.println("--------Dataset Augmentation--------");
        System.out.println("--7: User Dialog");
        System.out.println("--8: List");
        System.out.println("-------------------------");
        System.out.println("--9: Compute Relatedness");
        System.out.println();
        System.out.println("-------------------------");
        System.out.println("--10: Find Dataset");
        System.out.println("--11: Write Context Attributes");
        System.out.println("--12: Write Data Categories");
        String input = sc.nextLine();
        switch (input) {
            case("0"):
                Pipeline.runBiND();
                break;
            case("1"):
                selectEvaluationMode("ra");
                break;
            case("2"):
                selectPartialExecution();
                break;
            case("3"):
                break;
            case("4"):
                Vars.prepareEmbedding();
                selectOption(path);
                break;
            case("5"):
                return;
            case("6"):
                ExtendedPipeline.dialog();
                break;
            case("7"):
                DataSetAugmentation.dialog(path);
                break;
            case("8"):
                DataSetAugmentation.augmentList(new File(Vars.getAugmentPath()));
                break;
            case("9"):
                RelatedAttributeIdentification.identifyRelated(path);
                break;
            case ("10"):
                Helper.findDataSet();
                selectOption(path);
                break;
            case ("11"):
                Helper.findCommonAttributes();
                selectOption(path);
                break;
            case ("12"):
                Helper.writeContextCategories();
                selectOption(path);
                break;
            default:
                System.out.println("Please Select a Mode");
                selectOption(path);
        }
        sc.close();
    }

    public static void initializeApplication(String path) throws IOException {
        configFile = new File(path + "/config.txt");
        Vars.initBiND(configFile);
        long loadStarttime = System.nanoTime();
        Vars.loadInput(new File(Vars.getDataPath()), new File(Vars.getNarrativePath()),Vars.getDelim());
        Vars.loadEmbeddings(",");
        long loadRuntime = System.nanoTime() - loadStarttime;
        System.out.println("Load Time: " + (double)loadRuntime / 1000000000 + " seconds");
    }

    public static void selectEvaluationMode(String opt) throws IOException {
        double tEM,tPM,tRA;
        Scanner sc = new Scanner(System.in);
        System.out.println("Select Evaluation Mode:");
        System.out.println("--0: Full Evaluation");
        System.out.println("--1: Single Threshold");
        String input = sc.nextLine();
        switch (input) {
            case ("0"):
                BigDecimal t = new BigDecimal("1.0");
                BigDecimal delta = new BigDecimal("0.05");
                switch (opt) {
                    case ("em"):
                        while(t.doubleValue() >= 0.0) {
                            EvaluationTest.eventMatchEvaluation(t.doubleValue(),false);
                            t = t.subtract(delta);
                        }
                        break;
                    case ("pm"):
                        System.out.println("Set EventMatching threshold");
                        tEM = Double.parseDouble(sc.nextLine());
                        while(t.doubleValue() >= 0.0) {
                            EvaluationTest.propertyMatchEvaluation(tEM,t.doubleValue(),false);
                            t = t.subtract(delta);
                        }
                        break;
                    case ("im"):
                        System.out.println("Set EventMatching threshold");
                        tEM = Double.parseDouble(sc.nextLine());
                        while(t.doubleValue() >= 0.0) {
                            EvaluationTest.instanceMatchEvaluation(tEM,t.doubleValue(),false);
                            t = t.subtract(delta);
                        }
                        break;
                    case ("ra"):
                        System.out.println("Set EventMatching threshold");
                        tEM = Double.parseDouble(sc.nextLine());
                        System.out.println("Set PropertyMatching threshold");
                        tPM = Double.parseDouble(sc.nextLine());
                        while(t.doubleValue() >= 0.0) {
                            EvaluationTest.relationAssessmentEvaluation(tEM,tPM,t.doubleValue(),false);
                            t = t.subtract(delta);
                        }
                        break;
                    default:
                }
                break;
            case ("1"):
                switch (opt) {
                    case ("em"):
                        System.out.println("Set EventMatching threshold");
                        tEM = Double.parseDouble(sc.nextLine());
                        EvaluationTest.eventMatchEvaluation(tEM,true);
                        break;
                    case ("pm"):
                        System.out.println("Set EventMatching threshold");
                        tEM = Double.parseDouble(sc.nextLine());
                        System.out.println("Set PropertyMatching threshold");
                        tPM = Double.parseDouble(sc.nextLine());
                        EvaluationTest.propertyMatchEvaluation(tEM,tPM,true);
                        break;
                    case ("im"):
                        System.out.println("Set EventMatching threshold");
                        tEM = Double.parseDouble(sc.nextLine());
                        System.out.println("Set PropertyMatching threshold");
                        tPM = Double.parseDouble(sc.nextLine());
                        EvaluationTest.instanceMatchEvaluation(tEM,tPM,true);
                        break;
                    case ("ra"):
                        System.out.println("Set EventMatching threshold");
                        tEM = Double.parseDouble(sc.nextLine());
                        System.out.println("Set PropertyMatching threshold");
                        tPM = Double.parseDouble(sc.nextLine());
                        System.out.println("Set RelationAssessment threshold");
                        tRA = Double.parseDouble(sc.nextLine());
                        EvaluationTest.relationAssessmentEvaluation(tEM,tPM,tRA,true);
                        break;
                    default:
                }
                break;
            default:
                System.out.println("Please Select a Mode");
                selectEvaluationMode(opt);
        }
        sc.close();
    }

    public static void selectPartialExecution() throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Select Steps to run:");
        System.out.println("--0: Only Event Matching");
        System.out.println("--1: Event- and Property Matching");
        System.out.println("--2: Event-, Property- and Instance Matching");
        String input = sc.nextLine();
        switch (input) {
            case("0"):
                selectEvaluationMode("em");
                break;
            case("1"):
                selectEvaluationMode("pm");
                break;
            case("2"):
                selectEvaluationMode("im");
                break;
            default:
                System.out.println("Please Select an Option");
        }
        sc.close();
    }
}
