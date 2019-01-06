package dk.aau.cs.ds306e18.tournament.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dk.aau.cs.ds306e18.tournament.model.Stage;
import dk.aau.cs.ds306e18.tournament.model.Team;
import dk.aau.cs.ds306e18.tournament.model.format.StageStatus;
import dk.aau.cs.ds306e18.tournament.model.Tournament;
import dk.aau.cs.ds306e18.tournament.model.format.Format;
import dk.aau.cs.ds306e18.tournament.model.tiebreaker.TieBreaker;

/**
 * SerializeManager handles all methods associated with serializing and deserializing a Tournament object
 */
public class Serializer {

    private static Gson gson = new GsonBuilder()
            //TODO; enable versioning .setVersion(int)
            .enableComplexMapKeySerialization() // Enabling option to verbosely serialize round-map in SwissFormat
            .registerTypeAdapter(TieBreaker.class, new TiebreakerAdapter()) // Handles Tiebreaker inheritance
            .registerTypeAdapter(Format.class, new FormatAdapter()) // Handles Format inheritance
            .registerTypeAdapter(Team.class, new TeamIdAdapter()) // Stores teams by index based on list in Tournament class
            .create();

    /**
     * Takes a tournament and returns the serialized object as a String
     *
     * @param tournament is the object to be serialized
     * @return JSON-string representation of given tournament parameter
     */
    public static String serialize(Tournament tournament) {
        return gson.toJson(tournament);
    }

    /**
     * Takes a JSON-string representation of a Tournament object, deserializes it, repairs is, and returns it
     *
     * @param json the JSON-string representation
     * @return the reserialized Tournament object
     */
    public static Tournament deserialize(String json) {
        Tournament tournament = gson.fromJson(json, Tournament.class);
        for (Stage stage : tournament.getStages()) {
            if (stage.getFormat().getStatus() != StageStatus.PENDING) {
                stage.getFormat().repair();
            }
        }
        return tournament;
    }
}