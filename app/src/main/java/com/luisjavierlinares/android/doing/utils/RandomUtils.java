package com.luisjavierlinares.android.doing.utils;

import java.util.Random;

/**
 * Created by Luis on 28/03/2017.
 */

public class RandomUtils {

    public static int getRandomNumberBetween(int min, int max) {
        Random r = new Random();
        int randomInt = r.nextInt(max - min + 1) + min;

        return randomInt;
    }

    public static String getBase58ReadableRandom(int size)
    {
        String sampleAlphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz123456789";
        Random random = new Random();
        char[] buf = new char[size];
        for (int i = 0 ; i < size ; i++)
            buf[i] = sampleAlphabet.charAt(random.nextInt(sampleAlphabet.length()));
        return new String(buf);
    }

    public static String getRandomPassword(int size) {
        String sampleAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        char[] buf = new char[size];
        for (int i = 0 ; i < size ; i++)
            buf[i] = sampleAlphabet.charAt(random.nextInt(sampleAlphabet.length()));
        return new String(buf);
    }

}
