import java.util.HashMap;
import java.util.Map;

class DistributionAjaList {
    public Map<Integer, Double> distr;

    public DistributionAjaList(Map<Integer, Double> distr) {
        this.distr = new HashMap(distr);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + distr.hashCode();
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
        DistributionAjaList other = (DistributionAjaList) obj;
        if (distr.size() != other.distr.size()) {
            return false;
        } else {
            for (int i : distr.keySet()) {
                if (!other.distr.containsKey(i) || distr.get(i) != other.distr.get(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    public double getProbability(int index) {
        if (distr.containsKey(index)) {
            return distr.get(index);
        } else {
            return 0;
        }
    }

}
