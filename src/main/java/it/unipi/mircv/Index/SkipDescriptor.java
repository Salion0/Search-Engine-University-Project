package it.unipi.mircv.Index;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

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
    public int nextGEQ(int docId){
        /*for(int i = 0; i < maxDocIds.size(); i++)
            if(maxDocIds.get(i) >= docId) return offsetMaxDocIds.get(i);
        return -1;
        */
        // Custom binary search to find the index of the first integer greater than the input
        int low = 0;
        int high = maxDocIds.size();
        while (low < high)
        {
            int mid = low + (high - low) / 2;
            int midValue = maxDocIds.get(mid);
            if (midValue < docId)
                low = mid + 1; // Discard the left half
            else
                high = mid; // Include the current mid index in the search space
        }
        // Check if the index is within the bounds of the list
        return (low < maxDocIds.size()) ? offsetMaxDocIds.get(low) : -1; //TODO si poò mettere un if prima della ricerca binaria
    }

    @Override
    public String toString(){
        String stringToReturn = "";
        for (int i = 0; i <maxDocIds.size(); i++){
            stringToReturn += "maxDocId: " + maxDocIds.get(i) + " --> " + " offset: " + offsetMaxDocIds.get(i) + "\t";
        }
        return stringToReturn;
    }
}
