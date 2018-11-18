/**
 * TODO: Have things set up to swap districts around
 *  Need to go through result until nothing has been swapped (after checking all values)
 *  Need to clean up code
 * Might be working now but unlikely
 */

import java.util.ArrayList;
import java.util.Arrays;

import edu.princeton.cs.algs4.Graph;

/**
 * Baseline Gerrymanderer that divides the electorate into vertical stripes (when gerrymandering for the purple party)
 * or horizontal stripes (when gerrymandering for the yellow party).
 */
public class Striper implements Gerrymanderer {
    
    public int numberOfWinningDistricts(int[][] result, Electorate e, boolean party) {
        int numberOfWinningDistricts = 0;
        for (int[] row : result) {
            if (e.winner(row) == party) {
                numberOfWinningDistricts++;
            }
        }
        
        return numberOfWinningDistricts;
    }

    public int[][] swapCheck(int swap, int[][] result, ArrayList<Integer> ourVoters, Electorate e, boolean party) {
        // calculates number of winning districts before moving things around
        int winningDistrictsBefore = numberOfWinningDistricts(result, e, party);
        
        // checks all of the voters in the graph
        for (int voter = 0; voter < Math.pow(e.getNumberOfDistricts(), 2); voter++) {
            if (!ourVoters.contains(voter)) {
                // makes a copy of result so that we don't have an issue with reverting later
                int[][] copy = result.clone();
                
                // replaces the swap value with the other party voter and vice versa
                for (int row[] : copy) {
                    for (int voterInRow : row) {
                        if (voterInRow == voter) {
                            voterInRow = swap;
                        }
                        else if (voterInRow == swap) {
                            voterInRow = voter;
                        }
                    }
                }
                // calculates the number of winners now
                int winningDistrictsAfter = numberOfWinningDistricts(copy, e, party);
                
                // changes result to copy if copy is valid and copy has more winning districts
                if ((e.isValidMap(copy)) && (winningDistrictsBefore < winningDistrictsAfter)) {
                    result = copy;
                }
            }
        }


        return result;
    }

    public int difference(int[] row, ArrayList<Integer> ourVoters) {
        int difference = 0;
        for (int i = 0; i < row.length; i++) {
            if (ourVoters.contains(row[i])) {
                difference++;
            }
            else {
                difference--;
            }
        }
        return difference;
    }

    public int[][] horizontalOrVertical(Electorate e, boolean party) {
        // finds which is more efficient
        int d = e.getNumberOfDistricts();
        int[][] horizontal = new int[d][d], vertical = new int[d][d], result = new int[d][d];

        int i = 0;
        for (int x = 0; x < d; x++) {
            for (int y = 0; y < d; y++) {
                horizontal[x][y] = i;
                vertical[y][x] = i;
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

        return result;
    }

    @Override
    public int[][] gerrymander(Electorate e, boolean party) {
        System.out.println("INPUT: ");
        System.out.println(e.toString());
        System.out.println();

        boolean[] voters = e.getVoters();
        ArrayList<Integer> ourVoters = new ArrayList<>();
        ArrayList<Integer> otherVoters = new ArrayList<>();
        for (int voter = 0; voter < voters.length; voter++) {
            if (voters[voter] == party) {
                ourVoters.add(voter);
            }
            else {
                otherVoters.add(voter);
            }
        }

        int[][] result = horizontalOrVertical(e, party);

        for (int[] row : result) {
            System.out.println(Arrays.toString(row));
        }

        for (int[] row : result) {
            int difference = difference(row, ourVoters);
            if (difference > 1) {
                for (int voter : row) {
                    if (ourVoters.contains(voter)) {
                        // we found one of our extra voters!
                        result = swapCheck(voter, result, ourVoters, e, party);
                    }
                }
            }
        }

        return result;
    }

}
