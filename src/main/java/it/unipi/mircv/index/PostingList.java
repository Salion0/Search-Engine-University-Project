package it.unipi.mircv.index;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class PostingList {
    private final ArrayList<PostingElement> postingList = new ArrayList<>();
    public PostingList() {}
    public PostingList(PostingElement postingElement) {
        postingList.add(postingElement);
    }
    public void addPostingElement(PostingElement postingElement) {
        postingList.add(postingElement);
    }

    public void addPostingList(PostingList pl){
        postingList.addAll(pl.getList());
    }
    public ArrayList<PostingElement> getList() {return postingList;}
    public int getSize() {return postingList.size();}

    public byte[][] getBytes(){
        int bufferSize = postingList.size()*4;  //num of element times byte qt. for int
        ByteBuffer docIdBuffer = ByteBuffer.allocate(bufferSize);
        ByteBuffer termFreqBuffer = ByteBuffer.allocate(bufferSize);
        for (PostingElement pe: postingList){
            docIdBuffer.putInt(pe.getDocId());
            termFreqBuffer.putInt(pe.getTermFreq());
        }
        byte[][] postingListData = new byte[2][];
        postingListData[0] = docIdBuffer.array();
        postingListData[1] = termFreqBuffer.array();

        return postingListData;
    }

    public void addPostingList(){

    }
    @Override
    public String toString(){
        StringBuilder string = new StringBuilder();
        for (PostingElement pe : postingList){
            string.append(pe.toString()).append("\t");
        }
        return string.toString();
    }


}
