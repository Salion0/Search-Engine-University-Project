package it.unipi.mircv.File;

import java.io.FileNotFoundException;


public class LexiconHandler extends FileHandler {

    public LexiconHandler() throws FileNotFoundException
    {
        super("lexicon.dat");
    }
}
