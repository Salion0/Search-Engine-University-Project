package it.unipi.mircv.Index;

public class Config {
    public static final int TERM_BYTES_LENGTH = 64;
    public static final int OFFSET_BYTES_LENGTH = 4;
    public static final int DOCUMFREQ_BYTES_LENGTH = 4;

    public static final int COLLECTIONFREQ_BYTES_LENGTH = 4;

    public static final int LEXICON_ENTRY_LENGTH = TERM_BYTES_LENGTH+OFFSET_BYTES_LENGTH+DOCUMFREQ_BYTES_LENGTH;//TODO aggiungere collection freq

}
