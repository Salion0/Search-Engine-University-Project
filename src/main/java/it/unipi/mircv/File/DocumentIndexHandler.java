package it.unipi.mircv.File;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static it.unipi.mircv.Config.*;

public class DocumentIndexHandler{
    private final FileChannel fileChannel;
    private final RandomAccessFile randomAccessFile;
    long currentPosition;
    String filepath = "data/documentIndex.dat";

    public DocumentIndexHandler() throws IOException {
        File file = new File(filepath);
        if (file.exists()) {
            System.out.println("Document Index file founded");
        } else {
            // Create the file
            if (file.createNewFile()) {
                System.out.println("Document Index file created correctly");
            } else {
                System.out.println("Failed to create Document Index file");
            }
        }

        randomAccessFile = new RandomAccessFile(filepath,"rw");
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
            docNos[i] = readDocNo(docIds.get(resultsSize - i - 1));
        }
        return docNos;
    }

}
