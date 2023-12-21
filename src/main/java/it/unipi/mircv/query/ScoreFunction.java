package it.unipi.mircv.query;

import static it.unipi.mircv.Parameters.avgDocLen;
import static it.unipi.mircv.Parameters.collectionSize;

public class  ScoreFunction{
    public static Float BM25(int termFrequency, int documentLength, int documentFrequency) {
        return (termFrequency / ((0.75f*((float)documentLength/avgDocLen) + 0.25f) * 1.2f + termFrequency))
                * (float) Math.log10((float)collectionSize/documentFrequency);
    }
    /*
    public static Float BM25(int termFrequency, int documentLength, int documentFrequency) {
        return (0.75f*(termFrequency / ((0.75f*((float)documentLength/avgDocLen) + 0.25f) * 1.2f + termFrequency))
                * (float) Math.log10((float)collectionSize/documentFrequency)) + (0.25f*computeTFIDF(termFrequency, documentFrequency));
    }
     */
    public Float computeIDF(int documentFrequency) {
        return (float) Math.log10((double) collectionSize/documentFrequency);
    }

    public static Float computeTFIDF(int termFrequency, int documentFrequency) {
        return (float) ((float)(Math.log10(termFrequency) + 1) * Math.log10((float) collectionSize/documentFrequency));
    }
}
