package lab.davidahn.appshuttle.predict.matcher;

import android.app.AlarmManager;

public class MatcherConf {
	private final long duration;
	private final long acceptanceDelay;
	private final int minNumHistory;
	private final double minLikelihood;
	private final double minInverseEntropy;
	private final long timePeriod;
	private final long timeTolerance;
	private final int positionToleranceInMeter;
	
	private MatcherConf(Builder builder) {
		duration = builder.duration;
		acceptanceDelay = builder.acceptanceDelay;
		minNumHistory = builder.minNumHistory;
		minLikelihood = builder.minLikelihood;
		minInverseEntropy = builder.minInverseEntropy;
		timePeriod = builder.timePeriod;
		timeTolerance = builder.timeTolerance;
		positionToleranceInMeter = builder.positionToleranceInMeter;
	}
	
	public long getDuration() {
		return duration;
	}

	public long getAcceptanceDelay(){
		return acceptanceDelay;
	}
	
	public int getMinNumRelatedHistory() {
		return minNumHistory;
	}

	public double getMinLikelihood() {
		return minLikelihood;
	}

	public double getMinInverseEntropy() {
		return minInverseEntropy;
	}
	
	public long getTimePeriod() {
		return timePeriod;
	}

	public long getTimeTolerance() {
		return timeTolerance;
	}

	public int getPositionToleranceInMeter() {
		return positionToleranceInMeter;
	}

	public static class Builder {
		private long duration = 5 * AlarmManager.INTERVAL_DAY;
		private long acceptanceDelay = AlarmManager.INTERVAL_FIFTEEN_MINUTES / 3;
		private int minNumHistory = 3;
		private double minLikelihood = 0;
		private double minInverseEntropy = 0;
		private long timePeriod = AlarmManager.INTERVAL_DAY;
		private long timeTolerance = AlarmManager.INTERVAL_HOUR;
		private int positionToleranceInMeter = 2000;

		public MatcherConf build(){
			return new MatcherConf(this);
		}

		public Builder setDuration(long duration) {
			this.duration = duration;
			return this;
		}
		
		public Builder setAcceptanceDelay(long acceptanceDelay){
			this.acceptanceDelay = acceptanceDelay;
			return this;
		}

		public Builder setMinNumHistory(int minNumHistory) {
			this.minNumHistory = minNumHistory;
			return this;
		}

		public Builder setMinLikelihood(double minLikelihood) {
			this.minLikelihood = minLikelihood;
			return this;
		}

		public Builder setMinInverseEntropy(double minInverseEntropy) {
			this.minInverseEntropy = minInverseEntropy;
			return this;
		}
		
		public Builder setTimePeriod(long period) {
			this.timePeriod = period;
			return this;
		}

		public Builder setTimeTolerance(long tolerance) {
			if(tolerance > AlarmManager.INTERVAL_DAY)
				throw new IllegalArgumentException("tolerance should not exceed 24 hours");

			this.timeTolerance = tolerance;
			return this;
		}
		
		public Builder setPositionToleranceInMeter(int toleranceInMeter) {
			this.positionToleranceInMeter = toleranceInMeter;
			return this;
		}
	}
}