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

    public byte[] getBytes(){
        ByteBuffer dataBuffer = ByteBuffer.allocate(postingList.size()*8); //Each PostingElement occupies 8 bytes
        dataBuffer.position(0);
        for(PostingElement pe: postingList) {
            dataBuffer.put(pe.getBytes());
        }
        return dataBuffer.array();
    }

}
