package it.unipi.mircv.index;
import it.unipi.mircv.utility.Parameters;
import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.file.InvertedIndexFileHandler;
import it.unipi.mircv.file.LexiconFileHandler;
import it.unipi.mircv.file.SkipDescriptorFileHandler;
import it.unipi.mircv.query.ScoreFunction;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.PriorityQueue;

import static it.unipi.mircv.utility.Config.*;


public class BlockMerger {
    /*
    This class merges the blocks of the inverted index and the lexicon.
    The blocks are merged in a single file for each one.
    The final files are:
        - lexicon.dat
        - docIds.dat
        - termFreq.dat
    *   - postingListDesc.dat  --> skip descriptor file
    *
     */
    private static int numberOfBlocks;
    private static int offsetToWrite = 0;
    private static final ArrayList<LexiconFileHandler> lexiconBlocks = new ArrayList<>();
    private static ArrayList<InvertedIndexFileHandler> postingListBlocks = new ArrayList<>();
    private static ArrayList<LexiconEntry> currentBlockEntry = new ArrayList<>();

    private static PriorityQueue<String> minTermQueue = new PriorityQueue();
    private static int postingListOffset = 0;  //offset to write in the final lexicon file for each term
    private static int offsetSkipDescriptor = 0;

    private static SkipDescriptorFileHandler skipDescriptorFileHandler;

    public static void mergeBlocks(int numberOfBlocks) throws IOException {


        System.out.println(POSTING_LIST_DESC_FILE);
        //initialize the skip descriptor file handler
        skipDescriptorFileHandler = new SkipDescriptorFileHandler();
        //initialize the document index file handler
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler(INDEX_PATH + "/documentIndex.dat");
        //read the collection size and the average document length
        Parameters.collectionSize = documentIndexHandler.readCollectionSize();
        Parameters.avgDocLen = documentIndexHandler.readAvgDocLen();
        BlockMerger.numberOfBlocks = numberOfBlocks;


        //---------------------------------FILE HANDLER---------------------------------------------------------------------------------------------------
        for (int blockIndex = 0; blockIndex < numberOfBlocks; blockIndex++) {
            // initialize the handlers for each block

            LexiconFileHandler lexiconHandler = new LexiconFileHandler(INDEX_PATH + "/lexicon"+blockIndex+".dat",true);
            InvertedIndexFileHandler plHandler = new InvertedIndexFileHandler(
                    INDEX_PATH +"/docIds"+blockIndex+".dat",
                    INDEX_PATH +"/termFreq"+blockIndex+".dat");
            lexiconBlocks.add(lexiconHandler);
            postingListBlocks.add(plHandler);
        }

        FileOutputStream fosLexicon = new FileOutputStream(INDEX_PATH +"/lexicon.dat",true);
        FileOutputStream fosDocId = new FileOutputStream(INDEX_PATH +"/docIds.dat",true);
        FileOutputStream fosTermFreq = new FileOutputStream(INDEX_PATH +"/termFreq.dat",true);
        //------------------------------------------------------------------------------------------------------------------------------------------------


        //Initialize the priority queue with the first term of each block

        for (int i = 0; i < numberOfBlocks; i++) {
            LexiconEntry lexiconEntry = lexiconBlocks.get(i).nextBlockEntryLexiconFile();
            if(lexiconEntry!=null) {
                minTermQueue.add(lexiconEntry.getTerm());
            }
            currentBlockEntry.add(i, lexiconEntry);
        }


        String minTerm = minTermQueue.peek();

        //at each iteration a new term is handled. The minTerm will be the first term in lexicographical increasing order
        while(true) {

            //if the queue is empty, the merging is completed
            if(minTerm == null)
                break;
            //duplicate terms are removed from the queue
            while((minTermQueue.peek()!=null) && (minTerm.compareTo(minTermQueue.peek())== 0)) {
                minTerm = minTermQueue.poll();
            }


            //----------------------------------MERGING--------------------------------------------------------------------
            PostingList postingList = new PostingList();
            int docFreqSum = 0;
            int collFreqSum = 0;

            for (int i = 0; i < numberOfBlocks; i++) {  //for each block merge the corresponding entry with the min term
                if (currentBlockEntry.get(i) == null) continue;  //skip iteration if block is completed
                if (minTerm.compareTo(currentBlockEntry.get(i).getTerm()) == 0) {   //if the term is the same of the minTerm, add the posting list to the final posting list

                    postingList.addPostingList(postingListBlocks.get(i).getPostingList(
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
            //-------------------------------------------------------------------------------------------------------------
            //compute the termUpperBoundScore
            float[] termUpperBoundScore = computeTermUpperBound(documentIndexHandler,postingList);

            //appending term and posting list in final files
            writeToDisk(fosLexicon,fosDocId,fosTermFreq,minTerm, offsetToWrite, docFreqSum, collFreqSum,
                    termUpperBoundScore[0], termUpperBoundScore[1], postingList);
            offsetToWrite += docFreqSum;

            //update the minTerm
            minTerm = minTermQueue.peek();
        }
        System.out.println("Merge completed!");


        //close the final files
        fosLexicon.close();
        fosDocId.close();
        fosTermFreq.close();
    }

    private static void writeToDisk(FileOutputStream fosLexicon, FileOutputStream fosDocId, FileOutputStream fosTermFreq,
                                    String term, int offset, int docFreq, int collFreq,
                                    float termUpperBoundScoreBM25, float termUpperBoundScoreTFIDF, PostingList postingList) throws IOException {

        byte[] termBytes = term.getBytes(StandardCharsets.UTF_8);
        ByteBuffer termBuffer = ByteBuffer.allocate(LEXICON_ENTRY_LENGTH);
        termBuffer.put(termBytes);
        termBuffer.position(TERM_BYTES_LENGTH);
        termBuffer.putInt(offset);
        termBuffer.position(TERM_BYTES_LENGTH + OFFSET_BYTES_LENGTH);
        termBuffer.putInt(docFreq);
        termBuffer.position(TERM_BYTES_LENGTH + OFFSET_BYTES_LENGTH + DOCUMFREQ_BYTES_LENGTH);
        termBuffer.putInt(collFreq);
        termBuffer.position(TERM_BYTES_LENGTH + OFFSET_BYTES_LENGTH + DOCUMFREQ_BYTES_LENGTH + COLLECTIONFREQ_BYTES_LENGTH);
        termBuffer.putFloat(termUpperBoundScoreBM25);
        termBuffer.position(TERM_BYTES_LENGTH + OFFSET_BYTES_LENGTH + DOCUMFREQ_BYTES_LENGTH +
                COLLECTIONFREQ_BYTES_LENGTH + UPPER_BOUND_SCORE_LENGTH);
        termBuffer.putFloat(termUpperBoundScoreTFIDF);

        //update the offset to write in the lexicon for the next term (next iteration)
        postingListOffset += postingList.getSize();
        //Write posting list in docIds and termFreq files
        byte[][] bytePostingList = postingList.getBytes();
        fosDocId.write(bytePostingList[0]); //append to precedent PostingList docID
        fosTermFreq.write(bytePostingList[1]); //append to precedent PostingList termFreq

        int postingListSize = postingList.getSize();

        //if the posting list is big enough, write the skip descriptor
        if (postingListSize > (MIN_NUM_POSTING_TO_SKIP * MIN_NUM_POSTING_TO_SKIP)){
            SkipDescriptor skipDescriptor = new SkipDescriptor();
            int postingListSizeBlock = (int) Math.sqrt(postingListSize);

            for (int i = 0; i <= postingListSize - postingListSizeBlock; i += postingListSizeBlock){
                int maxDocId = postingList.getList().get(i + postingListSizeBlock - 1).getDocId();
                int offsetMaxDocId = offsetToWrite + i;
                skipDescriptor.add(maxDocId, offsetMaxDocId);
            }

            //the last offset will be written here
            if (postingListSize%postingListSizeBlock != 0) {
                int maxDocId = postingList.getList().get(postingListSize - 1).getDocId();
                int offsetMaxDocId = offsetToWrite + postingListSizeBlock*postingListSizeBlock;
                skipDescriptor.add(maxDocId, offsetMaxDocId);
            }

            termBuffer.position(TERM_BYTES_LENGTH + OFFSET_BYTES_LENGTH
                    + DOCUMFREQ_BYTES_LENGTH + COLLECTIONFREQ_BYTES_LENGTH + UPPER_BOUND_SCORE_LENGTH + UPPER_BOUND_SCORE_LENGTH);
            termBuffer.putInt(offsetSkipDescriptor);

            skipDescriptorFileHandler.writeSkipDescriptor(offsetSkipDescriptor, skipDescriptor);
            offsetSkipDescriptor += skipDescriptor.size(); //aggiorno l'offset che devo inserire nel lexiconEntry,
        }
        fosLexicon.write(termBuffer.array());
    }

    private static float[] computeTermUpperBound(DocumentIndexFileHandler documentIndexHandler,
                                        PostingList postingList) throws IOException {
        int documentFrequency = postingList.getSize();
        float maxScoreBM25 = -1;
        float maxScoreTFIDF = -1;

        for (PostingElement postingElement: postingList.getList())
        {
            //System.out.println(postingElement.getTermFreq() + "-" + documentIndexHandler.readDocumentLength(postingElement.getDocId()) + "-" + documentFrequency);
            float currentScoreBM25 = ScoreFunction.BM25(postingElement.getTermFreq(),
                    documentIndexHandler.readDocumentLength(postingElement.getDocId()),documentFrequency);
            float currentScoreTFIDF = ScoreFunction.TFIDF(postingElement.getTermFreq(),documentFrequency);
            if (currentScoreBM25 > maxScoreBM25)
                maxScoreBM25 = currentScoreBM25;
            if (currentScoreTFIDF > maxScoreTFIDF)
                maxScoreTFIDF = currentScoreTFIDF;
        }
        return new float[]{maxScoreBM25,maxScoreTFIDF};
    }
}
