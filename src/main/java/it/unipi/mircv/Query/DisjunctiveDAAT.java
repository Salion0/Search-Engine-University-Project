package it.unipi.mircv.Query;

import it.unipi.mircv.File.DocumentIndexHandler;
import it.unipi.mircv.File.InvertedIndexHandler;
import it.unipi.mircv.File.LexiconHandler;
import it.unipi.mircv.Index.PostingListBlock;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static it.unipi.mircv.Config.POSTING_LIST_BLOCK_LENGTH;
import static it.unipi.mircv.Config.collectionSize;

public class DisjunctiveDAAT {
    private final int numTermQuery;
    private final DocumentIndexHandler documentIndexHandler;
    private final InvertedIndexHandler invertedIndexHandler;
    private ArrayList<PostingListBlock> postingListBlocks;
    private final int[] numBlockRead;
    private final int[] docFreqs;
    private final int[] offsets;
    private final boolean[] endOfPostingListBlockFlag;
    private final boolean[] endOfPostingListFlag;

    public DisjunctiveDAAT(String[] queryTerms) throws IOException {
        documentIndexHandler = new DocumentIndexHandler();
        LexiconHandler lexiconHandler = new LexiconHandler();
        invertedIndexHandler = new InvertedIndexHandler();

        //--------------------DEFINE ARRAYS------------------------//
        numTermQuery = queryTerms.length;
        numBlockRead = new int[numTermQuery];

        docFreqs =  new int[numTermQuery];
        offsets = new int[numTermQuery];

        endOfPostingListBlockFlag = new boolean[numTermQuery];
        endOfPostingListFlag = new boolean[numTermQuery];


        //-------------INITIALIZE TERM STATISTICS------------------//
        for (int i = 0; i < numTermQuery; i++) {
            ByteBuffer entryBuffer = lexiconHandler.findTermEntry(queryTerms[i]);
            docFreqs[i] = lexiconHandler.getDf(entryBuffer);
            offsets[i] = lexiconHandler.getOffset(entryBuffer);
        }
        initializePostingListBlocks();

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

    public ArrayList<Integer> processQuery() throws IOException {
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
                    currentDocScore += ScoreFunction.BM25(currentTf, documentLength, docFreqs[i]);

                    //increment the position in the posting list
                    if(postingListBlock.next() == -1){         //increment position and if end of block reached then set the flag
                        endOfPostingListBlockFlag[i] = true;
                    }
                }
            }
            heapScores.insertIntoPriorityQueue(currentDocScore , minDocId);
            //heapScores.insertIntoPriorityQueue((int) (currentDocScore * 10000), minDocId);
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
    private void initializePostingListBlocks() throws IOException {
        postingListBlocks = new ArrayList<>(numTermQuery);
        for(int i=0; i<numTermQuery; i++){
            if(POSTING_LIST_BLOCK_LENGTH > docFreqs[i]){ //if posting list length is less than the block size
                postingListBlocks.add(i,this.invertedIndexHandler.getPostingList(offsets[i],docFreqs[i]));
            }
            else{                                     //else posting list length is greather than block size
                postingListBlocks.add(i,this.invertedIndexHandler.getPostingList(offsets[i],POSTING_LIST_BLOCK_LENGTH));
            }
            numBlockRead[i]++;
        }
    }

    //TODO cambiare la logica di questo metodo con la stessa che c'è in conjunctive DAAT in processQuery
    private void updatePostingListBlocks() throws IOException {
        for(int i=0; i<numTermQuery; i++){
            if(endOfPostingListBlockFlag[i]){ //if one block is completely processed then load the subsequent if exists
                // read the subsequent block
                int elementToRead = docFreqs[i] - POSTING_LIST_BLOCK_LENGTH*numBlockRead[i];
                //System.out.println(elementToRead); //DEBUG

                //check if exist a subsequent block using the docFreqs which is equal to the length of the posting list
                if (elementToRead > 0) {
                    if(elementToRead > POSTING_LIST_BLOCK_LENGTH ) {
                        elementToRead = POSTING_LIST_BLOCK_LENGTH;
                    }
                    postingListBlocks.set(i, // ho messo i perché sennò facevamo sempre append e non replace
                            this.invertedIndexHandler.getPostingList(offsets[i] + (POSTING_LIST_BLOCK_LENGTH * numBlockRead[i]),
                                    elementToRead));
                    //System.out.println(this.invertedIndexHandler.getPostingList(offsets[i] + (POSTING_LIST_BLOCK_LENGTH * numBlockRead[i]), elementToRead)); //DEBUG
                    endOfPostingListBlockFlag[i] = false; // resetto il campo perché ho caricato un altro blocco
                    numBlockRead[i]++; // ho letto un altro blocco quindi aumento il campo
                }
                else{
                    endOfPostingListFlag[i]=true;
                }
            }
        }
    }
}
