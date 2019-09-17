// Min cost max flow algorithm using an adjacency matrix.  If you
// want just regular max flow, setting all edge costs to 1 gives
// running time O(|E|^2 |V|).
//
// Running time: O(min(|V|^2 * totflow, |V|^3 * totcost))
//
// INPUT: cap -- a matrix such that cap[i][j] is the capacity of
//               a directed edge from node i to node j
//
//        cost -- a matrix such that cost[i][j] is the (positive)
//                cost of sending one unit of flow along a 
//                directed edge from node i to node j
//
//        source -- starting node
//        sink -- ending node
//
// OUTPUT: max flow and min cost; the matrix flow will contain
//         the actual flow values (note that unlike in the MaxFlow
//         code, you don't need to ignore negative flow values -- there
//         shouldn't be any)
//
// To use this, create a MinCostMaxFlow object, and call it like this:
//
//   MinCostMaxFlow nf;
//   int maxflow = nf.getMaxFlow(cap,cost,source,sink);

import java.util.*;

public class MinCostMaxFlow {
    boolean found[];
    int N, dad[];
    double cap[][], flow[][], cost[][], dist[], pi[];

    static final double INF = Double.MAX_VALUE / 2 - 1;

    boolean search(int source, int sink) {
        Arrays.fill(found, false);
        Arrays.fill(dist, INF);
        dist[source] = 0;

        while (source != N) {
            int best = N;
            found[source] = true;
            for (int k = 0; k < N; k++) {
                if (found[k]) continue;
                if (flow[k][source] != 0) {
                    double val = dist[source] + pi[source] - pi[k] - cost[k][source];
                    if (dist[k] > val) {
                        dist[k] = val;
                        dad[k] = source;
                    }
                }
                if (flow[source][k] < cap[source][k]) {
                    double val = dist[source] + pi[source] - pi[k] + cost[source][k];
                    if (dist[k] > val) {
                        dist[k] = val;
                        dad[k] = source;
                    }
                }

                if (dist[k] < dist[best]) best = k;
            }
            source = best;
        }
        for (int k = 0; k < N; k++)
            pi[k] = Math.min(pi[k] + dist[k], INF);
        return found[sink];
    }


    double[] getMaxFlow(double cap[][], double cost[][], int source, int sink) {
        this.cap = cap;
        this.cost = cost;

        N = cap.length;
        found = new boolean[N];
        flow = new double[N][N];
        dist = new double[N + 1];
        dad = new int[N];
        pi = new double[N];

        double totflow = 0, totcost = 0;
        while (search(source, sink)) {
            double amt = INF;
            for (int x = sink; x != source; x = dad[x])
                amt = Math.min(amt, flow[x][dad[x]] != 0 ? flow[x][dad[x]] :
                        cap[dad[x]][x] - flow[dad[x]][x]);
            for (int x = sink; x != source; x = dad[x]) {
                if (flow[x][dad[x]] != 0) {
                    flow[x][dad[x]] -= amt;
                    totcost -= amt * cost[x][dad[x]];
                } else {
                    flow[dad[x]][x] += amt;
                    totcost += amt * cost[dad[x]][x];
                }
            }
            totflow += amt;
        }

        return new double[]{totflow, totcost};
    }

    public static void main(String[] arg) {
        MinCostMaxFlow flow = new MinCostMaxFlow();

        int state = 2;
        double[] dist = new double[state * state];
        dist[1] = 1;
        dist[2] = 1;
        int num = state * 2 + 2;
        double[][] cap = new double[num][num];
        double[][] cost = new double[num][num];

        // Add arcs between the states
        for (int i = 0; i < state; ++i) {
            for (int j = state; j < 2 * state; j++) {
                cap[i][j] = Math.min(0.5, 0.5);
                cost[i][j] = dist[i * state + j - state];
            }
        }

        //add arcs from source
        for (int i = 0; i < state; i++) {
            cap[state * 2][i] = 0.5;
            cap[i + state][state * 2 + 1] = 0.5;
        }

        double[] res = flow.getMaxFlow(cap, cost, state * 2, state * 2 + 1);
        System.out.println(Arrays.toString(res));

    }
}
