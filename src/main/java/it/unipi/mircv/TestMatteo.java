package it.unipi.mircv;

import ca.rmen.porterstemmer.PorterStemmer;
import it.unipi.mircv.File.DocumentIndexFileHandler;

import it.unipi.mircv.Query.ConjunctiveDAAT;

import java.io.IOException;
import java.util.ArrayList;

import static it.unipi.mircv.Config.CACHE_SIZE;
import static it.unipi.mircv.Config.stopWords;

public class TestMatteo {

    public static String[] removeStopWords(String[] queryTerms) {
        ArrayList<String> filteredTerms = new ArrayList<>();
        for (String term : queryTerms) {
            if (!stopWords.contains(term)) {
                filteredTerms.add(term);
            }
        }
        return filteredTerms.toArray(new String[0]);
    }
    public static void main(String[] args) throws IOException {

        // Testing DAAT

        DocumentIndexFileHandler documentIndexFileHandler = new DocumentIndexFileHandler();
        Config.loadStopWordList();
        Config.collectionSize = documentIndexFileHandler.readCollectionSize();
        Config.avgDocLen = documentIndexFileHandler.readAvgDocLen();
        PorterStemmer stemmer = new PorterStemmer();


        //String[] queryTerms= TokenProcessing.doStopWordRemovalAndStemming(stemmer, "holy spirit".split(" "));

        //what is the distance between flat rock michigan and detroit

        /*
        //CONJUNCTIVE DAAT
        System.out.println("-----------------------------------------------------------");
        String[] queryTerms= "diet detox".split(" ");
        queryTerms = removeStopWords(queryTerms);
        System.out.println(queryTerms.length);
        ConjunctiveDAAT conjunctiveDAAT = new ConjunctiveDAAT(queryTerms);
        ArrayList<Integer> results = conjunctiveDAAT.processQuery();
        System.out.println(results);
         */

        //TESTING CONJUNCTIVE DAAT with CACHE
        LRUCache<Integer, Integer> docLenCache = new LRUCache<>(CACHE_SIZE);
        //
        System.out.println("-----------------------------------------------------------");
        long startTime = System.currentTimeMillis();
        String[] queryTerms= "railroad workers".split(" ");
        queryTerms = removeStopWords(queryTerms);
        System.out.println(queryTerms.length);
        ConjunctiveDAAT conjunctiveDAATCache = new ConjunctiveDAAT(queryTerms);
        ArrayList<Integer> results = conjunctiveDAATCache.processQuery();
        System.out.println(results);
        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("-----------------------------------------------------------");
        long startTime1 = System.currentTimeMillis();
        String[] queryTerms1= "railroad workers".split(" ");
        queryTerms1 = removeStopWords(queryTerms1);
        System.out.println(queryTerms1.length);
        ConjunctiveDAAT conjunctiveDAATCache1 = new ConjunctiveDAAT(queryTerms1);
        ArrayList<Integer> results1 = conjunctiveDAATCache1.processQuery();
        System.out.println(results1);
        long elapsedTime1 = System.currentTimeMillis() - startTime1;

        System.out.println("-----------------------------------------------------------");
        long startTime2 = System.currentTimeMillis();
        String[] queryTerms2= "railroad workers".split(" ");
        queryTerms2 = removeStopWords(queryTerms2);
        System.out.println(queryTerms2.length);
        ConjunctiveDAAT conjunctiveDAATCache2 = new ConjunctiveDAAT(queryTerms2);
        ArrayList<Integer> results2 = conjunctiveDAATCache2.processQuery();
        System.out.println(results2);
        long elapsedTime2 = System.currentTimeMillis() - startTime2;

        System.out.println("-----------------------------------------------------------");
        long startTime3 = System.currentTimeMillis();
        String[] queryTerms3= "railroad workers".split(" ");
        queryTerms3 = removeStopWords(queryTerms3);
        System.out.println(queryTerms3.length);
        ConjunctiveDAAT conjunctiveDAATCache3 = new ConjunctiveDAAT(queryTerms3);
        ArrayList<Integer> results3 = conjunctiveDAATCache3.processQuery();
        System.out.println(results3);
        long elapsedTime3 = System.currentTimeMillis() - startTime3;

        /* DISJUNCTIVE DAAT
        System.out.println("-----------------------------------------------------------");
        String[] queryTerms1= "what is the distance between flat rock michigan and detroit".split(" ");
        queryTerms1 = removeStopWords(queryTerms1);
        System.out.println(queryTerms1.length);
        DisjunctiveDAAT disjunctiveDAAT = new DisjunctiveDAAT(queryTerms1);
        ArrayList<Integer> results1 = disjunctiveDAAT.processQuery();
        System.out.println(results1);
        */


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


        System.out.println("finished in " + (float)elapsedTime/1000 +"sec");

        System.out.println("1 finished in " + (float)elapsedTime1/1000 +"sec");

        System.out.println("2 finished in " + (float)elapsedTime2/1000 +"sec");

        System.out.println("3 finished in " + (float)elapsedTime3/1000 +"sec");
    }
}
