package it.unipi.mircv.Query;
import it.unipi.mircv.LRUCache;
import java.io.IOException;

public class ConjunctiveDAATCache extends ConjunctiveDAAT {
    private final LRUCache<Integer, Integer> cacheDocIndex;
    public ConjunctiveDAATCache(String[] queryTerms, LRUCache<Integer, Integer> cacheDocIndex) throws IOException {
        super(queryTerms);
        this.cacheDocIndex = cacheDocIndex;
    }
    @Override
    protected void updateCurrentDocScore(int index) throws IOException {
        if (index == 1) {
            int docId = postingListBlocks[index].getCurrentDocId();
            currentDocLen = cacheDocIndex.get(docId);
            if (currentDocLen == null) { //need to read in the file the missing DocId -> DocLen and put it in cache
                currentDocLen = documentIndexHandler.readDocumentLength(docId);
                cacheDocIndex.put(docId, currentDocLen);
                //System.out.println("miss");
            } //else System.out.println("hit");
        }
        currentDocScore += ScoreFunction.BM25(postingListBlocks[index].getCurrentTf(), currentDocLen, docFreqs[index]);
    }
}