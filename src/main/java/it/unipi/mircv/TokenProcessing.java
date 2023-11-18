package it.unipi.mircv;

import ca.rmen.porterstemmer.PorterStemmer;

import java.util.ArrayList;

import static it.unipi.mircv.Config.TERM_BYTES_LENGTH;
import static it.unipi.mircv.Config.stopWords;

public class TokenProcessing {
    public static String[] doStopWordRemovalAndStemming(PorterStemmer stemmer, String[] initialQueryTerms) {  // remove stop words and do stemming on query terms

        ArrayList<String> currentQueryTerms = new ArrayList<>();
        for (String token: initialQueryTerms) {
            if (stopWords.contains(token)) // stopWordRemoval
                continue;

            token = stemmer.stemWord(token); // stemming
            if (token.length() > TERM_BYTES_LENGTH) // il token è più lungo di 64 byte quindi lo scartiamo
                continue;
            currentQueryTerms.add(token);
        }

        String[] finalQueryTerms = new String[currentQueryTerms.size()];
        for (int i = 0; i < currentQueryTerms.size(); i++)
            finalQueryTerms[i] = currentQueryTerms.get(i);

        return finalQueryTerms;
    }
}
