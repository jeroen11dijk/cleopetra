package dk.aau.cs.ds306e18.tournament.model;

import dk.aau.cs.ds306e18.tournament.TestUtilities;
import dk.aau.cs.ds306e18.tournament.model.format.StageStatus;
import dk.aau.cs.ds306e18.tournament.model.format.SwissFormat;
import dk.aau.cs.ds306e18.tournament.model.match.Match;
import dk.aau.cs.ds306e18.tournament.model.tiebreaker.TieBreakerBySeed;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SwissFormatTest {

    //Even number of teams
    @Test
    public void calculateMaxRounds01(){

        int numberOfTeams = 4;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        assertEquals(numberOfTeams - 1, bracket.getMaxRoundsPossible());
    }

    //Odd number of teams
    @Test
    public void calculateMaxRounds02(){

        int numberOfTeams = 5;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        assertEquals(numberOfTeams, bracket.getMaxRoundsPossible());
    }

    //No teams
    @Test
    public void calculateMaxRounds03(){

        int numberOfTeams = 0;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        assertEquals(numberOfTeams, bracket.getMaxRoundsPossible());
    }

    //more than 0 matches // one round
    @Test
    public void getAllMatches01(){

        int numberOfTeams = 4;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        List<Match> allMatches = bracket.getAllMatches();

        assertEquals(numberOfTeams/2, allMatches.size());
    }

    //0 matches // one round
    @Test
    public void getAllMatches02(){

        int numberOfTeams = 0;
        int teamSize = 0;

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        List<Match> allMatches = bracket.getAllMatches();

        assertEquals(0, allMatches.size());
    }

    //more than 0 matches // more round
    @Test
    public void getAllMatches03(){

        int numberOfTeams = 12;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        //The first round.
        assertEquals(numberOfTeams/2, bracket.getAllMatches().size());

        //Fill in scores
        List<Match> matches = bracket.getLatestRound();
        for(Match match : matches){
            match.setScores(5, 2, true);
        }

        bracket.startNextRound();

        assertEquals((numberOfTeams/2) * 2, bracket.getAllMatches().size());
    }

    @Test
    public void getPendingMatches01(){

        int numberOfTeams = 4;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        List<Match> unplayedMatches = bracket.getPendingMatches();

        assertEquals(0, unplayedMatches.size());
    }

    //0 matches
    @Test
    public void getPendingMatches02(){

        int numberOfTeams = 0;
        int teamSize = 0;

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        List<Match> unplayedMatches = bracket.getPendingMatches();

        assertEquals(0, unplayedMatches.size());
    }

    @Test
    public void getPendingMatches03(){

        int numberOfTeams = 4;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        //All has to be played
        setAllMatchesPlayed(bracket);

        assertEquals(0 , bracket.getPendingMatches().size());
    }

    @Test
    public void getUpcomingMatches01(){

        int numberOfTeams = 4;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        assertEquals(numberOfTeams/2 , bracket.getUpcomingMatches().size());
    }

    @Test
    public void getUpcomingMatches02(){

        int numberOfTeams = 16;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        assertEquals(numberOfTeams/2 , bracket.getUpcomingMatches().size());
    }

    @Test
    public void getUpcomingMatches03(){

        int numberOfTeams = 1;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        assertEquals(0 , bracket.getUpcomingMatches().size());
    }

    @Test
    public void getCompletedMatches01(){
        int numberOfTeams = 4;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        assertEquals(0 , bracket.getCompletedMatches().size());
    }

    @Test
    public void getCompletedMatches02(){
        int numberOfTeams = 4;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        setAllMatchesPlayed(bracket);

        assertEquals(bracket.getAllMatches().size() , bracket.getCompletedMatches().size());
    }

    //Create round is legal
    @Test
    public void createNewRound01(){

        int numberOfTeams = 4;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        //All has to be played
        setAllMatchesPlayed(bracket);

        assertTrue(bracket.startNextRound());
    }

    //Create round is illegal: max rounds has been created.
    @Test
    public void createNewRound02(){

        int numberOfTeams = 2;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        //All has to be played
        setAllMatchesPlayed(bracket);

        assertFalse(bracket.startNextRound());
    }

    //Create new round is illegal: no matches has been played
    @Test
    public void createNewRound03(){

        int numberOfTeams = 4;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        assertFalse(bracket.startNextRound());
    }

    //No team can play each other more than once
    @Test
    public void createNewRound04(){

        //Create a list of unique teams.
        List<Team> teams = new ArrayList<>();
        for(int i = 0; i < 7; i++)
            teams.add(new Team(String.valueOf(i), TestUtilities.generateBots(2), 0, "Hello"));

        //Create the bracket with the teams
        SwissFormat bracket = new SwissFormat();
        bracket.start(teams, true);

        //Generate all rounds and fill result
        do{
            List<Match> matches = bracket.getUpcomingMatches();
            for(Match match : matches)
                match.setHasBeenPlayed(true);

            bracket.startNextRound();
        }while(!bracket.hasUnstartedRounds());

        List<Match> allMatches = bracket.getAllMatches();

        //Check if no teams has played each other more than once
        for(int i = 0; i < allMatches.size(); i++){

            for(int j = i + 1; j < allMatches.size(); j++){

                Match match1 = allMatches.get(i);
                Match match2 = allMatches.get(j);

                //System.out.println("Match Comp 1: Match1B: " + match1.getBlueTeam().getTeamName() + " Match1O " + match1.getOrangeTeam().getTeamName()
                // + " Match2B " + match2.getBlueTeam().getTeamName() + " Match2O " + match2.getOrangeTeam().getTeamName());

                assertFalse(match1.getBlueTeam().getTeamName().equals(match2.getBlueTeam().getTeamName()) &&
                        match1.getOrangeTeam().getTeamName().equals(match2.getOrangeTeam().getTeamName()));
                assertFalse(match1.getBlueTeam().getTeamName().equals(match2.getOrangeTeam().getTeamName()) &&
                        match1.getOrangeTeam().getTeamName().equals(match2.getBlueTeam().getTeamName()));
            }
        }
    }

    //Create round is illegal: only one team.
    @Test
    public void createNewRound05(){

        int numberOfTeams = 1;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        //All has to be played
        setAllMatchesPlayed(bracket);

        assertFalse(bracket.startNextRound());
    }

    @Test
    public void hasUnstartedRounds01(){

        int numberOfTeams = 2;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        assertFalse(bracket.hasUnstartedRounds());
    }

    @Test
    public void hasUnstartedRounds02(){

        int numberOfTeams = 4;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        assertTrue(bracket.hasUnstartedRounds());
    }

    @Test
    public void hasUnstartedRounds03(){

        int numberOfTeams = 12;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.setRoundCount(1);
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        assertFalse(bracket.hasUnstartedRounds());
    }

    @Test
    public void setRoundCount01() {
        int numberOfTeams = 12;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.setRoundCount(3);
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        assertTrue(bracket.hasUnstartedRounds());
        assertEquals(3, bracket.getRoundCount());
    }

    @Test
    public void setRoundCount02() {
        int numberOfTeams = 8;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.setRoundCount(44444);
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);

        assertTrue(bracket.hasUnstartedRounds());
        assertEquals(7, bracket.getRoundCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setRoundCount03() {
        SwissFormat bracket = new SwissFormat();
        bracket.setRoundCount(-2);
    }

    @Test(expected = IllegalStateException.class)
    public void setRoundCount04() {
        int numberOfTeams = 8;
        int teamSize = 2;

        SwissFormat bracket = new SwissFormat();
        bracket.setRoundCount(3);
        bracket.start(TestUtilities.generateTeams(numberOfTeams, teamSize), true);
        bracket.setRoundCount(2);
    }

    @Test
    public void getStatus01(){ //Pending

        SwissFormat bracket = new SwissFormat();

        assertEquals(StageStatus.PENDING, bracket.getStatus());
    }

    @Test
    public void getStatus02(){ //Running

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(4, 2), true);
        bracket.startNextRound();

        assertEquals(StageStatus.RUNNING, bracket.getStatus());
    }

    @Test
    public void getStatus03(){ //Concluded // max number of rounds and all played

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(2,2), true);
        bracket.startNextRound();

        //Set all matches to played
        setAllMatchesPlayed(bracket);

        assertEquals(StageStatus.CONCLUDED, bracket.getStatus());
    }

    @Test
    public void getStatus04(){ //Concluded //max number of round but not played

        SwissFormat bracket = new SwissFormat();
        bracket.start(TestUtilities.generateTeams(2,2), true);
        bracket.startNextRound();

        assertNotEquals(StageStatus.CONCLUDED, bracket.getStatus());
    }

    @Test
    public void getTopTeams01(){ //No teams

        SwissFormat bracket = new SwissFormat();
        bracket.start(new ArrayList<Team>(), true);

        assertEquals(0, bracket.getTopTeams(10, new TieBreakerBySeed()).size());
    }

    @Test
    public void getTopTeams02(){

        SwissFormat bracket = new SwissFormat();
        ArrayList<Team> inputTeams = TestUtilities.generateTeams(4,2);
        bracket.start(inputTeams, true);

        setAllMatchesPlayed(bracket);
        //All teams now have the same amount of points.

        ArrayList<Team> top3Teams = new ArrayList<>(bracket.getTopTeams(3, new TieBreakerBySeed()));

        //The top teams should be the ones with the lowest seeds

        //Sort the input teams by seed
        Team teamWithHighestSeed = inputTeams.get(0);

        //Find team with highest seed
        for(Team team : inputTeams){
            if(team.getInitialSeedValue() > teamWithHighestSeed.getInitialSeedValue()){
                teamWithHighestSeed = team;
            }
        }

        //Make sure that that team is not a part of the top 3
        for(Team team : top3Teams)
            assertNotSame(team, teamWithHighestSeed);
    }

    /** sets all upcoming matches in the given format to have been played.*/
    private void setAllMatchesPlayed(GroupFormat format){

        //Set all matches to played
        List<Match> matches = format.getUpcomingMatches();
        for(Match match : matches){
            match.setHasBeenPlayed(true);
        }
    }
}