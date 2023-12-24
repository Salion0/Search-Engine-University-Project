package it.unipi.mircv;

public class Config {
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
            DOCUMFREQ_BYTES_LENGTH + COLLECTIONFREQ_BYTES_LENGTH + UPPER_BOUND_SCORE_LENGTH + UPPER_BOUND_SCORE_LENGTH +
            NUM_BYTE_TO_READ_BYTE_LENGTH + NUM_BYTE_TO_READ_BYTE_LENGTH + OFFSET_SKIP_DESC_BYTES_LENGTH;
    public static final int LEXICON_ENTRY_LENGTH = TERM_BYTES_LENGTH + OFFSET_BYTES_LENGTH + DOCUMFREQ_BYTES_LENGTH
            + COLLECTIONFREQ_BYTES_LENGTH + UPPER_BOUND_SCORE_LENGTH + UPPER_BOUND_SCORE_LENGTH + OFFSET_SKIP_DESC_BYTES_LENGTH;
    public static final int SKIP_DESC_ENTRY = 4 + OFFSET_BYTES_LENGTH;
    public static final int SKIP_DESC_ENTRY_COMPRESSION = 4 + OFFSET_COMPRESS_BYTES_LENGTH + 4 + OFFSET_COMPRESS_BYTES_LENGTH + 4;
    public static final int POSTING_LIST_BLOCK_LENGTH = 10000; //Expressed in element
    public static String INDEX_PATH = "data";
    public static String LEXICON_FILE = INDEX_PATH + "/lexicon.dat";
    public static String TERM_FREQ_FILE = INDEX_PATH + "/termFreq.dat";
    public static String DOC_ID_FILE = INDEX_PATH + "/docIds.dat";
    public static String DOCUMENT_INDEX_FILE = INDEX_PATH + "/documentIndex.dat";
    public static String POSTING_LIST_DESC_FILE = INDEX_PATH + "/postingListDesc.dat";
    public static final int MAX_NUM_DOC_RETRIEVED = 10;
    public static final int MIN_NUM_POSTING_TO_SKIP = 100; // vecchio valore era 10

}
