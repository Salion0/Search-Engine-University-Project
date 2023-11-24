package it.unipi.mircv;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Config {
    public static List<String> stopWords;
    public static int collectionSize;
    public static float avgDocLen;
    public static final int MEMORY_THRESHOLD_PERC = 8;
    public static final int TERM_BYTES_LENGTH = 64;
    public static final int DOC_ID_LENGTH = 4;
    public static final int TERM_FREQ_LENGTH = 4;
    public static final int OFFSET_BYTES_LENGTH = 4;
    public static final int DOCUMFREQ_BYTES_LENGTH = 4;
    public static final int DOCNO_BYTES_LENGTH = 12;
    public static final int AVGDOCLENGHT_BYTES_LENGTH = 4;
    public static final int DOCLENGTH_BYTES_LENGTH = 4;
    public static final int NUM_DOC_BYTES_LENGTH = 4;
    public static final int COLLECTIONFREQ_BYTES_LENGTH = 4;
    public static final int OFFSET_SKIP_DESC_BYTES_LENGTH = 4;
    public static final int UPPER_BOUND_SCORE_LENGTH = 4;
    public static final int LEXICON_ENTRY_LENGTH = TERM_BYTES_LENGTH + OFFSET_BYTES_LENGTH + DOCUMFREQ_BYTES_LENGTH
            + COLLECTIONFREQ_BYTES_LENGTH + UPPER_BOUND_SCORE_LENGTH + OFFSET_SKIP_DESC_BYTES_LENGTH;
    public static final int POSTING_LIST_BLOCK_LENGTH = 64000; //Expressed in element, vecchio valore era 20
    public static final int POSTING_LIST_BLOCK_LENGTH_BYTE = 4000; //Expressed in byte
    public static final String LEXICON_FILE = "data/lexicon.dat";
    public static final String TERM_FREQ_FILE = "data/termFreq.dat";
    public static final String DOC_ID_FILE = "data/docIds.dat";
    public static final String POSTING_LIST_DESC_FILE = "data/postingListDesc.dat";
    public static final int MAX_NUM_DOC_RETRIEVED = 20;
    public static final int MIN_NUM_POSTING_TO_SKIP = 10; // vecchio valore era 10
    public static final int MEMORY_THRESHOLD = 8; //Expressed in percentage
    public static void loadStopWordList() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File("stop_words_english.json");
            stopWords = objectMapper.readValue(file, new TypeReference<>() {}); // Read the JSON file into a List

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
