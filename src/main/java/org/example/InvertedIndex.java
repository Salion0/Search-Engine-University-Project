package org.example;

import java.util.HashMap;

public class InvertedIndex {
    private int blockId;
    HashMap<String, PostingList> invertedIndex = new HashMap<String, PostingList>();

    public InvertedIndex (int blockId){
        this.blockId = blockId;
    }
    public void processDocument(int docId, String[] tokens) {
        HashMap<String, Integer> wordCount = new HashMap<String, Integer>();

        for (String token : tokens)   //map with frequencies only
            if (wordCount.get(token) == null)
                wordCount.put(token,1);
            else
                wordCount.put(token,wordCount.get(token) + 1);

        //map with blockId included, therefore the dictionary of Posting Lists
        for (String term: wordCount.keySet()) {
            if (invertedIndex.get(term) == null) {
                PostingList postingList = new PostingList(blockId,new PostingElement(docId, Integer.valueOf(wordCount.get(term))));
                invertedIndex.put(term, postingList);
            } else{
                /*
                invertedIndex.put(
                        term, invertedIndex.get(term).addPostingElement(
                                new PostingElement(docId, Integer.valueOf(wordCount.get(term)))
                        )
                  );
                 */
                invertedIndex.get(term).addPostingElement(new PostingElement(docId, Integer.valueOf(wordCount.get(term))));
            }
        }

        for (String term: wordCount.keySet()) {
            //System.out.println("term: " + term + " , frequency: " + dictionary.get(term).);
            System.out.println();
        }

    }
}
