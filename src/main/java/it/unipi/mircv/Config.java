package it.unipi.mircv;

import java.util.List;

public class Config {
    public static List<String> stopWords;
    public static int collectionSize;
    public static float avgDocLen;
    public static int[] docsLen;
    public static final int MEMORY_THRESHOLD_PERC = 8;
    public static final int TERM_BYTES_LENGTH = 64;
    public static final int DOC_ID_LENGTH = 4;
    public static final int TERM_FREQ_LENGTH = 4;
    public static final int OFFSET_BYTES_LENGTH = 4;
    public static final int OFFSET_COMPRESS_BYTES_LENGTH = 8;
    public static final int DOCUMFREQ_BYTES_LENGTH = 4;
    public static final int DOCNO_BYTES_LENGTH = 12;
    public static final int AVGDOCLENGHT_BYTES_LENGTH = 4;
    public static final int DOCLENGTH_BYTES_LENGTH = 4;
    public static final int NUM_DOC_BYTES_LENGTH = 4;
    public static final int COLLECTIONFREQ_BYTES_LENGTH = 4;
    public static final int OFFSET_SKIP_DESC_BYTES_LENGTH = 4;
    public static final int UPPER_BOUND_SCORE_LENGTH = 4;
    public static final int NUM_BYTE_TO_READ_BYTE_LENGTH = 4;
    public static final int LEXICON_COMPRESS_ENTRY_LENGTH = TERM_BYTES_LENGTH +
            OFFSET_COMPRESS_BYTES_LENGTH + OFFSET_COMPRESS_BYTES_LENGTH +
            DOCUMFREQ_BYTES_LENGTH + COLLECTIONFREQ_BYTES_LENGTH + UPPER_BOUND_SCORE_LENGTH +
            NUM_BYTE_TO_READ_BYTE_LENGTH + NUM_BYTE_TO_READ_BYTE_LENGTH + OFFSET_SKIP_DESC_BYTES_LENGTH;
    public static final int LEXICON_ENTRY_LENGTH = TERM_BYTES_LENGTH + OFFSET_BYTES_LENGTH + DOCUMFREQ_BYTES_LENGTH
            + COLLECTIONFREQ_BYTES_LENGTH + UPPER_BOUND_SCORE_LENGTH + OFFSET_SKIP_DESC_BYTES_LENGTH;
    public static final int SKIP_DESC_ENTRY = 4 + OFFSET_BYTES_LENGTH;
    public static final int SKIP_DESC_ENTRY_COMPRESSION = 4 + OFFSET_COMPRESS_BYTES_LENGTH + 4 + OFFSET_COMPRESS_BYTES_LENGTH + 4;
    public static final int POSTING_LIST_BLOCK_LENGTH = 10000; //Expressed in element
    public static final String LEXICON_FILE = "data/lexicon.dat";
    public static final String TERM_FREQ_FILE = "data/termFreq.dat";
    public static final String DOC_ID_FILE = "data/docIds.dat";
    public static final String POSTING_LIST_DESC_FILE = "data/postingListDesc.dat";
    public static final int MAX_NUM_DOC_RETRIEVED = 20;
    public static final int MIN_NUM_POSTING_TO_SKIP = 10; // vecchio valore era 10
    public static final int CACHE_SIZE = 100000; //Expressed in number of entries

    // ************************* FLAG ********************************************
    public static boolean flagCompressedReading;
    public static boolean flagStopWordRemoval;
    public static boolean flagStemming;
    public enum QueryProcessor { DISJUNCTIVE, CONJUNCTIVE, DISJUNCTIVE_MAX_SCORE, CONJUNCTIVE_MAX_SCORE }
    public enum Score{ BM25, FTIDF }
}
