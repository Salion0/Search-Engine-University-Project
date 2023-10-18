package it.unipi.mircv.indexing;
import ca.rmen.porterstemmer.PorterStemmer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.TreeMap;

public class InvertedIndex {
//It represents the inverted index as a treeMap between Term and Posting List
    
    private  TreeMap<String, PostingList> treeMap = new TreeMap<String, PostingList>();
    private int size;

    public TreeMap<String, PostingList> getTreeMap(){
        return treeMap;
    }
    public PostingList getPostingList(String term){
        return treeMap.get(term);
    }
    public int getSize(){
        return treeMap.size();
    }
    public void processDocument(int docId, String[] tokens) {
        //Count all occurrence of all terms in a document

        TreeMap<String, Integer> wordCount = new TreeMap<String, Integer>();
        PorterStemmer stemmer = new PorterStemmer();

        for (String token : tokens) {  //map with frequencies only
            token = stemmer.stemWord(token);
            if (wordCount.get(token) == null)
                wordCount.put(token, 1);
            else
                wordCount.put(token, wordCount.get(token) + 1);
        }

        //
        for (String term: wordCount.keySet()) {
            if (treeMap.get(term) == null) {
                PostingList postingList = new PostingList(new PostingElement(docId, wordCount.get(term)));
                treeMap.put(term, postingList);
            } else{
                treeMap.get(term).addPostingElement(new PostingElement(docId, wordCount.get(term)));
            }
        }

        /*DEBUG
        for (String term: treeMap.keySet()) {
            System.out.println("term: " + term + " , frequency: " +
                    treeMap.get(term).getPostingList().toString());
            System.out.println();
        }
        */
    }


    public byte[] getBytes(){
        byte[] invertedIndexBytes;
        for(String term: treeMap.keySet()){
            //get Bytes data of term
            byte[] termData = term.getBytes();
            int termLength = termData.length;
            byte[] postingListData = treeMap.get(term).getBytes();
            int postingListLength = postingListData.length;

            // 1 byte for termLength + TermLength byte + 1 byte for postingListLength + postingListLength bytes
            ByteBuffer termBuffer = ByteBuffer.allocate(4+termLength+4+postingListLength);
            termBuffer.putInt(termLength).put(termData).putInt(postingListLength).put(postingListData);
        }
        //ByteBuffer invertedIndexBuffer = ByteBuffer.allocate()
        return null;
    }
}
