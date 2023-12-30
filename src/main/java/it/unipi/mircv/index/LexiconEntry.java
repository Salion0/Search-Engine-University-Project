package it.unipi.mircv.index;

public class LexiconEntry {
    /**
     * LexiconEntry is a class that represents a lexicon entry.
     * It contains the term, the collection frequency, the document frequency and the posting list.
     * Upperbound scores are stored in file cause it was not necessery to the indexing purpose cause they are
     * computed only during the merging phase.
     */

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
