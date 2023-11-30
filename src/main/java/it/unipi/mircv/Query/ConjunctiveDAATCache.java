package it.unipi.mircv.Query;
import it.unipi.mircv.LRUCache;
import java.io.IOException;

public class ConjunctiveDAATCache extends ConjunctiveDAAT {
    private final LRUCache<Integer, Integer> cacheDocIndex;
    public ConjunctiveDAATCache(String[] queryTerms, LRUCache<Integer, Integer> cacheDocIndex) throws IOException {
        super(queryTerms);
        this.cacheDocIndex = cacheDocIndex;
        //System.out.println("cache nel costruttore = " + this.cacheDocIndex.toString());
    }
    @Override
    protected void updateCurrentDocScore(int index) throws IOException {
        //System.out.println(index); DEBUG
        if (index != 0) { // prima era index == 1
            int docId = postingListBlocks[index].getCurrentDocId();
            //System.out.println("currentDocId = " + docId);
            currentDocLen = cacheDocIndex.get(docId);
            //System.out.println("currentDocLen = " + currentDocLen);
            if (currentDocLen == null) { //need to read in the file the missing DocId -> DocLen and put it in cache
                currentDocLen = documentIndexHandler.readDocumentLength(docId);
                cacheDocIndex.put(docId, currentDocLen);
                //System.out.println(cacheDocIndex.get(docId));
                //System.out.println("miss");
            } //else System.out.println("hit");
        }
        currentDocScore += ScoreFunction.BM25(postingListBlocks[index].getCurrentTf(), currentDocLen, docFreqs[index]);
    }
}