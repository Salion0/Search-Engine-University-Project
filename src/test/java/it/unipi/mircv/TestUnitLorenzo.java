package it.unipi.mircv;

import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.File.InvertedIndexHandler;
import it.unipi.mircv.File.LexiconHandler;
import it.unipi.mircv.File.SkipDescriptorFileHandler;
import it.unipi.mircv.Index.PostingElement;
import it.unipi.mircv.Index.PostingList;
import it.unipi.mircv.Index.PostingListBlock;
import it.unipi.mircv.Index.SkipDescriptor;
import it.unipi.mircv.Query.ConjunctiveDAAT;
import it.unipi.mircv.Query.MinHeapScores;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import static it.unipi.mircv.Config.collectionSize;
import static it.unipi.mircv.Utils.removeStopWords;

import org.junit.jupiter.api.Assertions;


public class TestUnitLorenzo {
    static PostingListBlock[] postingListBlocks = new PostingListBlock[5];
    static SkipDescriptor[] skipDescriptors = new SkipDescriptor[5];
    static boolean[] endOfPostingListFlag = new boolean[5];
    static int[] docFreqs = new int[5];
    static int[] numBlockRead = new int[5];
    static InvertedIndexHandler invertedIndexHandler;
    static SkipDescriptorFileHandler skipDescriptorFileHandler;
    static int[] offsets = new int[5];
    static HashMap<Float, ArrayList<Integer>> score2DocIdMap;

    public static void main(String[] args) throws IOException {

        DocumentIndexHandler documentIndexHandler = new DocumentIndexHandler();
        Utils.loadStopWordList();
        Config.collectionSize = documentIndexHandler.readCollectionSize();
        Config.avgDocLen = documentIndexHandler.readAvgDocLen();

        setPostingListBlocksForTesting();
        //testMinDocId();
        //testUploadPostingListBlock();
        //testSortArraysByArrays();
        //testNextGEQ();
        //testCurrentDocIdInPostingList();
        //testMinHeap();
        //testConjunctiveResults();
        testBM25();
    }

    public static void testBM25() throws IOException {
        PostingListBlock testPL1 = getPostingListFromLexiconEntry("sudduth");
        PostingListBlock testPL2 = getPostingListFromLexiconEntry("dziena");
        checkLexiconEntry("dziena",2841806);
        int[] tf = new int[]{1,1,1,2};
        int[] docLen = new int[]{27,27,43,35};
        int[] docFreq = new int[]{4,4,4,4};
        int count = 0;
        for (PostingElement postingElement: testPL2.getPostingList())
            checkLexiconEntry("dziena",postingElement.getDocId());
    }

    public static void testConjunctiveResults() throws IOException {
        PostingListBlock testPL1 = getPostingListFromLexiconEntry("sudduth");
        PostingListBlock testPL2 = getPostingListFromLexiconEntry("dziena");
        System.out.println(testPL1);
        System.out.println(testPL2);
        ArrayList<Integer> actualResults = new ArrayList<>(List.of(8658624));
        Assertions.assertEquals(actualResults,testNewConjunctive("sudduth dziena"));
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


    private static int getMinDocId() {
        int minDocId = collectionSize;  //valore che indica che le posting list sono state raggiunte

        //find the current min doc id in the posting lists of the query terms
        for (int i = 0; i < 5; i++){
            if (endOfPostingListFlag[i]) continue;
            int currentDocId = postingListBlocks[i].getCurrentDocId();
            if(currentDocId<minDocId){
                minDocId = currentDocId;
            }
        }
        return minDocId;
    }

    public static void testUploadPostingListBlock() throws IOException {

        setLongerFirstPostingList();
        setLongerSecondPostingList();
        setLongerThirdPostingList();
        setLongerFourthPostingList();
        setLongerFifthPostingList();

        int[][] arraysOfResults = new int[5][];

        arraysOfResults[0] = new int[]{2, 5, 6, 13, 14, 18, 21, 24, 25, 29};
        arraysOfResults[1] = new int[]{1, 2, 24, 26, 28, 30, 31, 33, 36, 39};
        arraysOfResults[2] = new int[]{7, 10, 11, 46, 50, 56, 57, 58, 66, 68};
        arraysOfResults[3] = new int[]{2, 4, 5, 36, 37, 40, 41, 44, 47, 50};
        arraysOfResults[4] = new int[]{1, 7, 9, 11, 12, 15, 19, 20, 22, 24};

        int[] arrayOfResults;
        for (int j = 0; j < postingListBlocks.length; j++)
        {
            arrayOfResults = new int[arraysOfResults[0].length];
            for (int i = 0; i < arraysOfResults[0].length; i++)
            {
                arrayOfResults[i] = postingListBlocks[j].getCurrentDocId();
                if (postingListBlocks[j].next() == -1)
                    updatePostingListBlock(j);
            }

            Assertions.assertArrayEquals(arrayOfResults, arraysOfResults[j]);
        }

        System.out.println("test on the method uploadPostingListBlock --> SUCCESSFUL");
    }

    public static boolean currentDocIdInPostingList(int indexTerm, int currentDocId){
        do{
            if(postingListBlocks[indexTerm].getCurrentDocId() == currentDocId) return true;
            if(postingListBlocks[indexTerm].getCurrentDocId() > currentDocId) return false;
        } while (postingListBlocks[indexTerm].next() != -1);
        return false;
    }

    public static void testMinHeap() {
        float[] arrayOfScores = new float[]{ 3.422f, 0.134f, 9.199f, 5.444f, 6.125f, 0.134f, 4.231f, 5.444f, 0.134f};
        int[] docIds = new int[]{78,23,15,10,30,55,100,21,3};
        MinHeapScores heapScores = new MinHeapScores();
        for (int i = 0; i < arrayOfScores.length; i++)
            heapScores.insertIntoPriorityQueue(arrayOfScores[i],docIds[i]);
        HashMap<Float, ArrayList<Integer>> testMap = heapScores.getScore2DocIdMap();
        Assertions.assertEquals(testMap.size(),6);
        Assertions.assertEquals(heapScores.getDocId(0.134f),new ArrayList<>(List.of(23,55,3)));
        Assertions.assertEquals(heapScores.getDocId(3.422f),new ArrayList<>(List.of(78)));
        Assertions.assertNull(heapScores.getDocId(1f));
        Assertions.assertEquals(0.134f,heapScores.getMinScore());

        score2DocIdMap = testMap;
        removeDocIdFromMap(5.444f);
        removeDocIdFromMap(0.134f);
        removeDocIdFromMap(0.134f);
        Assertions.assertEquals(score2DocIdMap.size(),6);
        Assertions.assertEquals(score2DocIdMap.get(5.444f), new ArrayList<>(List.of(10)));
        Assertions.assertEquals(score2DocIdMap.get(0.134f), new ArrayList<>(List.of(23)));
        removeDocIdFromMap(5.444f);
        removeDocIdFromMap(0.134f);
        Assertions.assertEquals(score2DocIdMap.size(),4);
        Assertions.assertNull(score2DocIdMap.get(5.444f));
        Assertions.assertNull(score2DocIdMap.get(0.134f));


        heapScores = new MinHeapScores();
        for (int i = 0; i < arrayOfScores.length; i++)
            heapScores.insertIntoPriorityQueue(arrayOfScores[i],docIds[i]);
        ArrayList<Integer> rankedDocIds = new ArrayList<>(List.of(23, 55, 3, 78, 100, 10, 21, 30, 15));
        ArrayList<Integer> resultDocIds = heapScores.getTopDocIdReversed();
        Assertions.assertEquals(rankedDocIds,resultDocIds);

        float[] secondArrayOfScores = new float[]{3.422f, 0.134f, 9.199f, 5.444f, 6.125f, 0.134f, 4.231f, 5.444f, 0.134f,
                                    1f,2f,3f,4f,5f,6f,7f,8f,9f,10f,11f,12f,13f,14f,15f,16f,17f,18f,19f,20f,21f};
        int[] secondDocIds = new int[]{78,23,15,10,30,55,100,21,3,1000,1001,1100,1200,1300,1400,1500,1600,1700,1800,1900,2000,
                            2100,2200,2300,2400,2500,2600,2700,2800,2900};
        MinHeapScores secondHeapScores = new MinHeapScores();
        for (int i = 0; i < secondArrayOfScores.length; i++)
            secondHeapScores.insertIntoPriorityQueue(secondArrayOfScores[i],secondDocIds[i]);

        ArrayList<Integer> secondRankedDocIds = new ArrayList<>(List.of(10, 21, 1400, 30, 1500, 1600, 1700, 15, 1800, 1900, 2000,
                2100, 2200, 2300, 2400, 2500, 2600, 2700, 2800, 2900));
        ArrayList<Integer> secondResultDocIds = secondHeapScores.getTopDocIdReversed();
        Assertions.assertEquals(secondRankedDocIds,secondResultDocIds);

        int[] thirdDocIds = new int[]{20,1,10,15,130,40};
        float[] thirdArrayOfScores = new float[]{3.422f, 3.422f, 3.422f, 3.422f, 3.422f, 3.422f};
        ArrayList<Integer> thirdRankedDocIds = new ArrayList<>(List.of(20,1,10,15,130,40));
        MinHeapScores thirdHeapScores = new MinHeapScores();
        for (int i = 0; i < thirdDocIds.length; i++)
            thirdHeapScores.insertIntoPriorityQueue(thirdArrayOfScores[i],thirdDocIds[i]);

        Assertions.assertEquals(thirdHeapScores.getScore2DocIdMap().size(),1);
        ArrayList<Integer> thirdResultDocIds = thirdHeapScores.getTopDocIdReversed();
        Assertions.assertEquals(thirdRankedDocIds,thirdResultDocIds);

        System.out.println("test on the class MinHeapScores --> SUCCESSFUL");
    }

    private static void removeDocIdFromMap(float score){
        ArrayList<Integer> docIds = score2DocIdMap.get(score);
        if(docIds.size()>1){ //if there are more than 1 docIDs associated to the score then remove only one
            docIds.remove(docIds.size()-1);
        }
        else{ //if there is only one element then remove the tuple from the hashmap
            score2DocIdMap.remove(score);
        }
    }

    public static void testCurrentDocIdInPostingList() {
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

    public static void testMinDocId() {

        int[] arraysOfMinDocIds = {1,2,4,5,6,7,9,10,11,24};
        int[] docFreqs = {3,3,3,3,3};
        int[] arraysOfResults = new int[arraysOfMinDocIds.length];

        int minDocId;
        int count = 0;
        while((minDocId = getMinDocId()) != collectionSize) {
            arraysOfResults[count] = minDocId;
            for (int i =0; i<5;i++)
            {
                if (postingListBlocks[i].getCurrentDocId() == minDocId)
                {
                    docFreqs[i]--;
                    if (docFreqs[i] == 0)
                        endOfPostingListFlag[i] = true;
                    else
                        postingListBlocks[i].next();
                }
            }
            count++;
        }

        // Assertion to check if arrays are equal
        Assertions.assertArrayEquals(arraysOfResults, arraysOfMinDocIds);
        System.out.println("test on the method getMinDocID --> SUCCESSFUL");
    }

    public static void testNextGEQ() throws IOException {
        LexiconHandler lexiconHandler = new LexiconHandler();
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

    public static void testSortArraysByArrays() throws IOException {
        setLongerFirstPostingList();
        setLongerSecondPostingList();
        setLongerThirdPostingList();
        setLongerFourthPostingList();
        setLongerFifthPostingList();

        int count = 0;
        for (int i = 0; i < 5; i++) {
            skipDescriptors[i] = new SkipDescriptor();
            skipDescriptors[i].add(13 + count,4 + count);
            skipDescriptors[i].add(16 + count,8 + count);
            skipDescriptors[i].add(120 + count,33 + count);
            count += 10;
            //System.out.println(postingListBlocks[i].toString());
        }

        docFreqs[0] = 1;
        docFreqs[1] = 50;
        docFreqs[2] = 20;
        docFreqs[3] = 10;
        docFreqs[4] = 60;

        PostingListBlock[] resultsPostingListBlocks = new PostingListBlock[5];

        for (int i = 0; i < 5; i++)
            resultsPostingListBlocks[i] = new PostingListBlock();

        for (PostingElement postingElement: postingListBlocks[0].getPostingList())
            resultsPostingListBlocks[0].addPostingElement(postingElement);

        for (PostingElement postingElement: postingListBlocks[4].getPostingList())
            resultsPostingListBlocks[4].addPostingElement(postingElement);

        for (PostingElement postingElement: postingListBlocks[2].getPostingList())
            resultsPostingListBlocks[2].addPostingElement(postingElement);

        for (PostingElement postingElement: postingListBlocks[3].getPostingList())
            resultsPostingListBlocks[1].addPostingElement(postingElement);

        for (PostingElement postingElement: postingListBlocks[1].getPostingList())
            resultsPostingListBlocks[3].addPostingElement(postingElement);

        int[] resultsDocFreqs = new int[]{1,10,20,50,60};
        int[] resultsOffsets = new int[]{0,30,20,10,40};
        SkipDescriptor[] resultsOfSkipDescriptors = new SkipDescriptor[5];
        for (int i = 0; i < 5; i++)
            resultsOfSkipDescriptors[i] = new SkipDescriptor();

        resultsOfSkipDescriptors[0].add(13,4);
        resultsOfSkipDescriptors[0].add(16,8);
        resultsOfSkipDescriptors[0].add(120,33);
        resultsOfSkipDescriptors[1].add(43,34);
        resultsOfSkipDescriptors[1].add(46,38);
        resultsOfSkipDescriptors[1].add(150,63);
        resultsOfSkipDescriptors[2].add(33,24);
        resultsOfSkipDescriptors[2].add(36,28);
        resultsOfSkipDescriptors[2].add(140,53);
        resultsOfSkipDescriptors[3].add(23,14);
        resultsOfSkipDescriptors[3].add(26,18);
        resultsOfSkipDescriptors[3].add(130,43);
        resultsOfSkipDescriptors[4].add(53,44);
        resultsOfSkipDescriptors[4].add(56,48);
        resultsOfSkipDescriptors[4].add(160,73);

        sortArraysByArray(docFreqs,offsets,skipDescriptors,postingListBlocks);

        Assertions.assertArrayEquals(resultsDocFreqs, docFreqs);
        Assertions.assertArrayEquals(resultsOffsets, offsets);
        for (int i = 0; i < 5; i++) {
            Assertions.assertArrayEquals(skipDescriptors[i].getMaxDocIds().toArray(new Integer[0]),
                    resultsOfSkipDescriptors[i].getMaxDocIds().toArray(new Integer[0]));

            Assertions.assertArrayEquals(postingListBlocks[i].getPostingList().toArray(),
                    resultsPostingListBlocks[i].getPostingList().toArray());

            //System.out.println(resultsPostingListBlocks[i].getPostingList());
        }

        System.out.println("test on the method sortArraysByArrays --> SUCCESSFUL");
    }

    public static void sortArraysByArray(int[] arrayToSort, int[] offsets,
                                         SkipDescriptor[] skipDescriptors, PostingListBlock[] plBlocks){

        Integer[] indexes = new Integer[arrayToSort.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }

        Arrays.sort(indexes, Comparator.comparingInt(i -> arrayToSort[i]));

        for (int i = 0; i < arrayToSort.length; i++) {
            if (indexes[i] != i) {
                int temp = arrayToSort[i];
                arrayToSort[i] = arrayToSort[indexes[i]];
                arrayToSort[indexes[i]] = temp;

                temp = offsets[i];
                offsets[i] = offsets[indexes[i]];
                offsets[indexes[i]] = temp;

                SkipDescriptor tempSkipDescriptor = skipDescriptors[i];
                skipDescriptors[i] = skipDescriptors[indexes[i]];
                skipDescriptors[indexes[i]] = tempSkipDescriptor;

                PostingListBlock postingListBlock = plBlocks[i];
                plBlocks[i] = plBlocks[indexes[i]];
                plBlocks[indexes[i]] = postingListBlock;

                indexes[indexes[i]] = indexes[i];
            }
        }
    }

    public static void setPostingListBlocksForTesting() throws IOException {

        invertedIndexHandler = new InvertedIndexHandler();
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
        LexiconHandler lexiconHandler = new LexiconHandler();
        InvertedIndexHandler invertedIndexHandler = new InvertedIndexHandler();
        ByteBuffer entryBuffer = lexiconHandler.findTermEntry(string);
        String term = lexiconHandler.getTerm(entryBuffer);
        int documentFrequency = lexiconHandler.getDf(entryBuffer);
        int offset = lexiconHandler.getOffset(entryBuffer);
        float termUpperBoundScore = lexiconHandler.getTermUpperBoundScore(entryBuffer);
        PostingListBlock postingListBlock = invertedIndexHandler.getPostingList(offset,documentFrequency);
        return postingListBlock;
    }

    public static void checkLexiconEntry(String string, int docId) throws IOException {
        DocumentIndexHandler documentIndexHandler = new DocumentIndexHandler();
        LexiconHandler lexiconHandler = new LexiconHandler();
        InvertedIndexHandler invertedIndexHandler = new InvertedIndexHandler();
        ByteBuffer entryBuffer = lexiconHandler.findTermEntry(string);
        String term = lexiconHandler.getTerm(entryBuffer);
        int documentFrequency = lexiconHandler.getDf(entryBuffer);
        int offset = lexiconHandler.getOffset(entryBuffer);
        float termUpperBoundScore = lexiconHandler.getTermUpperBoundScore(entryBuffer);
        PostingListBlock postingListBlock = invertedIndexHandler.getPostingList(offset,documentFrequency);
        System.out.println("term = " + string);
        System.out.println("postingList = " + postingListBlock);
        System.out.println("documentLength = " + documentIndexHandler.readDocumentLength(docId));
        System.out.println("documentFrequency = " + documentFrequency);
    }

    public static void printAllPostingLists() {
        for (int i = 0; i < 5; i++)
            System.out.println(postingListBlocks[i].getPostingList());
    }
}
