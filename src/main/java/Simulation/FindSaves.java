package Simulation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FindSaves {
    public static boolean DoNeedCreateNewSimulation;
    public static String SaveList;
    private static final String DEFAULT_SAVES_FOLDER = "src/main/resources/Saves";
    private static final String FILE_EXTENSION = ".json";

    public static void main(String[] args) {
        refreshSaveList();
    }

    public static boolean refreshSaveList() {
        return refreshSaveList(DEFAULT_SAVES_FOLDER);
    }

    public static boolean refreshSaveList(String folderPath) {
        if (folderPath == null || folderPath.trim().isEmpty()) {
            folderPath = DEFAULT_SAVES_FOLDER;
        }

        Path path = Paths.get(folderPath);
        File folder = path.toFile();
        
        System.out.println("[FindSaves]:Scanning folder: " + folder.getAbsolutePath());
        
        try {
            if (!Files.exists(path)) {
                System.out.println("[FindSaves]:Creating saves folder: " + folderPath);
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            System.err.println("[FindSaves]:Failed to create saves folder: " + e.getMessage());
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.ERROR, "[FindSaves]:Failed to create saves folder: " + e.getMessage());
            return false;
        }

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("[FindSaves]:Unknown folder path: " + folderPath);
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.WARN, "[FindSaves]:Unknown folder path: " + folderPath);
            CreateLogFile.getInstance().flush();
            return false;
        }
        File[] files = folder.listFiles();
        List<File> jsonFiles = new ArrayList<>();
        //列举出所有的文件
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(FILE_EXTENSION)) {
                    jsonFiles.add(file);
                }
            }
        }

        System.out.println("[FindSaves]:Found " + jsonFiles.size() + " files ending with " + FILE_EXTENSION);
        CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[FindSaves]:Found " + jsonFiles.size() + " files ending with " + FILE_EXTENSION);

        StringBuilder buildSaveList = new StringBuilder();
        for (File file : jsonFiles) {
            buildSaveList.append("  - ").append(file.getName()).append("\n");
        }

        SaveList = buildSaveList.toString();
        DoNeedCreateNewSimulation = jsonFiles.isEmpty();

        if (DoNeedCreateNewSimulation) {
            System.out.println("[FindSaves]:No files found");
            CreateLogFile.getInstance().log(CreateLogFile.LogLevel.INFO, "[FindSaves]:No files found");
        }

        CreateLogFile.getInstance().flush();
        return true;
    }

    public static boolean waitForFileCreation(String expectedFileName, long timeoutMs) {
        return waitForFileCreation(expectedFileName, timeoutMs, DEFAULT_SAVES_FOLDER);
    }

    public static boolean waitForFileCreation(String expectedFileName, long timeoutMs, String folderPath) {
        if (expectedFileName == null || expectedFileName.trim().isEmpty()) {
            return false;
        }

        long startTime = System.currentTimeMillis();
        Path filePath = Paths.get(folderPath, expectedFileName);

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (Files.exists(filePath)) {
                System.out.println("[FindSaves]:File detected: " + expectedFileName);
                return true;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println("[FindSaves]:File not found within timeout: " + expectedFileName);
        return false;
    }

}
