package it.unipi.mircv.Index;


import static it.unipi.mircv.Index.Config.*;

public class PostingListBlock extends PostingList
{
   public boolean endOfBlock;
    public int blockLength = POSTING_LIST_BLOCK_LENGTH;
    public int position;


}
