package it.unipi.mircv.file;

import it.unipi.mircv.index.PostingElement;
import it.unipi.mircv.index.PostingListBlock;
import it.unipi.mircv.index.PostingList2;
import it.unipi.mircv.compression.Unary;
import it.unipi.mircv.compression.VariableByte;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import static it.unipi.mircv.utility.Config.*;


public class InvertedIndexFileHandler {
    /*
    * This class represent an I/O handler for the inverted index data structure
    * that allows to handle all the read and write and operation from and the
    * inverted index file( termfreq and docid file)
    * mapping posting list in the files to object in java
    */

    private final FileChannel docIdChannel; //file channel for docId
    private final FileChannel termFreqChannel; //file channel for termFreq


    public InvertedIndexFileHandler() throws IOException {

        // open the docId file in read/write mode
        RandomAccessFile rafDocId = new RandomAccessFile(DOC_ID_FILE, "rw");
        this.docIdChannel = rafDocId.getChannel();

        //open the termFreq file in read/write mode
        RandomAccessFile rafTermFreq = new RandomAccessFile(TERM_FREQ_FILE, "rw");
        this.termFreqChannel = rafTermFreq.getChannel();
    }

    //same as above but with different paths
    public InvertedIndexFileHandler(String docIdPath, String termFreqPath) throws IOException {
        RandomAccessFile rafDocId = new RandomAccessFile(docIdPath, "rw");
        this.docIdChannel = rafDocId.getChannel();

        RandomAccessFile rafTermFreq = new RandomAccessFile(termFreqPath, "rw");
        this.termFreqChannel = rafTermFreq.getChannel();
    }
    public PostingListBlock getPostingListCompressed(int numPosting,
                                                     long startOffsetDocId, int numByteDocId,
                                                     long startOffsetTermFreq, int numByteTermFreq) throws IOException {
        /*---------------------------------------------------------
            get a posting list block from the inverted index
            file in compressed format given the start
            offset and the number of bytes to read for
            docId and termFreq
        ---------------------------------------------------------*/

        if(numByteDocId == 0 || numByteTermFreq == 0) return null;

        PostingListBlock postingListBlock = new PostingListBlock();

        //allocate the buffers with appropriate dimensions
        ByteBuffer docIdBuffer = ByteBuffer.allocate(numByteDocId);
        ByteBuffer termFreqBuffer = ByteBuffer.allocate(numByteTermFreq);

        //read the posting list from the file and put it in the buffers
        docIdChannel.read(docIdBuffer, startOffsetDocId);
        termFreqChannel.read(termFreqBuffer, startOffsetTermFreq);

        //decompress the posting list
        int[] docIds = VariableByte.decompress(docIdBuffer.array());
        int[] termFreqs = Unary.decompress(numPosting, termFreqBuffer.array());

        //set the fields of the posting list
        for (int i = 0; i < numPosting; i++){
            postingListBlock.addPostingElement(new PostingElement(docIds[i], termFreqs[i]));
        }
        postingListBlock.setFields(numPosting);
        return postingListBlock;
    }

    public PostingList2 getPostingList2(int offset, int length) throws IOException {
        /*---------------------------------------------------------
            get a posting list from the inverted index by specifying
            the offset and the length of the posting list
            and store it in a PostingList2 object
        ---------------------------------------------------------*/

        PostingList2 postingList2Compress = new PostingList2();

        //allocate the buffers with appropriate dimensions
        ByteBuffer docIdBuffer = ByteBuffer.allocate(DOC_ID_LENGTH * length);
        ByteBuffer termFreqBuffer = ByteBuffer.allocate(TERM_FREQ_LENGTH * length);

        //read the posting list from the file and put it in the buffers
        docIdChannel.read(docIdBuffer, (long) offset * DOC_ID_LENGTH);
        termFreqChannel.read(termFreqBuffer, (long) offset * TERM_FREQ_LENGTH);

        docIdBuffer.position(0);
        termFreqBuffer.position(0);

        //set the fields of the posting list
        for (int i = 0; i < length; i++){
            postingList2Compress.addDocId(docIdBuffer.getInt());
            postingList2Compress.addTermFreq(termFreqBuffer.getInt());
        }
        return postingList2Compress;
    }
    public PostingListBlock getPostingList(int offset, int length) throws IOException {
        /*---------------------------------------------------------
            get a posting list from the inverted index by specifying
            the offset and the length of the posting list
            and store it in a PostingListBlock object
        ---------------------------------------------------------*/

        PostingListBlock postingListBlock = new PostingListBlock();

        //allocate the buffers with appropriate dimensions
        ByteBuffer docIdBuffer = ByteBuffer.allocate(DOC_ID_LENGTH * length);
        ByteBuffer termFreqBuffer = ByteBuffer.allocate(TERM_FREQ_LENGTH * length);

        //read the posting list from the file and put it in the buffers
        docIdChannel.read(docIdBuffer, (long) offset * DOC_ID_LENGTH);
        termFreqChannel.read(termFreqBuffer, (long) offset * TERM_FREQ_LENGTH);

        //set the fields of the posting list
        for (int i = 0; i < length; i++){

            //position the buffers to right position at each iteration
            docIdBuffer.position(i* DOC_ID_LENGTH);
            termFreqBuffer.position(i * TERM_FREQ_LENGTH);

            //read the element from the buffers and add it to the posting list
            int docID = docIdBuffer.getInt();
            int termFreq = termFreqBuffer.getInt();
            postingListBlock.addPostingElement(new PostingElement(docID, termFreq));
        }

        //set the length of the posting list
        postingListBlock.setFields(length);
        return postingListBlock;
    }
    public void close() throws IOException {
        docIdChannel.close();
        termFreqChannel.close();
    }

}
