package it.unipi.mircv.query;

import static it.unipi.mircv.Parameters.avgDocLen;
import static it.unipi.mircv.Parameters.collectionSize;

public class  ScoreFunction{

    public static float BM25(int termFrequency, int documentLength, int documentFrequency) {
        return (termFrequency / ((0.75f*((float)documentLength/avgDocLen) + 0.25f) * 1.2f + termFrequency))
                * (float) Math.log10((float)collectionSize/documentFrequency);
    }
    public float computeIDF(int documentFrequency) {
        return (float) Math.log10((float) collectionSize/documentFrequency);
    }

    public static float computeTFIDF(int termFrequency, int documentFrequency) {
        return (float)(Math.log10(termFrequency) + 1) * (float)(Math.log10((float) collectionSize/documentFrequency));
    }
}
