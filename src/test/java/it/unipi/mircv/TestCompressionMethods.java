package it.unipi.mircv;

import it.unipi.mircv.compression.Unary;
import it.unipi.mircv.compression.VariableByte;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static it.unipi.mircv.Utils.*;

public class TestCompressionMethods {

    @Test
    void testUnary() {

        ArrayList<Integer> testIntegers = new ArrayList<>(List.of(5,1,1,1,4,3,1));
        byte[] actualResult = new byte[2];
        actualResult[0] = 0x37;
        actualResult[1] = 0x0F;
        byte[] predictedResult;
        predictedResult = Unary.compress(testIntegers);
        predictedResult = reverseByteArray(predictedResult);
        for (int i = 0; i < actualResult.length; i++)
            Assertions.assertEquals(predictedResult[i],actualResult[i]);

        testIntegers = new ArrayList<>(List.of(7,10,5,1,2,3));
        actualResult = new byte[4];
        actualResult[0] = (byte) 0b00000110;
        actualResult[1] = (byte) 0b10011110;
        actualResult[2] = (byte) 0b11111111;
        actualResult[3] = (byte) 0b10111111;
        predictedResult = Unary.compress(testIntegers);
        predictedResult = reverseByteArray(predictedResult);
        for (int i = 0; i < actualResult.length; i++)
            Assertions.assertEquals(predictedResult[i],actualResult[i]);

        testIntegers = new ArrayList<>(List.of(8,1));
        actualResult = new byte[2];
        actualResult[0] = (byte) 0b00000000;
        actualResult[1] = (byte) 0b01111111;
        predictedResult = Unary.compress(testIntegers);
        predictedResult = reverseByteArray(predictedResult);
        for (int i = 0; i < actualResult.length; i++)
            Assertions.assertEquals(predictedResult[i],actualResult[i]);

        testIntegers = new ArrayList<>(List.of(16));
        actualResult = new byte[2];
        actualResult[0] = (byte) 0b01111111;
        actualResult[1] = (byte) 0b11111111;
        predictedResult = Unary.compress(testIntegers);
        predictedResult = reverseByteArray(predictedResult);
        for (int i = 0; i < actualResult.length; i++)
            Assertions.assertEquals(predictedResult[i],actualResult[i]);

        testIntegers = new ArrayList<>(List.of(1));
        actualResult = new byte[1];
        actualResult[0] = (byte) 0b00000000;
        predictedResult = Unary.compress(testIntegers);
        predictedResult = reverseByteArray(predictedResult);
        for (int i = 0; i < actualResult.length; i++)
            Assertions.assertEquals(predictedResult[i],actualResult[i]);

        System.out.println("test on Unary Compression --> SUCCESSFUL");
    }

    @Test
    void testVariableByte() {

        ArrayList<Integer> testIntegers = new ArrayList<>(List.of(5,1,300));
        byte[] actualResult = new byte[4];
        actualResult[0] = (byte) 0b00000101;
        actualResult[1] = (byte) 0b00000001;
        actualResult[2] = (byte) 0b10101100;
        actualResult[3] = (byte) 0b00000010;
        byte[] predictedResult;
        predictedResult = VariableByte.compress(testIntegers);
        for (int i = 0; i < predictedResult.length; i++) {
            predictedResult[i] = reverseBits(predictedResult[i]);
            actualResult[i] = reverseBits(actualResult[i]);
        }
        for (int i = 0; i < predictedResult.length; i++)
            Assertions.assertEquals(predictedResult[i],actualResult[i]);


        testIntegers = new ArrayList<>(List.of(7,10,8713));
        actualResult = new byte[4];
        actualResult[0] = (byte) 0b00000111;
        actualResult[1] = (byte) 0b00001010;
        actualResult[2] = (byte) 0b10001001;
        actualResult[3] = (byte) 0b01000100;
        predictedResult = VariableByte.compress(testIntegers);
        for (int i = 0; i < predictedResult.length; i++) {
            predictedResult[i] = reverseBits(predictedResult[i]);
            actualResult[i] = reverseBits(actualResult[i]);
        }
        for (int i = 0; i < predictedResult.length; i++)
            Assertions.assertEquals(predictedResult[i],actualResult[i]);


        testIntegers = new ArrayList<>(List.of(22106,34500));
        actualResult = new byte[6];
        actualResult[0] = (byte) 0b11011010;
        actualResult[1] = (byte) 0b10101100;
        actualResult[2] = (byte) 0b00000001;
        actualResult[3] = (byte) 0b11000100;
        actualResult[4] = (byte) 0b10001101;
        actualResult[5] = (byte) 0b00000010;
        predictedResult = VariableByte.compress(testIntegers);
        for (int i = 0; i < predictedResult.length; i++) {
            predictedResult[i] = reverseBits(predictedResult[i]);
            actualResult[i] = reverseBits(actualResult[i]);
        }
        for (int i = 0; i < predictedResult.length; i++)
            Assertions.assertEquals(predictedResult[i],actualResult[i]);

        testIntegers = new ArrayList<>(List.of(255));
        actualResult = new byte[2];
        actualResult[0] = (byte) 0b11111111;
        actualResult[1] = (byte) 0b00000001;
        predictedResult = VariableByte.compress(testIntegers);
        for (int i = 0; i < predictedResult.length; i++) {
            predictedResult[i] = reverseBits(predictedResult[i]);
            actualResult[i] = reverseBits(actualResult[i]);
        }
        for (int i = 0; i < predictedResult.length; i++)
            Assertions.assertEquals(predictedResult[i],actualResult[i]);

        System.out.println("test on VariableByte Compression --> SUCCESSFUL");
    }


    // Function to reverse the bits in a byte, used for the Variable Byte test
    private static byte reverseBits(byte input) {
        int result = 0;
        for (int i = 0; i < 8; i++) {
            result = (result << 1) | ((input >> i) & 1);
        }
        return (byte) result;
    }

}
