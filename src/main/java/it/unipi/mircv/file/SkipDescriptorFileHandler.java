package it.unipi.mircv.file;


import it.unipi.mircv.index.SkipDescriptorCompression;
import it.unipi.mircv.index.SkipDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import static it.unipi.mircv.utility.Config.*;

public class SkipDescriptorFileHandler {
    /*
     * The SkipDescriptorFileHandler class is used to read and write the skip descriptors of
     *  the posting lists from the disk.
     * The skip descriptors are stored in a file called "posting_list_desc.txt" in the following format:
     * - each line represents a posting list
     * - each line is composed by a sequence of pairs (maxDocId, offsetMaxDocId)
     * - each pair is separated by a space
     */

    private final RandomAccessFile randomAccessFile;
    private final FileChannel fileChannel;

    public SkipDescriptorFileHandler() throws IOException {
        File file = new File(POSTING_LIST_DESC_FILE);
        // Check if the file already exists
        if (!file.exists()) {
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
        /*---------------------------------------------------------
            read a skip descriptor from the file by specifying
            the offset and the length of the skip descriptor in
            the file (in terms of number of entries)
        ---------------------------------------------------------*/
        SkipDescriptor skipDescriptor = new SkipDescriptor();
        ByteBuffer skipDescriptorBuffer = ByteBuffer.allocate(length * (SKIP_DESC_ENTRY));
        fileChannel.read(skipDescriptorBuffer, (long) offset * (SKIP_DESC_ENTRY));
        for (int i = 0; i < length; i++){
            skipDescriptorBuffer.position(i * (SKIP_DESC_ENTRY));
            skipDescriptor.add(skipDescriptorBuffer.getInt(), skipDescriptorBuffer.getInt());
        }
        return skipDescriptor;
    }

    //same as above but for compressed skip descriptors
    public SkipDescriptorCompression readSkipDescriptorCompression(int offset, int length) throws IOException {

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
        /*---------------------------------------------------------
            write a skip descriptor to the file by specifying
            the offset and the skip descriptor
        ---------------------------------------------------------*/
        //allocate a buffer of (SKIP_DESC_ENTRY+num of entry)bytes for each entry of the skip descriptor
        ByteBuffer byteBuffer = ByteBuffer.allocate(skipDescriptor.size() * (SKIP_DESC_ENTRY));
        ArrayList<Integer> maxDocIds = skipDescriptor.getMaxDocIds();
        ArrayList<Integer> offsetMaxDocIds = skipDescriptor.getOffsetMaxDocIds();

        for (int i = 0; i < skipDescriptor.size(); i++){
            byteBuffer.putInt(maxDocIds.get(i));
            byteBuffer.putInt(offsetMaxDocIds.get(i));
        }

        byteBuffer.rewind();
        //write the buffer to the file at the specified offset
        fileChannel.position((long) offset * (SKIP_DESC_ENTRY));
        fileChannel.write(byteBuffer);
    }

    //same as above but for compressed skip descriptors
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
