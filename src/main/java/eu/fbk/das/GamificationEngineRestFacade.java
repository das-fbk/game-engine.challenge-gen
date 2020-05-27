
package eu.fbk.das;

import it.smartcommunitylab.ApiClient;
import it.smartcommunitylab.ApiException;
import it.smartcommunitylab.basic.api.GameControllerApi;
import it.smartcommunitylab.basic.api.PlayerControllerApi;
import it.smartcommunitylab.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.threeten.bp.Instant;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;
import java.util.*;

import static eu.fbk.das.rs.utils.Utils.*;

public class GamificationEngineRestFacade {

    // API: https://dev.smartcommunitylab.it/gamification/swagger-ui.html

    private static final Logger logger = LogManager
            .getLogger(GamificationEngineRestFacade.class);

    private final PlayerControllerApi playerApi;
    private final GameControllerApi gameApi;

    private final HashMap<String, Map<String, PlayerStateDTO>> gameStateCache;

    public GamificationEngineRestFacade(final String endpoint, final String username, final String password) {
        ApiClient client = new ApiClient(endpoint);
        client.setUsername(username);
        client.setPassword(password);

        playerApi = new PlayerControllerApi(client);
        gameApi = new GameControllerApi(client);

        gameStateCache = new HashMap<>();
    }

    public PlayerStateDTO getPlayerState(String gameId, String pId) {

        checkGameId(gameId);
        checkPlayerId(pId);

        Map<String, PlayerStateDTO> contentCache = getGameCache(gameId);

        if (!contentCache.containsKey(pId)) {
            try {
                PlayerStateDTO state = playerApi.readStateUsingGET(gameId, pId);
                contentCache.put(pId, state);
            } catch (Exception e) {
                apiErr(e);
                return null;
            }
        }

        return contentCache.get(pId);
    }

    private Map<String, PlayerStateDTO> getGameCache(String gameId) {
        if (!gameStateCache.containsKey(gameId))
            gameStateCache.put(gameId, new HashMap<>());

        return gameStateCache.get(gameId);
    }

    private void checkPlayerId(String pId) {
        if (pId == null) {
            throw new IllegalArgumentException("playerId cannot be null");
        }
    }

    private void checkGameId(String gameId) {
        if (gameId == null || "".equals(gameId)) {
            throw new IllegalArgumentException("gameId cannot be null");
        }
    }

    /**
     * Gets the list of playerIds in the given game
     *
     * @param gameId id of the game
     * @return list of playerIds
     */
    public Set<String> getGamePlayers(String gameId) {
        checkGameId(gameId);

        Set<String> players = new TreeSet<>();
        String size = "100";

        Projection p = new Projection();
        p.addIncludeItem("playerId");
        RawSearchQuery rw = new RawSearchQuery();
        rw.setProjection(p);
        WrapperQuery q = new WrapperQuery();
        q.setRawQuery(rw);

        try {
            PagePlayerStateDTO result = playerApi.searchByQueryUsingPOST(gameId,  q,"1", size);
            int totPages = result.getTotalPages();
            addAllPlayers(players, result.getContent());
            for (int i = 1; i < totPages; i++) {
                result = playerApi.searchByQueryUsingPOST(gameId, q, Integer.toString(i), size);
                addAllPlayers(players, result.getContent());
            }

        } catch (Exception e) {
            apiErr(e);
            return null;
        }

        return players;
    }

    private void addAllPlayers(Set<String> players, List<PlayerStateDTO> content) {
        // Map<String, PlayerStateDTO> contentCache = getGameCache(gameId);

        for (PlayerStateDTO st: content) {
            String playerId = st.getPlayerId();
            players.add(playerId);
           //  contentCache.put(playerId, st);
        }
    }

    private void apiErr(Exception e) {
        p("ERRORE NELL'ESECUZIONE DI UNA API");
        logger.error(e);
    }

    public boolean assignChallengeToPlayer(ChallengeAssignmentDTO cdd, String gameId, String playerId) {
        if (cdd == null || gameId == null || playerId == null) {
            throw new IllegalArgumentException("challenge, gameId and playerId cannot be null");
        }

        try {
            playerApi.assignChallengeUsingPOST(cdd, gameId, playerId);
        } catch (ApiException e) {
            apiErr(e);
            return false;
        }

        return true;
    }


    public boolean assignGroupChallenge(GroupChallengeDTO cdd, String gameId) {
        if (cdd == null || gameId == null) {
            throw new IllegalArgumentException("challenge, gameId and playerId cannot be null");
        }

        try {
            playerApi.assignGroupChallengeUsingPOST(cdd, gameId);
        } catch (ApiException e) {
            apiErr(e);
            return false;
        }

        return true;
    }

    public List<ChallengeConcept> getChallengesPlayer(String gameId, String playerId) {
        if (gameId == null || playerId == null) {
            throw new IllegalArgumentException("challenge, gameId and playerId cannot be null");
        }

        try {
            return playerApi.getPlayerChallengeUsingGET(gameId, playerId);
        } catch (ApiException e) {
            apiErr(e);
            return null;
        }
    }

    public List<GameStatistics> readGameStatistics(String gameId, DateTime timestamp, String pcName) {
        checkGameId(gameId);
        List<GameStatistics> res;

        try {
            res = gameApi.readGameStatisticsUsingGET(gameId, pcName, "weekly", timestamp.getMillis(), "", -1, -1);
        } catch (ApiException e) {
            apiErr(e);
            return null;
        }

        return res;
    }

    public List<GameStatistics> readGameStatistics(String gameId, DateTime timestamp) {
        return readGameStatistics(gameId, timestamp, "");
    }
    
    public GroupChallengeDTO makeGroupChallengeDTO(String mode, String counter, String pId1, String pId2, DateTime start, DateTime end, Map<String, Double> res) {

        GroupChallengeDTO gcd = new GroupChallengeDTO();
        gcd.setChallengeModelName(mode);

        AttendeeDTO a1 = new AttendeeDTO();
        a1.setPlayerId(pId1);
        a1.setRole("GUEST");
        gcd.addAttendeesItem(a1);

        AttendeeDTO a2 = new AttendeeDTO();
        a2.setPlayerId(pId2);
        a2.setRole("GUEST");
        gcd.addAttendeesItem(a2);

        gcd.setChallengeTarget(res.get("target"));

        PointConceptDTO cpc = new PointConceptDTO();
        cpc.setName(counter);
        cpc.setPeriod("weekly");
        gcd.setChallengePointConcept(cpc);

        RewardDTO r = new RewardDTO();
        Map<String, Double> bonusScore = new HashMap<>();
        bonusScore.put(pId1, res.get("player1_prz"));
        bonusScore.put(pId2, res.get("player2_prz"));
        r.setBonusScore(bonusScore);
        gcd.setReward(r);

        gcd.setOrigin("gca");
        gcd.setState("ASSIGNED");

        gcd.setStart(jodaToOffset(start));
        gcd.setEnd(jodaToOffset(end));

        return gcd;
    }

    public static OffsetDateTime jodaToOffset(DateTime dt) {
        long millis = dt.getMillis();
        // java.time.Instant
        Instant instant = Instant.ofEpochMilli(millis);

        // get total offset (joda returns milliseconds, java.time takes seconds)
        int offsetSeconds = dt.getZone().getOffset(millis) / 1000;
        return OffsetDateTime.ofInstant(instant, ZoneOffset.ofTotalSeconds(offsetSeconds));
    }

    public Map<String, Object> getCustomDataPlayer(String gameId, String pId) {
        try {
            return playerApi.readCustomDataUsingGET(gameId, pId);
        } catch (ApiException e) {
            apiErr(e);
            return null;
        }
    }

    public void setCustomDataPlayer(String gameId, String pId, Map<String, Object> cs) {
        try {
            playerApi.updateCustomDataUsingPUT1(gameId, pId, cs);
        } catch (ApiException e) {
            apiErr(e);
        }
    }

    /*
    public Map<String, Object> getCustomDataPlayer(String gameId, String playerId) {
        if (gameId == null || playerId == null) {
            throw new IllegalArgumentException("challenge, gameId and playerId cannot be null");
        }

        try {
            res = playerApi.read.readGameStatisticsUsingGET(gameId, pcName, "weekly", timestamp.getMillis(), "", -1, -1);
        } catch (ApiException e) {
            apiErr(e);
            return null;
        }

        return res;
    }

    public boolean setCustomDataPlayer(String gameId, String playerId, Map<String, Object> cs) {
        if (gameId == null || playerId == null) {
            throw new IllegalArgumentException("challenge, gameId and playerId cannot be null");
        }
        WebTarget target = getCustomDataPath(gameId, playerId);
        Response response = put(target, cs);
        return response != null;
    }*/

}

