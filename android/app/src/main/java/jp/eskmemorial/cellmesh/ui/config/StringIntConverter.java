package jp.eskmemorial.cellmesh.ui.config;

import androidx.databinding.InverseMethod;

public class StringIntConverter {
    @InverseMethod("toInt")
    public static String toString(int value) {
        return Integer.toString(value);
    }

    public static int toInt(String value) {
        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            return 0;
        }
    }
}
