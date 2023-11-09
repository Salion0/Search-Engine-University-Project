package it.unipi.mircv;

public class Config {
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
    public static final int LEXICON_ENTRY_LENGTH = TERM_BYTES_LENGTH+OFFSET_BYTES_LENGTH+DOCUMFREQ_BYTES_LENGTH+COLLECTIONFREQ_BYTES_LENGTH;
    public static final int POSTING_LIST_BLOCK_LENGTH = 3; //Expressed in element
    public static final String LEXICON_FILE = "data/lexicon.dat";
    public static final String TERM_FREQ_FILE = "data/termFreq.dat";
    public static final String DOC_ID_FILE = "data/docIds.dat";

    public static final int MAX_NUM_DOC_RETRIEVED = 20;

}
