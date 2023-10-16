package it.unipi.mircv.indexing;

import it.unipi.mircv.indexing.PostingElement;

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

}
