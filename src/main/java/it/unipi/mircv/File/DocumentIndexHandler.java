package it.unipi.mircv.File;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static it.unipi.mircv.Index.Config.*;

public class DocumentIndexHandler{
    private final FileChannel fileChannel;
    long currentPosition;

    public DocumentIndexHandler() throws IOException {
        String filepath = "data/documentIndex.dat";
        fileChannel = new RandomAccessFile(filepath,"rw").getChannel();
        currentPosition = AVGDOCLENGHT_BYTES_LENGTH;
    }

    public void writeEntry(String docNo, int docLength) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(DOCNO_BYTES_LENGTH + DOCLENGTH_BYTES_LENGTH);
        byteBuffer.put(docNo.getBytes());
        byteBuffer.position(DOCNO_BYTES_LENGTH);
        byteBuffer.putInt(docLength);

        fileChannel.position(currentPosition);
        fileChannel.write(byteBuffer);
        currentPosition += (DOCNO_BYTES_LENGTH + DOCLENGTH_BYTES_LENGTH);
    }
    public void addAverageDocumentLength(float averageDocumentLength) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(AVGDOCLENGHT_BYTES_LENGTH);
        byteBuffer.putFloat(averageDocumentLength);
        fileChannel.position(0);
        fileChannel.write(byteBuffer);
    }

    public int readDocumentLength(int docId) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(DOCLENGTH_BYTES_LENGTH);
        fileChannel.position(AVGDOCLENGHT_BYTES_LENGTH + (long) docId * (DOCNO_BYTES_LENGTH + DOCLENGTH_BYTES_LENGTH));
        fileChannel.read(buffer);
        buffer.flip(); // Prepare the buffer for reading
        return buffer.getInt();
    }

}
