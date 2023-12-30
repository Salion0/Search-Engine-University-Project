package it.unipi.mircv.toDelete;

import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.file.InvertedIndexFileHandler;
import it.unipi.mircv.file.LexiconFileHandler;
import it.unipi.mircv.file.SkipDescriptorFileHandler;
import it.unipi.mircv.index.*;
import it.unipi.mircv.evaluation.SystemEvaluator;

import java.io.IOException;
import java.nio.ByteBuffer;

import static it.unipi.mircv.utility.Parameters.*;
import static it.unipi.mircv.utility.Parameters.QueryProcessor.DISJUNCTIVE_DAAT_C;
import static it.unipi.mircv.utility.Parameters.Score.TFIDF;
import static it.unipi.mircv.utility.Utils.loadStopWordList;
import static java.lang.System.currentTimeMillis;

public class TestCompression {
    public static void main(String[] args) throws IOException {
        flagStemming = true;
        flagStopWordRemoval = true;
        flagCompressedReading = true;
        long startTime;

        loadStopWordList();
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        scoreType = TFIDF;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();

        LexiconFileHandler lexiconFileHandler = new LexiconFileHandler();
        ByteBuffer byteBuffer = lexiconFileHandler.findTermEntryCompression("aziz");
        int docFreq = lexiconFileHandler.getDfCompression(byteBuffer);
        System.out.println(lexiconFileHandler.getTerm(byteBuffer));
        System.out.println("docFREQ: " + docFreq);
        System.out.println(lexiconFileHandler.getCfCompression(byteBuffer));
        System.out.println("offset skip: " + lexiconFileHandler.getOffsetSkipDescCompression(byteBuffer));
        System.out.println(lexiconFileHandler.getOffsetTermFreqCompression(byteBuffer));
        System.out.println(lexiconFileHandler.getOffsetDocIdCompression(byteBuffer));
        System.out.println(lexiconFileHandler.getNumByteDocId(byteBuffer));
        System.out.println(lexiconFileHandler.getNumByteTermFreq(byteBuffer));
        System.out.println(lexiconFileHandler.getTermUpperBoundScoreBM25Compression(byteBuffer));
        System.out.println(lexiconFileHandler.getTermUpperBoundScoreTFIDFCompression(byteBuffer));
        SkipDescriptorFileHandler skipDescriptorFileHandler = new SkipDescriptorFileHandler();
        SkipDescriptorCompression skipDescriptorCompression =
                skipDescriptorFileHandler.readSkipDescriptorCompression(
                        lexiconFileHandler.getOffsetSkipDescCompression(byteBuffer),
                        (int) Math.ceil((float) docFreq / (int) Math.sqrt(docFreq))
                );

        //______________LEGGO IL PRIMO_____________________//
        long offsetMaxDocId = skipDescriptorCompression.getOffsetMaxDocIds().get(0);
        long offsetTermFreq = skipDescriptorCompression.getOffsetTermFreqs().get(0);
        int numByteDocId = skipDescriptorCompression.getNumByteMaxDocIds().get(0);
        int numByteTermFreq = skipDescriptorCompression.getNumByteTermFreqs().get(0);

        System.out.println("SIZE: " + skipDescriptorCompression.size());
        System.out.println(offsetMaxDocId);
        System.out.println(offsetTermFreq);
        System.out.println(numByteDocId);
        System.out.println(numByteTermFreq);

        InvertedIndexFileHandler invertedIndexFileHandler = new InvertedIndexFileHandler();
        PostingListBlock postingListBlock = invertedIndexFileHandler.getPostingListCompressed(
                (int)(Math.sqrt(lexiconFileHandler.getDfCompression(byteBuffer))),
                offsetMaxDocId,
                numByteDocId,
                offsetTermFreq,
                numByteTermFreq
        );

        System.out.println("postingListBLOCK: " + postingListBlock);

        startTime = currentTimeMillis();
        SystemEvaluator.queryResult("project", DISJUNCTIVE_DAAT_C);
        System.out.println("time: " + (currentTimeMillis() - startTime));
    }
}
