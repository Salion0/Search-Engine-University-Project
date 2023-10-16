package it.unipi.mircv.indexing;

import java.util.HashMap;

public class InvertedIndex {
    HashMap<String, PostingList> invertedIndex = new HashMap<String, PostingList>();

    public void processDocument(int docId, String[] tokens) {
        //Count all occurrence of all terms in a document

        HashMap<String, Integer> wordCount = new HashMap<String, Integer>();

        for (String token : tokens)   //map with frequencies only
            if (wordCount.get(token) == null)
                wordCount.put(token,1);
            else
                wordCount.put(token,wordCount.get(token) + 1);

        //
        for (String term: wordCount.keySet()) {
            if (invertedIndex.get(term) == null) {
                PostingList postingList = new PostingList(new PostingElement(docId, wordCount.get(term)));
                invertedIndex.put(term, postingList);
            } else{
                invertedIndex.get(term).addPostingElement(new PostingElement(docId, wordCount.get(term)));
            }
        }


        //DEBUG
        /*
        for (String term: invertedIndex.keySet()) {
            System.out.println("term: " + term + " , frequency: " +
                    invertedIndex.get(term).getPostingList().toString());
            System.out.println();
        }
        */

    }

    //TODO
    public byte[] toBytes(){
        byte[] termData = new byte[64];
        byte lengthPostingList;
        byte[] postingListData;

        for (String term: invertedIndex.keySet()) {
            System.out.print("ToByte Method: " + term); //DEBUG
            lengthPostingList = (byte) invertedIndex.get(term).getPostingList().size();
            for (PostingElement postingElement: ){
                postingListData
            }
            System.out.println(); //DEBUG
        }

        finalData =
        return termData;
    }
}
