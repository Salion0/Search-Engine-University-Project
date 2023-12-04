package it.unipi.mircv.Index;
import it.unipi.mircv.Config;
import it.unipi.mircv.File.DocumentIndexFileHandler;
import it.unipi.mircv.File.InvertedIndexFileHandler;
import it.unipi.mircv.File.LexiconFileHandler;
import it.unipi.mircv.File.SkipDescriptorFileHandler;
import it.unipi.mircv.Query.ScoreFunction;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.PriorityQueue;

import static it.unipi.mircv.Config.*;

public class BlockMergerCompression {
    private static int numberOfBlocks;
    private static long offsetToWriteDocId = 0;
    private static long offsetToWriteTermFreq = 0;
    private static final ArrayList<LexiconFileHandler> lexiconBlocks = new ArrayList<>();
    private static ArrayList<InvertedIndexFileHandler> postingListBlocks = new ArrayList<>();
    private static ArrayList<LexiconEntry> currentBlockEntry = new ArrayList<>();
    private static ArrayList<Boolean> minTermFoundInBlock = new ArrayList<>();

    private static PriorityQueue<String> minTermQueue = new PriorityQueue();
    private static int postingListOffset = 0;  //offset to write in the final lexicon file for each term
    private static int offsetSkipDescriptor = 0;

    private static SkipDescriptorFileHandler skipDescriptorFileHandler;
    private static String path="data/";

    public void mergeBlocks(int numberOfBlocks) throws IOException {
        /*
        //count number of blocks
        String path = "./data/";
        File directory=new File(path);
        int numberOfBlocks = (directory.list().length-5)/3;
        */


        //initialize the skip descriptor file handler
        skipDescriptorFileHandler = new SkipDescriptorFileHandler();
        //initialize the document index file handler
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        //read the collection size and the average document length
        Config.collectionSize = documentIndexHandler.readCollectionSize();
        Config.avgDocLen = documentIndexHandler.readAvgDocLen();
        this.numberOfBlocks = numberOfBlocks;


        //---------------------------------FILE HANDLER---------------------------------------------------------------------------------------------------
        for (int blockIndex = 0; blockIndex < numberOfBlocks; blockIndex++) {
            // initialize the handlers for each block

            LexiconFileHandler lexiconHandler = new LexiconFileHandler(path+"lexicon"+blockIndex+".dat");
            InvertedIndexFileHandler plHandler = new InvertedIndexFileHandler(
                    path+"docIds"+blockIndex+".dat",
                    path+"termFreq"+blockIndex+".dat");
            lexiconBlocks.add(lexiconHandler);
            postingListBlocks.add(plHandler);
        }

        FileOutputStream fosLexicon = new FileOutputStream(path+"lexicon.dat",true);
        FileOutputStream fosDocId = new FileOutputStream(path+"docIds.dat",true);
        FileOutputStream fosTermFreq = new FileOutputStream(path+"termFreq.dat",true);
        //------------------------------------------------------------------------------------------------------------------------------------------------


        //Initialize the priority queue with the first term of each block
/*
        System.out.print("number of blocks:"+numberOfBlocks+"\n");  //DEBUG
*/
        for (int i = 0; i < numberOfBlocks; i++) {
            LexiconEntry lexiconEntry = lexiconBlocks.get(i).nextBlockEntryLexiconFile();
            minTermQueue.add(lexiconEntry.getTerm());
            currentBlockEntry.add(i,lexiconEntry);
        }


        String minTerm = minTermQueue.peek();

        //at each iteration a new term is handled. The minTerm will be the first term in lexicographical increasing order
        while(true) {
            //System.out.println("Merging working progress... Percentage: boh");

            //if the queue is empty, the merging is completed
            if(minTerm == null)
                break;
            //duplicate terms are removed from the queue
            while((minTermQueue.peek()!=null) && (minTerm.compareTo(minTermQueue.peek())== 0)) {
                minTerm = minTermQueue.poll();
            }


            //----------------------------------MERGING--------------------------------------------------------------------
            PostingList2 postingList2Compress = new PostingList2();
            int docFreqSum = 0;
            int collFreqSum = 0;

            for (int i = 0; i < numberOfBlocks; i++) {  //for each block merge the corresponding entry with the min term
                if (currentBlockEntry.get(i) == null) continue;  //skip iteration if block is completed
                if (minTerm.compareTo(currentBlockEntry.get(i).getTerm()) == 0) {   //if the term is the same of the minTerm, add the posting list to the final posting list
                    /*postingList.addPostingList(postingListBlocks.get(i).getPostingList(
                            currentBlockEntry.get(i).getOffset(),
                            currentBlockEntry.get(i).getDf()
                            )
                    );*/
                    postingList2Compress.addPostingList(postingListBlocks.get(i).getPostingList2(
                            currentBlockEntry.get(i).getOffset(),
                            currentBlockEntry.get(i).getDf()
                            )
                    );
                    docFreqSum += currentBlockEntry.get(i).getDf();
                    //System.out.println(docFreqSum);
                    collFreqSum += currentBlockEntry.get(i).getCf();

                    //update the currentBlockEntry
                    currentBlockEntry.set(i, lexiconBlocks.get(i).nextBlockEntryLexiconFile());

                    if (currentBlockEntry.get(i) != null) {
                            minTermQueue.add(currentBlockEntry.get(i).getTerm());
                    }
                }
            }
            System.out.println(offsetToWriteDocId);
            System.out.println(offsetToWriteTermFreq);

            //-------------------------------------------------------------------------------------------------------------
            //compute the termUpperBoundScore
            float termUpperBoundScore = computeTermUpperBound2(documentIndexHandler, postingList2Compress);

            //appending term and posting list in final files
            int[] increments = writeToDiskCompression(fosLexicon, fosDocId, fosTermFreq, minTerm,
                    docFreqSum, collFreqSum, termUpperBoundScore, postingList2Compress);

            offsetToWriteDocId += increments[0];
            offsetToWriteTermFreq += increments[1];

            //update the minTerm
            minTerm = minTermQueue.peek();
        }
        System.out.println("Merge completed!");


        //close the final files
        fosLexicon.close();
        fosDocId.close();
        fosTermFreq.close();
/*        //close the skip descriptor file handler
        skipDescriptorFileHandler.closeFileChannel();
        //close the handlers for each block
        for (int blockIndex = 0; blockIndex < numberOfBlocks; blockIndex++) {
            lexiconBlocks.get(blockIndex).close();
            postingListBlocks.get(blockIndex).close();
        }*/
    }
    private int[] writeToDiskCompression(
                FileOutputStream fosLexicon, FileOutputStream fosDocId,
                FileOutputStream fosTermFreq, String term, int docFreq, int collFreq,
                float termUpperBoundScore, PostingList2 postingList2Compress) throws IOException {

        ByteBuffer termBuffer = ByteBuffer.allocate(LEXICON_COMPRESS_ENTRY_LENGTH);
        termBuffer.put(term.getBytes(StandardCharsets.UTF_8));
        termBuffer.putLong(offsetToWriteDocId);
        termBuffer.putLong(offsetToWriteTermFreq);
        termBuffer.putInt(docFreq);
        termBuffer.putInt(collFreq);
        termBuffer.putFloat(termUpperBoundScore);

        //Write posting list in docIds and termFreq files
        byte[][] bytePostingList = postingList2Compress.getBytesCompressed();
        fosDocId.write(bytePostingList[0]); //append to precedent PostingList docID
        fosTermFreq.write(bytePostingList[1]); //append to precedent PostingList termFreq

        // SKIP DESCRIPTORS
        int postingListSize = postingList2Compress.getSize();

        //if the posting list is big enough, write the skip descriptor
        if (postingListSize > (MIN_NUM_POSTING_TO_SKIP * MIN_NUM_POSTING_TO_SKIP)){
            SkipDescriptorCompression skipDescriptorCompression = new SkipDescriptorCompression();
            int postingListSizeBlock = (int) Math.sqrt(postingListSize);


            for (int i = 0; i <= postingListSize - postingListSizeBlock; i += postingListSizeBlock){
                int maxDocId = postingList2Compress.getDocIds().get(i + postingListSizeBlock - 1);
                int offsetMaxDocId = offsetToWrite + i;
                skipDescriptor.add(maxDocId, offsetMaxDocId);
            }

            //the last offset will be written here
            if (postingListSize%postingListSizeBlock != 0) {
                int maxDocId = postingList.getPostingList().get(postingListSize - 1).getDocId();
                int offsetMaxDocId = offsetToWrite + postingListSizeBlock*postingListSizeBlock;
                skipDescriptor.add(maxDocId, offsetMaxDocId);
            }


            termBuffer.putLong(offsetDocIdSkipDescriptor);
            termBuffer.putLong(offsetTermFreqSkipDescriptor);

            skipDescriptorFileHandler.writeSkipDescriptor(offsetSkipDescriptor, skipDescriptor);
            offsetSkipDescriptor += skipDescriptor.size(); //aggiorno l'offset che devo inserire nel lexiconEntry,
        }
        fosLexicon.write(termBuffer.array());

        return new int[]{bytePostingList[0].length, bytePostingList[1].length};
    }

    private float computeTermUpperBound(DocumentIndexFileHandler documentIndexHandler, PostingList postingList) throws IOException {
        int documentFrequency = postingList.getSize();
        float maxScore = -1;

        for (PostingElement postingElement: postingList.getPostingList())
        {
            //System.out.println(postingElement.getTermFreq() + "-" + documentIndexHandler.readDocumentLength(postingElement.getDocId()) + "-" + documentFrequency);
            float currentScore = ScoreFunction.BM25(postingElement.getTermFreq(),
                    documentIndexHandler.readDocumentLength(postingElement.getDocId()),documentFrequency);
            if (currentScore > maxScore)
                maxScore = currentScore;
        }
        return maxScore;
    }
    private float computeTermUpperBound2(DocumentIndexFileHandler documentIndexHandler, PostingList2 postingList) throws IOException {
        int documentFrequency = postingList.getSize();
        float maxScore = -1;

        for(int i = 0; i < documentFrequency; i++){
            float currentScore = ScoreFunction.BM25(postingList.getTermFreqs().get(i),
                    documentIndexHandler.readDocumentLength(postingList.getDocIds().get(i)), documentFrequency);
            if (currentScore > maxScore)
                maxScore = currentScore;
        }

        return maxScore;
    }
}
