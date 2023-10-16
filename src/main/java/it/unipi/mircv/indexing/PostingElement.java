package it.unipi.mircv.indexing;

public class PostingElement {
    private int docId;
    private int occurrences;
    //altri campi opzionali qui
    public PostingElement(int docId,int occurrences) {
        this.docId = docId;
        this.occurrences = occurrences;
    }
    public PostingElement(int docId) {
        this.docId = docId;
    }
    public void addOccurrence() {
        occurrences += 1;
    }
    public int getDocId() { return docId; }
    public int getOccurrences() { return occurrences; }
    public String toString(){
        return "Doc ID: "+getDocId()+" - freq: "+getOccurrences();
    }

}