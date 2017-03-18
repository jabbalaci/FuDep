package com.github.jabbalaci.fudep.tane;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.github.jabbalaci.fudep.utils.Utils;

public class Dataset {

    private final static boolean DEBUG = false;

    private List<String> attr_names;
    private List<String[]> matrix;
    public Columns columns;
    public Partitions partitions;
    @SuppressWarnings("unused")
    private int num_columns;
    private int num_rows;
    public ValidRules valid_rules = new ValidRules();
    public Stripper stripper;

    public Dataset(String[] args) {
        String fname = null;
        try {
            fname = args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Error: provide an input file!");
            System.exit(1);
        }
        this.read_dataset(fname);
        this.columns = new Columns(this.attr_names);
        if (DEBUG) System.out.println(this.columns);
        this.partitions = new Partitions(this.columns, this.matrix);
        this.num_columns = this.matrix.get(0).length;
        this.num_rows = this.matrix.size();
        this.matrix.clear(); this.matrix = null;    // not needed anymore, free memory
        this.stripper = new Stripper(this.num_rows);

    }

    private void read_dataset(String fname) {
        File csv = new File(fname);
        if (csv.isFile() == false) {
            System.err.println("Error: the input file doesn't exist");
            System.exit(1);
        }
        boolean auto_cols = true;
        File cols = new File(Utils.parent(csv) + "/" + Utils.stem(csv) + ".cols");
        if (cols.isFile()) {
            auto_cols = false;
        }
        if (auto_cols == false) {
            this.attr_names = this.read_column_names(cols);
        }

        this.matrix = read_dataset(csv);
        int width = this.matrix.get(0).length;
        if (width > Utils.ENGLISH_ALPHABET.length()) {
            System.err.println("Error: the dataset has too many attributes (not yet supported)");
            System.exit(1);
        }
        if (auto_cols) {
            this.attr_names = Utils.string_to_list_of_strings(Utils.ENGLISH_ALPHABET.substring(0, width));
        }

        if (DEBUG) {
            System.out.println("Number of rows: " + this.matrix.size());
            System.out.println("Number of columns: " + this.attr_names.size());
            System.out.println("Columns: " + this.attr_names);
            System.out.println("Matrix:");
            for (String[] row: this.matrix) {
                System.out.println(Arrays.asList(row));
            }
        }
    }

    private List<String[]> read_dataset(File f) {
        List<String[]> m = new ArrayList<>();
        String[] parts;
        try {
            String line;
            BufferedReader br = new BufferedReader(new FileReader(f));
            while ((line = br.readLine()) != null)   {
                 line = line.replace("\n", "");
                 if (line.isEmpty() || line.startsWith("#")) {
                     continue;
                 }
                 parts = Utils.clean(line.split(","));
                 m.add(parts);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //
        int width = m.get(0).length;
        for (String[] row: m) {
            if (row.length != width) {
                System.err.println("Error: the number of columns differs in the CSV file");
                System.exit(1);
            }
        }
        //
        return m;
    }

    private List<String> read_column_names(File f) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        String line = null;
        try {
            line = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] parts = Utils.clean(line.split(","));
        return Arrays.asList(parts);
    }

}
