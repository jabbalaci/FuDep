package com.github.jabbalaci.fudep.tane;

public class Tane {
	
	private final static boolean DEBUG = false;

    private String[] args;

    public Tane(String[] args) {
        this.args = args;
    }

    public void start() {
        Dataset db = new Dataset(this.args);
        this.levelwise_exploration(db);
        db.valid_rules.show();
    }

    private void levelwise_exploration(Dataset db) {
        Level level = new Level(1, db, null);
        if (DEBUG) {
        	level.show();
        } else {
        	System.err.print("."); System.err.flush();
        }
        int i = 1;
        while (true) {
            level = new Level(i+1, db, level);
            ++i;
            if (level.isEmpty()) {
                break;
            }
            if (DEBUG) {
            	level.show();
            } else {
            	System.err.print("."); System.err.flush();
            }
        }
        if (DEBUG == false) System.err.println();
        System.out.println("==============================");
    }

}
