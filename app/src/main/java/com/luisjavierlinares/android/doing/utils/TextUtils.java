package com.luisjavierlinares.android.doing.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Luis on 14/09/2017.
 */

public class TextUtils {

    public static int countWords(String input) {
        if (input == null || input.isEmpty()) {
            return 0;
        }
        String[] words = input.split("\\s+");
        return words.length;
    }

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static String toNoAccentsNoSpacesLowerCase(String string) {
        String stringOut = string.replace("Á", "A")
                .replace("Â", "A")
                .replace("À", "A")
                .replace("Ä", "A")
                .replace("É", "E")
                .replace("Ê", "E")
                .replace("È", "E")
                .replace("Ë", "E")
                .replace("Í", "I")
                .replace("Î", "I")
                .replace("Ì", "I")
                .replace("Ï", "I")
                .replace("Ó", "O")
                .replace("Ô", "O")
                .replace("Ò", "O")
                .replace("Ö", "O")
                .replace("Ú", "U")
                .replace("Û", "U")
                .replace("Ù", "U")
                .replace("Ü", "U")
                .replace("á", "a")
                .replace("â", "a")
                .replace("à", "a")
                .replace("ä", "a")
                .replace("é", "e")
                .replace("ê", "e")
                .replace("è", "e")
                .replace("ë", "e")
                .replace("í", "i")
                .replace("î", "i")
                .replace("ì", "i")
                .replace("ï", "i")
                .replace("ó", "o")
                .replace("ô", "o")
                .replace("ò", "o")
                .replace("ö", "o")
                .replace("ú", "u")
                .replace("û", "u")
                .replace("ù", "u")
                .replace("ü", "u");

        stringOut = stringOut.toLowerCase();

        stringOut = stringOut.replaceAll("\\s","");

        return stringOut;
    }

}
