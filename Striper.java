import java.util.Arrays;

import edu.princeton.cs.algs4.Graph;

/**
 * Baseline Gerrymanderer that divides the electorate into vertical stripes (when gerrymandering for the purple party)
 * or horizontal stripes (when gerrymandering for the yellow party).
 */
public class Striper implements Gerrymanderer {



    @Override
    public int[][] gerrymander(Electorate e, boolean party) {
        int d = e.getNumberOfDistricts();
        int[][] horizontal = new int[d][d];
        int[][] vertical = new int[d][d];
        int[][] result = new int[d][d];

        int i = 0;
        for (int x = 0; x < d; x++) {
            for (int y = 0; y < d; y++) {
                if (party) {
                    horizontal[x][y] = i;
                }

                else {
                    vertical[y][x] = i;
                }
                i++;
            }
        }

        int horizontalCheck = 0, verticalCheck = 0;

        for (int[] row : horizontal) {
            if (e.winner(row) == party) {
                horizontalCheck++;
            }
        }
        for (int[] row : vertical) {
            if (e.winner(row) == party) {
                verticalCheck++;
            }
        }

        if (horizontalCheck > verticalCheck) {
            result = horizontal.clone();
        }
        else {
            result = vertical.clone();
        }

        for (int[] row : result) {
            System.out.println(Arrays.toString(row));
        }


        Graph map = e.getGraph();
        System.out.println();
        System.out.println(map.toString());
        System.out.println();


        Graph districtLines = e.graphWithOnlyWithinDistrictLines(result);
        System.out.println(districtLines.toString());

        // swap randomly, check if valid?

        return result;
    }

}
