package it.unipi.mircv.Query;

import it.unipi.mircv.File.DocumentIndexFileHandler;
import it.unipi.mircv.File.InvertedIndexFileHandler;
import it.unipi.mircv.File.LexiconFileHandler;
import it.unipi.mircv.File.SkipDescriptorFileHandler;
import it.unipi.mircv.Index.PostingListBlock;
import it.unipi.mircv.Index.SkipDescriptorCompression;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static it.unipi.mircv.Config.*;

public class DisjunctiveDAATCompression {
    private final int numTermQuery;
    private final DocumentIndexFileHandler documentIndexHandler;
    private final InvertedIndexFileHandler invertedIndexFileHandler;
    private final LexiconFileHandler lexiconFileHandler;
    private final PostingListBlock[] postingListBlocks;
    private final int[] numBlockRead;
    private final int[] docFreqs;
    private final long[] offsetsDocId;
    private final long[] offsetsTermFreq;
    private final SkipDescriptorCompression[] skipDescriptorsCompression;
    private final int[] numPostingPerBlock;
    private final boolean[] endOfPostingListFlag;

    public DisjunctiveDAATCompression(String[] queryTerms) throws IOException {
        documentIndexHandler = new DocumentIndexFileHandler();
        lexiconFileHandler = new LexiconFileHandler();
        invertedIndexFileHandler = new InvertedIndexFileHandler();
        SkipDescriptorFileHandler skipDescriptorFileHandler = new SkipDescriptorFileHandler();

        //--------------------DEFINE ARRAYS------------------------//
        numTermQuery = queryTerms.length;

        postingListBlocks = new PostingListBlock[numTermQuery];
        docFreqs =  new int[numTermQuery];
        offsetsDocId = new long[numTermQuery];
        offsetsTermFreq = new long[numTermQuery];
        numBlockRead = new int[numTermQuery];
        endOfPostingListFlag = new boolean[numTermQuery];
        skipDescriptorsCompression = new SkipDescriptorCompression[numTermQuery];
        numPostingPerBlock = new int[numTermQuery];

        //-------------INITIALIZE TERM STATISTICS------------------//
        for (int i = 0; i < numTermQuery; i++)
        {
            ByteBuffer entryBuffer = lexiconFileHandler.findTermEntryCompression(queryTerms[i]);
            docFreqs[i] = lexiconFileHandler.getDfCompression(entryBuffer);
            offsetsDocId[i] = lexiconFileHandler.getOffsetDocIdCompression(entryBuffer);
            offsetsTermFreq[i] = lexiconFileHandler.getOffsetTermFreqCompression(entryBuffer);

            if(docFreqs[i] > (MIN_NUM_POSTING_TO_SKIP * MIN_NUM_POSTING_TO_SKIP)){
                numPostingPerBlock[i] = (int) Math.sqrt(docFreqs[i]);
                skipDescriptorsCompression[i] = skipDescriptorFileHandler.readSkipDescriptorCompression(
                        lexiconFileHandler.getOffsetSkipDescCompression(entryBuffer), (int) Math.ceil((float) docFreqs[i] / (int) Math.sqrt(docFreqs[i])));
                System.out.println("skipDescriptorsCompression SIZE : " + skipDescriptorsCompression[i].size());
                System.out.println("skipDescriptorsCompression" + skipDescriptorsCompression[i]);
                postingListBlocks[i] = invertedIndexFileHandler.getPostingListCompressed(
                        numPostingPerBlock[i],
                        offsetsDocId[i], skipDescriptorsCompression[i].getNumByteMaxDocIds().get(0),
                        offsetsTermFreq[i], skipDescriptorsCompression[i].getNumByteTermFreqs().get(0));
            }
            else{
                skipDescriptorsCompression[i] = null;

                postingListBlocks[i] = invertedIndexFileHandler.getPostingListCompressed(
                        docFreqs[i],
                        offsetsDocId[i], lexiconFileHandler.getNumByteDocId(entryBuffer),
                        offsetsTermFreq[i], lexiconFileHandler.getNumByteTermFreq(entryBuffer));
                System.out.println(postingListBlocks[0]);
            }

            numBlockRead[i] = 1;
        }
    }

    private int getMinDocId() {
        int minDocId = collectionSize;  //valore che indica che le posting list sono state raggiunte

        //find the current min doc id in the posting lists of the query terms
        for (int i = 0; i < numTermQuery; i++){
            if (endOfPostingListFlag[i]) continue;
            int currentDocId = postingListBlocks[i].getCurrentDocId();
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

        while ((minDocId = getMinDocId()) != collectionSize)
        {
            currentDocScore = 0;
            //System.out.println("minDocId: " + minDocId); //DEBUG
            //-----------------------COMPUTE THE SCORE-------------------------------------------------------
            int currentTf;
            //AIUDOO
            //int documentLength = documentIndexHandler.readDocumentLength(minDocId);
            int documentLength = docsLen[minDocId];
            for (int i =0; i<numTermQuery;i++)
            {
                if (postingListBlocks[i].getCurrentDocId() == minDocId)
                {
                    currentTf = postingListBlocks[i].getCurrentTf();
                    currentDocScore += ScoreFunction.BM25(currentTf, documentLength, docFreqs[i]);

                    if(!endOfPostingListFlag[i] && postingListBlocks[i].next() == -1)  //increment position and if end of block reached then set the flag
                        updatePostingListBlock(i);
                }
            }
            heapScores.insertIntoPriorityQueue(currentDocScore , minDocId);
        }

        return heapScores.getTopDocIdReversed();
    }

    //TODO cambiare la logica di questo metodo con la stessa che c'è in conjunctive DAAT in processQuery
    private void updatePostingListBlock(int i) throws IOException {
        // read the subsequent block
        if (skipDescriptorsCompression[i] == null){
            System.out.println("finita " + i);
            endOfPostingListFlag[i] = true;
        }else{
            //numBlockProcessed is equal to the index that we have to use to get the next block
            if (numBlockRead[i] < skipDescriptorsCompression[i].size()-1){//there is another block to load
                postingListBlocks[i] = invertedIndexFileHandler.getPostingListCompressed(
                        numPostingPerBlock[i],
                        skipDescriptorsCompression[i].getOffsetMaxDocIds().get(numBlockRead[i]),
                        skipDescriptorsCompression[i].getNumByteMaxDocIds().get(numBlockRead[i]),
                        skipDescriptorsCompression[i].getOffsetTermFreqs().get(numBlockRead[i]),
                        skipDescriptorsCompression[i].getNumByteTermFreqs().get(numBlockRead[i])
                );
                numBlockRead[i]++;
            }

            else if(numBlockRead[i] == skipDescriptorsCompression[i].size()-1 && docFreqs[i] % numPostingPerBlock[i] != 0){
                //there is another INCOMPLETE block to load
                System.out.println(docFreqs[i] % numPostingPerBlock[i]);
                postingListBlocks[i] = invertedIndexFileHandler.getPostingListCompressed(
                        docFreqs[i] % numPostingPerBlock[i],
                        skipDescriptorsCompression[i].getOffsetMaxDocIds().get(numBlockRead[i]),
                        skipDescriptorsCompression[i].getNumByteMaxDocIds().get(numBlockRead[i]),
                        skipDescriptorsCompression[i].getOffsetTermFreqs().get(numBlockRead[i]),
                        skipDescriptorsCompression[i].getNumByteTermFreqs().get(numBlockRead[i])
                );
                numBlockRead[i]++;
            }else{
                //there are no more blocks to load
                endOfPostingListFlag[i] = true;
            }
        }
    }
}
