package it.unipi.mircv.Index;


import static it.unipi.mircv.Index.Config.*;

public class PostingListBlock extends PostingList
{
   public boolean endOfBlock;
    public int blockLength = POSTING_LIST_BLOCK_LENGTH;
    public int position;
    public int maxDocID;

    //TODO costruttore

    public int getPosition(){
        return this.position;
    }

    public int next(){
        //TODO
        return this.position;
    }

    public int  getMaxDocID(){
        return this.getMaxDocID();
    }

    public int getCurrentTf(){
        int tf = 0;
        //TODO
        return tf;
    }
    public int getCurrentDocId(){
        int docID = 0;
        //TODO
        return docID;
    }

}
