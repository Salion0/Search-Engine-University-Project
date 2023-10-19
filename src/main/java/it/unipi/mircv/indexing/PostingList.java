package it.unipi.mircv.indexing;

import it.unipi.mircv.indexing.PostingElement;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class PostingList {

    private ArrayList<PostingElement> postingList = new ArrayList<PostingElement>();
    public PostingList(PostingElement postingElement) {
        postingList.add(postingElement);
    }
    public void addPostingElement(PostingElement postingElement) {
        postingList.add(postingElement);
    }
    public ArrayList<PostingElement> getPostingList() {return postingList;}

    public int getSize() {return postingList.size();}

     //TODO
    public byte[][] getBytes(){
        int bufferSize = postingList.size()*4;  //num of element times byte qt. for int
        ByteBuffer docIdBuffer = ByteBuffer.allocate(bufferSize);
        ByteBuffer termFreqBuffer = ByteBuffer.allocate(bufferSize);
        for (PostingElement pe: postingList){
            docIdBuffer.putInt(pe.getDocId());
            termFreqBuffer.putInt(pe.getOccurrences());
        }
        byte[][] postingListData = new byte[2][];
        postingListData[0] = docIdBuffer.array();
        postingListData[1] = termFreqBuffer.array();

        return postingListData;
    }

    public byte[][] getBytes(ByteBuffer docIdBuffer,ByteBuffer termFreqBuffer){

        for (PostingElement pe: postingList){
            docIdBuffer.putInt(pe.getDocId());
            termFreqBuffer.putInt(pe.getOccurrences());
        }
        byte[][] postingListData = new byte[2][];
        postingListData[0] = docIdBuffer.array();
        postingListData[1] = termFreqBuffer.array();

        return postingListData;
    }
}
