import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PerformanceDistanceOneRandom {
        
    public static void main(String[] args) {
        Scanner input = null;
        int numOfStates = -1;
        int[] labels = null;
        Map<Integer, Set<DistributionAjaList>> transitions = new HashMap<>();
        // parse input file
        if (args.length != 1) {
            System.out.println(
                               "Use java PerformanceCompare 0: <inputTransitionFile> ");
        } else {
            // process the command line arguments
            try {
                input = new Scanner(new File(args[0]));
            } catch (FileNotFoundException e) {
                System.out.printf("Input file %s not found%n", args[0]);
                System.exit(1);
            }
            
            while (input.hasNextInt()) {
                try {
                    numOfStates = input.nextInt();
                    labels = new int[numOfStates];
                    for (int i = 0; i < numOfStates; i++) {
                        labels[i] = input.nextInt();
                    }
                    
                    for (int i = 0; i < numOfStates; i++) {
                        int nDistr = input.nextInt();
                        Set<DistributionAjaList> set = new HashSet<>();
                        for (int iDistr = 0; iDistr < nDistr; iDistr++) {
                            Map<Integer, Double> distr = new HashMap<>();
                            for (int j = 0; j < numOfStates; j++) {
                                double tmp = input.nextDouble();
                                if(tmp != 0){
                                    distr.put(j, tmp);
                                }
                            }
                            set.add(new DistributionAjaList(distr));
                        }
                        transitions.put(i, set);
                    }
                    
                    // input.nextLine();
                } catch (NoSuchElementException e) {
                    System.out.printf("Input file %s not in the correct format%n", args[0]);
                }
                //print transitions
                /*
                 for (int i : trans.keySet()) {
                 System.out.println(i);
                 for (DistributionAjaList dis : trans.get(i)) {
                 String mapAsString = dis.distr.keySet().stream().map(key -> key + "=" + dis.distr.get(key)).collect(Collectors.joining(", ", "{", "}"));
                 System.out.println(mapAsString);
                 }
                 }
                 */
                
                
                //print labels
                //System.out.println("labels: " + Arrays.toString(labels));
                long startTime = System.nanoTime();
                Set<Pair> bisimulationSet = new HashSet<>();
                ProbBisimilarity probBis = new ProbBisimilarity(numOfStates, labels, transitions);
                bisimulationSet = probBis.computeProbabilisticBisimilarity();
                long bisimulationTime = System.nanoTime();
                System.out.println("Bisimulation time = " + (bisimulationTime - startTime)/1_000_000 + " ms");
                System.out.println("bisimulationSet size = " + bisimulationSet.size());
                //System.out.println(bisimulationSet);
                
                DistanceOne distanceOne = new DistanceOne(numOfStates, labels, transitions, bisimulationSet);
                Set<Pair> distanceOneSet = distanceOne.getDistanceOneSet();
                long disOneTime = System.nanoTime();
                System.out.println("Distance one time = " + (disOneTime - bisimulationTime)/1_000_000 + " ms");
                System.out.println("Distance one size = " + distanceOneSet.size());
                //System.out.println(distanceOneSet);
            }
        }
    }
}
