package eu.fbk.das.rs.challengeGeneration;

/**
 * Convenience class for mode configuration for
 * {@link RecommendationSystemConfig}
 */
public class SingleModeConfig {

	private String modeName;
	private int weight;

	public SingleModeConfig(String modeName, int weight, long prizeMatrixMin,
			long prizeMatrixMax, long prizeMatrixIntermediate) {
		this.modeName = modeName;
		this.weight = weight;
		this.prizeMatrixMin = prizeMatrixMin;
		this.prizeMatrixMax = prizeMatrixMax;
		this.prizeMatrixIntermediate = prizeMatrixIntermediate;
	}

	private long prizeMatrixMin;
	private long prizeMatrixMax;
	private long prizeMatrixIntermediate;

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public long getPrizeMatrixMin() {
		return prizeMatrixMin;
	}

	public void setPrizeMatrixMin(long prizeMatrixMin) {
		this.prizeMatrixMin = prizeMatrixMin;
	}

	public long getPrizeMatrixMax() {
		return prizeMatrixMax;
	}

	public void setPrizeMatrixMax(long prizeMatrixMax) {
		this.prizeMatrixMax = prizeMatrixMax;
	}

	public long getPrizeMatrixIntermediate() {
		return prizeMatrixIntermediate;
	}

	public void setPrizeMatrixIntermediate(long prizeMatrixIntermediate) {
		this.prizeMatrixIntermediate = prizeMatrixIntermediate;
	}

	public String getModeName() {
		return modeName;
	}

	public void setModeName(String modeName) {
		this.modeName = modeName;
	}

}
