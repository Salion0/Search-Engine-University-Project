package it.unipi.mircv.evaluation;

public class Query {
    private final int queryId;
    private final String queryText;
    Query(int queryId, String queryText){
        this.queryId = queryId;
        this.queryText = queryText;
    }
    public int getQueryId(){
        return queryId;
    }
    public String getQueryText(){
        return queryText;
    }
    @Override
    public String toString() {
        return "queryId: " + queryId + " queryText: " + queryText;
    }

}
