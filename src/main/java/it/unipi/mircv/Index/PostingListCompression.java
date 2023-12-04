package it.unipi.mircv.Index;

import java.util.ArrayList;

import static it.unipi.mircv.Utils.printReverseBytes;

public class PostingListCompression {
    private ArrayList<Integer> docIds;
    private ArrayList<Integer> termFreq;

    public int getNumBytesDocIds(){
        return docIds.length;
    }
    public int getNumBytesTermFreqs(){
        return termFreq.length;
    }
    public byte[] getTermFreqs() {
        return termFreq;
    }
    public byte[] getDocIds() {
        return docIds;
    }
    public void print(){
        System.out.print("docIds comp: ");
        printReverseBytes(docIds);
        System.out.print("\ntermFreq comp: ");
        printReverseBytes(termFreq);
    }
    public void addPostingList(){

    }
}
