package it.unipi.mircv.evaluation;

public class Query {
    private final int queryId;    // The queryId is the id of the query in the dataset
    private final String queryText;    // The queryText is the text of the query

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
