import java.util.*;
import java.util.stream.Collectors;

public class DistanceOne {
    private int numOfStates;
    private Map<Integer, Set<DistributionAjaList>> transitions;
    private int[] labels;
    private Set<Pair> bisimulationSet;
    private Set<Pair> diffLabelsSet;
    private Set<Pair> nonZeroSet;
    private int numMinCostFlow = 0;

    private double solveMinCostFlow(DistributionAjaList mu, DistributionAjaList nu, Set<Pair> set) {
        numMinCostFlow++;

        MinCostMaxFlow flow = new MinCostMaxFlow();
        int num = this.numOfStates * 2 + 2;
        double[][] cap = new double[num][num];
        double[][] cost = new double[num][num];

        // Initialize the capacity and cost arrays
        for(int i = 0; i < num; i++){
            cap[i] = new double[num];
            cost[i] = new double[num];
        }
        // Add arcs between the states
        for (int i = 0; i < this.numOfStates; ++i) {
            for (int j = this.numOfStates; j < 2 * this.numOfStates; j++) {
                cap[i][j] = Math.min(mu.getProbability(i), nu.getProbability(j - this.numOfStates));
                Pair pair = new Pair(i, j - this.numOfStates);
                if (set.contains(pair)) {
                    cost[i][j] = 1;
                }
            }
        }

        //add arcs from source and to target
        for (int i = 0; i < this.numOfStates; i++) {
            cap[this.numOfStates * 2][i] = mu.getProbability(i);
            cap[i + this.numOfStates][this.numOfStates * 2 + 1] = nu.getProbability(i);
        }

        double[] res = flow.getMaxFlow(cap, cost, this.numOfStates * 2, this.numOfStates * 2 + 1);
        return res[1];
    }

    public DistanceOne(int numOfStates, int[] labels, Map<Integer, Set<DistributionAjaList>> transitions, Set<Pair> bisimulationSet) {
        this.numOfStates = numOfStates;
        this.labels = labels;
        this.transitions = transitions;
        this.bisimulationSet = bisimulationSet;
        this.diffLabelsSet = new HashSet<>();
        this.nonZeroSet = new HashSet<>();
    }

    //Given two distributions mu and nu, check if all support of couplings of \Omega(mu, nu) intersect with the given set
    private boolean isCouplingsIntersect(DistributionAjaList mu, DistributionAjaList nu, Set<Pair> set) {
        double cost = solveMinCostFlow(mu, nu, set);
        numMinCostFlow++;
        if (cost > 0) {
            return true;
        }
        return false;
    }

    //Given two distributions mu and nu, check if all support of couplings of \Omega(mu, nu) is subset of the given set
    private boolean isCouplingsSubset(DistributionAjaList mu, DistributionAjaList nu, Set<Pair> set) {
        //brute-force
        Set<Integer> muSet = new HashSet<>();
        Set<Integer> nuSet = new HashSet<>();

        for (int i = 0; i < this.numOfStates; i++) {
            if (mu.getProbability(i) > 0) {
                muSet.add(i);
            }
            if (nu.getProbability(i) > 0) {
                nuSet.add(i);
            }
        }
        for (int i : muSet) {
            for (int j : nuSet) {
                if (!set.contains(new Pair(i, j))) {
                    return false;
                }
            }
        }
        return true;
    }

    private void getPairOfDiffLabels() {
        for (int i = 0; i < this.numOfStates; i++) {
            for (int j = 0; j < i; j++) {
                if (this.labels[i] != this.labels[j]) {
                    this.diffLabelsSet.add(new Pair(i, j));
                }
            }
        }
    }

    private void getNonZeroPairs() {
        for (int i = 0; i < this.numOfStates; i++) {
            for (int j = 0; j < i; j++) {
                Pair p = new Pair(i, j);
                if (!this.bisimulationSet.contains(p) && !this.diffLabelsSet.contains(p)) {
                    this.nonZeroSet.add(p);
                }
            }
        }
    }

    private Set<Pair> applyGamma(Set<Pair> setX, Set<Pair> setY) {
        Set<Pair> newSet = this.nonZeroSet.parallelStream().filter(p -> {
            int s = p.getRow();
            int t = p.getColumn();
            if (applyGammaHelper(s, t, setX, setY) || applyGammaHelper(t, s, setX, setY)) {
                return true;
            }
            return false;
        }).collect(Collectors.toSet());

        newSet.addAll(this.diffLabelsSet);
        return newSet;
    }

    private boolean applyGammaHelper(int s, int t, Set<Pair> setX, Set<Pair> setY) {
        for (DistributionAjaList mu : this.transitions.get(s)) {
            boolean isSatisfy = true;
            for (DistributionAjaList nu : this.transitions.get(t)) {
                if (!isCouplingsSubset(mu, nu, setX) || !isCouplingsIntersect(mu, nu, setY)) {
                    isSatisfy = false;
                    break;
                }
            }
            if (isSatisfy) {
                return true;
            }
        }
        return false;
    }

    private Set<Pair> applyGammaIntersect(Set<Pair> setY) {
        Set<Pair> newSet = this.nonZeroSet.parallelStream().filter(p -> {
            int s = p.getRow();
            int t = p.getColumn();
            if (applyGammaIntersectHelper(s, t, setY) || applyGammaIntersectHelper(t, s, setY)) {
                return true;
            }
            return false;
        }).collect(Collectors.toSet());

        newSet.addAll(this.diffLabelsSet);
        return newSet;
    }

    private boolean applyGammaIntersectHelper(int s, int t, Set<Pair> setY) {
        for (DistributionAjaList mu : this.transitions.get(s)) {
            boolean isSatisfy = true;
            for (DistributionAjaList nu : this.transitions.get(t)) {
                if (!isCouplingsIntersect(mu, nu, setY)) {
                    isSatisfy = false;
                    break;
                }
            }
            if (isSatisfy) {
                return true;
            }
        }
        return false;
    }

    public Set<Pair> getDistanceOneSet() {
        //calculate diffLabelsSet and nonZeroSet
        getPairOfDiffLabels();
        getNonZeroPairs();
        System.out.println("dif labels size = " + this.diffLabelsSet.size());
        System.out.println("non-zero size = " + this.nonZeroSet.size());


        //get the initial setX and setY
        Set<Pair> set = new HashSet<>();
        Set<Pair> setNew = new HashSet<>();
        long outerloop = 0;
        do {
            outerloop++;
            set.addAll(setNew);
            setNew = applyGammaIntersect(set);

        } while (!set.equals(setNew));
        //the main algorithm
        // calculate an interplay of gfp and lfp
        Set<Pair> setX = new HashSet(set);
        Set<Pair> setXPrevious;

        outerloop = 0;
        do {
            outerloop++;
            Set<Pair> setY = new HashSet<>();
            Set<Pair> setYPrevious = new HashSet<>();
            long innerloop = 0;
            do {
                innerloop++;
                setYPrevious.addAll(setY);
                setY = applyGamma(setX, setYPrevious);
            } while (!setY.equals(setYPrevious));

            setXPrevious = new HashSet();
            setXPrevious.addAll(setX);
            setX = new HashSet();
            setX.addAll(setY);
        } while (!setX.equals(setXPrevious));
        System.out.println("Number of mincostflow  = " + numMinCostFlow);
        return setX;
    }

}



