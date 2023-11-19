package it.unipi.mircv.File;

import it.unipi.mircv.Index.SkipDescriptor;

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
        //la length sarà la radice quadrata della posting list castata ad int + 1
        //offset è quello logico
        SkipDescriptor skipDescriptor = new SkipDescriptor();
        ByteBuffer skipDescriptorBuffer = ByteBuffer.allocate(length * (DOC_ID_LENGTH + OFFSET_BYTES_LENGTH));
        fileChannel.read(skipDescriptorBuffer, (long) offset * (DOC_ID_LENGTH + OFFSET_BYTES_LENGTH));
        for (int i = 0; i < length; i++){
            skipDescriptorBuffer.position(i * (DOC_ID_LENGTH + OFFSET_BYTES_LENGTH));
            skipDescriptor.add(skipDescriptorBuffer.getInt(), skipDescriptorBuffer.getInt());
        }
        return skipDescriptor;
    }

    public void writeSkipDescriptor(int offset, SkipDescriptor skipDescriptor) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(skipDescriptor.size() * (DOC_ID_LENGTH + OFFSET_BYTES_LENGTH));
        ArrayList<Integer> maxDocIds = skipDescriptor.getMaxDocIds();
        ArrayList<Integer> offsetMaxDocIds = skipDescriptor.getOffsetMaxDocIds();

        for (int i = 0; i < skipDescriptor.size(); i++){
            byteBuffer.putInt(maxDocIds.get(i));
            byteBuffer.putInt(offsetMaxDocIds.get(i));
        }

        byteBuffer.rewind();
        fileChannel.position((long) offset * (DOC_ID_LENGTH + OFFSET_BYTES_LENGTH));
        fileChannel.write(byteBuffer);
    }

    public void closeFileChannel() throws IOException {
        randomAccessFile.close();
        fileChannel.close();
    }
}
