package it.unipi.mircv.index;

public class PostingElement {
    /*
     * This class represents a posting element, i.e. a pair (docId, termFreq)
     */
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
