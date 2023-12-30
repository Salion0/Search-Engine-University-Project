package it.unipi.mircv.index;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class PostingList {
    /*
     * PostingList is a list of PostingElement so it is implemented as an ArrayList
     * of PostingElement. It is used to store the posting list of a term.
     */
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
    @Override
    public String toString(){
        StringBuilder string = new StringBuilder();
        for (PostingElement postingElement : postingList) {
            string.append(" | ");
            string.append(postingElement.getDocId());
            string.append("-");
            string.append(postingElement.getTermFreq());
        }
        return string.toString();
    }
}
