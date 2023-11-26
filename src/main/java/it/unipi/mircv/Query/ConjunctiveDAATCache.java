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
        int docId = postingListBlocks[index].getCurrentDocId();
        Integer docLen = cacheDocIndex.get(docId);
        if (docLen == null) { //need to read in the file the missing DocId -> DocLen and put it in cache
            docLen = documentIndexHandler.readDocumentLength(docId);
            cacheDocIndex.put(docId, docLen);
            System.out.println("miss"); //DEBUG
        }else System.out.println("Ho hittato in cache: " + docLen); //DEBUG

        currentDocScore += ScoreFunction.BM25(postingListBlocks[index].getCurrentTf(), docLen, docFreqs[index]);
    }
}