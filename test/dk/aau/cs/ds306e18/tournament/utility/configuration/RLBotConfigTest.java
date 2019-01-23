package dk.aau.cs.ds306e18.tournament.utility.configuration;

import dk.aau.cs.ds306e18.tournament.model.Bot;
import dk.aau.cs.ds306e18.tournament.model.Team;
import dk.aau.cs.ds306e18.tournament.model.match.Match;
import org.junit.Test;

import java.util.ArrayList;

import static dk.aau.cs.ds306e18.tournament.utility.configuration.ConfigFileEditorTest.testRLBotConfigFilename;
import static dk.aau.cs.ds306e18.tournament.utility.configuration.ConfigFileEditorTest.testDir;
import static junit.framework.TestCase.assertEquals;

public class RLBotConfigTest {

    /**
     * Creates a simple Match with three bots on each team
     * @return created match
     */
    private static Match createTestMatch() {
        Bot blueBot1 = new Bot("Blue1", "BlueDev1", "path/blue/1");
        Bot blueBot2 = new Bot("Blue2", "BlueDev2", "path/blue/2");
        Bot blueBot3 = new Bot("Blue3", "BlueDev3", "path/blue/3");

        ArrayList<Bot> blueBots = new ArrayList<>();
        blueBots.add(blueBot1);
        blueBots.add(blueBot2);
        blueBots.add(blueBot3);

        Team blueTeam = new Team("BlueTeam", blueBots, 0, "BlueDescription");

        Bot orangeBot1 = new Bot("Orange1", "OrangeDev1", "path/orange/1");
        Bot orangeBot2 = new Bot("Orange2", "OrangeDev2", "path/orange/2");
        Bot orangeBot3 = new Bot("Orange3", "OrangeDev3", "path/orange/3");

        ArrayList<Bot> orangeBots = new ArrayList<>();
        orangeBots.add(orangeBot1);
        orangeBots.add(orangeBot2);
        orangeBots.add(orangeBot3);

        Team orangeTeam = new Team("OrangeTeam", orangeBots, 1, "OrangeDescription");

        return new Match(blueTeam, orangeTeam);
    }

    @Test
    public void setupMatchTest() {
        RLBotConfig rlBotConfig = new RLBotConfig(testDir + testRLBotConfigFilename);
        Match match = createTestMatch();
        rlBotConfig.setupMatch(match);

        assertEquals("6", ConfigFileEditor.getValueOfLine("num_participant"));

        // checking correctly set paths
        assertEquals(match.getBlueTeam().getBots().get(0).getConfigPath(),
                ConfigFileEditor.getValueOfLine("participant_config_0"));
        assertEquals(match.getBlueTeam().getBots().get(1).getConfigPath(),
                ConfigFileEditor.getValueOfLine("participant_config_1"));
        assertEquals(match.getBlueTeam().getBots().get(2).getConfigPath(),
                ConfigFileEditor.getValueOfLine("participant_config_2"));

        assertEquals(match.getOrangeTeam().getBots().get(0).getConfigPath(),
                ConfigFileEditor.getValueOfLine("participant_config_3"));
        assertEquals(match.getOrangeTeam().getBots().get(1).getConfigPath(),
                ConfigFileEditor.getValueOfLine("participant_config_4"));
        assertEquals(match.getOrangeTeam().getBots().get(2).getConfigPath(),
                ConfigFileEditor.getValueOfLine("participant_config_5"));

        // checking correctly set team positions
        for (int i = 0; i < match.getBlueTeam().size(); i++) {
            assertEquals("0", ConfigFileEditor.getValueOfLine("participant_team_" + i));
        }

        for (int i = match.getBlueTeam().size(); i < match.getOrangeTeam().size(); i++) {
            assertEquals("1", ConfigFileEditor.getValueOfLine("participant_team_" + i));
        }
    }
}