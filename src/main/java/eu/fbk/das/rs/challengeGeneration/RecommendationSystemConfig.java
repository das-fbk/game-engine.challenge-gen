package eu.fbk.das.rs.challengeGeneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class RecommendationSystemConfig {

	// we need to put all the configuration in different modes
	public static final String[] defaultMode = { "Bike_Km", "BikeSharing_Km",
			"BikeSharing_Trips" };

	public static final String[] defaultModetrip = { "Bike_Trips",
			"BikeSharing_Trips" };

	// , "Bike_Trips", "BikeSharing_Trips", "Bus_Trips",
	// "Train_Trips" };

	// , "Walk_Trips", "Bike_Trips", "BikeSharing_Trips", "Bus_Trips",
	// "Train_Trips"

	public static List<String> playerIds;

	public static HashMap<String, Integer> modeWeights;

	public RecommendationSystemConfig() {
		init();
	}

	private static void init() {
		RecommendationSystemConfig.modeWeights = new HashMap<String, Integer>();
		// just for test
		modeWeights.put("Walk", 0);
		modeWeights.put("Bike", 10);
		modeWeights.put("BikeSharing", 9);
		modeWeights.put("Bus", 0);
		modeWeights.put("Train", 0);
		modeWeights.put("ZeroImpact", 0);
		modeWeights.put("NoCar", 1);

		// list of default player ids
		RecommendationSystemConfig.playerIds = new ArrayList<String>();
		// Collections.addAll(RecommendationSystemConfig.playerIds, "23501",
		// "1658", "23692", "23897", "19092", "24502",
		// "4458");

		// Collections.addAll(RecommendationSystemConfig.playerIds, "7", "1835",
		// "17741", "2535", "17953", "291", "4458",
		// "11125", "23501", "1658", "23692", "23897", "23515", "19092", "2795",
		// "24502", "23513");

	}

	public static Integer getWeight(String key) {
		if (modeWeights == null) {
			init();
		}
		return modeWeights.get(key);
	}

	public static Set<String> getWeightKeySet() {
		if (modeWeights == null) {
			init();
		}
		return modeWeights.keySet();
	}

	public static List<String> getPlayerIds() {
		if (playerIds == null) {
			init();
		}
		return playerIds;
	}

}
