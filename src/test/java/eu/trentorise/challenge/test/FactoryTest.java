package eu.trentorise.challenge.test;

import eu.trentorise.challenge.BaseTest;
import eu.trentorise.game.challenges.ChallengeFactory;
import eu.trentorise.game.challenges.api.ChallengeFactoryInterface;
import eu.trentorise.game.challenges.api.Constants;
import eu.trentorise.game.challenges.exception.UndefinedChallengeException;
import eu.trentorise.game.challenges.model.Challenge;
import eu.trentorise.game.challenges.model.ChallengeType;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class FactoryTest extends BaseTest {
    private ChallengeFactoryInterface chFactory;

    private final String testUserId = "999";

    @Test
    public void buildChallenges() {
        // simulates Factory client
        chFactory = new ChallengeFactory();
        Challenge c = null;

        // PERCENT Challenge building
        HashMap<String, Object> params;
        try {
            c = chFactory.createChallenge(ChallengeType.PERCENT,
                    "rules/templates");
            params = new HashMap<String, Object>();
            params.put("target", new Double(0.15));
            params.put("mode", "walkDistance");
            params.put("bonus", new Integer(50));
            params.put("point_type", "green leaves");
            params.put("baseline", new Double(100.0));
            c.setTemplateParams(params);
            c.compileChallenge(testUserId);
        } catch (UndefinedChallengeException uce) {
            uce.printStackTrace();
        }
        Assert.assertTrue("Challenge " + ChallengeType.PERCENT + " created",
                c != null);
        Assert.assertTrue(c.getType().equals(ChallengeType.PERCENT));
        Assert.assertTrue(c.getGeneratedRules() != null
                && !c.getGeneratedRules().equals(""));
        System.out.println(c.getGeneratedRules() + "\n\n");

        // BSPERCENT (BIKE SHARE) Challenge building
        try {
            c = chFactory.createChallenge(ChallengeType.BSPERCENT,
                    "rules/templates");
            params = new HashMap<String, Object>();
            params.put("target", new Double(0.15));
            params.put("mode", "bikesharing");
            params.put("bonus", new Integer(50));
            params.put("point_type", "green leaves");
            params.put("baseline", new Double(100.0));
            c.setTemplateParams(params);
            c.compileChallenge(testUserId);
        } catch (UndefinedChallengeException uce) {
            uce.printStackTrace();
        }
        Assert.assertTrue("Challenge " + ChallengeType.BSPERCENT + " created",
                c != null);
        Assert.assertTrue(c.getType().equals(ChallengeType.BSPERCENT));
        Assert.assertTrue(c.getGeneratedRules() != null
                && !c.getGeneratedRules().equals(""));
        System.out.println(c.getGeneratedRules() + "\n\n");

        // TRIPNUMBER Challenge building
        try {
            // TODO: cambiare nome per allinearlo a drt? e' TravelMode
            c = chFactory.createChallenge(ChallengeType.TRIPNUMBER,
                    "rules/templates");
            params = new HashMap<String, Object>();
            //params.put("trips", new Integer(1));
            params.put("mode", "busDistance");
            params.put("bonus", new Integer(50));
            params.put("target", new Double(1));
            params.put("point_type", "green leaves");
            c.setTemplateParams(params);
            c.compileChallenge(testUserId);
        } catch (UndefinedChallengeException uce) {
            uce.printStackTrace();
        }
        Assert.assertTrue("Challenge " + ChallengeType.TRIPNUMBER + " created",
                c != null);
        Assert.assertTrue(c.getType().equals(ChallengeType.TRIPNUMBER));
        Assert.assertTrue(c.getGeneratedRules() != null
                && !c.getGeneratedRules().equals(""));
        System.out.println(c.getGeneratedRules() + "\n\n");

        // NEGATEDMODE Challenge building
        //analogous to TRIPNUMBER but !mode
        try {
            c = chFactory.createChallenge(ChallengeType.NEGATEDMODE,
                    "rules/templates");
            params = new HashMap<String, Object>();
            //params.put("trips", new Integer(4));
            params.put("mode", "carDistance");
            params.put("bonus", new Integer(50));
            params.put("target", new Double(4));
            params.put("point_type", "green leaves");
            c.setTemplateParams(params);
            c.compileChallenge(testUserId);
        } catch (UndefinedChallengeException uce) {
            uce.printStackTrace();
        }
        Assert.assertTrue("Challenge " + ChallengeType.NEGATEDMODE + " created",
                c != null);
        Assert.assertTrue(c.getType().equals(ChallengeType.NEGATEDMODE));
        Assert.assertTrue(c.getGeneratedRules() != null
                && !c.getGeneratedRules().equals(""));
        System.out.println(c.getGeneratedRules() + "\n\n");

        // BSTRIPNUMBER (BIKE SHARE) Challenge building
        try {
            c = chFactory.createChallenge(ChallengeType.BSTRIPNUMBER,
                    "rules/templates");
            params = new HashMap<String, Object>();
            params.put("trips", new Integer(1));
            params.put("mode", "bikesharing");
            params.put("bonus", new Integer(50));
            params.put("target", new Double(20));
            params.put("point_type", "green leaves");
            c.setTemplateParams(params);
            c.compileChallenge(testUserId);
        } catch (UndefinedChallengeException uce) {
            uce.printStackTrace();
        }
        Assert.assertTrue("Challenge " + ChallengeType.BSTRIPNUMBER + " created",
                c != null);
        Assert.assertTrue(c.getType().equals(ChallengeType.BSTRIPNUMBER));
        Assert.assertTrue(c.getGeneratedRules() != null
                && !c.getGeneratedRules().equals(""));
        System.out.println(c.getGeneratedRules() + "\n\n");

        // GAME RECOMMENDATION Challenge building
        try {
            c = chFactory.createChallenge(ChallengeType.RECOMMENDATION,
                    "rules/templates");
            params = new HashMap<String, Object>();
            params.put("target", new Double(10));
            params.put("point_type", "green leaves");
            params.put("bonus", new Integer(50));
            c.setTemplateParams(params);
            c.compileChallenge(testUserId);
        } catch (UndefinedChallengeException uce) {
            uce.printStackTrace();
        }
        Assert.assertTrue("Challenge " + ChallengeType.RECOMMENDATION
                + " created", c != null);
        Assert.assertTrue(c.getType().equals(ChallengeType.RECOMMENDATION));
        Assert.assertTrue(c.getGeneratedRules() != null
                && !c.getGeneratedRules().equals(""));
        System.out.println(c.getGeneratedRules() + "\n\n");


        // SURVEY COMPLETION Challenge building
        try {
            c = chFactory.createChallenge(ChallengeType.SURVEYDATA,
                    "rules/templates");
            params = new HashMap<String, Object>();
            params.put("point_type", "green leaves");
            params.put("bonus", new Integer(500));
            c.setTemplateParams(params);
            c.compileChallenge(testUserId);
        } catch (UndefinedChallengeException uce) {
            uce.printStackTrace();
        }
        Assert.assertTrue("Challenge " + ChallengeType.SURVEYDATA
                + " created", c != null);
        Assert.assertTrue(c.getType().equals(ChallengeType.SURVEYDATA));
        Assert.assertTrue(c.getGeneratedRules() != null
                && !c.getGeneratedRules().equals(""));
        System.out.println(c.getGeneratedRules() + "\n\n");

        // POINTSEARNED Challenge building
        try {
            c = chFactory.createChallenge(ChallengeType.POINTSEARNED,
                    "rules/templates");
            params = new HashMap<String, Object>();
            params.put("target", new Long(100));
            params.put("bonus", new Integer(50));
            params.put("point_type", "green leaves");
            params.put((String) Constants.MODE, "green leaves week 3");
            c.setTemplateParams(params);
            c.compileChallenge(testUserId);
        } catch (UndefinedChallengeException uce) {
            uce.printStackTrace();
        }
        Assert.assertTrue("Challenge " + ChallengeType.POINTSEARNED + " created",
                c != null);
        Assert.assertTrue(c.getType().equals(ChallengeType.POINTSEARNED));
        Assert.assertTrue(c.getGeneratedRules() != null
                && !c.getGeneratedRules().equals(""));
        System.out.println(c.getGeneratedRules() + "\n\n");

        // ZEROIMPACT Challenge building
        try {
            c = chFactory.createChallenge(ChallengeType.ZEROIMPACT,
                    "rules/templates");
            params = new HashMap<String, Object>();
            params.put("target", new Long(10));
            params.put("bonus", new Integer(50));
            params.put("point_type", "green leaves");
            c.setTemplateParams(params);
            c.compileChallenge(testUserId);
        } catch (UndefinedChallengeException uce) {
            uce.printStackTrace();
        }
        Assert.assertTrue("Challenge " + ChallengeType.ZEROIMPACT + " created",
                c != null);
        Assert.assertTrue(c.getType().equals(ChallengeType.ZEROIMPACT));
        Assert.assertTrue(c.getGeneratedRules() != null
                && !c.getGeneratedRules().equals(""));
        System.out.println(c.getGeneratedRules() + "\n\n");


        // NEXTBADGE Challenge building
        try {
            c = chFactory.createChallenge(ChallengeType.NEXTBADGE,
                    "rules/templates");
            params = new HashMap<String, Object>();
            params.put("target", new Long(1));
            params.put("bonus", new Integer(50));
            params.put("point_type", "green leaves");
            params.put("badge_collection", "green leaves");
            c.setTemplateParams(params);
            c.compileChallenge(testUserId);
        } catch (UndefinedChallengeException uce) {
            uce.printStackTrace();
        }
        Assert.assertTrue("Challenge " + ChallengeType.NEXTBADGE + " created",
                c != null);
        Assert.assertTrue(c.getType().equals(ChallengeType.NEXTBADGE));
        Assert.assertTrue(c.getGeneratedRules() != null
                && !c.getGeneratedRules().equals(""));
        System.out.println(c.getGeneratedRules() + "\n\n");

        // BADGE COLLECTION COMPLETION Challenge building
        try {
            c = chFactory.createChallenge(ChallengeType.BADGECOLLECTION,
                    "rules/templates");
            params = new HashMap<String, Object>();
            params.put("badge_collection", "park and ride pioneer");
            params.put("target", new Double(2));
            params.put("bonus", new Integer(27));
            params.put("point_type", "green leaves");
            c.setTemplateParams(params);
            c.compileChallenge(testUserId);
        } catch (UndefinedChallengeException uce) {
            uce.printStackTrace();
        }
        Assert.assertTrue("Challenge " + ChallengeType.BADGECOLLECTION
                + " created", c != null);
        Assert.assertTrue(c.getType().equals(ChallengeType.BADGECOLLECTION));
        Assert.assertTrue(c.getGeneratedRules() != null
                && !c.getGeneratedRules().equals(""));
        System.out.println(c.getGeneratedRules() + "\n\n");

        // LEADERBOARD IMPROVEMENT Challenge building
        // TODO: finish this challenge
/*	
	try {
	    c = chFactory.createChallenge(ChallengeType.LEADERBOARDPOSITION,
		    "rules/templates");
	    params = new HashMap<String, Object>();
	    params.put("baseline", "week classification green_past");
	    params.put("leaderboard", "week classification green");
	    params.put("bonus", new Integer(15));
	    params.put("point_type", "green leaves");
	    c.setTemplateParams(params);
	    c.compileChallenge(testUserId);
	} catch (UndefinedChallengeException uce) {
	    uce.printStackTrace();
	}
	Assert.assertTrue("Challenge " + ChallengeType.LEADERBOARDPOSITION
		+ " created", c != null);
	Assert.assertTrue(c.getType().equals(ChallengeType.LEADERBOARDPOSITION));
	Assert.assertTrue(c.getGeneratedRules() != null
		&& !c.getGeneratedRules().equals(""));
	System.out.println(c.getGeneratedRules() + "\n\n");
*/

    }

}
