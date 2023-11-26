package it.unipi.mircv.Query;

import static it.unipi.mircv.Config.avgDocLen;
import static it.unipi.mircv.Config.collectionSize;

public class ScoreFunction {
    public static float BM25(int termFrequency, int documentLength, int documentFrequency) {
        return (float) (( termFrequency / (termFrequency + 1.5 * ((1 - 0.75) + 0.75*(documentLength / avgDocLen))) )
                * Math.log10((float)collectionSize/documentFrequency));
    }
    public float computeIDF(int documentFrequency) {
        return (float) Math.log10((float) documentFrequency/collectionSize);
    }
    public float computeTFIDF(int termFrequency,int documentFrequency) {
        return (float) ((1 + Math.log10(termFrequency)) * Math.log10((float) documentFrequency/collectionSize));
    }
}
