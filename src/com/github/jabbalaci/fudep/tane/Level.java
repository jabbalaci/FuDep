package com.github.jabbalaci.fudep.tane;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Level {

    private int length;
    public Dataset db;
    private Level prev_level;
    private List<Row> rows = new ArrayList<>();
    private Set<BitSet> set_of_itemsets = new HashSet<>();
    private Map<BitSet, BitSet> c_plus_dict = new HashMap<>();

    public Level(int length, Dataset db, Level prev_level) {
        if (prev_level != null) {
            prev_level.prune();
        }
        this.length = length;
        this.db = db;
        this.prev_level = prev_level;
        //
        if (length == 1) {
            this.initialize_level_1();
        } else {
            this.create_level_from_prev_level();
        }
        //
        for (Row row: this.rows) {
            this.c_plus_dict.put(row.getAttrs(), row.getCPlus());
        }
    }

    public Map<BitSet, BitSet> getCPlusDict() {
        return this.c_plus_dict;
    }

    public List<Row> getRows() {
        return this.rows;
    }

    public int getLength() {
        return this.length;
    }

    public Level getPrevLevel() {
        return this.prev_level;
    }

    private void create_level_from_prev_level() {
        int l, k, c;
        int length = this.prev_level.getLength();
        int[][] itemsets = Apriori.getItemsetsInArray(this.prev_level);
        Row new_row, row_a, row_b;
//      for (int[] row: itemsets) {
//          System.out.println(Arrays.toString(row));
//      }

        for (l = 0; l < itemsets.length - 1; ++l)
        {
           middle:
           for (k = l + 1; k < itemsets.length; ++k)
           {
              for (c = 0; c < length - 1; ++c) {
                 if (itemsets[l][c] != itemsets[k][c]) {
                     break middle;    // the prefix parts are not equal
                 }
              }

              BitSet candidate = Apriori.merge(itemsets[l], itemsets[k][length-1]);
//            System.out.println(candidate);
              if (this.prev_level.contains_all_subsets_of(candidate)) {
                  new_row = new Row(candidate, this);
                  new_row.compute_c_cplus();
                  row_a = this.prev_level.rows.get(l);
                  row_b = this.prev_level.rows.get(k);
                  new_row.set_partition(row_a, row_b);
                  new_row.generate_rules();
                  this.rows.add(new_row);
                  this.set_of_itemsets.add(candidate);
              }
           }
        }
    }

    private boolean contains_all_subsets_of(BitSet cand) {
        if (cand.cardinality() >= 3) {
            for (BitSet sub: Apriori.getOneSizeSmallerSubsetsOf(cand)) {
                if (this.set_of_itemsets.contains(sub) == false) {
                    return false;
                }
            }
        }
        //
        return true;
    }

    private void initialize_level_1() {
        Row row;
        BitSet bs;

        for (int attr_id: this.db.columns.attr_ids) {
            bs = new BitSet(); bs.set(attr_id);
            row = new Row(bs, this);
            row.compute_c_cplus();
            this.rows.add(row);
            this.set_of_itemsets.add(bs);
        }

    }

    private void prune() {
////      System.out.println("before: " + this.rows.size());
//        List<Row> del = new ArrayList<>();
//        for (Row row: this.rows) {
//            if (row.getCPlus().isEmpty()) {
//                del.add(row);
//            }
//        }
//        this.rows.removeAll(del);
////      System.out.println("after: " + this.rows.size());

        List<Row> keep = new ArrayList<>();
        for (Row row: this.rows) {
            if (row.getCPlus().isEmpty() == false) {
                keep.add(row);
            }
        }
        this.rows = keep;
    }

    public void show() {
        System.out.println("------------------------------");
        System.out.println("Level " + this.length);
        for (Row row: this.rows) {
            System.out.println(row);
        }
    }

    public boolean isEmpty() {
        return this.rows.isEmpty();
    }

}
