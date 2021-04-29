package com.yscz.upgrade.tools;

public class BatFileTools {

    private static volatile BatFileTools batFileTools;

    private Runtime runtime = Runtime.getRuntime();

    public BatFileTools() {}

    public static synchronized BatFileTools getInstance() {
        if(batFileTools == null) {
            batFileTools = new BatFileTools();
        }
        return batFileTools;
    }

    public void execBatFile(String batFilePath) {
        Process process;
        try {
            process = runtime.exec(batFilePath);
            process.waitFor();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
