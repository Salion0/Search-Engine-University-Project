package it.unipi.mircv.utility;

import ca.rmen.porterstemmer.PorterStemmer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static it.unipi.mircv.utility.Parameters.stopWords;
import static it.unipi.mircv.utility.Config.*;

public class Utils {
    private static final PorterStemmer porterStemmer = new PorterStemmer();

    public static byte[] reverseByteArray(byte[] array) {
        int length = array.length;
        byte[] reversedArray = new byte[length];

        for (int i = 0; i < length; i++) {
            reversedArray[i] = array[length - 1 - i];
        }
        return reversedArray;
    }

    //STOPWORDS
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

    //TOKENIZATION
    public static String[] tokenization(String doc) {
        //html tags removal
        doc = doc.replaceAll("<[^>]*>", "");
        //punctuation and whitespace
        String result = doc.replaceAll("\\p{Punct}","").toLowerCase();
        return result.split("\\s+");
    }

    //FOLDER MANAGEMENT
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
        } else Files.createDirectory(Paths.get(INDEX_PATH));
    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists())
            if (file.delete()) System.out.println("File deleted: " + file.getName());
            else System.out.println("File not deleted");
        else System.out.println("File does not exist.");
    }

    //STEMMING
    public static String stemWord(String toStem){
        return porterStemmer.stemWord(toStem);
    }
    public static void stemPhrase(String[] phrase){
        for(int i = 0; i < phrase.length; i++){
            phrase[i] = porterStemmer.stemWord(phrase[i]);
        }
    }

    //FILE PATHS
    public static void setFilePaths() {
        LEXICON_FILE = INDEX_PATH + "/lexicon.dat";
        TERM_FREQ_FILE = INDEX_PATH + "/termFreq.dat";
        DOC_ID_FILE = INDEX_PATH + "/docIds.dat";
        DOCUMENT_INDEX_FILE = INDEX_PATH + "/documentIndex.dat";
        POSTING_LIST_DESC_FILE = INDEX_PATH + "/postingListDesc.dat";
    }

    public static void printFilePaths() {
        System.out.println(INDEX_PATH);
        System.out.println(LEXICON_FILE);
        System.out.println(TERM_FREQ_FILE);
        System.out.println(DOC_ID_FILE);
        System.out.println(DOCUMENT_INDEX_FILE);
        System.out.println(POSTING_LIST_DESC_FILE);
    }

}
