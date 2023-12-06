package it.unipi.mircv.file;

import it.unipi.mircv.index.SkipDescriptorCompression;
import it.unipi.mircv.index.SkipDescriptor;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import static it.unipi.mircv.Config.*;

public class SkipDescriptorFileHandler {
    private final RandomAccessFile randomAccessFile;
    private final FileChannel fileChannel;

    public SkipDescriptorFileHandler() throws IOException {
        File file = new File(POSTING_LIST_DESC_FILE);
        if (file.exists()) {
            System.out.println("Posting List Descriptor file founded");
        } else {
            // Create the file
            if (file.createNewFile()) {
                System.out.println("Posting List Descriptor file created correctly");
            } else {
                System.out.println("Failed to create Posting List Descriptor file");
            }
        }

        randomAccessFile = new RandomAccessFile(POSTING_LIST_DESC_FILE,"rw");
        fileChannel = randomAccessFile.getChannel();
    }

    //this method will be called for each term in the conjunctive query in order to perform nextGEQ()
    public SkipDescriptor readSkipDescriptor(int offset, int length) throws IOException {
        //la length sarà la radice quadrata della posting list approssimata per eccesso
        //offset è quello logico
        SkipDescriptor skipDescriptor = new SkipDescriptor();
        ByteBuffer skipDescriptorBuffer = ByteBuffer.allocate(length * (SKIP_DESC_ENTRY));
        fileChannel.read(skipDescriptorBuffer, (long) offset * (SKIP_DESC_ENTRY));
        for (int i = 0; i < length; i++){
            skipDescriptorBuffer.position(i * (SKIP_DESC_ENTRY));
            skipDescriptor.add(skipDescriptorBuffer.getInt(), skipDescriptorBuffer.getInt());
        }
        return skipDescriptor;
    }
    public SkipDescriptorCompression readSkipDescriptorCompression(int offset, int length) throws IOException {
        //la length sarà la radice quadrata della posting list approssimata per eccesso
        //offset è quello logico
        SkipDescriptorCompression skipDescriptorCompression = new SkipDescriptorCompression();
        ByteBuffer skipDescriptorBuffer = ByteBuffer.allocate(length * (SKIP_DESC_ENTRY_COMPRESSION));
        fileChannel.read(skipDescriptorBuffer, (long) offset * (SKIP_DESC_ENTRY_COMPRESSION));
        for (int i = 0; i < length; i++){
            skipDescriptorBuffer.position(i * (SKIP_DESC_ENTRY_COMPRESSION));
            skipDescriptorCompression.add(skipDescriptorBuffer.getInt(), skipDescriptorBuffer.getLong(),
                    skipDescriptorBuffer.getInt(), skipDescriptorBuffer.getLong(),
                    skipDescriptorBuffer.getInt()
            );
        }
        return skipDescriptorCompression;
    }

    public void writeSkipDescriptor(int offset, SkipDescriptor skipDescriptor) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(skipDescriptor.size() * (SKIP_DESC_ENTRY));
        ArrayList<Integer> maxDocIds = skipDescriptor.getMaxDocIds();
        ArrayList<Integer> offsetMaxDocIds = skipDescriptor.getOffsetMaxDocIds();

        for (int i = 0; i < skipDescriptor.size(); i++){
            byteBuffer.putInt(maxDocIds.get(i));
            byteBuffer.putInt(offsetMaxDocIds.get(i));
        }

        byteBuffer.rewind();
        fileChannel.position((long) offset * (SKIP_DESC_ENTRY));
        fileChannel.write(byteBuffer);
    }
    public void writeSkipDescriptorCompressed(int offset, SkipDescriptorCompression skipDescriptorCompression) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(skipDescriptorCompression.size() * (SKIP_DESC_ENTRY_COMPRESSION));
        ArrayList<Integer> maxDocIds = skipDescriptorCompression.getMaxDocIds();
        ArrayList<Long> offsetMaxDocIds = skipDescriptorCompression.getOffsetMaxDocIds();
        ArrayList<Integer> numByteMaxDocIds = skipDescriptorCompression.getNumByteMaxDocIds();
        ArrayList<Long> getOffsetTermFreqs = skipDescriptorCompression.getOffsetTermFreqs();
        ArrayList<Integer> numByteTermFreqs = skipDescriptorCompression.getNumByteTermFreqs();

        for (int i = 0; i < skipDescriptorCompression.size(); i++){
            byteBuffer.putInt(maxDocIds.get(i));
            byteBuffer.putLong(offsetMaxDocIds.get(i));
            byteBuffer.putInt(numByteMaxDocIds.get(i));
            byteBuffer.putLong(getOffsetTermFreqs.get(i));
            byteBuffer.putInt(numByteTermFreqs.get(i));
        }

        byteBuffer.rewind();
        fileChannel.position((long) offset * (SKIP_DESC_ENTRY_COMPRESSION));
        fileChannel.write(byteBuffer);
    }
    public void closeFileChannel() throws IOException {
        randomAccessFile.close();
        fileChannel.close();
    }
}
