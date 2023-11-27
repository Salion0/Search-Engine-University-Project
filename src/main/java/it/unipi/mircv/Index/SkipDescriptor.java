package it.unipi.mircv.Index;
import java.io.IOException;
import java.util.ArrayList;

import static it.unipi.mircv.Config.stopWords;

public class SkipDescriptor {
    private final ArrayList<Integer> maxDocIds;
    private final ArrayList<Integer> offsetMaxDocIds;

    public SkipDescriptor(){
        maxDocIds = new ArrayList<>();
        offsetMaxDocIds = new ArrayList<>();
    }
    public int size(){
        return maxDocIds.size();
    }
    public void add(int maxDocId, int offsetDocId){
        maxDocIds.add(maxDocId);
        offsetMaxDocIds.add(offsetDocId);
    }
    public ArrayList<Integer> getMaxDocIds(){
        return maxDocIds;
    }
    public ArrayList<Integer> getOffsetMaxDocIds(){
        return offsetMaxDocIds;
    }

    //TODO ricerca binaria
    public int nextGEQ(int docId){
        for(int i = 0; i < maxDocIds.size(); i++){
            if(maxDocIds.get(i) > docId) return offsetMaxDocIds.get(i);
        }
        return -1;
    }

    public boolean seekInMaxDocIds(int docId) throws IOException {

        int l = 0, r = maxDocIds.size() - 1;

        while (l <= r)
        {
            int m = l + (r - l) / 2;
            int res = docId.compareTo(stopWords.get(m));
            if (res == 0)
                return true;
            if (res > 0)
                l = m + 1;
            else
                r = m - 1;
        }

        return false;
    }

    @Override
    public String toString(){
        String stringToReturn = "";
        for (int i = 0; i <maxDocIds.size(); i++){
            stringToReturn += "maxDocId: " + maxDocIds.get(i) + " --> " + " offset: " + offsetMaxDocIds.get(i) + "\n";
        }
        return stringToReturn;
    }
}
