import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/*
 * The algorithm presented in Figure 9 (two-phased partitioning algorithm)
 * Deciding Bisimilarity and Similarity for Probabilistic Processes by Baier et al.
 * */

class StepClass {
    private int label;
    private Set<DistributionAjaList> setOfDistr;

    public StepClass(int label) {
        this.label = label;
        this.setOfDistr = new HashSet<DistributionAjaList>();
    }

    public StepClass(int label, Set<DistributionAjaList> setOfDistr) {
        this(label);
        this.setOfDistr.addAll(setOfDistr);
    }

    public void addAllDistribution(Set<DistributionAjaList> setOfDistr) {
        this.setOfDistr.addAll(setOfDistr);
    }

    public void addDistribution(DistributionAjaList distr) {
        this.setOfDistr.add(distr);
    }

    public int getLabel() {
        return label;
    }

    public Set<DistributionAjaList> getDistributions() {
        return this.setOfDistr;
    }

    public boolean isIntersect(Set<DistributionAjaList> set) {
        return !Collections.disjoint(set, this.setOfDistr); // disjoint == no
        // intersection
    }
}

public class ProbBisimilarity {
    private int numOfStates;
    private Map<Integer, Set<DistributionAjaList>> transitions;
    private int[] labels;
    private Set<Set<Integer>> NewBlocks;
    private Set<StepClass> NewStepClasses;
    private Set<Set<Integer>> partitions;
    private Set<StepClass> stepClasses;
    // may use later
    /*
     * public static double discount = 1; public static double accuracy = 0.1;
     * public double[] discrepancy; private Map<Pair, double[]> coupling;
     * private Set<Pair> toCompute = new HashSet<Pair>();
     */

    public ProbBisimilarity(int numOfStates, int[] labels, Map<Integer, Set<DistributionAjaList>> transitions) {
        this.numOfStates = numOfStates;
        this.labels = labels;
        this.transitions = transitions;
        this.NewBlocks = new HashSet<Set<Integer>>();
        this.NewStepClasses = new HashSet<StepClass>();
        this.partitions = new HashSet<Set<Integer>>();
        this.stepClasses = new HashSet<StepClass>();
    }

    public void initialize() {
        // initialize the partitions by labels
        Map<Integer, Set<Integer>> mapPartitions = new HashMap<>();
        for (int i = 0; i < this.numOfStates; i++) {
            mapPartitions.computeIfAbsent(labels[i], k -> new HashSet<Integer>()).add(i);
        }
        int max = 0;
        Set<Integer> maxSet = null;
        for (Set<Integer> s : mapPartitions.values()) {
            partitions.add(s);
            if (s.size() > max) {
                maxSet = s;
                max = maxSet.size();
            }
            NewBlocks.add(s);
        }
        if (NewBlocks.contains(maxSet)) {
            NewBlocks.remove(maxSet);
        }

        // initialize the StepClasses by labels
        Map<Integer, StepClass> mapStepClasses = new HashMap<>();
        for (int i = 0; i < this.numOfStates; i++) {
            int label = labels[i];
            mapStepClasses.computeIfAbsent(label, k -> new StepClass(label)).addAllDistribution(transitions.get(i));
        }

        stepClasses.addAll(mapStepClasses.values());
        this.NewStepClasses.addAll(stepClasses);
    }

    public void split(Set<StepClass> stepClasses, Set<Integer> splitter) {
        Set<StepClass> stepClassesOld = new HashSet<StepClass>(stepClasses);
        stepClasses.clear();
        for (StepClass stepClass : stepClassesOld) {
            // split it
            int label = stepClass.getLabel();
            Map<Double, Set<DistributionAjaList>> mapSplitDistributions = new HashMap<>();
            // loop through all the distributions
            for (DistributionAjaList distr : stepClass.getDistributions()) {
                double sum = 0;
                for (int s : splitter) {
                    sum += distr.getProbability(s);
                }
                mapSplitDistributions.computeIfAbsent(sum, k -> new HashSet<DistributionAjaList>()).add(distr);
            }
            // update SetClasses
            for (Set<DistributionAjaList> set : mapSplitDistributions.values()) {
                stepClasses.add(new StepClass(label, set));
            }
            // update NewStepClasses
            if (mapSplitDistributions.size() >= 2) {
                for (Set<DistributionAjaList> set : mapSplitDistributions.values()) {
                    NewStepClasses.add(new StepClass(label, set));
                }
            }
        }
    }

    public void refine(Set<Set<Integer>> partitions, StepClass stepClass) {
        Set<Set<Integer>> partitionsOld = new HashSet<Set<Integer>>(partitions);
        partitions.clear();
        for (Set<Integer> part : partitionsOld) {
            // possibly split each part
            int label = this.labels[(int) part.toArray()[0]];
            if (label != stepClass.getLabel()) {
                partitions.add(part);
                continue;
            }
            Set<Integer> newSet = new HashSet<>();

            for (Integer i : part) {
                if (stepClass.isIntersect(transitions.get(i))) {
                    newSet.add(i);
                }
            }
            // the complement of the set
            Set<Integer> compSet = new HashSet<>(part);
            compSet.removeAll(newSet);
            // update the partitions
            if (!newSet.isEmpty()) {
                partitions.add(newSet);
            }
            if (!compSet.isEmpty()) {
                partitions.add(compSet);
            }
            // update NewBlocks
            if (newSet.isEmpty() || compSet.isEmpty()) {
                continue;
            }
            if (newSet.size() < compSet.size()) {
                NewBlocks.add(newSet);
            } else {
                NewBlocks.add(compSet);
            }
        }
    }

    public Set<Pair> computeProbabilisticBisimilarity() {
        this.initialize();
        while (!this.NewStepClasses.isEmpty() || !this.NewBlocks.isEmpty()) {
            /* phase 1 */
            Iterator<Set<Integer>> itrNewBlocks = this.NewBlocks.iterator();
            while (itrNewBlocks.hasNext()) {
                Set<Integer> block = (Set<Integer>) itrNewBlocks.next();
                itrNewBlocks.remove();
                this.split(this.stepClasses, block);
            }
            // phase 2
            Iterator<StepClass> itrNewStepClasses = this.NewStepClasses.iterator();
            while (itrNewStepClasses.hasNext()) {
                StepClass sc = (StepClass) itrNewStepClasses.next();
                itrNewStepClasses.remove();
                this.refine(this.partitions, sc);
            }
        }
        Set<Pair> bisimilationSet = new HashSet<>();
        for (Set<Integer> set : this.partitions) {
            Integer[] intArray = set.toArray(new Integer[0]);
            for (int i = 0; i < intArray.length; i++) {
                int s = intArray[i];
                for (int j = 0; j <= i; j++) {
                    bisimilationSet.add(new Pair(s, intArray[j]));
                }
            }
        }
        return bisimilationSet;
    }
}
