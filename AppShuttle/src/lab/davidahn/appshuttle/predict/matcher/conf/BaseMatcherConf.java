package lab.davidahn.appshuttle.predict.matcher.conf;

import android.app.AlarmManager;

public class BaseMatcherConf {
	protected final long duration;
	protected final long acceptanceDelay;
	protected final int minNumHistory;
	protected final double minLikelihood;
	protected final double minInverseEntropy;

	protected <B extends Builder<B>> BaseMatcherConf(Builder<B> builder) {
		duration = builder.duration;
		acceptanceDelay = builder.acceptanceDelay;
		minNumHistory = builder.minNumHistory;
		minLikelihood = builder.minLikelihood;
		minInverseEntropy = builder.minInverseEntropy;
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

	public static abstract class Builder<B extends Builder<B>>{
		protected long duration = 5 * AlarmManager.INTERVAL_DAY;
		protected long acceptanceDelay = AlarmManager.INTERVAL_FIFTEEN_MINUTES / 3;
		protected int minNumHistory = 3;
		protected double minLikelihood = 0;
		protected double minInverseEntropy = 0;
	
		protected Builder(){}

		public abstract B getThis();
	
		public B setDuration(long duration) {
			this.duration = duration;
			return getThis();
		}
		
		public B setAcceptanceDelay(long acceptanceDelay){
			this.acceptanceDelay = acceptanceDelay;
			return getThis();
		}

		public B setMinNumHistory(int minNumHistory) {
			this.minNumHistory = minNumHistory;
			return getThis();
		}

		public B setMinLikelihood(double minLikelihood) {
			this.minLikelihood = minLikelihood;
			return getThis();
		}

		public B setMinInverseEntropy(double minInverseEntropy) {
			this.minInverseEntropy = minInverseEntropy;
			return getThis();
		}
	}
}