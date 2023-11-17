package it.unipi.mircv;

import it.unipi.mircv.File.PLDescriptorFileHandler;
import it.unipi.mircv.Index.BlockMerger;
import it.unipi.mircv.Index.Index;

import java.io.IOException;
import java.util.ArrayList;

public class TestMatteo {
    public static void main(String[] args) throws IOException {


        //testing PL Descriptor
        /*
        Index index = new Index("test_collection.tsv");
        int numberOfBlocks = index.getNumberOfBlocks();
        BlockMerger blockMerger = new BlockMerger(numberOfBlocks);
        blockMerger.mergeBlocks(); */

        PLDescriptorFileHandler plDescriptorFileHandler = new PLDescriptorFileHandler();
        ArrayList<Integer> maxDocIds = plDescriptorFileHandler.getMaxDocIds(0, 73);
        System.out.println(maxDocIds);

        // testing DAAT
        /*
        String query = "continues homeostasis biofeedback scenar";
        QueryProcessor queryProcessor = new QueryProcessor(query);
        ArrayList<Integer> docId = queryProcessor.DAAT();
        System.out.println("Doc Id retrieved: ");
        System.out.println(docId);

         */

        /*
        //testing inverted index handler
        InvertedIndexHandler invertedIndexHandler = new InvertedIndexHandler();
        PostingList postingList = invertedIndexHandler.getPostingList(1345, 10);
        System.out.println(postingList);

        // testing lexicon handler

        LexiconHandler lexiconHandler = new LexiconHandler();
        ByteBuffer dataBuffer = lexiconHandler.findTermEntry("your");

        int df = lexiconHandler.getDf(dataBuffer);
        int offset = lexiconHandler.getOffset(dataBuffer);
        System.out.println("term: " + lexiconHandler.getTerm(dataBuffer));
        System.out.println("offset: " + offset);
        System.out.println("df: " + df);
        System.out.println("cf: " + lexiconHandler.getCf(dataBuffer));

        //posting list block
        PostingListBlock plTerm = invertedIndexHandler.getPostingList(offset, df);
        System.out.println("pl position: " + plTerm.getPosition());
        System.out.println("pl current DocId: " + plTerm.getCurrentDocId());
        System.out.println("pl current Tf: " + plTerm.getCurrentTf());

        System.out.println("pl.next(): " + plTerm.next());
        System.out.println("pl position: " + plTerm.getPosition());
        System.out.println("pl current DocId: " + plTerm.getCurrentDocId());
        System.out.println("pl current Tf: " + plTerm.getCurrentTf());

        int count = 2;
        while(plTerm.next() > 0){
            count +=1;
            //cycle to finish the posting list
        }
        System.out.println("count: " + count);
        System.out.println("getSize(): " + plTerm.getSize());
        System.out.println("next(): " + plTerm.next());
        System.out.println("last DocId con getSize(): " + plTerm.getDocId(plTerm.getSize()-1));
        System.out.println("getMaxId(): " + plTerm.getMaxDocID());

        */
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

        Utils.printReverseBytes(intCompressed1);
        Utils.printReverseBytes(intCompressed2);

        System.out.println(VariableByte.decompress(intCompressed1));
        System.out.println(VariableByte.decompress(intCompressed2));
        */

        //test Unary compression
        /*int[] values1 = {1,1,2,1,3,1};
        int[] values2 = {2,3,4,1,1,2,5};
        int[] values3 = {2,1,2};
        int[] values4 = {2,1,2,9,9,9};

        byte[] valuesCompressed1 = Unary.compress(values1);
        byte[] valuesCompressed2 = Unary.compress(values2);
        byte[] valuesCompressed3 = Unary.compress(values3);
        byte[] valuesCompressed4 = Unary.compress(values4);

        Utils.printBytes(valuesCompressed1);
        Utils.printBytes(valuesCompressed2);
        Utils.printBytes(valuesCompressed3);
        Utils.printBytes(valuesCompressed4);

        System.out.println(Arrays.toString(Unary.decompress(values1.length, valuesCompressed1)));
        System.out.println(Arrays.toString(Unary.decompress(values2.length, valuesCompressed2)));
        System.out.println(Arrays.toString(Unary.decompress(values3.length, valuesCompressed3)));
        System.out.println(Arrays.toString(Unary.decompress(values4.length, valuesCompressed4)));
        */

    }
}
