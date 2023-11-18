package it.unipi.mircv.Query;

import it.unipi.mircv.Config;
import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.File.InvertedIndexHandler;
import it.unipi.mircv.File.LexiconHandler;
import it.unipi.mircv.Index.PostingListBlock;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class DisjunctiveDAAT {
    private final String[] queryTerms;
    private final int numTermQuery;
    private final DocumentIndexHandler documentIndexHandler;
    private final InvertedIndexHandler invertedIndexHandler;
    private final int collectionSize;
    private final float avgDocLen;

    public DisjunctiveDAAT(String[] queryTerms) throws IOException {
        documentIndexHandler = new DocumentIndexHandler();
        LexiconHandler lexiconHandler = new LexiconHandler();
        invertedIndexHandler = new InvertedIndexHandler();
        //---------------INITIALIZE ARRAYS---------------------------
        this.queryTerms = queryTerms;
        numTermQuery = queryTerms.length;
        this.numBlockRead = new int[numTermQuery];
        this.docFreqs =  new int[numTermQuery];
        this.collectionFreqs = new int[numTermQuery];
        this.offsets = new int[numTermQuery];
        this.endOfPostingListBlockFlag = new boolean[numTermQuery];
        this.endOfPostingListFlag = new boolean[numTermQuery];

        //-------------INITIALIZE COLLECTION STATISTICS -------------
        collectionSize = documentIndexHandler.collectionSize();
        avgDocLen = documentIndexHandler.readAvgDocLen();


        //-------------INITIALIZE TERM STATISTICS---------------------
        for (int i = 0; i < numTermQuery; i++) {
            ByteBuffer entryBuffer = lexiconHandler.findTermEntry(queryTerms[i]);
            docFreqs[i] = lexiconHandler.getDf(entryBuffer);
            collectionFreqs[i] = lexiconHandler.getCf(entryBuffer);
            offsets[i] = lexiconHandler.getOffset(entryBuffer);
        }
        initializePostingListBlocks();

    }
    public ArrayList<Integer> process() throws IOException {
        MinHeapScores heapScores = new MinHeapScores();
        float currentDocScore;
        int minDocId;

        int count = 0;//DEBUG
        while ((minDocId = getMinDocId()) != collectionSize) {
            currentDocScore = 0;
            System.out.println("minDocId: " + minDocId);
            //-----------------------COMPUTE THE SCORE-------------------------------------------------------
            int currentTf;
            int documentLength = documentIndexHandler.readDocumentLength(minDocId);
            for (int i =0; i<numTermQuery;i++) {
                PostingListBlock postingListBlock = postingListBlocks.get(i);
                if (postingListBlock.getCurrentDocId() == minDocId) {

                    currentTf = postingListBlock.getCurrentTf();
                    //currentDocScore += docPartialScore(currentTf); //questa era la versione originale dove usavamo le frequency per lo score
                    currentDocScore += computeBM25(currentTf,documentLength,docFreqs[i]);

                    //increment the position in the posting list
                    if(postingListBlock.next() == -1){         //increment position and if end of block reached then set the flag
                        endOfPostingListBlockFlag[i] = true;
                    }
                }
            }
            heapScores.insertIntoPriorityQueue(currentDocScore, minDocId);
            updatePostingListBlocks();


            if(count == 0) { //DEBUG
                for(PostingListBlock plb: postingListBlocks){
                    System.out.print("Print postingListBlocks");
                    System.out.println(plb);
                }
            }
            count++; //DEBUG
        }

        //System.out.println("DEBUGGG --> " + heapScores.getDocId((float) 3.1066878)); //DEBUG
        return heapScores.getTopDocIdReversed();
    }
    private int getMinDocId() {
        int minDocId = collectionSize;  //valore che indica che le posting list sono state raggiunte

        //find the current min doc id in the posting lists of the query terms
        for (int i = 0; i < numTermQuery; i++){
            if (endOfPostingListFlag[i]) continue;
            int currentDocId = postingListBlocks.get(i).getCurrentDocId();
            if(currentDocId<minDocId){
                minDocId = currentDocId;
            }
        }

        return minDocId;
    }
}
