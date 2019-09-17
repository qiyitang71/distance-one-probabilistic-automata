public class Pair {
    private final int row;
    private final int column;

    public Pair(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        int c = (column >= row) ? column : row;
        int r = column + row - c;
        result = prime * result + c;
        result = prime * result + r;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pair other = (Pair) obj;
        if ((column == other.column && row == other.row) || (column == other.row && row == other.column)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Pair [row=" + row + ", column=" + column + "]";
    }

}

