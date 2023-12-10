package it.unipi.mircv.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import static it.unipi.mircv.Config.*;
public class MinHeapScores {

    //------------------Priority Queue of Doc Retrieved-----------------------//
    private final HashMap<Float, ArrayList<Integer>> score2DocIdMap;  //list of docIDs retrieved sorted by ranking
    private final PriorityQueue<Float> topScores;
    private int topDocCount; //counter for keep track of how many document have been inserted in the min-heap

    private int parameterForMaxScore; // DEBUG

    public MinHeapScores(){
        score2DocIdMap = new HashMap<>();
        topScores = new PriorityQueue<>();
        topDocCount = 0;
        parameterForMaxScore = 0;
    }

    private void insertDocIdInMap(float score,int docId){
        if (score2DocIdMap.containsKey(score)) {  //if score is present in hashmap
            score2DocIdMap.get(score).add(docId); //add element to the arrayList of docId
        }else{
            ArrayList<Integer> arrayList = new ArrayList<>();
            arrayList.add(docId);
            score2DocIdMap.put(score, arrayList);
        }
    }
    private void removeDocIdFromMap(float score){
        ArrayList<Integer> docIds = score2DocIdMap.get(score);
        if(docIds.size()>1){ //if there are more than 1 docIDs associated to the score then remove only one
            docIds.remove(docIds.size()-1);
        }
        else{ //if there is only one element then remove the tuple from the hashmap
            score2DocIdMap.remove(score);
        }
    }
    public void insertIntoPriorityQueue(float docScore, int minDocId){
        if (topDocCount < MAX_NUM_DOC_RETRIEVED){  //There less than k documents in the priority queue
            topDocCount++;
            try {
                topScores.add(docScore);
                insertDocIdInMap(docScore,minDocId);
            }catch(Exception e){
                e.printStackTrace();
            }

        }else{      //there are more than k documents in the priority queue

            float peek = topScores.peek();
            if(docScore > peek) { //need to check if minDocId should be inserted
                topScores.remove(peek); // in the peek there is the minScore
                topScores.add(docScore);
                removeDocIdFromMap(peek);
                insertDocIdInMap(docScore,minDocId);
            }
        }
    }

    public void insertIntoPriorityQueueMAXSCORE(float docScore, int minDocId){
        float peek = topScores.peek();
        if(docScore > peek) { //need to check if minDocId should be inserted
            topScores.remove(peek); // in the peek there is the minScore
            topScores.add(docScore);
            if (parameterForMaxScore >= topDocCount)
                removeDocIdFromMap(peek);
            insertDocIdInMap(docScore,minDocId);
            parameterForMaxScore++;
        }
    }

    public Float getMinScore() {return topScores.peek();}

    public PriorityQueue<Float> getTopScores(){return topScores; }
    public ArrayList<Integer> getDocId(float scores){return this.score2DocIdMap.get(scores);}

    public ArrayList<Integer> getTopDocIdReversed() {
        Float score;
        ArrayList<Integer> topDocId = new ArrayList<>();

        Float prevScore = 0f;

        System.out.println(score2DocIdMap); //DEBUG
        while((score = topScores.poll()) != null){
            if(score.equals(prevScore)) continue;
            prevScore = score;
            for (int docId:score2DocIdMap.get(score)) {
                topDocId.add(docId);
            }
        }
        return topDocId;
    }

    public void setTopDocCount(int quantity) {
        for (int i = 0; i < quantity; i++)
            topScores.offer((float) 0);
        topDocCount = quantity;
    } // PER MAX-SCORE

    public HashMap<Float, ArrayList<Integer>> getScore2DocIdMap() { // for TestUnit purpose
        return score2DocIdMap;
    }
}
