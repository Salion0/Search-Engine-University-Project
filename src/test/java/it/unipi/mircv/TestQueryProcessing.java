package it.unipi.mircv;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import static it.unipi.mircv.utility.Config.INDEX_PATH;
import static it.unipi.mircv.utility.Parameters.*;
import static it.unipi.mircv.utility.Parameters.flagCompressedReading;
import static it.unipi.mircv.utility.Utils.*;

import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.file.InvertedIndexFileHandler;
import it.unipi.mircv.file.LexiconFileHandler;
import it.unipi.mircv.index.PostingElement;
import it.unipi.mircv.index.PostingListBlock;
import it.unipi.mircv.index.SkipDescriptor;
import it.unipi.mircv.utility.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class TestQueryProcessing {
    static PostingListBlock[] postingListBlocks = new PostingListBlock[5];
    static SkipDescriptor[] skipDescriptors = new SkipDescriptor[5];
    static boolean[] endOfPostingListFlag = new boolean[5];
    static int[] docFreqs = new int[5];
    static int[] numBlockRead = new int[5];
    static InvertedIndexFileHandler invertedIndexHandler;
    static int[] offsets = new int[5];


    private static void updatePostingListBlock(int i) throws IOException {
        // load a block with at max 3 posting elements
        int block_len = 3;
        int elementToRead = docFreqs[i] - block_len*numBlockRead[i];

        if (elementToRead > 0)
        {
            if(elementToRead > block_len )
                elementToRead = block_len;

            postingListBlocks[i] = invertedIndexHandler.getPostingList(
                    offsets[i] + (block_len * numBlockRead[i]), elementToRead);

            numBlockRead[i]++;
        }
        else
            endOfPostingListFlag[i]=true;

    }

    @Test
    void testUploadPostingListBlock() throws IOException {
        // test if the posting lists blocks are loaded correctly in memory
        flagStemming = false;
        flagStopWordRemoval = true;
        flagCompressedReading = false;
        INDEX_PATH = "dataForQueryTest";
        setFilePaths();
        printFilePaths();
        Utils.loadStopWordList();
        // create the posting lists for testing
        setPostingListBlocksForTesting();

        // create the posting lists blocks for the testing
        postingListBlocks = new PostingListBlock[3];
        postingListBlocks[0] = getPostingListFromLexiconEntry("telekinesis");
        postingListBlocks[1] = getPostingListFromLexiconEntry("accuplacer");
        postingListBlocks[2] = getPostingListFromLexiconEntry("orbiting");

        // store the complete posting lists for the terms above
        ArrayList<Integer> arraysOfResults0 = new ArrayList<>(List.of(699, 701, 702, 703, 704, 708));
        ArrayList<Integer> arraysOfResults1 = new ArrayList<>(List.of(719, 721, 722, 726));
        ArrayList<Integer> arraysOfResults2 = new ArrayList<>(List.of(730, 6678));

        docFreqs[0] = arraysOfResults0.size();
        docFreqs[1] = arraysOfResults1.size();
        docFreqs[2] = arraysOfResults2.size();

        // initialize the array lists where the computed results are going to be store
        ArrayList<Integer> arrayOfPrediction0 = new ArrayList<>();
        ArrayList<Integer> arrayOfPrediction1 = new ArrayList<>();
        ArrayList<Integer> arrayOfPrediction2 = new ArrayList<>();
        int count = 0;

        // process block by block the posting list of the first term
        for (int i = 0; i < postingListBlocks[count].getList().size(); i++)
        {
            arrayOfPrediction0.add(postingListBlocks[count].getCurrentDocId());
            if (postingListBlocks[count].next() == -1)
                updatePostingListBlock(count);
        }

        count++;
        // process block by block the posting list of the second term
        for (int i = 0; i < postingListBlocks[count].getList().size(); i++)
        {
            arrayOfPrediction1.add(postingListBlocks[count].getCurrentDocId());
            if (postingListBlocks[count].next() == -1)
                updatePostingListBlock(count);
        }

        count++;
        // process block by block the posting list of the third term
        for (int i = 0; i < postingListBlocks[count].getList().size(); i++)
        {
            arrayOfPrediction2.add(postingListBlocks[count].getCurrentDocId());
            if (postingListBlocks[count].next() == -1)
                updatePostingListBlock(count);
        }

        Assertions.assertEquals(arraysOfResults0,arrayOfPrediction0);
        Assertions.assertEquals(arraysOfResults1,arrayOfPrediction1);
        Assertions.assertEquals(arraysOfResults2,arrayOfPrediction2);

        System.out.println("test on the method uploadPostingListBlock --> SUCCESSFUL");
    }

    public static boolean currentDocIdInPostingList(int indexTerm, int currentDocId){
        do{
            if(postingListBlocks[indexTerm].getCurrentDocId() == currentDocId) return true;
            if(postingListBlocks[indexTerm].getCurrentDocId() > currentDocId) return false;
        } while (postingListBlocks[indexTerm].next() != -1);
        return false;
    }

    @Test
    void testCurrentDocIdInPostingList() throws IOException {
        // test if the posting elements are processed in the correct order
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();

        // create the posting lists for testing
        setPostingListBlocksForTesting();
        // add some posting element to each posting list
        setLongerFirstPostingList();
        setLongerSecondPostingList();
        setLongerThirdPostingList();
        setLongerFourthPostingList();
        setLongerFifthPostingList();

        Assertions.assertTrue(currentDocIdInPostingList(0, 2));
        Assertions.assertFalse(currentDocIdInPostingList(0, -1));
        Assertions.assertTrue(currentDocIdInPostingList(0, 29));
        Assertions.assertTrue(currentDocIdInPostingList(1, 26));
        Assertions.assertTrue(currentDocIdInPostingList(2, 10));
        Assertions.assertFalse(currentDocIdInPostingList(3, 52));
        Assertions.assertTrue(currentDocIdInPostingList(4, 22));

        System.out.println("test on the method currentDocIdInPostingList --> SUCCESSFUL");
    }

    @Test
    void testNextGEQ() throws IOException {
        // test if the nextGEQ method works correctly
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        setPostingListBlocksForTesting();

        LexiconFileHandler lexiconHandler = new LexiconFileHandler();
        // create the posting lists to test the method on
        setLongerFirstPostingList();
        setLongerSecondPostingList();
        postingListBlocks[1].addPostingElement(new PostingElement(50,1));
        postingListBlocks[1].addPostingElement(new PostingElement(51,1));
        postingListBlocks[1].addPostingElement(new PostingElement(52,1));
        postingListBlocks[2].addPostingElement(new PostingElement(46,1));

        for (int i = 0; i < 5; i++)
            skipDescriptors[i] = new SkipDescriptor();
            //System.out.println(postingListBlocks[i].getPostingList());

        // {2, 5, 6, 13, 14, 18, 21, 24, 25, 29}; first PostingList
        // {1, 2, 24, 26, 28, 30, 31, 33, 36, 39, 50, 51, 52}; second PostingList
        // {7, 10, 11, 46}; third PostingList

        // create the skip descriptors with the actual maxDocIds arrays
        skipDescriptors[0].add(6,0);
        skipDescriptors[0].add(18, 3);
        skipDescriptors[0].add(25, 6);
        skipDescriptors[0].add(29, 9);

        skipDescriptors[1].add(24,0);
        skipDescriptors[1].add(30,3);
        skipDescriptors[1].add(36,6);
        skipDescriptors[1].add(51,9);

        skipDescriptors[2].add(10,0);
        skipDescriptors[2].add(46,2);

        // check if the nextGEQ method returns the same result w.r.t to the created skip descriptors
        Assertions.assertEquals(skipDescriptors[0].nextGEQ(4),0);
        Assertions.assertEquals(skipDescriptors[0].nextGEQ(33),-1);
        Assertions.assertEquals(skipDescriptors[0].nextGEQ(29),9);
        Assertions.assertEquals(skipDescriptors[1].nextGEQ(30),3);
        Assertions.assertEquals(skipDescriptors[1].nextGEQ(2),0);
        Assertions.assertEquals(skipDescriptors[1].nextGEQ(0),0);
        Assertions.assertEquals(skipDescriptors[2].nextGEQ(11),2);
        Assertions.assertEquals(skipDescriptors[2].nextGEQ(46),2);

        System.out.println("test on the method nextGEQ --> SUCCESSFUL");
    }

    // ******************** Down here some crafted objects for testing are set **************************

    public static void setPostingListBlocksForTesting() throws IOException {  // initialize some PostingListBlocks

        invertedIndexHandler = new InvertedIndexFileHandler();
        collectionSize = Integer.MAX_VALUE;

        int count = 0;
        for (int i = 0; i < 5; i++) {
            postingListBlocks[i] = new PostingListBlock();
            endOfPostingListFlag[i] = false;
            docFreqs[i] = 3;
            numBlockRead[i] = 0;
            offsets[i] = count;
            count += 3;
        }

        postingListBlocks[0].addPostingElement(new PostingElement(2,4));
        postingListBlocks[0].addPostingElement(new PostingElement(5,1));
        postingListBlocks[0].addPostingElement(new PostingElement(6,3));
        postingListBlocks[0].setFields(3);

        postingListBlocks[1].addPostingElement(new PostingElement(1,2));
        postingListBlocks[1].addPostingElement(new PostingElement(2,3));
        postingListBlocks[1].addPostingElement(new PostingElement(24,7));
        postingListBlocks[1].setFields(3);

        postingListBlocks[2].addPostingElement(new PostingElement(7,2));
        postingListBlocks[2].addPostingElement(new PostingElement(10,6));
        postingListBlocks[2].addPostingElement(new PostingElement(11,2));
        postingListBlocks[2].setFields(3);

        postingListBlocks[3].addPostingElement(new PostingElement(2,5));
        postingListBlocks[3].addPostingElement(new PostingElement(4,5));
        postingListBlocks[3].addPostingElement(new PostingElement(5,3));
        postingListBlocks[3].setFields(3);

        postingListBlocks[4].addPostingElement(new PostingElement(1,4));
        postingListBlocks[4].addPostingElement(new PostingElement(7,1));
        postingListBlocks[4].addPostingElement(new PostingElement(9,1));
        postingListBlocks[4].setFields(3);
    }

    public static void setLongerFirstPostingList() {  // add some posting elements to the first created posting list
        postingListBlocks[0].addPostingElement(new PostingElement(13,4));
        postingListBlocks[0].addPostingElement(new PostingElement(14,1));
        postingListBlocks[0].addPostingElement(new PostingElement(18,3));
        postingListBlocks[0].addPostingElement(new PostingElement(21,4));
        postingListBlocks[0].addPostingElement(new PostingElement(24,1));
        postingListBlocks[0].addPostingElement(new PostingElement(25,3));
        postingListBlocks[0].addPostingElement(new PostingElement(29,3));
        postingListBlocks[0].setFields(10);
        docFreqs[0] = 10;

    }

    public static void setLongerSecondPostingList() {  // add some posting elements to the second created posting list
        postingListBlocks[1].addPostingElement(new PostingElement(26,2));
        postingListBlocks[1].addPostingElement(new PostingElement(28,3));
        postingListBlocks[1].addPostingElement(new PostingElement(30,7));
        postingListBlocks[1].addPostingElement(new PostingElement(31,2));
        postingListBlocks[1].addPostingElement(new PostingElement(33,3));
        postingListBlocks[1].addPostingElement(new PostingElement(36,7));
        postingListBlocks[1].addPostingElement(new PostingElement(39,2));
        postingListBlocks[1].setFields(10);
        docFreqs[1] = 10;
        offsets[1] = 10;
    }

    public static void setLongerThirdPostingList() {  // add some posting elements to the third created posting list
        postingListBlocks[2].addPostingElement(new PostingElement(46,2));
        postingListBlocks[2].addPostingElement(new PostingElement(50,3));
        postingListBlocks[2].addPostingElement(new PostingElement(56,7));
        postingListBlocks[2].addPostingElement(new PostingElement(57,2));
        postingListBlocks[2].addPostingElement(new PostingElement(58,3));
        postingListBlocks[2].addPostingElement(new PostingElement(66,7));
        postingListBlocks[2].addPostingElement(new PostingElement(68,2));
        postingListBlocks[2].setFields(10);
        docFreqs[2] = 10;
        offsets[2] = 20;
    }

    public static void setLongerFourthPostingList() {   // add some posting elements to the fourth created posting list
        postingListBlocks[3].addPostingElement(new PostingElement(36,2));
        postingListBlocks[3].addPostingElement(new PostingElement(37,3));
        postingListBlocks[3].addPostingElement(new PostingElement(40,7));
        postingListBlocks[3].addPostingElement(new PostingElement(41,2));
        postingListBlocks[3].addPostingElement(new PostingElement(44,3));
        postingListBlocks[3].addPostingElement(new PostingElement(47,7));
        postingListBlocks[3].addPostingElement(new PostingElement(50,2));
        postingListBlocks[3].setFields(10);
        docFreqs[3] = 10;
        offsets[3] = 30;
    }

    public static void setLongerFifthPostingList() {   // add some posting elements to the fifth created posting list
        postingListBlocks[4].addPostingElement(new PostingElement(11,2));
        postingListBlocks[4].addPostingElement(new PostingElement(12,3));
        postingListBlocks[4].addPostingElement(new PostingElement(15,7));
        postingListBlocks[4].addPostingElement(new PostingElement(19,2));
        postingListBlocks[4].addPostingElement(new PostingElement(20,3));
        postingListBlocks[4].addPostingElement(new PostingElement(22,7));
        postingListBlocks[4].addPostingElement(new PostingElement(24,2));
        postingListBlocks[4].setFields(10);
        docFreqs[4] = 10;
        offsets[4] = 40;
    }

    public static PostingListBlock getPostingListFromLexiconEntry(String string) throws IOException {
        // retrieve the complete posting list of the term passed as argument to this method
        LexiconFileHandler lexiconHandler = new LexiconFileHandler();
        InvertedIndexFileHandler invertedIndexHandler = new InvertedIndexFileHandler();
        ByteBuffer entryBuffer = lexiconHandler.findTermEntry(string);
        String term = lexiconHandler.getTerm(entryBuffer);
        int documentFrequency = lexiconHandler.getDf(entryBuffer);
        int offset = lexiconHandler.getOffset(entryBuffer);
        float termUpperBoundScore = lexiconHandler.getTermUpperBoundScoreTFIDF(entryBuffer);
        PostingListBlock postingListBlock = invertedIndexHandler.getPostingList(offset,documentFrequency);
        System.out.println(postingListBlock);
        return postingListBlock;
    }

    public static void printAllPostingLists() {  // utility method to print all created posting lists
        for (int i = 0; i < 5; i++)
            System.out.println(postingListBlocks[i].getList());
    }
}
