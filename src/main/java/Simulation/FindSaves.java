package Simulation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FindSaves {
    public static boolean DoNeedCreateNewSimulation;
    public static String SaveList;

    public static void main(String[] args) {
        String folderPath = "src/main/resources/Saves";
        String extension = ".json";
        File folder = new File(folderPath);
        System.out.println("[FindSaves]:Scanning folder: " + folder.getAbsolutePath());
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("[FindSaves]:Unknown folder path");
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.WARN,"[FindSaves]:Unknown folder path");
            CreateLogFile.getInstance().flush();
            return;
        }
        File[] files = folder.listFiles();
        List<File> jsonFiles = new ArrayList<>();
        //列举出所有的文件
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(extension)) {
                    jsonFiles.add(file);
                }
            }
        }
        System.out.println("[FindSaves]:Find " + jsonFiles.size() + " files end with " + extension);
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO,"[FindSaves]:Find " + jsonFiles.size() + " files end with " + extension);
        CreateLogFile.getInstance().flush();
        StringBuilder buildSaveList = new StringBuilder();
        for (File file : jsonFiles) {
            //System.out.println("  - " + file.getName());
            buildSaveList.append("  - ").append(file.getName()).append("\n");

        }
        SaveList = buildSaveList.toString();
        //DoNeedCreateNewSimulation = jsonFiles.isEmpty();//if jsonFiles.size() == 0,jsonFiles.isEmpty() == true
        if (jsonFiles.size() == 0) {
            System.out.println("[FindSaves]:No files found");
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO,"[FindSaves]:No files found");
            DoNeedCreateNewSimulation = true;
            return;
        }
    }
}
