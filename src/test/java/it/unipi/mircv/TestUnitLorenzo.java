package it.unipi.mircv;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import static it.unipi.mircv.Parameters.collectionSize;
import static it.unipi.mircv.Utils.removeStopWords;

import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.file.InvertedIndexFileHandler;
import it.unipi.mircv.file.LexiconFileHandler;
import it.unipi.mircv.index.PostingElement;
import it.unipi.mircv.index.PostingListBlock;
import it.unipi.mircv.index.SkipDescriptor;
import it.unipi.mircv.query.ConjunctiveDAAT;
import it.unipi.mircv.query.DisjunctiveDAAT;
import it.unipi.mircv.query.MaxScoreDisjunctive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class TestUnitLorenzo {
    static PostingListBlock[] postingListBlocks = new PostingListBlock[5];
    static SkipDescriptor[] skipDescriptors = new SkipDescriptor[5];
    static boolean[] endOfPostingListFlag = new boolean[5];
    static int[] docFreqs = new int[5];
    static int[] numBlockRead = new int[5];
    static InvertedIndexFileHandler invertedIndexHandler;
    static int[] offsets = new int[5];

    public static void testConjunctiveResults() throws IOException {
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        Parameters.collectionSize = documentIndexHandler.readCollectionSize();
        Parameters.avgDocLen = documentIndexHandler.readAvgDocLen();
        setPostingListBlocksForTesting();

        PostingListBlock testPL1 = getPostingListFromLexiconEntry("sudduth");
        PostingListBlock testPL2 = getPostingListFromLexiconEntry("dziena");
        System.out.println(testPL1);
        System.out.println(testPL2);
        ArrayList<Integer> actualResults = new ArrayList<>(List.of(8658624));
        Assertions.assertEquals(actualResults,testNewConjunctive("sudduth dziena"));

        System.out.println("test on the method testConjunctiveResults --> SUCCESSFUL");
    }

    private static void updatePostingListBlock(int i) throws IOException {

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
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        setPostingListBlocksForTesting();

        postingListBlocks = new PostingListBlock[3];
        postingListBlocks[0] = getPostingListFromLexiconEntry("telekinesis");
        postingListBlocks[1] = getPostingListFromLexiconEntry("accuplacer");
        postingListBlocks[2] = getPostingListFromLexiconEntry("orbiting");

        ArrayList<Integer> arraysOfResults0 = new ArrayList<>(List.of(699, 701, 702, 703, 704, 708));
        ArrayList<Integer> arraysOfResults1 = new ArrayList<>(List.of(719, 721, 722, 726));
        ArrayList<Integer> arraysOfResults2 = new ArrayList<>(List.of(730, 6678));

        docFreqs[0] = arraysOfResults0.size();
        docFreqs[1] = arraysOfResults1.size();
        docFreqs[2] = arraysOfResults2.size();

        ArrayList<Integer> arrayOfPrediction0 = new ArrayList<>();
        ArrayList<Integer> arrayOfPrediction1 = new ArrayList<>();
        ArrayList<Integer> arrayOfPrediction2 = new ArrayList<>();
        int count = 0;

        for (int i = 0; i < postingListBlocks[count].getList().size(); i++)
        {
            arrayOfPrediction0.add(postingListBlocks[count].getCurrentDocId());
            if (postingListBlocks[count].next() == -1)
                updatePostingListBlock(count);
        }

        count++;
        for (int i = 0; i < postingListBlocks[count].getList().size(); i++)
        {
            arrayOfPrediction1.add(postingListBlocks[count].getCurrentDocId());
            if (postingListBlocks[count].next() == -1)
                updatePostingListBlock(count);
        }

        count++;
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
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        setPostingListBlocksForTesting();

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
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        setPostingListBlocksForTesting();

        LexiconFileHandler lexiconHandler = new LexiconFileHandler();
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

    // ******************** down here some crafted objects for testing are set **************************

    public static void setPostingListBlocksForTesting() throws IOException {

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

    public static void setLongerFirstPostingList() {
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

    public static void setLongerSecondPostingList() {
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

    public static void setLongerThirdPostingList() {
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

    public static void setLongerFourthPostingList() {
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

    public static void setLongerFifthPostingList() {
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

    public static ArrayList<Integer> testNewConjunctive(String string) throws IOException {
        long startTime = System.currentTimeMillis();
        String[] queryTerms = string.split(" ");
        queryTerms = removeStopWords(queryTerms);
        ConjunctiveDAAT newConjunctiveDAAT = new ConjunctiveDAAT(queryTerms);
        ArrayList<Integer> results = newConjunctiveDAAT.processQuery();
        System.out.println(results);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("NEW-CONJUNCTIVE finished in " + (float)elapsedTime/1000 +"sec");
        return results;
    }

    public static PostingListBlock getPostingListFromLexiconEntry(String string) throws IOException {
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

    public static void printAllPostingLists() {
        for (int i = 0; i < 5; i++)
            System.out.println(postingListBlocks[i].getList());
    }
}
