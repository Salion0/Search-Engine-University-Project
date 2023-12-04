package it.unipi.mircv.compression;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class Unary {
    public static byte[] compress(ArrayList<Integer> values) {
        //this method works with the assumption that values are > 0
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ByteManipulator byteManipulator = new ByteManipulator();

        int position = 0;
        for (int value : values) {
            for (int i = 0; i < (value-1); i++){
                if(position > 7){
                    byteArrayOutputStream.write(byteManipulator.getByte());
                    byteManipulator = new ByteManipulator();
                    byteManipulator.setBitToOne(0);
                    position = 1;
                }else {
                    byteManipulator.setBitToOne(position);
                    position++;
                }
            }
            if(position > 7){
                byteArrayOutputStream.write(byteManipulator.getByte());
                byteManipulator = new ByteManipulator();
                position = 0;
            }
            position ++;
        }
        byteArrayOutputStream.write(byteManipulator.getByte());
        return byteArrayOutputStream.toByteArray();
    }
    public static int[] decompress(int length, byte[] byteArray) {
        //this method works with the assumption that values are > 0
        int[] values = new int[length];
        ByteManipulator byteManipulator;

        int i = 0;
        int countOnes = 0;
        for (byte byteElem : byteArray) {
            byteManipulator = new ByteManipulator(byteElem);
            for (int pos = 0; pos <8; pos++){
                if (byteManipulator.getBit(pos)){
                    countOnes++;
                }
                else{
                    values[i] = countOnes + 1;
                    i++;
                    countOnes = 0;
                    if(i == length) break;
                }
            }
        }
        return values;
    }
}
