package it.unipi.mircv;

import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.File.InvertedIndexHandler;
import it.unipi.mircv.File.SkipDescriptorFileHandler;
import it.unipi.mircv.Index.PostingElement;
import it.unipi.mircv.Index.PostingListBlock;
import it.unipi.mircv.Index.SkipDescriptor;
import it.unipi.mircv.Query.ScoreFunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import static it.unipi.mircv.Config.collectionSize;

import org.junit.jupiter.api.Assertions;


public class TestUnitLorenzo {
    static PostingListBlock[] postingListBlocks = new PostingListBlock[5];
    static SkipDescriptor[] skipDescriptors = new SkipDescriptor[5];
    static boolean[] endOfPostingListFlag = new boolean[5];
    static int[] docFreqs = new int[5];
    static int[] numBlockRead = new int[5];
    static InvertedIndexHandler invertedIndexHandler;
    static int[] offsets = new int[5];

    public static void main(String[] args) throws IOException {

        setPostingListBlocksForTesting();
        //testMinDocId();
        //testUploadPostingListBlock();
        //testSortArraysByArrays();
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
}
