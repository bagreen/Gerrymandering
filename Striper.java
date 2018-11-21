import java.util.ArrayList;
import edu.princeton.cs.algs4.Graph;

public class Striper implements Gerrymanderer {

    /**
     * method to find the number of winning districts in a two dimensional array
     * simply runs the method winner in electorate for each district
     * and finds how many have the winner for our party
     *
     * @param districts is our two dimensional array of districts
     * @param electorate is our electorate this is for
     * @param party is the party we want to gerrymander for
     * @return the number of winning districts
     */
    private int numberOfWinningDistricts(int[][] districts, Electorate electorate, boolean party) {
        int numberOfWinningDistricts = 0;
        for (int[] district : districts) {
            if (electorate.winner(district) == party) numberOfWinningDistricts++;
        }
        return numberOfWinningDistricts;
    }

    /**
     * method to try swapping values in districts
     * does so by trying to swap a voter in one district
     * to a voter of a different party in another district
     * and then checks if this new map is valid
     * and if the new map is better than the old one
     *
     * @param ourVoterToSwap voter to be swapped, it will always be
     *             a voter of our party in a district that
     *             has more of our parties voters than we
     *             need for a majority
     * @param districts is our two dimensional array of districts
     * @param adjacentVoters is an arraylist of the voters in the other party
     *                       that are adjacent to our district
     * @param electorate is our electorate this is for
     * @param party is the party we want to gerrymander for
     * @return the new and changed two dimensional array of districts
     */
    private int[][] swap(int ourVoterToSwap, int[][] districts, ArrayList<Integer> adjacentVoters, Electorate electorate, boolean party) {

        // looks through the voters adjacent to this district that are not our voters and tries swapping our extra voters with them
        for (int adjacentVoter : adjacentVoters) {

            // makes a copy of result so that if the change makes an invalid map, we can revert our change
            int[][] changedDistricts = new int[districts.length][districts.length];
            for (int i = 0; i < districts.length; i++) {
                for (int j = 0; j < districts[i].length; j++) {
                    changedDistricts[i][j] = districts[i][j];
                }
            }

            // replaces the ourVoterToSwap value with adjacentVoter and vice versa
            // this way we can try moving people around to different districts
            // and undo the changes as necessary
            // variable found helps by ending the loops if we have found both values
            // this helps us speed up the search by stopping it once both are found
            int found = 0;
            for (int district[] : changedDistricts) {
                if (found == 2) {
                    break;
                }
                else {
                    for (int i = 0; i < district.length; i++) {
                        if (found == 2) {
                            break;
                        }
                        else if (district[i] == adjacentVoter) {
                            district[i] = ourVoterToSwap;
                            found++;
                        }
                        else if (district[i] == ourVoterToSwap) {
                            district[i] = adjacentVoter;
                            found++;
                        }
                    }
                }
            }

            // checks if the new map is valid, and if not the original array of districts will stay the same
            // having two separate loops here is slightly more efficient by avoiding checking
            // the number of winningDistrictsBefore until we know we need to
            if (electorate.isValidMap(changedDistricts)) {

                // calculates number of winning districts before moving things around
                int winningDistrictsBefore = numberOfWinningDistricts(districts, electorate, party);

                // calculates the number of winning districts after the change
                int winningDistrictsAfter = numberOfWinningDistricts(changedDistricts, electorate, party);

                // only changes to the new map if the change helped us gain more districts
                // if not we should not make the change
                if (winningDistrictsAfter > winningDistrictsBefore) {
                    return changedDistricts;
                }
            }
        }

        return districts;
    }

    /**
     * finds if there are extra voters in a row for our party
     * to optimize our gerrymandering, we want to have as slim
     * of a majority as possible when we win each district
     * therefore we do this check to make sure that we are only
     * barely winning each district
     *
     * @param district is each district in our result
     * @param ourVoters is an arraylist of all of the voters in our party
     * @return the difference of voters in our party - voters in other party
     */
    private int difference(int[] district, ArrayList<Integer> ourVoters) {
        int difference = 0;

        // difference goes up if voter is our party, down if they aren't
        // if we have a majority by one, difference = 1
        // if our majority is bigger, then difference > 1
        for (int i = 0; i < district.length; i++) {
            if (ourVoters.contains(district[i])) {
                difference++;
            }
            else {
                difference--;
            }
        }
        return difference;
    }

    /**
     * finds if the beginning districts are better set up horizontally or vertically
     * then, returns the more optimal of the two
     * @param electorate
     * @param party
     * @return
     */
    private int[][] horizontalOrVertical(Electorate electorate, boolean party) {

        // sets up two two dimensional arrays horizontal and vertical
        int d = electorate.getNumberOfDistricts();
        int[][] horizontal = new int[d][d];
        int[][] vertical = new int[d][d];

        // makes horizontal and vertical districts in each two dimensional array
        int i = 0;
        for (int x = 0; x < d; x++) {
            for (int y = 0; y < d; y++) {
                horizontal[x][y] = i;
                vertical[y][x] = i;
                i++;
            }
        }

        // finds the number of winning districts horizontally vs. vertically
        int horizontalWinningDistricts = numberOfWinningDistricts(horizontal, electorate, party);
        int verticalWinningDistricts = numberOfWinningDistricts(vertical, electorate, party);

        // if more districts win horizontally, return that, otherwise return vertical
        if (horizontalWinningDistricts > verticalWinningDistricts) {
            return horizontal;
        }
        else {
            return vertical;
        }
    }

    @Override
    public int[][] gerrymander(Electorate electorate, boolean party) {

        // gets the voter parties for the electorate
        boolean[] voters = electorate.getVoters();

        // gets a map to later check which voters are adjacent to others
        Graph map = electorate.getGraph();

        // creates an arraylist ourVoters that is a list
        // of the voters that vote for our party
        // this makes it easier to check if a voter is ours
        ArrayList<Integer> ourVoters = new ArrayList<>();
        for (int voter = 0; voter < voters.length; voter++) {
            if (voters[voter] == party) ourVoters.add(voter);
        }

        // makes our districts by first picking horizontal or vertical,
        // depending on which is more efficient
        int[][] districts = horizontalOrVertical(electorate, party);

        // looks at districts with extra voters in our party
        // then tries to swap them into another district
        // and change the other party to be swapped into that district
        for (int[] district : districts) {
            // finds if there are more voters than we need in a district
            // we only want to swap voters out of districts where we
            // already have a majority
            if (difference(district, ourVoters) > 1) {

                // want to find all of this district's neighbors and try swapping with them
                ArrayList<Integer> adjacentVoters = new ArrayList<>();
                for (int voter : district) {

                    // only want to try swapping if the adjacent voter is not our party
                    // otherwise, swapping does not make sense
                    for (int adjacent : map.adj(voter)) {
                        if (!ourVoters.contains(adjacent)) {
                            adjacentVoters.add(adjacent);
                        }
                    }
                }

                // now goes through the voters in the district to find the ones that are our party
                // and then tries swapping them with one of our neighbors in the other party
                for (int voter : district) {

                    // if voter is in our party, and our district still has an extra voter
                    // try swapping extra voter in our district with a voter not in our party
                    // that our district is adjacent to
                    if (ourVoters.contains(voter) && (difference(district, ourVoters) > 1)) {
                        districts = swap(voter, districts, adjacentVoters, electorate, party);
                    }
                }
            }
        }

        return districts;
    }

}
