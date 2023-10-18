package it.unipi.mircv.indexing;

public class Lexicon {
    private int df;
    private int cf;
    private long offset;
    private int postingListLength;
    private int blockID;

    public Lexicon(int df,
                   int cf,
                   long offset,
                   int postingListLength,
                   int blockID){
        this.df = df;
        this.cf = cf;
        this.offset = offset;
        this.postingListLength = postingListLength;
        this.blockID = blockID;
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

    public void setCf(int cf) {
        this.cf = cf;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public int getBlockID() {
        return blockID;
    }

    public void setBlockID(int blockID) {
        this.blockID = blockID;
    }

    public int getPostingListLength() {
        return postingListLength;
    }

    public void setPostingListLength(int postingListLength) {
        this.postingListLength = postingListLength;
    }
}
