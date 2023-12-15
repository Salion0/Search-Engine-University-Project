package it.unipi.mircv;

import ca.rmen.porterstemmer.PorterStemmer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import static it.unipi.mircv.Parameters.stopWords;

public class Utils {
    private static final PorterStemmer porterStemmer = new PorterStemmer();

    //PER LA COMPRESSIONE
    static public void printReverseBytes(byte[] bytesToPrint){
        bytesToPrint = reverseByteArray(bytesToPrint);
        System.out.print("bytes: " + bytesToPrint.length + "\tcode: ");
        for (byte b :bytesToPrint) {
            for (int i = 7; i >= 0; i--) {
                byte bit = (byte) ((b >> i) & 1);
                System.out.print(bit);
            }System.out.print(" ");
        }System.out.println();
    }

    static public void printBytes(byte[] bytesToPrint){
        System.out.print("bytes: " + bytesToPrint.length + "\tcode: ");
        for (byte b :bytesToPrint) {
            for (int i = 0; i < 8; i++) {
                byte bit = (byte) ((b >> i) & 1);
                System.out.print(bit);
            }System.out.print(" ");
        }System.out.println();
    }
    static public void printByte(byte b){
        System.out.print("bytes: 1" + "\tcode: ");
        for (int i = 0; i < 8; i++) {
            byte bit = (byte) ((b >> i) & 1);
            System.out.print(bit);
        }System.out.println();
    }

    public static byte[] reverseByteArray(byte[] array) {
        int length = array.length;
        byte[] reversedArray = new byte[length];

        for (int i = 0; i < length; i++) {
            reversedArray[i] = array[length - 1 - i];
        }
        return reversedArray;
    }

    //PER LE STOPWORDS
    public static void loadStopWordList() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File("stop_words_english.json");
            stopWords = objectMapper.readValue(file, new TypeReference<>() {}); // Read the JSON file into a List

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] removeStopWords(String[] queryTerms) throws IOException {
        ArrayList<String> filteredTerms = new ArrayList<>();
        for (String term : queryTerms) {
            if (!seekInStopwords(term)) filteredTerms.add(term);
        }
        return filteredTerms.toArray(new String[0]);
    }

    public static boolean seekInStopwords(String term) throws IOException {
        int l = 0, r = stopWords.size() - 1;
        while (l <= r) {
            int m = l + (r - l) / 2;
            int res = term.compareTo(stopWords.get(m));
            if (res == 0) return true;
            if (res > 0) l = m + 1;
            else r = m - 1;
        }
        return false;
    }

    public static String[] tokenization(String doc) {
        //html tags removal
        doc = doc.replaceAll("<[^>]*>", "");
        //punctuation and whitespace
        String result = doc.replaceAll("\\p{Punct}","").toLowerCase();
        return result.split("\\s+");
    }

    public static void cleanFolder(String folderName) throws IOException {
        //function called every time the indexing starts in order to clean up the folder where blocks are stored
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

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists())
            if (file.delete()) System.out.println("File deleted: " + file.getName());
            else System.out.println("File not deleted");
        else System.out.println("File does not exist.");
    }


    public static String stemWord(String toStem){
        return porterStemmer.stemWord(toStem);
    }
    public static String[] stemPhrase(String[] phrase){
        for(int i = 0; i < phrase.length; i++){
            phrase[i] = porterStemmer.stemWord(phrase[i]);
        }
        return  phrase;
    }

}
