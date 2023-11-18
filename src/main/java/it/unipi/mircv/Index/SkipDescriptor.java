package it.unipi.mircv.Index;
import java.util.ArrayList;

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

    @Override
    public String toString(){
        String stringToReturn = "";
        for (int i = 0; i <maxDocIds.size(); i++){
            stringToReturn += "maxDocId: " + maxDocIds.get(i) + " --> " + " offset: " + offsetMaxDocIds.get(i) + "\n";
        }
        return stringToReturn;
    }
}
