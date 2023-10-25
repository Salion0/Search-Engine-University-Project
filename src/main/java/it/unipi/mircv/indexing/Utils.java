package it.unipi.mircv.indexing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Utils {

    //function called every time the indexing starts in order to clean up the folder where blocks are stored
    public static void cleanFolder(String folderName) throws IOException {
        File folder = new File(folderName);
        if(folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        if (file.delete()) System.out.println("File deleted: " + file.getName());
                        else System.err.println("It's impossible to delete the file: " + file.getName());
                    }
                }
            }
        } else Files.createDirectory(Paths.get("data"));
    }
}
