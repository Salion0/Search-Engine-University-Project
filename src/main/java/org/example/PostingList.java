package org.example;

import java.util.ArrayList;

public class PostingList {

    private int blockId;
    private ArrayList<PostingElement> postingList = new ArrayList<PostingElement>();

    public PostingList(int blockId, PostingElement postingElement) {
        this.blockId = blockId;
        postingList.add(postingElement);
    }

    public PostingList(int blockId) {
        this.blockId = blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public void addPostingElement(PostingElement postingElement) {
        postingList.add(postingElement);
    }

    public int getBlockId() {return blockId;}
    public ArrayList<PostingElement> getPostingList() {return postingList;}

}
