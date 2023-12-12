package it.unipi.mircv;

import it.unipi.mircv.file.DocumentIndexFileHandler;
import it.unipi.mircv.file.InvertedIndexFileHandler;
import it.unipi.mircv.evaluation.SystemEvaluator;
import it.unipi.mircv.index.BlockMergerCompression;
import it.unipi.mircv.index.DocumentIndex;
import it.unipi.mircv.index.Index;

import java.io.IOException;

import static it.unipi.mircv.Parameters.*;
import static it.unipi.mircv.Parameters.QueryProcessor.*;
import static it.unipi.mircv.Parameters.Score.*;

public class TestMatteo {
    public static void main(String[] args) throws IOException {

        flagStemming = false;
        flagStopWordRemoval = true;
        flagCompressedReading = true;

        Index index = new Index("data/","test_collection.tsv",false);

        BlockMergerCompression blockMerger = new BlockMergerCompression();
        blockMerger.mergeBlocks(index.getNumberOfBlocks());

        //QUERY ------
        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        collectionSize = documentIndexFileHandler.readCollectionSize();
        avgDocLen = documentIndexFileHandler.readAvgDocLen();
        scoreType = TFIDF;
        docsLen = documentIndexFileHandler.loadAllDocumentLengths();

        SystemEvaluator.queryResult("railroad workers", DISJUNCTIVE_MAX_SCORE_C);

        //SystemEvaluator.evaluateSystemTime("query/msmarco-test2020-queries.tsv", CONJUNCTIVE, BM25,true, false);
        //SystemEvaluator.evaluateSystemTime("query/msmarco-test2020-queries.tsv", CONJUNCTIVE, BM25,true, false);
        //SystemEvaluator.createFileQueryResults("queryResult/disjunctive.txt","query/msmarco-test2020-queries.tsv", DISJUNCTIVE_DAAT, BM25,true, false);

        //testing PL Descriptor


        /*

        SkipDescriptorFileHandler plDescriptorFileHandler = new SkipDescriptorFileHandler();
        ArrayList<Integer> maxDocIds = plDescriptorFileHandler.getMaxDocIds(0, 73);
        System.out.println(maxDocIds);
        maxDocIds = plDescriptorFileHandler.getMaxDocIds(39, 20);
        System.out.println(maxDocIds);
        */

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
        /*
        int[] values1 = {1,1,2,1,3,1};
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
