package it.unipi.mircv;

import it.unipi.mircv.File.InvertedIndexFileHandler;
import it.unipi.mircv.File.LexiconFileHandler;
import it.unipi.mircv.File.SkipDescriptorFileHandler;
import it.unipi.mircv.Index.*;

import java.io.IOException;
import java.nio.ByteBuffer;

import static it.unipi.mircv.Config.*;

public class TestCompression {
    public static void main(String[] args) throws IOException {
        flagStemming = false;
        flagStopWordRemoval = true;
        flagCompressedReading = false;


        Index index = new Index("test_collection.tsv");
        BlockMergerCompression blockMergerCompression = new BlockMergerCompression();
        blockMergerCompression.mergeBlocks(index.getNumberOfBlocks());

        InvertedIndexFileHandler invertedIndexFileHandler = new InvertedIndexFileHandler();
        LexiconFileHandler lexiconFileHandler = new LexiconFileHandler();
        ByteBuffer byteBuffer = lexiconFileHandler.findTermEntryCompression("10");

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
        System.out.println(postingListBlock);
        SkipDescriptorFileHandler skipDescriptorFileHandler = new SkipDescriptorFileHandler();
        SkipDescriptorCompression skipDescriptorCompression = skipDescriptorFileHandler.readSkipDescriptorCompression(offsetSkipDesc, (int) Math.ceil(Math.sqrt(docFreq)));
        System.out.println(skipDescriptorCompression.size());

    }
}
