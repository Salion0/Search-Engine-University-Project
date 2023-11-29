package it.unipi.mircv.Index;
import it.unipi.mircv.File.InvertedIndexFileHandler;
import it.unipi.mircv.File.LexiconFileHandler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.PriorityQueue;

import static it.unipi.mircv.Config.*;


public class BlockMerger {
    private static int numberOfBlocks;
    private static int offsetToWrite = 0;
    private static final ArrayList<LexiconFileHandler> lexiconBlocks = new ArrayList<>();
    private static ArrayList<InvertedIndexFileHandler> postingListBlocks = new ArrayList<>();
    private static ArrayList<LexiconEntry> currentBlockEntry = new ArrayList<>();
    private static ArrayList<Boolean> minTermFoundInBlock = new ArrayList<>();

    private static PriorityQueue<String> minTermQueue = new PriorityQueue();
    private static int postingListOffset = 0;  //offset to write in the final lexicon file for each term
    private static String path="data/";

    public void mergeBlocks(int numberOfBlocks) throws IOException {

/*        //count number of blocks
        String path = "./data/";
        File directory=new File(path);
        int numberOfBlocks = (directory.list().length-5)/3;*/

        this.numberOfBlocks = numberOfBlocks;




        //---------------------------------FILE HANDLER---------------------------------------------------------------------------------------------------
        for (int blockIndex = 0; blockIndex < numberOfBlocks; blockIndex++) {
            // initialize the handlers for each block
            LexiconFileHandler lexiconHandler = new LexiconFileHandler(path+"lexicon"+blockIndex+".dat");
            InvertedIndexFileHandler plHandler = new InvertedIndexFileHandler(path+"docIds"+blockIndex+".dat",path+"termFreq"+blockIndex+".dat");
            lexiconBlocks.add(lexiconHandler);
            postingListBlocks.add(plHandler);
        }

        FileOutputStream fosLexicon = new FileOutputStream(path+"lexicon.dat",true);
        FileOutputStream fosDocId = new FileOutputStream(path+"docIds.dat",true);
        FileOutputStream fosTermFreq = new FileOutputStream(path+"termFreq.dat",true);
        //--------------------------------------------------------------------------------------------------------------------------------------------------------



        //Initialzie the priority queue with the first term of each block
        System.out.print("number of blocks:"+numberOfBlocks+"\n");
        for (int i = 0; i < numberOfBlocks; i++) {
            LexiconEntry lexiconEntry = lexiconBlocks.get(i).nextEntryLexiconFile();
            System.out.println("Block "+i+" first term:"+lexiconEntry.getTerm());
            minTermQueue.add(lexiconEntry.getTerm());
            currentBlockEntry.add(i,lexiconEntry);
        }

        for(int i=0;i<numberOfBlocks;i++){
            System.out.println("Block"+i+"  currentBlockentry():"+currentBlockEntry.get(i).getTerm()+" offset "+currentBlockEntry.get(i).getOffset()+" df "+currentBlockEntry.get(i).getDf()+" cf "+currentBlockEntry.get(i).getCf());
        }

        while(true) {
            String minTerm = minTermQueue.peek();
            //at each iteration a new term is handled. The minTerm will be the first term in lexicographical increasing order
            if(minTerm == null)
                break;
            //duplicate terms are removed from the queue
            System.out.println("Min term queue:"+minTermQueue.toString());
            while((minTermQueue.peek()!=null) && (minTerm.compareTo(minTermQueue.peek())== 0)) {
                minTerm = minTermQueue.poll();
            }
            System.out.println("Min term:"+minTerm);
            System.out.println("Min term queue:"+minTermQueue.toString());
            //if the queue is empty, the merging is completed


            //----------------------------------MERGING--------------------------------------------------------------------
            PostingList postingList = new PostingList();
            int docFreqSum = 0;
            int collFreqSum = 0;
            for (int i = 0; i < numberOfBlocks; i++) {  //for each block merge the corresponding entry with the min term
                if (currentBlockEntry.get(i) == null) continue;  //skip iteration if block is completed
                if (minTerm.compareTo(currentBlockEntry.get(i).getTerm()) == 0) {
                   //if the term is the same of the minTerm, add the posting list to the final posting list
                    System.out.println("Block "+i+" term:"+currentBlockEntry.get(i).getTerm()+" is equal to minTerm:"+minTerm);
                    System.out.println("Block "+i+" offset:"+currentBlockEntry.get(i).getOffset()+" df:"+currentBlockEntry.get(i).getDf()+" cf:"+currentBlockEntry.get(i).getCf());
                    System.out.println("Block "+i+" posting list:"+postingListBlocks.get(i).getPostingList(0, 12));
                    postingList.addPostingList(postingListBlocks.get(i).getPostingList(
                            currentBlockEntry.get(i).getOffset(),
                            currentBlockEntry.get(i).getDf()
                            )
                    );

                    //compute the sum of docFreq and collFreq
                    docFreqSum += currentBlockEntry.get(i).getDf();
                    collFreqSum += currentBlockEntry.get(i).getCf();

                    //update the currentBlockEntry with the next entry of the block
                    currentBlockEntry.set(i, lexiconBlocks.get(i).nextEntryLexiconFile());
                    if (currentBlockEntry.get(i) != null) {
                        //if the block is not completed, add the next term to the minTermQueue
                        //check if the term is already in the priority queue
                        if(minTerm.compareTo(currentBlockEntry.get(i).getTerm()) != 0)
                            minTermQueue.add(currentBlockEntry.get(i).getTerm());

                    }
                }
            }
            //-------------------------------------------------------------------------------------------------------------


            //appending term and posting list in final files
            writeToDisk(fosLexicon,fosDocId,fosTermFreq,minTerm, offsetToWrite, docFreqSum, collFreqSum, postingList);
            offsetToWrite += docFreqSum;

            //DEBUG -----------------------------
            //terms.add(minTerm);  //salvo term e Posting List associata
            //postingLists.add(postingList);
            //System.out.println(minTerm + " --->> " + postingList);
            //DEBUG ---------------------------------------

        }
        fosLexicon.close();
        fosDocId.close();
        fosTermFreq.close();

        //DEBUG ------printing the whole merged lexicon-------
        /*for (int i = 0; i < terms.size(); i++) {
            System.out.println(terms.get(i) + " --->> " + postingLists.get(i));
        }
        */
        //DEBUG ---------------------------------------
    }
    private void writeToDisk(FileOutputStream fosLexicon,FileOutputStream fosDocId,FileOutputStream fosTermFreq,String term, int offset, int docFreq, int collFreq, PostingList postingList) throws IOException {

        byte[] termBytes = term.getBytes(StandardCharsets.UTF_8);
        ByteBuffer termBuffer = ByteBuffer.allocate(LEXICON_ENTRY_LENGTH);
        termBuffer.put(termBytes);
        termBuffer.position(TERM_BYTES_LENGTH);
        termBuffer.putInt(offset);
        termBuffer.position(TERM_BYTES_LENGTH + OFFSET_BYTES_LENGTH);
        termBuffer.putInt(docFreq);
        termBuffer.position(TERM_BYTES_LENGTH + OFFSET_BYTES_LENGTH + DOCUMFREQ_BYTES_LENGTH);
        termBuffer.putInt(collFreq);

        //update the offset to write in the lexicon for the next term (next iteration)
        postingListOffset += postingList.getSize();
        //Write posting list in docIds and termFreq files
        byte[][] bytePostingList = postingList.getBytes();
        fosLexicon.write(termBuffer.array());
        fosDocId.write(bytePostingList[0]); //append to precedent PostingList docID
        fosTermFreq.write(bytePostingList[1]); //append to precedent PostingList termFreq
    }
}
