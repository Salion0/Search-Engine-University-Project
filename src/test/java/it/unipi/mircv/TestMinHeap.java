package it.unipi.mircv;

import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.query.MinHeapScores;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestMinHeap {
    static HashMap<Float, ArrayList<Integer>> score2DocIdMap;

    public static void main(String[] args) throws IOException {
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        Parameters.collectionSize = documentIndexHandler.readCollectionSize();
        Parameters.avgDocLen = documentIndexHandler.readAvgDocLen();

        testMinHeap();
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

}
