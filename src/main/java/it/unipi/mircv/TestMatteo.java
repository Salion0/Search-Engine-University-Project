package it.unipi.mircv;

import it.unipi.mircv.compression.ByteManipulator;
import it.unipi.mircv.compression.VariableByte;
import it.unipi.mircv.compression.Utils;

import java.io.IOException;

public class TestMatteo {
    public static void main(String[] args) throws IOException {

        /*
        //test DocumentIndexHandler
        Index index = new Index("test_collection.tsv");
        int numberOfBlocks = index.getNumberOfBlocks();
        BlockMerger blockMerger = new BlockMerger(numberOfBlocks);
        blockMerger.mergeBlocks();

        DocumentIndexHandler documentIndexHandler = new DocumentIndexHandler();
        System.out.println("document length: " + documentIndexHandler.readDocumentLength(3));
        documentIndexHandler.closeFileChannel();
        */


        /*
        // test Variable Byte compression
        byte[] intCompressed1 = VariableByte.compress(1000);
        byte[] intCompressed2 = VariableByte.compress(10023402);

        Utils.printBytes(intCompressed1);
        Utils.printBytes(intCompressed2);

        Utils.printBytes(Utils.reverseByteArray(intCompressed1));
        Utils.printBytes(Utils.reverseByteArray(intCompressed2));

        System.out.println(VariableByte.decompress(intCompressed1));
        System.out.println(VariableByte.decompress(intCompressed2));
        */

        //test
        ByteManipulator b = new ByteManipulator();
        b.setBitToOne(2);
        b.setBitToZero(1);
        System.out.println(b);

    }


}
