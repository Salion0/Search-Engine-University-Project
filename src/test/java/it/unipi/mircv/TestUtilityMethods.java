package it.unipi.mircv;

import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.index.PostingElement;
import it.unipi.mircv.index.PostingListBlock;
import it.unipi.mircv.index.SkipDescriptor;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import static it.unipi.mircv.Config.collectionSize;

public class TestUtilityMethods {
    static PostingListBlock[] postingListBlocks = new PostingListBlock[5];
    static boolean[] endOfPostingListFlag = new boolean[5];
    static SkipDescriptor[] skipDescriptors = new SkipDescriptor[5];
    static int[] docFreqs = new int[5];
    static int[] numBlockRead = new int[5];
    static int[] offsets = new int[5];

    public static void main(String[] args) throws IOException {
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        Config.collectionSize = documentIndexHandler.readCollectionSize();
        Config.avgDocLen = documentIndexHandler.readAvgDocLen();
        setPostingListBlocksForTesting();

        testMinDocId();
        testSortArraysByArrays();
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

        for (PostingElement postingElement: postingListBlocks[0].getList())
            resultsPostingListBlocks[0].addPostingElement(postingElement);

        for (PostingElement postingElement: postingListBlocks[4].getList())
            resultsPostingListBlocks[4].addPostingElement(postingElement);

        for (PostingElement postingElement: postingListBlocks[2].getList())
            resultsPostingListBlocks[2].addPostingElement(postingElement);

        for (PostingElement postingElement: postingListBlocks[3].getList())
            resultsPostingListBlocks[1].addPostingElement(postingElement);

        for (PostingElement postingElement: postingListBlocks[1].getList())
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

            Assertions.assertArrayEquals(postingListBlocks[i].getList().toArray(),
                    resultsPostingListBlocks[i].getList().toArray());

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
