package dk.aau.cs.ds306e18.tournament.model;

import dk.aau.cs.ds306e18.tournament.model.match.Match;
import dk.aau.cs.ds306e18.tournament.model.match.MatchResultDependencyException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class MatchTest {

    @Test
    public void isReadyToPlay01() {
        Match match = new Match(new Team("A", null, 0, "a"), new Team("B", null, 0, "b"));
        assertTrue(match.isReadyToPlay());
    }

    @Test
    public void isReadyToPlay02() {
        Match firstMatch = new Match(new Team("A", null, 0, "a"), new Team("B", null, 0, "b"));
        Match secondMatch = new Match().setTeamOne(new Team("C", null, 0, "c")).setTeamTwoToWinnerOf(firstMatch);
        secondMatch.setTeamTwoToWinnerOf(firstMatch);
        assertFalse(secondMatch.isReadyToPlay());

        firstMatch.setScores(4, 2, true);
        assertTrue(secondMatch.isReadyToPlay());
    }

    @Test
    public void getWinnerAndLoser01() {
        // team one wins
        Team expectedWinner = new Team("A", null, 0, "a");
        Team expectedLoser = new Team("B", null, 0, "b");
        Match match = new Match(expectedWinner, expectedLoser);
        match.setScores(3, 2, true);
        assertSame(match.getWinner(), expectedWinner);
        assertSame(match.getLoser(), expectedLoser);
    }

    @Test
    public void getWinnerAndLoser02() {
        // team two wins
        Team expectedWinner = new Team("A", null, 0, "a");
        Team expectedLoser = new Team("B", null, 0, "b");
        Match match = new Match(expectedLoser, expectedWinner);
        match.setScores(3, 5, true);
        assertSame(match.getWinner(), expectedWinner);
        assertSame(match.getLoser(), expectedLoser);
    }

    @Test(expected = IllegalStateException.class)
    public void getWinnerAndLoser03() {
        Match match = new Match(new Team("A", null, 0, "a"), new Team("B", null, 0, "b"));
        match.setScores(3, 5); // note: match is not finished
        match.getWinner();
    }

    @Test(expected = IllegalStateException.class)
    public void getWinnerAndLoser04() {
        Match match = new Match(new Team("A", null, 0, "a"), new Team("B", null, 0, "b"));
        match.setScores(3, 5); // note: match is not finished
        match.getLoser();
    }

    @Test
    public void getStatus01() {
        Match firstMatch = new Match(new Team("A", null, 0, "a"), new Team("B", null, 0, "b"));
        Match secondMatch = new Match().setTeamOne(new Team("C", null, 0, "c")).setTeamTwoToWinnerOf(firstMatch);
        assertSame(secondMatch.getStatus(), Match.Status.NOT_PLAYABLE);
    }

    @Test
    public void getStatus02() {
        Match firstMatch = new Match(new Team("A", null, 0, "a"), new Team("B", null, 0, "b"));
        Match secondMatch = new Match().setTeamOne(new Team("C", null, 0, "c")).setTeamTwoToWinnerOf(firstMatch);
        firstMatch.setScores(0, 2, true);
        assertSame(secondMatch.getStatus(), Match.Status.READY_TO_BE_PLAYED);
    }

    @Test
    public void getStatus03() {
        Match match = new Match(new Team("A", null, 0, "a"), new Team("B", null, 0, "b"));
        match.setScores(0, 0, true);
        assertSame(match.getOutcome(), Match.Outcome.DRAW);
    }


    @Test
    public void getStatus04() {
        Match match = new Match(new Team("A", null, 0, "a"), new Team("B", null, 0, "b"));
        match.setScores(2, 0, true);
        assertSame(match.getOutcome(), Match.Outcome.TEAM_ONE_WINS);
    }

    @Test
    public void getStatus05() {
        Match match = new Match(new Team("A", null, 0, "a"), new Team("B", null, 0, "b"));
        match.setScores(0, 2, true);
        assertSame(match.getOutcome(), Match.Outcome.TEAM_TWO_WINS);
    }

    @Test
    public void dependsOn01() {
        Match firstMatch = new Match(new Team("A", null, 0, "a"), new Team("B", null, 0, "b"));
        Match secondMatch = new Match().setTeamOne(new Team("C", null, 0, "c")).setTeamTwoToWinnerOf(firstMatch);
        assertTrue(secondMatch.dependsOn(firstMatch));
    }

    @Test
    public void dependsOn02() {
        Match firstMatch = new Match(new Team("A", null, 0, "a"), new Team("B", null, 0, "b"));
        Match secondMatch = new Match().setTeamOne(new Team("C", null, 0, "c")).setTeamTwoToLoserOf(firstMatch);
        assertTrue(secondMatch.dependsOn(firstMatch));
    }

    @Test
    public void dependsOn03() {
        Match firstMatch = new Match(new Team("A", null, 0, "a"), new Team("B", null, 0, "b"));
        Match secondMatch = new Match().setTeamOne(new Team("C", null, 0, "c")).setTeamTwoToWinnerOf(firstMatch);
        assertFalse(firstMatch.dependsOn(secondMatch));
    }

    @Test
    public void dependsOn04() {
        Match firstMatch = new Match(new Team("A", null, 0, "a"), new Team("B", null, 0, "b"));
        Match secondMatch = new Match().setTeamOne(new Team("C", null, 0, "c")).setTeamTwoToLoserOf(firstMatch);
        assertFalse(firstMatch.dependsOn(secondMatch));
    }

    @Test
    public void dependsOn05() {
        Match firstMatch = new Match(new Team("A", null, 0, "a"), new Team("B", null, 0, "b"));
        Match secondMatch = new Match(new Team("C", null, 0, "c"), new Team("D", null, 0, "d"));
        Match thirdMatch = new Match().setTeamOneToWinnerOf(firstMatch).setTeamTwoToLoserOf(secondMatch);

        assertFalse(firstMatch.dependsOn(secondMatch));
        assertFalse(secondMatch.dependsOn(firstMatch));

        assertTrue(thirdMatch.dependsOn(firstMatch));
        assertTrue(thirdMatch.dependsOn(secondMatch));
    }

    @Test
    public void getTreeAsListBFS01() {
        Match firstMatch = new Match(new Team("A", null, 0, "a"), new Team("B", null, 0, "b"));
        Match secondMatch = new Match(new Team("C", null, 0, "c"), new Team("D", null, 0, "d"));
        Match thirdMatch = new Match(new Team("E", null, 0, "e"), new Team("F", null, 0, "f"));
        Match fourthMatch = new Match(new Team("G", null, 0, "g"), new Team("H", null, 0, "h"));
        Match firstSemiFinal = new Match().setTeamOneToWinnerOf(firstMatch).setTeamTwoToWinnerOf(secondMatch);
        Match secondSemiFinal = new Match().setTeamOneToWinnerOf(thirdMatch).setTeamTwoToWinnerOf(fourthMatch);
        Match finalMatch = new Match().setTeamOneToWinnerOf(firstSemiFinal).setTeamTwoToWinnerOf(secondSemiFinal);

        List<Match> bfs = finalMatch.getTreeAsListBFS();

        assertSame(finalMatch, bfs.get(0));
        assertSame(secondSemiFinal, bfs.get(1));
        assertSame(firstSemiFinal, bfs.get(2));
        assertSame(fourthMatch, bfs.get(3));
        assertSame(thirdMatch, bfs.get(4));
        assertSame(secondMatch, bfs.get(5));
        assertSame(firstMatch, bfs.get(6));
    }

    @Test
    public void getTreeAsListBFS02() {
        Match firstMatch = new Match(new Team("A", null, 0, "a"), new Team("B", null, 0, "b"));
        Match secondMatch = new Match().setTeamOneToWinnerOf(firstMatch).setTeamTwo(new Team("C", null, 0, "c"));
        Match thirdMatch = new Match().setTeamOneToWinnerOf(secondMatch).setTeamTwo(new Team("D", null, 0, "d"));

        List<Match> bfs = thirdMatch.getTreeAsListBFS();

        assertSame(thirdMatch, bfs.get(0));
        assertSame(secondMatch, bfs.get(1));
        assertSame(firstMatch, bfs.get(2));
        assertEquals(3, bfs.size());
    }

    @Test
    public void reconnect01() {
        Team expectedWinner = new Team("A", null, 0, "a");
        Match matchOne = new Match(expectedWinner, new Team("B", null, 0, "b"));
        Match matchTwo = new Match().setTeamOne(new Team("C", null, 0, "c")).setTeamTwoToWinnerOf(matchOne);
        matchOne.setScores(4, 2, true);

        assertSame(expectedWinner, matchTwo.getTeamTwo());

        // change match one winner now goes to third match instead
        Match matchThree = new Match().setTeamOne(new Team("D", null, 0, "d")).setTeamTwoToWinnerOf(matchOne);

        assertFalse(matchTwo.dependsOn(matchOne));
        assertNull(matchTwo.getTeamTwo());
        assertSame(expectedWinner, matchThree.getTeamTwo());
    }

    @Test
    public void reconnect02() {
        Team expectedWinner = new Team("A", null, 0, "a");
        Match matchOne = new Match(expectedWinner, new Team("B", null, 0, "b"));
        Match matchTwo = new Match().setTeamOne(new Team("C", null, 0, "c")).setTeamTwoToWinnerOf(matchOne);
        matchOne.setScores(4, 2, true);

        assertSame(expectedWinner, matchTwo.getTeamTwo());

        // change match two's team two to be winner of match three
        Match matchThree = new Match(new Team("D", null, 0, "d"), new Team("E", null, 0, "e"));
        matchTwo.setTeamTwoToWinnerOf(matchThree);

        assertFalse(matchTwo.dependsOn(matchOne));
        assertTrue(matchTwo.dependsOn(matchThree));
        assertNull(matchTwo.getTeamTwo());
    }

    @Test
    public void reconnect03() {
        Team expectedLoser = new Team("A", null, 0, "a");
        Match matchOne = new Match(expectedLoser, new Team("B", null, 0, "b"));
        Match matchTwo = new Match().setTeamOne(new Team("C", null, 0, "c")).setTeamTwoToLoserOf(matchOne);
        matchOne.setScores(0, 3, true);

        assertSame(expectedLoser, matchTwo.getTeamTwo());

        // change match one loser now goes to third match instead
        Match matchThree = new Match().setTeamOne(new Team("D", null, 0, "d")).setTeamTwoToLoserOf(matchOne);

        assertFalse(matchTwo.dependsOn(matchOne));
        assertNull(matchTwo.getTeamTwo());
        assertSame(expectedLoser, matchThree.getTeamTwo());
    }

    @Test
    public void reconnect04() {
        Team expectedLoser = new Team("A", null, 0, "a");
        Match matchOne = new Match(expectedLoser, new Team("B", null, 0, "b"));
        Match matchTwo = new Match().setTeamOne(new Team("C", null, 0, "c")).setTeamTwoToLoserOf(matchOne);
        matchOne.setScores(0, 3, true);

        assertSame(expectedLoser, matchTwo.getTeamTwo());

        // change match two's team two to be loser of match three
        Match matchThree = new Match(new Team("D", null, 0, "d"), new Team("E", null, 0, "e"));
        matchTwo.setTeamTwoToLoserOf(matchThree);

        assertFalse(matchTwo.dependsOn(matchOne));
        assertTrue(matchTwo.dependsOn(matchThree));
        assertNull(matchTwo.getTeamTwo());
    }

    @Test
    public void setScores01(){

        Team teamOne = new Team("A", null, 1, null);
        Team teamTwo = new Team("B", null, 1, null);
        Match match = new Match(teamOne, teamTwo);

        int teamOneScore = 5;
        int teamTwoScore = 2;
        match.setScores(teamOneScore, teamTwoScore, true);

        assertEquals(teamOneScore, match.getTeamOneScore());
        assertEquals(teamTwoScore, match.getTeamTwoScore());
    }

    @Test
    public void setScores02(){

        Team teamOne = new Team(null, null, 1, null);
        Team teamTwo = new Team(null, null, 1, null);
        Match match = new Match(teamOne, teamTwo);

        int teamOneScore1 = 5;
        int teamTwoScore1 = 2;
        int teamOneScore2 = 2;
        int teamTwoScore2 = 2;
        match.setScores(teamOneScore1, teamTwoScore1, true);
        match.setScores(teamOneScore2, teamTwoScore2, true);

        assertEquals(teamOneScore2, match.getTeamOneScore());
        assertEquals(teamTwoScore2, match.getTeamTwoScore());
    }

    @Test
    public void reconnectAfterPlay01() {
        Team tA = new Team("A", null, 0, "a");
        Team tE = new Team("E", null, 0, "e");
        Match matchOne = new Match(tA, new Team("B", null, 0, "b"));
        Match matchTwo = new Match().setTeamOne(new Team("C", null, 0, "c")).setTeamTwoToWinnerOf(matchOne);
        Match matchThree = new Match(tE, new Team("D", null, 0, "d"));

        // Team A and E wins
        matchOne.setScores(1, 0, true);
        matchThree.setScores(1, 0, true);

        // Match two's team two should be A
        assertSame(tA, matchTwo.getTeamTwo());

        // Match two's team two should be E, if we change it to be winner of match three
        matchTwo.setTeamTwoToWinnerOf(matchThree);
        assertSame(tE, matchTwo.getTeamTwo());
    }

    @Test
    public void reset01() {
        Team teamA = new Team("A", null, 0, "a");
        Team teamB = new Team("B", null, 0, "b");
        Team teamC = new Team("C", null, 0, "c");

        Match matchOne = new Match(teamA, teamB);
        Match matchTwo = new Match().setTeamOne(teamC).setTeamTwoToWinnerOf(matchOne);
        matchOne.setScores(2, 1, true);
        matchTwo.setScores(4, 3, false);

        assertEquals(2, matchOne.getTeamOneScore());
        assertEquals(1, matchOne.getTeamTwoScore());
        assertEquals(4, matchTwo.getTeamOneScore());
        assertEquals(3, matchTwo.getTeamTwoScore());

        // This forced reset should remove all scores. Also in match two that has not been played
        matchOne.setScores(0, 0, false, true);

        assertEquals(0, matchOne.getTeamOneScore());
        assertEquals(0, matchOne.getTeamTwoScore());
        assertEquals(0, matchTwo.getTeamOneScore());
        assertEquals(0, matchTwo.getTeamTwoScore());
    }

    @Test(expected = MatchResultDependencyException.class)
    public void reset02() {
        Team teamA = new Team("A", null, 0, "a");
        Team teamB = new Team("B", null, 0, "b");
        Team teamC = new Team("C", null, 0, "c");

        Match matchOne = new Match(teamA, teamB);
        Match matchTwo = new Match().setTeamOne(teamC).setTeamTwoToWinnerOf(matchOne);
        matchOne.setScores(2, 1, true);
        matchTwo.setScores(4, 3, false);

        assertEquals(2, matchOne.getTeamOneScore());
        assertEquals(1, matchOne.getTeamTwoScore());
        assertEquals(4, matchTwo.getTeamOneScore());
        assertEquals(3, matchTwo.getTeamTwoScore());

        // This reset should not be legal as match two has entered scores
        matchOne.setScores(0, 0, false, false);
    }

    @Test(expected = MatchResultDependencyException.class)
    public void reset03() {
        Team teamA = new Team("A", null, 0, "a");
        Team teamB = new Team("B", null, 0, "b");
        Team teamC = new Team("C", null, 0, "c");

        Match matchOne = new Match(teamA, teamB);
        Match matchTwo = new Match().setTeamOne(teamC).setTeamTwoToWinnerOf(matchOne);
        matchOne.setScores(2, 1, true);
        matchTwo.setScores(4, 3, true);

        assertEquals(2, matchOne.getTeamOneScore());
        assertEquals(1, matchOne.getTeamTwoScore());
        assertEquals(4, matchTwo.getTeamOneScore());
        assertEquals(3, matchTwo.getTeamTwoScore());

        // This reset should not be legal as match two has been played
        matchOne.setScores(0, 0, false, false);
    }

    @Test
    public void reset04() {
        Team teamA = new Team("A", null, 0, "a");
        Team teamB = new Team("B", null, 0, "b");
        Team teamC = new Team("C", null, 0, "c");

        Match matchOne = new Match(teamA, teamB);
        Match matchTwo = new Match().setTeamOne(teamC).setTeamTwoToWinnerOf(matchOne);
        matchOne.setScores(2, 1, true);
        matchTwo.setScores(4, 3, false);

        assertEquals(2, matchOne.getTeamOneScore());
        assertEquals(1, matchOne.getTeamTwoScore());
        assertEquals(4, matchTwo.getTeamOneScore());
        assertEquals(3, matchTwo.getTeamTwoScore());

        // This reset should be legal as it doesn't change the outcome of match one
        matchOne.setScores(4, 2, true, false);

        assertEquals(4, matchOne.getTeamOneScore());
        assertEquals(2, matchOne.getTeamTwoScore());
        assertEquals(4, matchTwo.getTeamOneScore());
        assertEquals(3, matchTwo.getTeamTwoScore());
    }

    @Test
    public void reset05() {
        Team teamA = new Team("A", null, 0, "a");
        Team teamB = new Team("B", null, 0, "b");
        Team teamC = new Team("C", null, 0, "c");

        Match matchOne = new Match(teamA, teamB);
        Match matchTwo = new Match().setTeamOne(teamC).setTeamTwoToWinnerOf(matchOne);
        matchOne.setScores(2, 1, true);
        matchTwo.setScores(4, 3, true);

        assertEquals(2, matchOne.getTeamOneScore());
        assertEquals(1, matchOne.getTeamTwoScore());
        assertEquals(4, matchTwo.getTeamOneScore());
        assertEquals(3, matchTwo.getTeamTwoScore());

        // This reset should be legal as it doesn't change the outcome of match one, even when match two has been played
        matchOne.setScores(4, 2, true, false);

        assertEquals(4, matchOne.getTeamOneScore());
        assertEquals(2, matchOne.getTeamTwoScore());
        assertEquals(4, matchTwo.getTeamOneScore());
        assertEquals(3, matchTwo.getTeamTwoScore());
    }

    @Test
    public void identifier01() {
        Team teamA = new Team("A", null, 0, "a");
        Team teamB = new Team("B", null, 0, "b");
        Team teamC = new Team("C", null, 0, "c");
        Team teamD = new Team("D", null, 0, "d");

        Match matchOne = new Match(teamA, teamB);
        Match matchTwo = new Match().setTeamOne(teamC).setTeamTwoToWinnerOf(matchOne);
        Match matchThree = new Match().setTeamTwo(teamD).setTeamOneToLoserOf(matchOne);

        matchOne.setIdentifier(1);
        matchTwo.setIdentifier(2);
        matchThree.setIdentifier(3);

        assertEquals("Winner of 1", matchTwo.getTeamTwoAsString());
        assertEquals("C", matchTwo.getTeamOneAsString());
        assertEquals("Loser of 1", matchThree.getTeamOneAsString());
        assertEquals("D", matchThree.getTeamTwoAsString());
    }

    @Test
    public void switchingColors01() {
        Team teamA = new Team("A", null, 0, "a");
        Team teamB = new Team("B", null, 0, "b");

        Match match = new Match(teamA, teamB);

        assertSame(teamA, match.getTeamOne());
        assertSame(teamB, match.getTeamTwo());
        assertSame(teamA, match.getBlueTeam());
        assertSame(teamB, match.getOrangeTeam());

        match.setTeamOneToBlue(false);

        assertSame(teamA, match.getTeamOne());
        assertSame(teamB, match.getTeamTwo());
        assertSame(teamB, match.getBlueTeam());
        assertSame(teamA, match.getOrangeTeam());
    }

    @Test
    public void switchingColors02() {
        Team teamA = new Team("A", null, 0, "a");
        Team teamB = new Team("B", null, 0, "b");

        Match match = new Match(teamA, teamB);
        match.setScores(2, 0, true);

        assertSame(2, match.getTeamOneScore());
        assertSame(0, match.getTeamTwoScore());
        assertSame(2, match.getBlueScore());
        assertSame(0, match.getOrangeScore());

        match.setTeamOneToBlue(false);

        assertEquals(2, match.getTeamOneScore());
        assertEquals(0, match.getTeamTwoScore());
        assertEquals(0, match.getBlueScore());
        assertEquals(2, match.getOrangeScore());
    }

    @Test
    public void switchingColors03() {
        Team teamA = new Team("A", null, 0, "a");
        Team teamB = new Team("B", null, 0, "b");

        Match match = new Match(teamA, teamB);
        match.setScores(2, 0, true);

        assertEquals("A", match.getTeamOneAsString());
        assertEquals("B", match.getTeamTwoAsString());
        assertEquals("A", match.getBlueTeamAsString());
        assertEquals("B", match.getOrangeTeamAsString());

        match.setTeamOneToBlue(false);

        assertEquals("A", match.getTeamOneAsString());
        assertEquals("B", match.getTeamTwoAsString());
        assertEquals("B", match.getBlueTeamAsString());
        assertEquals("A", match.getOrangeTeamAsString());
    }
}