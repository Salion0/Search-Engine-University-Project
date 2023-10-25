package it.unipi.mircv.indexing;
import java.util.ArrayList;


public class DocumentIndex {
    private final ArrayList<Integer> documentLengths = new ArrayList<>();

    public void add(int docLength){
        documentLengths.add(docLength);
    }
    public int get(int docId){
        return documentLengths.get(docId);
    }

}
