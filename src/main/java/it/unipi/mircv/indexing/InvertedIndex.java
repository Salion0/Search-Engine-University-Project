package it.unipi.mircv.indexing;
import java.util.ArrayList;
import java.util.HashMap;

public class InvertedIndex {
    private  HashMap<String, PostingList> invertedIndex = new HashMap<String, PostingList>();

    public HashMap<String, PostingList> getInvertedIndex(){
        return invertedIndex;
    }
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
        byte[] dataToReturn = null;  //tutto inverted index

        for (String term: invertedIndex.keySet()) {
            System.out.print("ToByte Method Processing: " + term); //DEBUG
            ArrayList<PostingElement> postingList = invertedIndex.get(term).getPostingList();
            int lengthPostingList = postingList.size();
            //64 bytes for String term + 1 int for lengthPostingList + lengthPostingList * 2
            //2 per postingElement because we have to store docId and occurrence (frequency)
            byte[] data = new byte[64 + 1 + lengthPostingList*2];

            byte[] termData = term.getBytes();
            System.arraycopy(termData, 0, data, 0, termData.length);

            data[63] = (byte) lengthPostingList; //in 64th position

            for (int i=0; i < lengthPostingList; i++){
                byte byteDocId = (byte) postingList.get(i).getDocId();
                byte byteOccurrences = (byte) postingList.get(i).getOccurrences();
                //data[64] = TODO Contatore che va di 64 + 1+ 2*PostingListLength
            }

            System.out.println(); //DEBUG
        }

        return dataToReturn;
    }
}
