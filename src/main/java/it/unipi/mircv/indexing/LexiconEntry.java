package it.unipi.mircv.indexing;

import java.nio.ByteBuffer;

public class LexiconEntry {

    private int df;
    private int cf;
    private PostingList postingList;

    public LexiconEntry(){}
    public LexiconEntry(int df, int cf, PostingList postingList){
        this.df=df;
        this.cf=cf;
        this.postingList=postingList;
    }
    public int getDf() {
        return df;
    }

    public void setDf(int df) {
        this.df = df;
    }

    public int getCf() {
        return cf;
    }

    public void setCf(int tf) {
        this.cf = tf;
    }

    public PostingList getPostingList() {
        return postingList;
    }

    public void setPostingList(PostingList postingList) {
        this.postingList = postingList;
    }

}
