package it.unipi.mircv.Index;

public class PostingElement {
    private int docId;
    private int tf;
    //altri campi opzionali qui
    public PostingElement(int docId,int tf) {
        this.docId = docId;
        this.tf = tf;
    }
    public PostingElement(int docId) {
        this.docId = docId;
    }
    public void addTf() {
        tf += 1;
    }
    public int getDocId() { return docId; }
    public int getTf() { return tf; }
    public String toString(){
        return "doc id: "+getDocId()+" - freq: "+ getTf();
    }


}
