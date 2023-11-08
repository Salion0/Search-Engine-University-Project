package it.unipi.mircv.Index;

public class PostingElement {
    private final int docId;
    private final int termFreq;

    public PostingElement(int docId, int termFreq) {
        this.docId = docId;
        this.termFreq = termFreq;
    }
    public int getDocId() { return docId; }
    public int getTermFreq() { return termFreq; }
    @Override
    public String toString(){
        return "docId: " + docId + " - termFreq: "+ termFreq;
    }
}
