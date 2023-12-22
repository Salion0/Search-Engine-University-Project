package it.unipi.mircv.file;

import it.unipi.mircv.Config;
import it.unipi.mircv.Parameters;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import static it.unipi.mircv.Config.*;

public class DocumentIndexFileHandler {
    private final FileChannel fileChannel;
    private final RandomAccessFile randomAccessFile;
    long currentPosition;

    public DocumentIndexFileHandler() throws IOException {
        File file = new File(DOCUMENT_INDEX_FILE);
        if (!file.exists()) {
            if(file.createNewFile()) System.out.println("Document Index file created correctly");
            else System.out.println("Error in Document Index file creation");
        } else System.out.println("Document Index file founded");

        randomAccessFile = new RandomAccessFile(DOCUMENT_INDEX_FILE,"rw");
        fileChannel = randomAccessFile.getChannel();
        currentPosition = AVGDOCLENGHT_BYTES_LENGTH + NUM_DOC_BYTES_LENGTH;
    }
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
        ByteBuffer byteBuffer = ByteBuffer.allocate(DOCNO_BYTES_LENGTH + DOCLENGTH_BYTES_LENGTH);
        byteBuffer.put(docNo.getBytes());
        byteBuffer.position(DOCNO_BYTES_LENGTH);
        byteBuffer.putInt(docLength);

        byteBuffer.rewind();
        fileChannel.position(currentPosition);
        fileChannel.write(byteBuffer);
        currentPosition += (DOCNO_BYTES_LENGTH + DOCLENGTH_BYTES_LENGTH);
    }

    public void writeAverageDocumentLength(float averageDocumentLength, int numberOfDocuments) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(AVGDOCLENGHT_BYTES_LENGTH + NUM_DOC_BYTES_LENGTH);
        byteBuffer.putFloat(averageDocumentLength);
        byteBuffer.position(AVGDOCLENGHT_BYTES_LENGTH);
        byteBuffer.putInt(numberOfDocuments);
        byteBuffer.rewind();
        fileChannel.position(0);
        fileChannel.write(byteBuffer);
    }

    public int readDocumentLength(int docId) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(DOCLENGTH_BYTES_LENGTH);
        fileChannel.position(AVGDOCLENGHT_BYTES_LENGTH + NUM_DOC_BYTES_LENGTH + (long) docId * (DOCNO_BYTES_LENGTH + DOCLENGTH_BYTES_LENGTH) + DOCNO_BYTES_LENGTH);
        fileChannel.read(buffer);
        buffer.position(0);
        return buffer.getInt();
    }
    public int[] loadAllDocumentLengths() throws IOException {
        int[] docsLen = new int[Parameters.collectionSize];
        ByteBuffer buffer = ByteBuffer.allocate(
                (DOCLENGTH_BYTES_LENGTH+DOCNO_BYTES_LENGTH) * Parameters.collectionSize
        );
        System.out.print(buffer.limit());
        fileChannel.position(AVGDOCLENGHT_BYTES_LENGTH + NUM_DOC_BYTES_LENGTH);
        fileChannel.read(buffer);
        buffer.position(0); //skip first docno
        for(int i = 0; buffer.position()+ DOCNO_BYTES_LENGTH < buffer.limit(); i++) {
            buffer.position(buffer.position() + DOCNO_BYTES_LENGTH);
            docsLen[i] = buffer.getInt();
        }
        return docsLen;
    }

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
        ByteBuffer buffer = ByteBuffer.allocate(AVGDOCLENGHT_BYTES_LENGTH);
        fileChannel.position(0);
        fileChannel.read(buffer);
        buffer.position(0);
        return buffer.getFloat();
    }

    public int readCollectionSize() throws IOException {
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
        ByteBuffer buffer = ByteBuffer.allocate(DOCNO_BYTES_LENGTH);
        fileChannel.position(AVGDOCLENGHT_BYTES_LENGTH + NUM_DOC_BYTES_LENGTH + (long) docId * (DOCNO_BYTES_LENGTH + DOCLENGTH_BYTES_LENGTH));
        fileChannel.read(buffer);
        buffer.position(0);
        return new String(buffer.array(), StandardCharsets.UTF_8);
    }

    public String[] getDocNoREVERSE(ArrayList<Integer> docIds) throws IOException {
        int resultsSize = docIds.size();
        String[] docNos = new String[resultsSize];
        for(int i = 0; i < resultsSize; i ++){
            docNos[i] = readDocNo(docIds.get(resultsSize - i - 1)).replace("\0", "");
        }
        return docNos;
    }

}
