package it.unipi.mircv.Index;

public class PostingListBlock extends PostingList
{
    private int currentPosition;
    private int length;
    private int maxDocID;
    public PostingListBlock(int length){
        super();
        this.currentPosition = 0;
        this.length = length;
        this.maxDocID = getDocId(length - 1);
    }
    public int getPosition(){
        return this.currentPosition;
    }

    public int next(){
        if(currentPosition < length - 1) currentPosition += 1;
        else return -1;
        return currentPosition;
    }

    public int getMaxDocID(){
        return maxDocID;
    }
    public int getDocId(int position){
        if(position >= 0)
            return getPostingList().get(position).getDocId();
        else
            return -1;
    }
    public int getTf(int position){
        if(position >= 0)
            return getPostingList().get(position).getTermFreq();
        else
            return -1;
    }
    public int getCurrentDocId(){
        return getDocId(currentPosition);
    }
    public int getCurrentTf(){
        return getTf(currentPosition);
    }
}
