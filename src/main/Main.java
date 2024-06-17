package main;

import execution.Pipeline;
import system.CausalityAssessment;
import ui.applicationSelect;

import java.io.IOException;
import java.net.URISyntaxException;

import static ui.applicationSelect.initializeApplication;

public class Main {

    public static void main(String[] args) throws IOException {
        CausalityAssessment.computeFullALift();
        CausalityAssessment.printOut();
    }

    /*public static void main(String[] args) throws IOException, URISyntaxException {
        path = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        String[] sub = path.split("/");
        path = "";
        for (int i = 0; i <= sub.length-2; i++) {
            path = path + "/" + sub[i];
        }
        initializeApplication(path);
        WriteDataSet.printDataSetHeaders(Vars.getDataSets(),new File(Vars.getOutputPath() + "/Headers.txt"));
    }*/

    /*public static void main(String[] args) throws IOException, URISyntaxException {
        String[] sub = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().split("/");
        StringBuilder path = new StringBuilder();
        for (int i = 0; i <= sub.length-2; i++) {
            path.append("/").append(sub[i]);
        }
        if (args.length == 0) {
            initializeApplication(path.toString());
            applicationSelect.selectOption(path.toString());
        } else {
            Pipeline.runNoInput(path.toString(), args[0]);
        }

    }*/

}
