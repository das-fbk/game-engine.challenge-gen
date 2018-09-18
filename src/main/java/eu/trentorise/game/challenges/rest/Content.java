package eu.trentorise.game.challenges.rest;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "playerId",
        "gameId",
        "state",
        "customData",
        "levels"
})
public class Content {

    @JsonProperty("playerId")
    private String playerId;
    @JsonProperty("gameId")
    private String gameId;
    @JsonProperty("state")
    private State state;
    @JsonProperty("customData")
    private CustomData customData;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    @JsonProperty("levels")
    private List<PlayerLevel> levels;

    /**
     * @return The playerId
     */
    @JsonProperty("playerId")
    public String getPlayerId() {
        return playerId;
    }

    /**
     * @param playerId The playerId
     */
    @JsonProperty("playerId")
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    /**
     * @return The gameId
     */
    @JsonProperty("gameId")
    public String getGameId() {
        return gameId;
    }

    /**
     * @param gameId The gameId
     */
    @JsonProperty("gameId")
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    /**
     * @return The state
     */
    @JsonProperty("state")
    public State getState() {
        return state;
    }

    /**
     * @param state The state
     */
    @JsonProperty("state")
    public void setState(State state) {
        this.state = state;
    }

    /**
     * @return The customData
     */
    @JsonProperty("customData")
    public CustomData getCustomData() {
        return customData;
    }

    /**
     * @param customData The customData
     */
    @JsonProperty("customData")
    public void setCustomData(CustomData customData) {
        this.customData = customData;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
