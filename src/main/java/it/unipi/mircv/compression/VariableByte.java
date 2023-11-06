package it.unipi.mircv.compression;

import java.io.ByteArrayOutputStream;

public class VariableByte {

    //this method works with the assumption that value is a positive int
    public static byte[] compress(int value) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (value > 0) {
            byte tempByte = (byte) (value & 0x7F);

            value >>>= 7;
            if (value > 0) {
                tempByte |= 0x80;
            }

            byteArrayOutputStream.write(tempByte);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static int decompress(byte[] bytes) {
        int result = 0;
        int shift = 0;
        for (byte b : bytes) {
            result |= (b & 0x7F) << shift;
            shift += 7;
            if ((b & 0x80) == 0) {
                break;
            }
        }
        return result;
    }
}
