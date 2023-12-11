package it.unipi.mircv.index;
import it.unipi.mircv.compression.Unary;
import it.unipi.mircv.compression.VariableByte;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class PostingList2 {
    private final ArrayList<Integer> docIds;
    private final ArrayList<Integer> termFreqs;

    public PostingList2(){
        docIds = new ArrayList<Integer>();
        termFreqs = new ArrayList<Integer>();
    }
    public PostingList2(ArrayList<Integer> docIds, ArrayList<Integer> termFreqs) {
        this.docIds = docIds;
        this.termFreqs = termFreqs;
    }
    public void addDocId(int docId){
        docIds.add(docId);
    }
    public void addTermFreq(int termFreq){
        termFreqs.add(termFreq);
    }
    public void addPostingList(PostingList2 postingList2Attach){
        docIds.addAll(postingList2Attach.getDocIds());
        termFreqs.addAll(postingList2Attach.getTermFreqs());
    }
    public ArrayList<Integer> getDocIds() {
        return docIds;
    }
    public ArrayList<Integer> getSomeDocIds(int start, int end){
        return new ArrayList<>(docIds.subList(start, end));
    }
    public ArrayList<Integer> getSomeTermFreq(int start, int end){
        return  new ArrayList<>(termFreqs.subList(start, end));
    }
    public int getMaxDocId(){return docIds.get(docIds.size()-1);}
    public ArrayList<Integer> getTermFreqs() {
        return termFreqs;
    }

    //TODO test it
    public byte[][] getBytesCompressed(){
        byte[][] postingListData = new byte[2][];
        postingListData[0] = VariableByte.compress(docIds);
        postingListData[1] = Unary.compress(termFreqs);

        return postingListData;
    }
    public int getSize() {return docIds.size();}
    public byte[][] getBytes(){
        int bufferSize = docIds.size() * 4;  //num of element times byte qt. for int
        ByteBuffer docIdBuffer = ByteBuffer.allocate(bufferSize);
        ByteBuffer termFreqBuffer = ByteBuffer.allocate(bufferSize);
        for (int i = 0; i < docIds.size(); i++){
            docIdBuffer.putInt(docIds.get(i));
            termFreqBuffer.putInt(termFreqs.get(i));
        }
        byte[][] postingListData = new byte[2][];
        postingListData[0] = docIdBuffer.array();
        postingListData[1] = termFreqBuffer.array();

        return postingListData;
    }
    @Override
    public String toString(){
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < docIds.size(); i++){
            string.append(" | ");
            string.append(docIds.get(i));
            string.append("-");
            string.append(termFreqs.get(i));
        }
        return string.toString();
    }

}