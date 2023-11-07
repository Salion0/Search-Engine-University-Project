package it.unipi.mircv.File;

import it.unipi.mircv.Index.PostingElement;
import it.unipi.mircv.Index.PostingList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import static it.unipi.mircv.Index.Config.*;

public class InvertedIndexHandler {
    private String docIdFile = "docIds.dat";
    private String termFreqFile = "termFreq.dat";
    private String path = "data/";

    private FileChannel docIdChannel;
    private FileChannel termFreqChannel;
    public InvertedIndexHandler() throws FileNotFoundException{
        RandomAccessFile rafDocId = new RandomAccessFile(this.path + this.docIdFile, "rw");
        this.docIdChannel = rafDocId.getChannel();

        RandomAccessFile rafTermFreq = new RandomAccessFile(this.path + this.termFreqFile, "rw");
        this.termFreqChannel = rafTermFreq.getChannel();
    }

    public PostingList getPostingList(int offset,int length) throws IOException {
     PostingList pl = new PostingList();
     ByteBuffer docIdBuffer = ByteBuffer.allocate(DOC_ID_LENGTH*length);
     ByteBuffer termFreqBuffer = ByteBuffer.allocate(TERM_FREQ_LENGTH*length);

    docIdChannel.read(docIdBuffer,offset);
    termFreqChannel.read(termFreqBuffer,offset);
    docIdBuffer.position(0);
    termFreqBuffer.position(0);
     for (int i=1; i<length+1;i++){
        int docID = docIdBuffer.getInt();
        int termFreq = termFreqBuffer.getInt();
        pl.addPostingElement(new PostingElement(docID,termFreq));
        docIdBuffer.position((i)*DOC_ID_LENGTH);
        termFreqBuffer.position((i)*TERM_FREQ_LENGTH);
     }
     return pl;
    }
}
