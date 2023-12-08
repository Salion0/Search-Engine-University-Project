package it.unipi.mircv.File;

import it.unipi.mircv.Index.PostingElement;
import it.unipi.mircv.Index.PostingListBlock;
import it.unipi.mircv.Index.PostingList2;
import it.unipi.mircv.compression.Unary;
import it.unipi.mircv.compression.VariableByte;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import static it.unipi.mircv.Config.*;
import static it.unipi.mircv.compression.Unary.decompress;

public class InvertedIndexFileHandler {
    private final FileChannel docIdChannel;
    private final FileChannel termFreqChannel;


    public InvertedIndexFileHandler() throws IOException {
        RandomAccessFile rafDocId = new RandomAccessFile(DOC_ID_FILE, "rw");
        this.docIdChannel = rafDocId.getChannel();

        RandomAccessFile rafTermFreq = new RandomAccessFile(TERM_FREQ_FILE, "rw");
        this.termFreqChannel = rafTermFreq.getChannel();
    }
    public InvertedIndexFileHandler(String docIdPath, String termFreqPath) throws IOException {
        RandomAccessFile rafDocId = new RandomAccessFile(docIdPath, "rw");
        this.docIdChannel = rafDocId.getChannel();

        RandomAccessFile rafTermFreq = new RandomAccessFile(termFreqPath, "rw");
        this.termFreqChannel = rafTermFreq.getChannel();
    }
    public PostingListBlock getPostingListCompressed(
            int numPosting,
            long startOffsetDocId, int numByteDocId,
            long startOffsetTermFreq, int numByteTermFreq) throws IOException {

        if(numByteDocId == 0 || numByteTermFreq == 0) return null;

        PostingListBlock postingListBlock = new PostingListBlock();
        ByteBuffer docIdBuffer = ByteBuffer.allocate(numByteDocId);
        ByteBuffer termFreqBuffer = ByteBuffer.allocate(numByteTermFreq);

        docIdChannel.read(docIdBuffer, startOffsetDocId);
        termFreqChannel.read(termFreqBuffer, startOffsetTermFreq);
        int[] docIds = VariableByte.decompress(docIdBuffer.array());
        int[] termFreqs = Unary.decompress(numPosting, termFreqBuffer.array());

        for (int i = 0; i < numPosting; i++){
            postingListBlock.addPostingElement(new PostingElement(docIds[i], termFreqs[i]));
        }
        postingListBlock.setFields(numPosting);
        return postingListBlock;
    }
    public PostingList2 getPostingList2(int offset, int length) throws IOException {
        PostingList2 postingList2Compress = new PostingList2();
        ByteBuffer docIdBuffer = ByteBuffer.allocate(DOC_ID_LENGTH * length);
        ByteBuffer termFreqBuffer = ByteBuffer.allocate(TERM_FREQ_LENGTH * length);

        docIdChannel.read(docIdBuffer, (long) offset * DOC_ID_LENGTH);
        termFreqChannel.read(termFreqBuffer, (long) offset * TERM_FREQ_LENGTH);

        docIdBuffer.position(0);
        termFreqBuffer.position(0);

        for (int i = 0; i < length; i++){
            postingList2Compress.addDocId(docIdBuffer.getInt());
            postingList2Compress.addTermFreq(termFreqBuffer.getInt());
        }
        return postingList2Compress;
    }
    public PostingListBlock getPostingList(int offset, int length) throws IOException {
        PostingListBlock postingListBlock = new PostingListBlock();
        ByteBuffer docIdBuffer = ByteBuffer.allocate(DOC_ID_LENGTH * length);
        ByteBuffer termFreqBuffer = ByteBuffer.allocate(TERM_FREQ_LENGTH * length);

        docIdChannel.read(docIdBuffer, (long) offset * DOC_ID_LENGTH);
        termFreqChannel.read(termFreqBuffer, (long) offset * TERM_FREQ_LENGTH);

        for (int i = 0; i < length; i++){
            docIdBuffer.position(i* DOC_ID_LENGTH);
            termFreqBuffer.position(i * TERM_FREQ_LENGTH);
            int docID = docIdBuffer.getInt();
            int termFreq = termFreqBuffer.getInt();
            postingListBlock.addPostingElement(new PostingElement(docID, termFreq));
        }
        postingListBlock.setFields(length);
        return postingListBlock;
    }
    public void close() throws IOException {
        this.docIdChannel.close();
        termFreqChannel.close();
    }

}
