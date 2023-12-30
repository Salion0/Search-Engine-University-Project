package it.unipi.mircv.file;

import it.unipi.mircv.utility.Parameters;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import static it.unipi.mircv.utility.Config.*;

public class DocumentIndexFileHandler {
    private final FileChannel fileChannel;   //file channel for the document index file
    private final RandomAccessFile randomAccessFile;  //random access file for the document index file
    long currentPosition; //current position in the file

    public DocumentIndexFileHandler() throws IOException {
        // Create the object file
        File file = new File(DOCUMENT_INDEX_FILE);
        //if the file doesn't exist, create it
        if (!file.exists()) {
            if(file.createNewFile()) System.out.println("Document Index file created correctly");
            else System.out.println("Error in Document Index file creation");
        }

        //open the file
        randomAccessFile = new RandomAccessFile(DOCUMENT_INDEX_FILE,"rw");
        fileChannel = randomAccessFile.getChannel();
        //set the current position to the first docno
        currentPosition = AVGDOCLENGHT_BYTES_LENGTH + NUM_DOC_BYTES_LENGTH;
    }


    //this constructor is equal to the precedent
    //except for the fact that it allow to specify
    //the path of the file
    public DocumentIndexFileHandler(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            if(file.createNewFile()) System.out.println("Document Index file created correctly");
            else System.out.println("Error in Document Index file creation");
        } else System.out.println("Document Index file founded");
        randomAccessFile = new RandomAccessFile(filePath,"rw");
        fileChannel = randomAccessFile.getChannel();
        currentPosition = AVGDOCLENGHT_BYTES_LENGTH + NUM_DOC_BYTES_LENGTH;
    }

    public void writeEntry(String docNo, int docLength) throws IOException {
    /*---------------------------------------------------------
       write entry in the document index file as follows:
         docno (string) | docLength (int)
            4 bytes     |   4 bytes
    ---------------------------------------------------------*/

        //allocate a byte buffer with the length of the docno and the doclength
        ByteBuffer byteBuffer = ByteBuffer.allocate(DOCNO_BYTES_LENGTH + DOCLENGTH_BYTES_LENGTH);
        //put the docno in the byte buffer
        byteBuffer.put(docNo.getBytes());
        //set the position to the doc length position in the byte buffer = docNo length
        byteBuffer.position(DOCNO_BYTES_LENGTH);
        byteBuffer.putInt(docLength);

        byteBuffer.rewind(); //set the buffer position to 0

        //set the position in the file channel to the current position and write the entry to the file
        fileChannel.position(currentPosition);
        fileChannel.write(byteBuffer);
        //increment the current position by the length of the entry
        currentPosition += (DOCNO_BYTES_LENGTH + DOCLENGTH_BYTES_LENGTH);
    }

    public void writeAverageDocumentLength(float averageDocumentLength, int numberOfDocuments) throws IOException {
    /*---------------------------------------------------------
       write entry in the document index file as follows:
         docno (string) | docLength (int)
            4 bytes     |   4 bytes
    ---------------------------------------------------------*/

        //allocate a byte buffer with the length of the average document length + num_doc_bytes_length
        ByteBuffer byteBuffer = ByteBuffer.allocate(AVGDOCLENGHT_BYTES_LENGTH + NUM_DOC_BYTES_LENGTH);
        //put the avgDocLength in the byte buffer
        byteBuffer.putFloat(averageDocumentLength);
        //set the position to the "num of doc" position in the byte buffer = avg length in bytes
        byteBuffer.position(AVGDOCLENGHT_BYTES_LENGTH);
        byteBuffer.putInt(numberOfDocuments);
        byteBuffer.rewind();    //set the buffer position to 0
        //set the position in the file channel to the current position and write the entry to the file
        fileChannel.position(0);
        fileChannel.write(byteBuffer);
    }

    public int readDocumentLength(int docId) throws IOException {
    /*---------------------------------------------------------
        read the document length from the document index file
        of the document with the specified docId
    ---------------------------------------------------------*/

        //allocate a byte buffer with the length of the doclength
        ByteBuffer buffer = ByteBuffer.allocate(DOCLENGTH_BYTES_LENGTH);
        //set the position in the file channel to the position of the doclength of the specified docId
        // position = skip the firts 8 byte and then go to line of the docId = docId * (docno + doclength)
        fileChannel.position(AVGDOCLENGHT_BYTES_LENGTH + NUM_DOC_BYTES_LENGTH + (long) docId * (DOCNO_BYTES_LENGTH + DOCLENGTH_BYTES_LENGTH) + DOCNO_BYTES_LENGTH);
        fileChannel.read(buffer);
        //set the buffer position to 0 and read the doclength
        buffer.position(0);
        return buffer.getInt();
    }
    public int[] loadAllDocumentLengths() throws IOException {
    /*---------------------------------------------------------
        load in memory all the document lengths from
        the document index file
    ---------------------------------------------------------*/
        //array of document lengths to return
        int[] docsLen = new int[Parameters.collectionSize];
        //allocate a byte buffer with document index file length
        ByteBuffer buffer = ByteBuffer.allocate(
                (DOCLENGTH_BYTES_LENGTH+DOCNO_BYTES_LENGTH) * Parameters.collectionSize
        );
        //read all the entry in the document index file
        fileChannel.position(AVGDOCLENGHT_BYTES_LENGTH + NUM_DOC_BYTES_LENGTH);
        fileChannel.read(buffer);

        //get all the doclengths from the byte buffer
        buffer.position(0); //skip first docno
        for(int i = 0; buffer.position()+ DOCNO_BYTES_LENGTH < buffer.limit(); i++) {
            buffer.position(buffer.position() + DOCNO_BYTES_LENGTH);
            docsLen[i] = buffer.getInt();
        }
        return docsLen;
    }

    //equal to the precedent but with a different parameter
    public int[] loadAllDocumentLengths(int collectionSize) throws IOException {
        int[] docsLen = new int[collectionSize];
        ByteBuffer buffer = ByteBuffer.allocate(
                (DOCLENGTH_BYTES_LENGTH+DOCNO_BYTES_LENGTH) * collectionSize
        );
        fileChannel.position(AVGDOCLENGHT_BYTES_LENGTH + NUM_DOC_BYTES_LENGTH);
        fileChannel.read(buffer);
        buffer.position(0); //skip first docno
        System.out.println("buffer limit: " + buffer.limit());
        for(int i = 0; (buffer.position()+DOCNO_BYTES_LENGTH)<buffer.limit(); i++) {
            buffer.position(buffer.position() + DOCNO_BYTES_LENGTH);
            docsLen[i] = buffer.getInt();
            System.out.println("docId: " + i + " docLen: " + docsLen[i]);
        }
        return docsLen;
    }

    public float readAvgDocLen() throws IOException {
        /*---------------------------------------------------------
            read the average document length from the document index file
        ---------------------------------------------------------*/
        ByteBuffer buffer = ByteBuffer.allocate(AVGDOCLENGHT_BYTES_LENGTH);
        fileChannel.position(0);
        fileChannel.read(buffer);
        buffer.position(0);
        return buffer.getFloat();
    }

    public int readCollectionSize() throws IOException {
        /*---------------------------------------------------------
            read the number of documents from the document index file
        ---------------------------------------------------------*/
        ByteBuffer buffer = ByteBuffer.allocate(NUM_DOC_BYTES_LENGTH);
        fileChannel.position(AVGDOCLENGHT_BYTES_LENGTH);
        fileChannel.read(buffer);
        buffer.position(0);
        return buffer.getInt();
    }

    public void closeFileChannel() throws IOException {
        randomAccessFile.close();
        fileChannel.close();
    }

    public String readDocNo(int docId) throws IOException {
        /*---------------------------------------------------------
            read the docno from the document index file
            of the document with the specified docId
        ---------------------------------------------------------*/
        ByteBuffer buffer = ByteBuffer.allocate(DOCNO_BYTES_LENGTH);
        fileChannel.position(AVGDOCLENGHT_BYTES_LENGTH + NUM_DOC_BYTES_LENGTH + (long) docId * (DOCNO_BYTES_LENGTH + DOCLENGTH_BYTES_LENGTH));
        fileChannel.read(buffer);
        buffer.position(0);
        return new String(buffer.array(), StandardCharsets.UTF_8);
    }

    public String[] getDocNoREVERSE(ArrayList<Integer> docIds) throws IOException {
        /*---------------------------------------------------------
            read the docNos from the document index file
            by specifing their docIDs
        ---------------------------------------------------------*/
        int resultsSize = docIds.size();
        String[] docNos = new String[resultsSize];
        for(int i = 0; i < resultsSize; i ++){
            docNos[i] = readDocNo(docIds.get(resultsSize - i - 1)).replace("\0", "");
        }
        return docNos;
    }

}
