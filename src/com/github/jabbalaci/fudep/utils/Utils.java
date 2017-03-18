package com.github.jabbalaci.fudep.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class Utils {

    public final static String ENGLISH_ALPHABET = "abcdefghijklmnopqrstuvwxyz";

    public static String parent(File f) {
        String parent = f.getParent();
        if (parent != null) {
            return parent;
        }
        return ".";
    }

    public static String stem(File f) {
        return f.getName().replaceFirst("[.][^.]+$", "");
    }

    public static String[] clean(String[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i].trim();
        }
        return array;
    }

    public static List<String> string_to_list_of_strings(String s) {
        List<String> res = new ArrayList<>();
        for (char c : s.toCharArray()) {
            res.add("" + c);
        }
        return res;
    }

    public static BitSet array_to_bitset(int[] array) {
        BitSet bs = new BitSet();
        for (int i: array) {
            bs.set(i);
        }
        return bs;
    }

    /**
     * Difference operation. Example:
     * A = {1, 2, 3, 4, 5, 6}
     * B = {1, 3, 5, 6}
     * A \ B = {2, 4}
     *
     * @param setA Set A.
     * @param setB Set B.
     * @return Difference set operation on sets A and B, i.e. A \ B.
     */
    public static BitSet minus(BitSet setA, BitSet setB) {
        BitSet cloneA = (BitSet) setA.clone();
        cloneA.andNot(setB);
        return cloneA;
    }

}
