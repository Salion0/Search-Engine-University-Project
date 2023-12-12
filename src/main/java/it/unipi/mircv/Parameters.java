package it.unipi.mircv;

import java.util.List;

public class Parameters {
    public static List<String> stopWords;
    public static int collectionSize;
    public static float avgDocLen;
    public static int[] docsLen;
    public static boolean flagCompressedReading;
    public static boolean flagStopWordRemoval;
    public static boolean flagStemming;
    public static QueryProcessor queryProcessType;
    public static Score scoreType;
    public enum QueryProcessor {
        DISJUNCTIVE_DAAT, CONJUNCTIVE_DAAT, DISJUNCTIVE_MAX_SCORE,
        DISJUNCTIVE_DAAT_C, CONJUNCTIVE_DAAT_C, DISJUNCTIVE_MAX_SCORE_C
    }
    public enum Score{ BM25, FTIDF }
}