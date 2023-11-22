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

    public MinHeapScores(){
        score2DocIdMap = new HashMap<>();
        topScores = new PriorityQueue<>();
        topDocCount =0;
    }
    private void insertDocIdInMap(float score,int docId){
        if (score2DocIdMap.containsKey(score)) {  //if score is present in hashmap
            score2DocIdMap.get(score).add(docId); //add element to the arrayList of docId
            System.out.println("score: " + score + " esiste già nell'hasmap, l'indirizzo è "+ score2DocIdMap.get(score));
        }else{
            System.out.println("score: " + score + " non esiste");
            ArrayList<Integer> arrayList = new ArrayList<>();
            arrayList.add(docId);
            score2DocIdMap.put(score, arrayList);
            System.out.println("adesso è diventato: " + score2DocIdMap.get(score));
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
    public boolean insertIntoPriorityQueue(float docScore, int minDocId){
        boolean result = false;
        if (topDocCount < MAX_NUM_DOC_RETRIEVED){  //There less than k documents in the priority queue
            topDocCount++;
            System.out.println("Entra nell' if insertPriorityQueue"); //DEBUG
            try {
                topScores.add(docScore);
                insertDocIdInMap(docScore,minDocId);
                result = true;

            }catch(Exception e){
                System.out.println("Errore Non posso inserire doc:"+minDocId);
                e.printStackTrace();
            }

        }else{      //there are more than k documents in the priority queue

            float peek = topScores.peek();
            if(docScore > peek) { //need to check if minDocId should be inserted
                topScores.remove(peek); // in the peek there is the minScore
                topScores.add(docScore);
                removeDocIdFromMap(peek);
                insertDocIdInMap(docScore,minDocId);
                result = true;
            }
        }

        return result;
    }

    public Float getMinScore() {return topScores.peek();}

    public PriorityQueue<Float> getTopScores(){return topScores; }
    public ArrayList<Integer> getDocId(float scores){return this.score2DocIdMap.get(scores);}

    public ArrayList<Integer> getTopDocIdReversed() {
        Float score;
        ArrayList<Integer> topDocId = new ArrayList<Integer>();

        Float prevScore = 0f;

        System.out.println(topScores);
        System.out.println(score2DocIdMap);
        while((score = topScores.poll()) != null){
            if(score.equals(prevScore)) continue;
            prevScore = score;
            for (int docId:score2DocIdMap.get(score)) {
                topDocId.add(docId);
            }
        }
        return topDocId;
    }
}
