#!/usr/bin/env python3

"""
Verify a dataset and print some statistics about it.

If a line is longer than the others (i.e. it has more columns),
then drop an error.

A dataset is a CSV file.
The column names can be in a .cols file next to the CSV file (with the same name).
If such a file is found, the algorithm will use those column names. Otherwise,
the attributes will be named as a, b, c, etc.
"""

import sys
from pathlib import Path

ALPHABET = "abcdefghijklmnopqrstuvwxyz"


class DatasetProblem(Exception):
    pass


def clean(li):
    return [e.strip() for e in li]


def read_column_names(fpath):
    with fpath.open() as f:
        return clean(f.readline().strip().split(','))


def read_dataset(fpath):
    m = []
    with fpath.open() as f:
        for line in f:
            line = line.rstrip('\n')
            if not line or line.startswith('#'):
                continue
            #
            parts = clean(line.split(','))
            m.append(parts)
        #
    #
    width = len(m[0])
    for row in m:
        if len(row) != width:
            raise DatasetProblem("the number of columns differs in the CSV file")
    #
    return m


def main(fname):
    auto_cols = True
    csv = Path(fname)
    cols = Path(csv.parent, csv.stem + '.cols')
    if cols.is_file():
        auto_cols = False

    if not auto_cols:
        columns = read_column_names(cols)

    matrix = read_dataset(csv)
    width = len(matrix[0])
    if width > len(ALPHABET):
        raise DatasetProblem("the dataset has too many attributes (not yet supported)")
    if auto_cols:
        columns = list(ALPHABET[:width])

    print("Number of rows:", len(matrix))
    print("Number of columns:", len(columns))
    print("Columns:", columns)

##############################################################################

if __name__ == "__main__":
    if len(sys.argv) == 1:
        print("Error: provide a dataset", file=sys.stderr)
        exit(1)
    #
    main(sys.argv[1])
