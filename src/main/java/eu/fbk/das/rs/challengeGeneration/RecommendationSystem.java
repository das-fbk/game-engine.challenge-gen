package eu.fbk.das.rs.challengeGeneration;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.fbk.das.rs.sortfilter.LeaderboardPosition;
import eu.fbk.das.rs.sortfilter.RecommendationSystemChallengeFilteringAndSorting;
import eu.fbk.das.rs.valuator.RecommendationSystemChallengeValuator;
import eu.trentorise.game.challenges.model.ChallengeDataDTO;
import eu.trentorise.game.challenges.rest.Content;
import eu.trentorise.game.challenges.rest.GamificationEngineRestFacade;
import eu.trentorise.game.challenges.rest.PointConcept;
import eu.trentorise.game.challenges.util.ExcelUtil;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static eu.fbk.das.rs.Utils.*;

/**
 * Recommandation System main class, requires running Gamification Engine in
 * order to run
 */
public class RecommendationSystem {

    private static final Logger logger = LogManager.getLogger(RecommendationSystem.class);

    private GamificationEngineRestFacade facade;
    public RecommendationSystemConfig cfg;
    private Map<String, List<ChallengeDataDTO>> toWriteChallenge = new HashMap<String, List<ChallengeDataDTO>>();
    private RecommendationSystemChallengeGeneration generator;
    private RecommendationSystemChallengeValuator rscv;
    private RecommendationSystemChallengeFilteringAndSorting rscf;
    private RecommendationSystemStatistics stats;

    private String[] configuration = new String[]{"HOST", "GAME_ID", "USERNAME", "PASSWORD", "DATE", "PLAYER_IDS"};

    public RecommendationSystem() {
        this(new RecommendationSystemConfig());
    }

    public RecommendationSystem(RecommendationSystemConfig configuration) {
        this.cfg = configuration;
        generator = new RecommendationSystemChallengeGeneration(configuration);
        rscv = new RecommendationSystemChallengeValuator(configuration);
        rscf = new RecommendationSystemChallengeFilteringAndSorting(
                configuration);
        stats = new RecommendationSystemStatistics(cfg);
        dbg(logger, "Recommendation System init complete");

    }

    public Map<String, List<ChallengeDataDTO>> recommendation() {

        HashMap<String, String> conf = new HashMap<>();
        for (String s : configuration)
            conf.put(s, cfg.get(s));

        return recommendation(conf);
    }


    public Map<String, List<ChallengeDataDTO>> recommendation(Map<String, String> conf) {

        setFacade(new GamificationEngineRestFacade(conf.get("HOST"), conf.get("USERNAME"),
                conf.get("PASSWORD")));

        dbg(logger, "Reading players from game");
        Set<String> allPlayerIds = facade.getGamePlayers(conf.get("GAME_ID"));

        Date date = stringToDate(conf.get("DATE"));
        if (date == null) {
            err(logger, "Invalid date! %s", conf.get("DATE"));
        }

        HashMap<String, double[]> stats = this.stats.checkAndUpdateStats(facade, date, cfg.getDefaultMode());
        rscv.prepare(stats);
        rscf.prepare(stats);

        Map<String, List<ChallengeDataDTO>> challenges = new HashMap<>();

        String playerIds = conf.get("PLAYER_IDS");
        if ("".equals(playerIds)) {
            // generate for all player ids!
            for (String pId : allPlayerIds)
                challenges.put(pId, recommend(pId, date));
        } else {
            // check if given ids exists
            String[] splited = playerIds.split("\\s+");
            for (String pId : splited)
                if (!allPlayerIds.contains(pId))
                    throw new IllegalArgumentException(f("Given player id %d is nowhere to be found in the game", pId));

            for (String pId : splited)
                challenges.put(pId, recommend(pId, date));
        }

        return challenges;
    }

    public void setFacade(GamificationEngineRestFacade fac) {
        this.facade = fac;
    }

    // generate challenges
    public List<ChallengeDataDTO> recommend(String pId, Date d) {

        Content state = facade.getPlayerState(cfg.get("GAME_ID"), pId);
        Map<String, List<ChallengeDataDTO>> m_cha = new HashMap<>();

        List<ChallengeDataDTO> challanges = new ArrayList<>();
        for (String mode : cfg.getDefaultMode()) {
            List<ChallengeDataDTO> l_cha = generator.generate(state, mode, d);

            for (int i = 0; i < l_cha.size(); i++)
                rscv.valuate(l_cha.get(i));

            challanges.addAll(l_cha);

        }

        return rscf.filter(challanges, state, d);
    }

    /**
     * Generate challenges using {@link RecommendationSystemChallengeGeneration}
     * then {@link RecommendationSystemChallengeValuator} and
     * {@link RecommendationSystemChallengeFilteringAndSorting} modules
     *
     * @param start
     * @param end
     * @return a {@link Map} of generated challenges, where key is playerId and
     * value is a {@link List} of {@link ChallengeDataDTO}
     * @throws NullPointerException when data from gamification engine is null
     */
    public Map<String, List<ChallengeDataDTO>> recommendation(Date start, Date end) {

        facade = new GamificationEngineRestFacade(cfg.get("HOST") + cfg.get("CONTEXT"), cfg.get("USERNAME"),
                cfg.get("PASSWORD"));
        dbg(logger, "Reading game data from gamification engine");
        List<Content> gameData = facade.readGameState(cfg.get("GAME_ID"));
        if (gameData == null) {
            throw new NullPointerException(
                    "No game data from Gamification Engine");
        }
        return recommendation(gameData, start, end);
    }

    /**
     * Generate challenges using {@link RecommendationSystemChallengeGeneration}
     * then {@link RecommendationSystemChallengeValuator} and
     * {@link RecommendationSystemChallengeFilteringAndSorting} modules
     *
     * @param gameData game data from gamification engine
     * @param start
     * @param end
     * @return a {@link Map} of generated challenges, where key is playerId and
     * value is a {@link List} of {@link ChallengeDataDTO}
     * @throws NullPointerException when data from gamification engine is null
     */
    public Map<String, List<ChallengeDataDTO>> recommendation(
            List<Content> gameData, Date start, Date end)
            throws NullPointerException {

        logger.info("Recommendation system challenge generation start");
        if (gameData == null) {
            throw new IllegalArgumentException("gameData must be not null");
        }
        List<Content> listofContent = new ArrayList<Content>();
        for (Content c : gameData) {
            if (cfg.isUserfiltering()) {
                if (cfg.getPlayerIds().contains(c.getPlayerId())) {
                    listofContent.add(c);
                }
            } else {
                listofContent.add(c);
            }
        }
        dbg(logger, "Generating challenges");
        Map<String, List<ChallengeDataDTO>> challengeCombinations = generator
                .generate(listofContent, start, end);

        // Map<String, List<ChallengeDataDTO>> evaluatedChallenges = rscv.valuate(challengeCombinations, listofContent);
        Map<String, List<ChallengeDataDTO>> evaluatedChallenges = null;


        // build a leaderboard, for now is the current, to be parameterized for
        // weekly or general leaderboard
        List<LeaderboardPosition> leaderboard = buildLeaderBoard(listofContent);
        Collections.sort(leaderboard);
        int index = 0;
        for (LeaderboardPosition pos : leaderboard) {
            pos.setIndex(index);
            index++;
        }

        // rscf
        dbg(logger, "Filtering challenges");
        Map<String, List<ChallengeDataDTO>> filteredChallenges = rscf
                .filterAndSort(evaluatedChallenges, leaderboard);

        filteredChallenges = rscf.removeDuplicates(filteredChallenges);

        Map<String, Integer> count = new HashMap<String, Integer>();

        // select challenges and avoid duplicate mode
        for (String key : filteredChallenges.keySet()) {
            // upload and assign challenge
            if (count.get(key) == null) {
                count.put(key, 0);
            }
            if (toWriteChallenge.get(key) == null) {
                toWriteChallenge.put(key, new ArrayList<ChallengeDataDTO>());
            }

            // filter used modes
            List<String> usedModes = new ArrayList<String>();

            for (ChallengeDataDTO dto : filteredChallenges.get(key)) {

                if (cfg.isSelecttoptwo()) {
                    if (count.get(key) < 2) {
                        String counter = (String) dto.getData().get(
                                "counterName");
                        if (counter != null && !usedModes.contains(counter)) {
                            usedModes.add(counter);
                            count.put(key, count.get(key) + 1);
                            toWriteChallenge.get(key).add(dto);
                        }
                    } else {
                        break;
                    }
                } else {
                    toWriteChallenge.get(key).add(dto);
                }
            }
        }
        logger.info("Generated challenges " + toWriteChallenge.size());
        return toWriteChallenge;
    }

    /**
     * Build game leaderboard using players green leaves's points
     *
     * @param gameData
     * @return
     */
    private List<LeaderboardPosition> buildLeaderBoard(List<Content> gameData) {
        List<LeaderboardPosition> result = new ArrayList<LeaderboardPosition>();
        for (Content content : gameData) {
            for (PointConcept pc : content.getState().getPointConcept()) {
                if (pc.getName().equals("green leaves")) {
                    Integer score = (int) Math.round(pc
                            .getPeriodCurrentScore("weekly"));
                    result.add(new LeaderboardPosition(score, content
                            .getPlayerId()));
                }
            }
        }
        return result;
    }

    /**
     * Write challenges to xlsx (Excel) file and configuration as json file
     *
     * @param toWriteChallenge
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void writeToFile(Map<String, List<ChallengeDataDTO>> toWriteChallenge)
            throws FileNotFoundException, IOException {
        if (toWriteChallenge == null) {
            throw new IllegalArgumentException(
                    "Impossible to write null or empty challenges");
        }
        // Get the workbook instance for XLS file
        Workbook workbook = new XSSFWorkbook();

        // Get first sheet from the workbook
        Sheet sheet = workbook.createSheet();
        String[] labels = {"PLAYER_ID", "CHALLENGE_TYPE_NAME",
                "CHALLENGE_NAME", "MODE", "MODE_WEIGHT", "DIFFICULTY", "WI",
                "BONUS_SCORE", "BASELINE", "TARGET", "PERCENTAGE"};

        Row header = sheet.createRow(0);
        int i = 0;
        for (String label : labels) {
            header.createCell(i).setCellValue(label);
            i++;
        }
        int rowIndex = 1;

        for (String key : toWriteChallenge.keySet()) {
            for (ChallengeDataDTO dto : toWriteChallenge.get(key)) {
                Row row = sheet.createRow(rowIndex);
                sheet = ExcelUtil.buildRow(cfg, sheet, row, key, dto);
                rowIndex++;
            }
        }

        workbook.write(new FileOutputStream(new File(
                "reportGeneratedChallenges.xlsx")));
        workbook.close();
        logger.info("written reportGeneratedChallenges.xlsx");

        // print configuration
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

        FileOutputStream oout;

        try {
            oout = new FileOutputStream(new File(
                    "recommendationSystemConfiguration.json"));
            IOUtils.write(mapper.writeValueAsString(cfg), oout);

            oout.flush();
            logger.info("written recommendationSystemConfiguration.json");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void prepare(RecommendationSystemConfig cfg, Date date) {

        RecommendationSystemStatistics statistics = new RecommendationSystemStatistics(cfg);
        HashMap<String, double[]> stats = statistics.checkAndUpdateStats(facade, date, cfg.getDefaultMode());

        rscv.prepare(stats);
        rscf.prepare(stats);
    }
}
