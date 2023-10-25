package it.unipi.mircv.indexing;

import java.nio.ByteBuffer;

public class LexiconEntry {
    private int documentFreq;
    private int collectionFreq;
    private PostingList postingList;
    public LexiconEntry(){}
    public LexiconEntry(int df, int cf, PostingList postingList){
        this.documentFreq = df;
        this.collectionFreq = cf;
        this.postingList=postingList;
    }
    public int getDf() {
        return documentFreq;
    }

    public void setDf(int df) {
        this.documentFreq = df;
    }

    public int getCf() {
        return collectionFreq;
    }

    public void setCf(int tf) {
        this.collectionFreq = tf;
    }

    public PostingList getPostingList() {
        return postingList;
    }

    public void setPostingList(PostingList postingList) {
        this.postingList = postingList;
    }

}
