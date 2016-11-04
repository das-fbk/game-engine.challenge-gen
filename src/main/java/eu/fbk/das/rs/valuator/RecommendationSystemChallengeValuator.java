package eu.fbk.das.rs.valuator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.math.Quantiles;

import eu.fbk.das.rs.challengeGeneration.RecommendationSystemConfig;
import eu.fbk.das.rs.challengeGeneration.SingleModeConfig;
import eu.trentorise.game.challenges.model.ChallengeDataDTO;
import eu.trentorise.game.challenges.rest.Content;
import eu.trentorise.game.challenges.rest.PointConcept;
import eu.trentorise.game.challenges.rest.PointConcept.PeriodInstanceImpl;
import eu.trentorise.game.challenges.rest.PointConcept.PeriodInternal;

public class RecommendationSystemChallengeValuator {

	private Map<String, PlanePointFunction> prizeMatrixMap = new HashMap<String, PlanePointFunction>();

	public RecommendationSystemChallengeValuator() {
		for (String mode : RecommendationSystemConfig.getModeKeySet()) {
			SingleModeConfig config = RecommendationSystemConfig
					.getModeConfig(mode);
			PlanePointFunction matrix = new PlanePointFunction(
					RecommendationSystemConfig.PRIZE_MATRIX_NROW,
					RecommendationSystemConfig.PRIZE_MATRIX_NCOL,
					config.getPrizeMatrixMin(), config.getPrizeMatrixMax(),
					config.getPrizeMatrixIntermediate(),
					RecommendationSystemConfig.PRIZE_MATRIX_APPROXIMATOR);
			prizeMatrixMap.put(mode, matrix);
		}
	}

	public Map<String, List<ChallengeDataDTO>> valuate(
			Map<String, List<ChallengeDataDTO>> combinations,
			List<Content> input) {

		for (int i = 0; i < RecommendationSystemConfig.defaultMode.length; i++) {

			String mode = RecommendationSystemConfig.defaultMode[i];

			// String mode = RecommendationSystemConfig.defaultMode[i];
			List<Double> activePlayersvalues = new ArrayList<Double>();
			//
			for (Content content : input) {
				for (PointConcept pc : content.getState().getPointConcept()) {
					// Adding filter for users
					// if
					// (RecommendationSystemConfig.getPlayerIds().contains(content.getPlayerId()))
					// {
					if (pc.getName().equals(mode)) {
						if (mode == "Bus_Km") {
							// System.out.println(mode);
						}
						for (String period : pc.getPeriods().keySet()) {
							PeriodInternal periodInstance = pc.getPeriods()
									.get(period);
							for (PeriodInstanceImpl p : periodInstance
									.getInstances()) {
								if (p.getScore() > 0) {
									activePlayersvalues.add(p.getScore());
								}

							}

						}

					}
				}
				// }

			}

			Collections.sort(activePlayersvalues);
			// ObjectMapper mapper = new ObjectMapper();
			// FileOutputStream oout;
			// try {
			// oout = new FileOutputStream(new
			// File("/Users/rezakhoshkangini/Documents/data.json"));
			// IOUtils.write(mapper.writeValueAsString(activePlayersvalues),
			// oout);
			// oout.flush();
			// } catch (JsonProcessingException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			// finding the percentiles of mode walk "weekly" from start to
			// now
			Map<Integer, Double> quartiles = Quantiles.scale(10)
					.indexes(4, 7, 9).compute(activePlayersvalues);

			System.out.println(mode);

			// if (mode == "Bus_Km") {
			// System.out.println(mode);
			// }
			// System.out.println(activePlayersvalues);

			for (String playerId : combinations.keySet()) {

				// List<ChallengeDataDTO> toSave = new
				// ArrayList<ChallengeDataDTO>();
				List<ChallengeDataDTO> challenges = combinations.get(playerId);

				for (ChallengeDataDTO challenge : challenges) {
					if (mode == "ZeroImpact_Trips" && playerId.equals("23515")) {
						System.out.println(mode);
					}
					// System.out.println(challenge.getData().get("counterName"));
					// if (challenge.getData().get("counterName").equals(mode)
					// && playerId.equals("24502")) {
					// System.out.println(mode);
					// }
					String counterName = (String) challenge.getData().get(
							"counterName");
					if (isSameOf(counterName, mode)) {

						if (challenge.getModelName() == "percentageIncrement") {
							Double baseline = (Double) challenge.getData().get(
									"baseline");
							Double target = (Double) challenge.getData().get(
									"target");
							// Integer zone =
							// DifficultyCalculator.computeZone(quartiles,
							// baseline);
							Integer difficulty = DifficultyCalculator
									.computeDifficulty(quartiles, baseline,
											target);
							System.out.println("Challenge baseline=" + baseline
									+ ", target=" + target + " difficulty="
									+ difficulty);
							challenge.getData().put("difficulty", difficulty);

							double d = (double) challenge.getData().get(
									"percentage");

							Long prize = calculatePrize(difficulty, d,
									counterName);
							challenge.getData().put("bonusScore", prize);
						} else if (challenge.getModelName() == "absoluteIncrement") {
							challenge.getData().put("difficulty",
									DifficultyCalculator.MEDIUM);
							long tryOnceBonus = prizeMatrixMap
									.get(counterName)
									.getTryOncePrize(
											RecommendationSystemConfig.PRIZE_MATRIX_TRY_ONCE_ROW_INDEX,
											RecommendationSystemConfig.PRIZE_MATRIX_TRY_ONCE_COL_INDEX);
							challenge.getData().put("bonusScore", tryOnceBonus);
						}

					}

				}
				combinations.put(playerId, challenges);

			}

		}

		combinations.get("24502");

		return combinations;

	}

	private boolean isSameOf(String v, String mode) {
		if (v.equals(mode)) {
			return true;
		}
		// if (v.contains("_") && mode.contains("_")) {
		// String[] s = v.split("_");
		// String[] m = mode.split("_");
		// return s[0].equals(m[0]);
		// }
		return false;
	}

	private long calculatePrize(Integer difficulty, double percent,
			String modeName) {
		// TODO: config!
		int y = 0;
		if (percent == 0.1) {
			y = 0;
		} else if (percent == 0.2) {
			y = 1;
		} else if (percent == 0.3) {
			y = 2;
		} else if (percent == 0.5) {
			y = 4;
		} else if (percent == 1) {
			y = 9;
		}

		long prize = prizeMatrixMap.get(modeName).get(difficulty - 1, y);

		return prize;
	}

}
