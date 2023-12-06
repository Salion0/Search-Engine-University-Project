package it.unipi.mircv.file;

import it.unipi.mircv.index.PostingElement;
import it.unipi.mircv.index.PostingListBlock;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import static it.unipi.mircv.Config.*;

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
    public PostingListBlock getCompressedPostingList(long startOffsetDocId, long endOffsetDocId, long startOffsetTermFreq, long endOffsetTermFreq){
        PostingListBlock postingListBlock = new PostingListBlock();
        ByteBuffer docIdBuffer = ByteBuffer.allocate((int) (endOffsetDocId - startOffsetDocId));
        ByteBuffer termFreqBuffer = ByteBuffer.allocate((int) (endOffsetTermFreq - startOffsetTermFreq));

        return null;
    }
    public PostingListBlock getPostingList(int offset, int length) throws IOException {
        PostingListBlock postingListBlock = new PostingListBlock();
        ByteBuffer docIdBuffer = ByteBuffer.allocate(DOC_ID_LENGTH*length);
        ByteBuffer termFreqBuffer = ByteBuffer.allocate(TERM_FREQ_LENGTH*length);

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
