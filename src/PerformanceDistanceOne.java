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

public class PerformanceDistanceOne {


    public static void main(String[] args) {
        Scanner input = null;
        Scanner inputLabels = null;
        int numOfStates = -1;
        int[] labels = null;
        Map<Integer, Set<DistributionAjaList>> trans = new HashMap<>();
        // parse input file
        if (args.length != 2) {
            System.out.println(
                    "Use java PerformanceCompare 0: <inputTransitionFile> 1: <inputLabelFile> ");
        } else {
            // process the command line arguments
            try {
                input = new Scanner(new File(args[0]));
            } catch (FileNotFoundException e) {
                System.out.printf("Input file %s not found%n", args[0]);
                System.exit(1);
            }

            try {
                inputLabels = new Scanner(new File(args[1]));
            } catch (FileNotFoundException e) {
                System.out.printf("Output file %s not created%n", args[1]);
                System.exit(1);
            }

            // parse labels
            HashMap<Integer, Integer> labelsMap = new HashMap<>();
            while (inputLabels.hasNextLine()) {
                try {
                    //skip all lines with no ":" sign
                    String line = inputLabels.nextLine();
                    while (inputLabels.hasNextLine() && !line.contains(":")) {
                        line = inputLabels.nextLine();
                    }
                    String[] array = line.split(": ");
                    if (array.length != 2) {
                        System.err.println("Input file not in the right format");
                        System.exit(1);
                    }
                    int num = Integer.parseInt(array[0]);
                    labelsMap.put(num, array[1].hashCode());
                } catch (Exception e) {
                    System.err.println("Input file not in the correct format");
                }
            }

            // parse transition matrix
            while (input.hasNextLine()) {
                try {
                    numOfStates = input.nextInt();
                    input.nextLine();
                    int prevState = -1;
                    int prevTranId = -1;
                    Map<Integer, Double> distr = new HashMap<>();
                    Set<DistributionAjaList> set = new HashSet<>();
                    while (input.hasNextLine()) {
                        String line = input.nextLine();
                        String[] splitLine = line.split(" ");
                        if(splitLine.length < 4){
                            break;
                        }
                        int startState = Integer.parseInt(splitLine[0]);
                        int tranId = Integer.parseInt(splitLine[1]);
                        int endState = Integer.parseInt(splitLine[2]);
                        double prob = Double.parseDouble(splitLine[3]);
                        // deal with previous state
                        if (startState == prevState && tranId != prevTranId) {
                            set.add(new DistributionAjaList(distr));
                            distr = new HashMap<>();
                        } else if (prevState != -1 && startState != prevState) {
                            set.add(new DistributionAjaList(distr));
                            trans.put(prevState, new HashSet<>(set));
                            distr = new HashMap<>();
                            set = new HashSet<>();
                        }
                        if (distr.containsKey(endState)) {
                            distr.put(endState, distr.get(endState) + prob);
                        } else {
                            distr.put(endState, prob);
                        }
                        prevState = startState;
                        prevTranId = tranId;
                    }
                    //System.out.println("###"+set.toString());

                    set.add(new DistributionAjaList(distr));
                    trans.put(prevState, set);
                } catch (NoSuchElementException e) {
                    System.out.printf("Input file %s not in the correct format%n", args[0]);
                }

                System.out.println("numOfStates = " + numOfStates);

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

                labels = new int[numOfStates];
                for (int i = 0; i < numOfStates; i++) {
                    if (labelsMap.containsKey(i)) {
                        labels[i] = labelsMap.get(i);
                    } else {
                        labels[i] = -1;
                    }
                }



                //print labels
                //System.out.println("labels: " + Arrays.toString(labels));
                long startTime = System.nanoTime();
                Set<Pair> bisimulationSet = new HashSet<>();
                ProbBisimilarity probBis = new ProbBisimilarity(numOfStates, labels, trans);
                bisimulationSet = probBis.computeProbabilisticBisimilarity();
                long bisimulationTime = System.nanoTime();
                System.out.println("Bisimulation time = " + (bisimulationTime - startTime)/1_000_000 + "ms");
                System.out.println("bisimulationSet size = " + bisimulationSet.size());
                //System.out.println(bisimulationSet);

                DistanceOne distanceOne = new DistanceOne(numOfStates, labels, trans, bisimulationSet);
                Set<Pair> distanceOneSet = distanceOne.getDistanceOneSet();
                long disOneTime = System.nanoTime();
                System.out.println("Distance one time = " + (disOneTime - bisimulationTime)/1_000_000 + "ms");
                System.out.println("Distance one size = " + distanceOneSet.size());
                //System.out.println(distanceOneSet);
            }
        }
    }
}



