package it.unipi.mircv.compression;

import java.io.IOException;
import java.util.ArrayList;

import static it.unipi.mircv.Config.stopWords;

public class Utils {
    static public void printReverseBytes(byte[] bytesToPrint){
        bytesToPrint = reverseByteArray(bytesToPrint);
        System.out.print("bytes: " + bytesToPrint.length + "\tcode: ");
        for (byte b :bytesToPrint) {
            for (int i = 7; i >= 0; i--) {
                byte bit = (byte) ((b >> i) & 1);
                System.out.print(bit);
            }System.out.print(" ");
        }System.out.println();
    }

    static public void printBytes(byte[] bytesToPrint){
        System.out.print("bytes: " + bytesToPrint.length + "\tcode: ");
        for (byte b :bytesToPrint) {
            for (int i = 0; i < 8; i++) {
                byte bit = (byte) ((b >> i) & 1);
                System.out.print(bit);
            }System.out.print(" ");
        }System.out.println();
    }
    static public void printByte(byte b){
        System.out.print("bytes: 1" + "\tcode: ");
        for (int i = 0; i < 8; i++) {
            byte bit = (byte) ((b >> i) & 1);
            System.out.print(bit);
        }System.out.println();
    }

    public static byte[] reverseByteArray(byte[] array) {
        int length = array.length;
        byte[] reversedArray = new byte[length];

        for (int i = 0; i < length; i++) {
            reversedArray[i] = array[length - 1 - i];
        }
        return reversedArray;
    }

    public static String[] removeStopWords(String[] queryTerms) throws IOException {
        ArrayList<String> filteredTerms = new ArrayList<>();
        for (String term : queryTerms) {
            if (!seekInStopwords(term)) {
                filteredTerms.add(term);
            }
        }
        return filteredTerms.toArray(new String[0]);
    }

    public static boolean seekInStopwords(String term) throws IOException {

        int l = 0, r = stopWords.size() - 1;

        while (l <= r)
        {
            int m = l + (r - l) / 2;
            int res = term.compareTo(stopWords.get(m));
            if (res == 0)
                return true;
            if (res > 0)
                l = m + 1;
            else
                r = m - 1;
        }

        return false;
    }

}
