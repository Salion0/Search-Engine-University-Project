package it.unipi.mircv;

import it.unipi.mircv.File.DocumentIndexFileHandler;
import it.unipi.mircv.File.InvertedIndexFileHandler;
import it.unipi.mircv.File.LexiconFileHandler;
import it.unipi.mircv.File.SkipDescriptorFileHandler;
import it.unipi.mircv.Index.*;
import it.unipi.mircv.Query.ConjunctiveDAAT;
import it.unipi.mircv.Query.ConjunctiveDAATCompression;
import it.unipi.mircv.Query.DisjunctiveDAAT;
import it.unipi.mircv.Query.DisjunctiveDAATCompression;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import static it.unipi.mircv.Config.*;

public class TestCompression {
    public static void main(String[] args) throws IOException {
        flagStemming = false;
        flagStopWordRemoval = true;
        flagCompressedReading = false;

        long startTime = System.currentTimeMillis();
        Index index = new Index("collection.tsv");
        BlockMergerCompression blockMergerCompression = new BlockMergerCompression();
        blockMergerCompression.mergeBlocks(index.getNumberOfBlocks());
        System.out.println(System.currentTimeMillis() - startTime);
        /*
        InvertedIndexFileHandler invertedIndexFileHandler = new InvertedIndexFileHandler();
        LexiconFileHandler lexiconFileHandler = new LexiconFileHandler();
        ByteBuffer byteBuffer = lexiconFileHandler.findTermEntryCompression("workers");

        int docFreq = lexiconFileHandler.getDfCompression(byteBuffer);
        int collFreq = lexiconFileHandler.getCfCompression(byteBuffer);
        long offsetDocIdCompression = lexiconFileHandler.getOffsetDocIdCompression(byteBuffer);
        long offsetTermFreqCompression = lexiconFileHandler.getOffsetTermFreqCompression(byteBuffer);
        int numByteDocId = lexiconFileHandler.getNumByteDocId(byteBuffer);
        int numByteTermFreq = lexiconFileHandler.getNumByteTermFreq(byteBuffer);
        int offsetSkipDesc = lexiconFileHandler.getOffsetSkipDescCompression(byteBuffer);
        System.out.println("docFreq: " + docFreq);
        System.out.println("collFreq: " + collFreq);
        System.out.println("offsetDocIdCompression: " + offsetDocIdCompression);
        System.out.println("offsetTermFreqCompression: " + offsetTermFreqCompression);
        System.out.println("numByteDocId: " + numByteDocId);
        System.out.println("numByteTermFreq: " + numByteTermFreq);
        System.out.println("offsetSkipDesc: " + offsetSkipDesc);

        PostingListBlock postingListBlock = invertedIndexFileHandler.getPostingListCompressed(
                docFreq,
                offsetDocIdCompression, numByteDocId,
                offsetTermFreqCompression, numByteTermFreq
        );
        System.out.println("postingListBlock: " + postingListBlock);
        SkipDescriptorFileHandler skipDescriptorFileHandler = new SkipDescriptorFileHandler();
        SkipDescriptorCompression skipDescriptorCompression = skipDescriptorFileHandler.readSkipDescriptorCompression(offsetSkipDesc, (int) Math.ceil(Math.sqrt(docFreq)));
        System.out.println("skipDescriptorCompression size: " + skipDescriptorCompression.size());
        System.out.println("maxDocIds: " + skipDescriptorCompression.getMaxDocIds());
        long[] nextGEQresult = skipDescriptorCompression.nextGEQ(8711);
        System.out.println("nextGEQresult[0]: " + nextGEQresult[0]);
        System.out.println("nextGEQresult[1]: " + nextGEQresult[1]);
        System.out.println("nextGEQresult[2]: " + nextGEQresult[2]);
        System.out.println("nextGEQresult[3]: " + nextGEQresult[3]);
        System.out.println("nextGEQresult[4]: " + nextGEQresult[4]);
        PostingListBlock postingListBlock1 = invertedIndexFileHandler.getPostingListCompressed(
                7,
                4581,
                14,
                504,
                1
        );
        System.out.println(postingListBlock1);


        //--------------------CARICO LE DOC LEN--------------------------------------------------------
        DocumentIndexFileHandler documentIndexHandler = new DocumentIndexFileHandler();
        Utils.loadStopWordList();
        Config.collectionSize = documentIndexHandler.readCollectionSize();
        Config.avgDocLen = documentIndexHandler.readAvgDocLen();

        //TestLorenzo.checkLexiconEntry("diet");

        //TODO da fare più veloce perchè così ci vuole una vita e poi da mettere in Documenet Index
        Config.docsLen = new int[Config.collectionSize];
        for (int i = 0; i < Config.collectionSize; i++){
            Config.docsLen[i] = documentIndexHandler.readDocumentLength(i);
        }
        //----------------------------------------------------------------------------------------------
        System.out.println("------------query------------------------");
        String[] query = new String[]{"railroad", "workers"};
        DisjunctiveDAAT conjunctiveDAATCompression = new DisjunctiveDAAT(query);
        ArrayList<Integer> result = conjunctiveDAATCompression.processQuery();

        for (String s: documentIndexHandler.getDocNoREVERSE(result)) {
            System.out.println(s);
        }
        */
    }
}
