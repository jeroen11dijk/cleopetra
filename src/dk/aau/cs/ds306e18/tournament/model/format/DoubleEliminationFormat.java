package dk.aau.cs.ds306e18.tournament.model.format;

import dk.aau.cs.ds306e18.tournament.model.Team;
import dk.aau.cs.ds306e18.tournament.model.TieBreaker;
import dk.aau.cs.ds306e18.tournament.model.match.Match;
import dk.aau.cs.ds306e18.tournament.model.match.MatchPlayedListener;
import dk.aau.cs.ds306e18.tournament.model.match.MatchStatus;
import dk.aau.cs.ds306e18.tournament.ui.BracketOverviewTabController;
import dk.aau.cs.ds306e18.tournament.ui.bracketObjects.DoubleEliminationNode;
import dk.aau.cs.ds306e18.tournament.ui.bracketObjects.ModelCoupledUI;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static dk.aau.cs.ds306e18.tournament.utility.PowMath.log2;
import static dk.aau.cs.ds306e18.tournament.utility.PowMath.pow2;

public class DoubleEliminationFormat implements Format, MatchPlayedListener {

    private StageStatus status = StageStatus.PENDING;
    private int upperBracketRounds;
    private Match finalMatch;
    private Match[] upperBracket;
    private Match[] lowerBracket;

    transient private List<StageStatusChangeListener> statusChangeListeners = new LinkedList<>();

    @Override
    public void start(List<Team> seededTeams, boolean doSeeding) {
        generateBracket(seededTeams, doSeeding);
        status = StageStatus.RUNNING;
        finalMatch.registerMatchPlayedListener(this);
    }

    /** Generates the complete double elimination bracket given a list of teams ordered by seed. If doSeeding is false
     * the teams will just be insert without taking their seed into account. */
    private void generateBracket(List<Team> seededTeams, boolean doSeeding) {
        upperBracketRounds = log2(seededTeams.size());
        generateUpperBracket();
        generateLowerBracket();

        // Create byes
        int teamCount = pow2(upperBracketRounds);
        List<Team> teams = new ArrayList<>(seededTeams);
        List<Team> byes = new ArrayList<>();
        while (byes.size() + teams.size() < teamCount) {
            byes.add(new Team("Bye" + byes.size(), null, 999, ""));
        }
        teams.addAll(byes);

        insertTeams(teams, doSeeding);
        removeByes(byes);
        giveMatchesLabels();
    }

    /** Generates the matches in the upper bracket and connects them. All the matches will be empty. */
    private void generateUpperBracket() {
        int matchesInFirstRound = pow2(upperBracketRounds - 1);
        int numberOfMatches = pow2(upperBracketRounds) - 1;
        upperBracket = new Match[numberOfMatches];
        for(int i = numberOfMatches - 1; i >= 0; i--) {
            // Creates empty matches for first round
            if(i >= numberOfMatches - matchesInFirstRound) {
                upperBracket[i] = new Match();
            }
            // Creates the remaining matches which contains winners from their left- and right child-indexes.
            else {
                upperBracket[i] = new Match()
                        .setBlueToWinnerOf(upperBracket[2 * (i + 1)])
                        .setOrangeToWinnerOf(upperBracket[2 * (i + 1) - 1]);
            }
        }
    }

    /** Generates all the matches in the lower bracket and connects them to the upper bracket matches. Also creates
     * the final match. */
    private void generateLowerBracket() {
        int matchesInCurrentRound = pow2(upperBracketRounds - 2);
        int ubLoserIndex = upperBracket.length - 1;
        List<Match> lowerBracketMatches = new ArrayList<>();

        // For the first lower bracket round, use losers from first round in upper bracket
        for (int i = 0; i < matchesInCurrentRound; i++) {
            lowerBracketMatches.add(new Match()
                    .setBlueToLoserOf(upperBracket[ubLoserIndex--])
                    .setOrangeToLoserOf(upperBracket[ubLoserIndex--]));
        }

        int lbWinnerIndex = 0;
        int rounds = 2 * upperBracketRounds - 2;

        // For the remaining lower bracket rounds, alternate between:
        // - odd rounds: using only winners of prior lower bracket round
        // - even rounds: and using winners from lower bracket against loser from upper bracket.
        for (int i = 1; i < rounds; i++) {

            if (i % 2 == 1) {

                // Odd round
                // Every other odd round we take winners in the opposite order
                int dir = -1;
                if (i % 4 == 1) {
                    ubLoserIndex = ubLoserIndex - matchesInCurrentRound + 1;
                    dir = 1;
                }

                for (int j = 0; j < matchesInCurrentRound; j++) {
                    lowerBracketMatches.add(new Match()
                            .setBlueToWinnerOf(lowerBracketMatches.get(lbWinnerIndex++))
                            .setOrangeToLoserOf(upperBracket[ubLoserIndex]));

                    ubLoserIndex += dir;
                }

                // Reset behaviour for every odd round
                if (i % 4 == 1) {
                    ubLoserIndex = ubLoserIndex - matchesInCurrentRound - 1;
                }

            } else {

                // Odd round
                // The amount of matches is halved in odd rounds as no teams arrive to the loser bracket
                matchesInCurrentRound /= 2;

                for (int j = 0; j < matchesInCurrentRound; j++) {
                    lowerBracketMatches.add(new Match()
                            .setBlueToWinnerOf(lowerBracketMatches.get(lbWinnerIndex++))
                            .setOrangeToWinnerOf(lowerBracketMatches.get(lbWinnerIndex++)));
                }
            }
        }

        // Creates an array of lower bracket matches
        lowerBracket = lowerBracketMatches.toArray(new Match[0]);

        // The final is the winner of upper bracket versus winner of lower bracket
        finalMatch = new Match()
                .setBlueToWinnerOf(upperBracket[0])
                .setOrangeToWinnerOf(lowerBracketMatches.get(lowerBracketMatches.size() - 1));
    }

    /** Inserts the teams in the correct starting positions. If doSeeding is false
     * the teams will just be insert without taking their seed into account. */
    private void insertTeams(List<Team> teams, boolean doSeeding) {

        // Create byes
        int teamCount = pow2(upperBracketRounds);

        // Seeding
        if (doSeeding) {
            teams = Seeding.fairSeedList(teams);
        }

        // Inserting
        int matchIndex = upperBracket.length - 1;
        for (int i = 0; i < teamCount; i += 2, matchIndex--) {
            upperBracket[matchIndex]
                    .setBlue(teams.get(i))
                    .setOrange(teams.get(i + 1));
        }
    }

    /** Removes matches containing byes. */
    private void removeByes(List<Team> byes) {

        // Remove byes from upper bracket
        int ubFirstRoundMatchCount = pow2(upperBracketRounds - 1);
        for (int i = 0; i < ubFirstRoundMatchCount; i++) {
            int ubIndex =  upperBracket.length - i - 1;
            Match m = upperBracket[ubIndex];

            // We know orange is the bye, if there is any
            if (byes.contains(m.getOrangeTeam())) {
                // This is a bye match in upper bracket

                // Resolve match and remove references to this match
                upperBracket[ubIndex] = null;
                m.getWinnerDestination().setBlue(m.getBlueTeam());
                if (m.doesLoserGoToBlue()) {
                    m.getLoserDestination().setBlue(m.getOrangeTeam());
                } else {
                    m.getLoserDestination().setOrange(m.getOrangeTeam());
                }
            }
        }

        // Remove byes from lower bracket
        int lbFirstRoundMatchCount = pow2(upperBracketRounds - 2);
        for (int i = 0; i < lbFirstRoundMatchCount; i++) {
            Match m = lowerBracket[i];
            // Team orange can only be a bye if blue also is a bye, so we test orange first
            if (byes.contains(m.getOrangeTeam())) {
                // Both teams are byes
                // Remove matches from bracket
                lowerBracket[i] = null;
                for (int j = 0; j < lbFirstRoundMatchCount * 2; j++) {
                    if (lowerBracket[j] == m.getWinnerDestination()) {
                        lowerBracket[j] = null;
                        break;
                    }
                }

                // Sets the winner destination of m's winner destination to receive the loser that m's
                // winner destination should have received
                Match wd = m.getWinnerDestination();
                if (wd.doesWinnerGoToBlue()) {
                    wd.getWinnerDestination().setBlueToLoserOf(wd.getOrangeFromMatch());
                } else {
                    wd.getWinnerDestination().setOrangeToLoserOf(wd.getOrangeFromMatch());
                }

            } else if (byes.contains(m.getBlueTeam())) {
                // Only blue is a bye. Orange is the loser of a match in upper bracket.
                // Resolve match and remove references to this match
                lowerBracket[i] = null;
                m.getWinnerDestination().setBlueToLoserOf(m.getOrangeFromMatch());
            }
        }
    }

    /** Gives the matches identifiers. */
    private void giveMatchesLabels() {

        int nextIdentifier = 1;

        // We give upper bracket the small identifiers as those matches always can be played first
        for (int i = upperBracket.length - 1; i >= 0; i--) {
            if (upperBracket[i] != null) {
                upperBracket[i].setIdentifier(nextIdentifier++);
            }
        }
        for (int i = 0; i < lowerBracket.length; i++) {
            if (lowerBracket[i] != null) {
                lowerBracket[i].setIdentifier(nextIdentifier++);
            }
        }
        finalMatch.setIdentifier(nextIdentifier++);
        // TODO give the extra match an identifier
    }

    @Override
    public StageStatus getStatus() {
        return status;
    }

    @Override
    public List<Team> getTopTeams(int count, TieBreaker tieBreaker) {
        // TODO
        return new ArrayList<>();
    }

    @Override
    public List<Match> getAllMatches() {
        return finalMatch.getTreeAsListBFS();
    }

    @Override
    public List<Match> getUpcomingMatches() {
        return getAllMatches().stream().filter(c -> c.getStatus().equals(MatchStatus.READY_TO_BE_PLAYED) && !c.hasBeenPlayed()).collect(Collectors.toList());
    }

    @Override
    public List<Match> getPendingMatches() {
        return getAllMatches().stream().filter(c -> c.getStatus().equals(MatchStatus.NOT_PLAYABLE)).collect(Collectors.toList());
    }

    @Override
    public List<Match> getCompletedMatches() {
        return getAllMatches().stream().filter(Match::hasBeenPlayed).collect(Collectors.toList());
    }

    public Match getFinalMatch() {
        return finalMatch;
    }

    public int getUpperBracketRounds() {
        return upperBracketRounds;
    }

    public Match[] getUpperBracket() {
        return upperBracket;
    }

    public Match[] getLowerBracket() {
        return lowerBracket;
    }

    @Override
    public void registerStatusChangedListener(StageStatusChangeListener listener) {
        statusChangeListeners.add(listener);
    }

    @Override
    public void unregisterStatusChangedListener(StageStatusChangeListener listener) {
        statusChangeListeners.remove(listener);
    }

    /** Let listeners know, that the status has changed */
    private void notifyStatusListeners(StageStatus oldStatus, StageStatus newStatus) {
        for (StageStatusChangeListener listener : statusChangeListeners) {
            listener.onStageStatusChanged(this, oldStatus, newStatus);
        }
    }

    @Override
    public DoubleEliminationNode getBracketFXNode(BracketOverviewTabController bracketOverview) {
        return new DoubleEliminationNode(this, bracketOverview);
    }

    @Override
    public Node getSettingsFXNode() {
        return null;
    }

    /** Repairs match-structure after deserialization. */
    @Override
    public void repair() {
        // TODO
    }

    @Override
    public void onMatchPlayed(Match match) {
        // Was it last match?
        StageStatus oldStatus = status;
        if (finalMatch.hasBeenPlayed()) {
            status = StageStatus.CONCLUDED;
        } else {
            status = StageStatus.RUNNING;
        }

        // Notify listeners if status changed
        if (oldStatus != status) {
            notifyStatusListeners(oldStatus, status);
        }
    }
}