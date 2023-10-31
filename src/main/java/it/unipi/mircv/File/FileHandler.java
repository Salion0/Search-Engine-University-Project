package it.unipi.mircv.File;

import java.io.*;

public abstract class FileHandler extends RandomAccessFile{
    //Abstract class that open the extends the class random access file by opening the file in read mode
    public FileHandler(String name) throws FileNotFoundException {
        super("data1/"+name,"r");
    }
}
