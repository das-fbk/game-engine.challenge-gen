package eu.trentorise.challenge.rest;

import eu.trentorise.game.challenges.api.Constants;
import eu.trentorise.game.challenges.rest.*;
import eu.trentorise.game.challenges.rest.PointConcept.PeriodInstanceImpl;
import eu.trentorise.game.challenges.util.CalendarUtil;
import eu.trentorise.game.challenges.util.ConverterUtil;
import eu.trentorise.game.challenges.util.ExcelUtil;
import eu.trentorise.game.challenges.util.JourneyData;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static eu.trentorise.challenge.PropertiesUtil.*;
import static org.junit.Assert.assertTrue;

public class RestTest {

    private static final Logger logger = LogManager.getLogger(RestTest.class);

    private GamificationEngineRestFacade facade;
    private GamificationEngineRestFacade insertFacade;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss, zzz ZZ");

    @Before
    public void setup() {
        facade = new GamificationEngineRestFacade(get(HOST) + get(CONTEXT), get(USERNAME),
                get(PASSWORD));
        insertFacade = new GamificationEngineRestFacade(get(HOST) + get(INSERT_CONTEXT),
                get(USERNAME), get(PASSWORD));
    }

    @Test
    public void gameReadGameStateTest() {
        List<Content> result = facade.readGameState(get(GAMEID));
        assertTrue(!result.isEmpty());
    }

    @Test
    public void gameReadGameStateTest1() {
        List<Content> result = facade.readGameState(get(GAMEID));
        assertTrue(!result.get(0).getState().getBadgeCollectionConcept().isEmpty());
    }

    @Test
    public void gameInsertRuleTest() {
        // define rule
        InsertedRuleDto rule = new InsertedRuleDto();
        rule.setContent("/* */");
        rule.setName("sampleRule");
        // insert rule
        InsertedRuleDto result = insertFacade.insertGameRule(get(GAMEID), rule);
        assertTrue(!result.getId().isEmpty());
        // delete inserted rule
        boolean res = insertFacade.deleteGameRule(get(GAMEID), result.getId());
        assertTrue(res);
    }

    @Test
    public void saveItineraryTest() {
        ExecutionDataDTO input = new ExecutionDataDTO();
        input.setActionId(get(SAVE_ITINERARY));
        input.setPlayerId("1");
        input.setGameId(get(GAMEID));
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("bikeDistance", Double.valueOf(1.0d));
        input.setData(data);
        boolean result = facade.saveItinerary(input);

        assertTrue(result);
    }

    @Test
    public void saveUsersItineraryLoadedFromFile() throws IOException {
        // create input
        String ref = "178-bus.json";

        // read all lines from file
        List<String> lines = IOUtils
                .readLines(Thread.currentThread().getContextClassLoader().getResourceAsStream(ref));

        for (String line : lines) {
            JourneyData jd = ConverterUtil.extractJourneyData(line);
            ExecutionDataDTO input = new ExecutionDataDTO();
            input.setActionId(get(SAVE_ITINERARY));
            input.setPlayerId(jd.getUserId());
            input.setGameId(get(GAMEID));
            input.setData(jd.getData());
            boolean result = facade.saveItinerary(input);

            assertTrue(result);
        }
    }

    @Test
    public void printGameStatus() throws FileNotFoundException, IOException {
        List<Content> result = facade.readGameState(get(GAMEID));
        assertTrue(!result.isEmpty());

        String customNames = get(RELEVANT_CUSTOM_DATA);
        assertTrue(customNames != null);

        StringBuffer toWrite = new StringBuffer();

        toWrite.append("PLAYER_ID;SCORE_GREEN_LEAVES;" + customNames + "\n");
        for (Content content : result) {
            toWrite.append(content.getPlayerId() + ";"
                    + getScore(content, "green leaves", true, false) + ";" // false,
                    // false
                    // =
                    // current
                    // week
                    // counter
                    + getCustomData(content, true) + "\n");

        }
        IOUtils.write(toWrite.toString(), new FileOutputStream("gameStatus.csv"));

        assertTrue(!toWrite.toString().isEmpty());
    }

    private String getCustomData(Content content, boolean weekly) {
        String result = "";
        List<PointConcept> concepts = content.getState().getPointConcept();
        Collections.sort(concepts, new Comparator<PointConcept>() {
            @Override
            public int compare(PointConcept o1, PointConcept o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        Iterator<PointConcept> iter = concepts.iterator();
        while (iter.hasNext()) {
            PointConcept pc = iter.next();
            if (weekly) {
                // result += pc.getPeriodPreviousScore("weekly") + ";";
                result += pc.getPeriodCurrentScore("weekly") + ";";
            } else {
                result += pc.getScore() + ";";
            }
        }
        return result;
    }

    private String getCustomDataForWeek(Content content, int w) {
        String result = "";
        List<PointConcept> concepts = content.getState().getPointConcept();
        Collections.sort(concepts, new Comparator<PointConcept>() {
            @Override
            public int compare(PointConcept o1, PointConcept o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        Iterator<PointConcept> iter = concepts.iterator();
        while (iter.hasNext()) {
            PointConcept pc = iter.next();
            try {
                PeriodInstanceImpl v = pc.getPeriods().get("weekly").getInstances().get(w);
                result += v.getScore() + ";";
            } catch (Exception e) {
                result += "0;";
            }
        }
        return result;
    }

    private Double getScore(Content content, String points, boolean previous, boolean total) {
        for (PointConcept pc : content.getState().getPointConcept()) {
            if (pc.getName().equalsIgnoreCase(points)) {
                if (total) {
                    return pc.getScore();
                }
                if (previous) {
                    return pc.getPeriodPreviousScore("weekly");
                }
                return pc.getPeriodCurrentScore("weekly");
            }
        }
        return null;
    }

    private Double getScore(Content content, String points, Long moment) {
        for (PointConcept pc : content.getState().getPointConcept()) {
            if (pc.getName().equalsIgnoreCase(points)) {
                return pc.getPeriodScore("weekly", moment);
            }
        }
        return null;
    }

    @Test
    public void challengeReportDetails() throws FileNotFoundException, IOException {
        // a small utility to get a list of all users with a given challenge in
        // a period and its status
        List<Content> result = facade.readGameState(get(GAMEID));
        assertTrue(!result.isEmpty());

        StringBuffer toWrite = new StringBuffer();

        // build weeks details
        toWrite.append(
                "PLAYER_ID;CHALLENGE_UUID;MODEL_NAME;TARGET;BONUS_SCORE;BONUS_POINT_TYPE;START;END;COMPLETED;DATE_COMPLETED;BASELINE;PERIOD_NAME;COUNTER_NAME;COUNTER_VALUE"
                        + "\n");

        for (Content user : result) {
            // if (getScore(user, "green leaves", false, true) > 0) {
            for (ChallengeConcept cc : user.getState().getChallengeConcept()) {
                toWrite.append(user.getPlayerId() + ";");
                toWrite.append(cc.getName() + ";");
                toWrite.append(cc.getModelName() + ";");
                toWrite.append(cc.getFields().get(Constants.TARGET) + ";");
                toWrite.append(cc.getFields().get(Constants.BONUS_SCORE) + ";");
                toWrite.append(cc.getFields().get(Constants.BONUS_POINT_TYPE) + ";");
                toWrite.append(CalendarUtil.format((Long) cc.getStart()) + ";");
                toWrite.append(CalendarUtil.format((Long) cc.getEnd()) + ";");
                toWrite.append(cc.getCompleted() + ";");
                toWrite.append(CalendarUtil.format(cc.getDateCompleted()) + ";");
                toWrite.append(cc.getFields().get(Constants.BASELINE) + ";");
                toWrite.append(cc.getFields().get(Constants.PERIOD_NAME) + ";");
                toWrite.append(cc.getFields().get(Constants.COUNTER_NAME) + ";");
                toWrite.append(getScore(user, (String) cc.getFields().get(Constants.COUNTER_NAME),
                        cc.getStart()) + ";\n");
            }
            // }
        }

        String writable = toWrite.toString();
        writable = StringUtils.replace(writable, "null", "");

        IOUtils.write(writable, new FileOutputStream("challengeReportDetails.csv"));
        logger.info("challengeReportDetails.csv written");
        assertTrue(!writable.isEmpty());
    }

    @Test
    public void printPoints() throws FileNotFoundException, IOException {
        // a small utility to get a list of all users with a given challenge in
        // a period and its status
        List<Content> result = facade.readGameState(get(GAMEID));
        assertTrue(!result.isEmpty());

        // week11 playerIds
        // String playerIds =
        // "24813,24538,17741,24816,24498,24871,24279,24612,24853,24339,24391,24150,24650,11125,24092,24869,24329,24826,24828,24762,24883,24288,24486,24566,24224,24741,24367,19092,24864,24347,24823,23513,24526,24327,1667,24120,24482,24320,24122,24440";
        // List<String> ids = new ArrayList<String>();
        // Collections.addAll(ids, playerIds.split(","));

        System.out.println("PLAYER_ID;TOTAL_SCORE");
        for (Content user : result) {
            // if (ids.contains(user.getPlayerId())) {
            // if (user.getPlayerId().equals("24823")) {
            // System.out.println();
            // }
            for (PointConcept pc : user.getState().getPointConcept()) {
                if (pc.getName().equals("green leaves")) {
                    if (pc.getPeriods().get("weekly").getInstances().size() > 12) {
                        Double score =
                                pc.getPeriods().get("weekly").getInstances().get(12).getScore();
                        System.out.println(user.getPlayerId() + ";" + score);

                    } else {
                        // System.out.println();
                    }
                }
            }
            // }
        }
    }

    @Test
    /**
     * Print an excel file, for every week, the status for all players
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void finalGameStatus() throws FileNotFoundException, IOException {
        List<Content> result = facade.readGameState(get(GAMEID));

        // Get the workbook instance for XLS file
        Workbook workbook = new XSSFWorkbook();

        String customNames = get(RELEVANT_CUSTOM_DATA);
        customNames = "PLAYER_ID;" + customNames;
        String[] labels = customNames.split(";");

        for (int w = 1; w <= 12; w++) {
            // Get first sheet from the workbook
            Sheet sheet = workbook.createSheet("Week" + w);

            Row header = sheet.createRow(0);
            int i = 0;
            for (String label : labels) {
                header.createCell(i).setCellValue(label);
                i++;
            }
            int rowIndex = 1;

            for (Content user : result) {
                if (existInWeek(user, w)) {
                    Row row = sheet.createRow(rowIndex);
                    sheet = ExcelUtil.buildRow(user.getPlayerId(), getCustomDataForWeek(user, w),
                            sheet, row);
                    rowIndex++;
                }
            }

        }

        workbook.write(new FileOutputStream(new File("finalGameStatus.xlsx")));
        workbook.close();
        logger.info("written finalGameStatus.xlsx");
    }

    private boolean existInWeek(Content user, int w) {
        for (PointConcept pc : user.getState().getPointConcept()) {
            if (pc.getName().equals("green leaves")) {
                try {
                    PeriodInstanceImpl v = pc.getPeriods().get("weekly").getInstances().get(w);
                    if (v.getScore() > 0) {
                        return true;
                    }
                } catch (IndexOutOfBoundsException e) {
                    // continue
                }
            }
        }
        return false;
    }

}
