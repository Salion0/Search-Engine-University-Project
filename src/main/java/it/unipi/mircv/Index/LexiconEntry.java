package it.unipi.mircv.Index;

public class LexiconEntry {

    private String term;
    private int documentFreq;
    private int collectionFreq;
    private int offset;

    private PostingList postingList;
    public LexiconEntry(){}
    public LexiconEntry(String term, int cf, int df, PostingList postingList){
        this.term= term;
        this.collectionFreq = cf;
        this.documentFreq = df;
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

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
