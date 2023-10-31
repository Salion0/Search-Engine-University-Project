package it.unipi.mircv.Index;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class PostingList {

    private int position = 0;

    private ArrayList<PostingElement> postingList = new ArrayList<PostingElement>();
    public PostingList(PostingElement postingElement) {
        postingList.add(postingElement);
    }

    public PostingList() {}
    public void addPostingElement(PostingElement postingElement) {
        postingList.add(postingElement);
    }
    public ArrayList<PostingElement> getPostingList() {return postingList;}

    public int getSize() {return postingList.size();}

    public int currentDocId(){
        //return the current posting
        return this.postingList.get(position).getDocId();
    }
    public int currentTf(){
        return postingList.get(position).getTf();
    }
    public int next(){
        //moves the iterator to the next posting
        //it returns -1 if the posting list end is reached
        if(this.position == postingList.size())
            return -1;
        else{
            this.position +=1;
            return position;
        }
    }


    public void nextGEQ(){
        //TODO implement nextGEQ
    }
    public double score(String[] query){
        double score = 0;
        //Compute the score for the current pointed posting

        //TODO implement a score function
       return score;
    }

    public byte[][] getBytes(){
        int bufferSize = postingList.size()*4;  //num of element times byte qt. for int
        ByteBuffer docIdBuffer = ByteBuffer.allocate(bufferSize);
        ByteBuffer termFreqBuffer = ByteBuffer.allocate(bufferSize);
        for (PostingElement pe: postingList){
            docIdBuffer.putInt(pe.getDocId());
            termFreqBuffer.putInt(pe.getTf());
        }
        byte[][] postingListData = new byte[2][];
        postingListData[0] = docIdBuffer.array();
        postingListData[1] = termFreqBuffer.array();

        return postingListData;
    }
    public String toString(){
        String string = new String();
        for (PostingElement pe : postingList){
            string += pe.toString() + "\t";
        }
        return string;
    }
}
