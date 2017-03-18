package com.github.jabbalaci.fudep.tane;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;


public class Apriori {

    public static int[][] getItemsetsInArray(Level level) {
        int i, k, m;
        Row row;
        BitSet bs;
        List<Row> rows = level.getRows();
        int size = rows.size();
        int[][] result = new int[size][];

        for (k = 0; k < size; ++k)
        {
           row = rows.get(k);
           bs = row.getAttrs();
           result[k] = new int[bs.cardinality()];
           m = 0;
           for (i = bs.nextSetBit(0); i>=0; i = bs.nextSetBit(i+1)) {
              result[k][m++] = i;
           }
        }

        return result;
    }

    public static BitSet merge(int[] array, int val) {
        BitSet res = new BitSet();
        for (int i: array) {
            res.set(i);
        }
        res.set(val);

        return res;
    }

   /**
    * @param itemset An arbitrary itemset.
    * @return List of its one-size smaller subsets.
    */
    public static List<BitSet> getOneSizeSmallerSubsetsOf(BitSet itemset) {
       List<BitSet> li = new ArrayList<>();

       for (int i = itemset.nextSetBit(0); i >= 0; i = itemset.nextSetBit(i+1)) {
          itemset.clear(i);
          li.add((BitSet) itemset.clone());
          itemset.set(i);
       }
       return li;
    }

}
