package lab.davidahn.appshuttle.predict.matcher.conf;

import android.app.AlarmManager;

public class BaseMatcherConf {
	protected final long duration;
	protected final int minNumHistory;
	protected final double minLikelihood;
	protected final double minInverseEntropy;

	protected <T extends Builder<T>> BaseMatcherConf(Builder<T> builder) {
		duration = builder.duration;
		minNumHistory = builder.minNumHistory;
		minLikelihood = builder.minLikelihood;
		minInverseEntropy = builder.minInverseEntropy;
	}
	
	public long getDuration() {
		return duration;
	}

	public int getMinNumHistory() {
		return minNumHistory;
	}

	public double getMinLikelihood() {
		return minLikelihood;
	}

	public double getMinInverseEntropy() {
		return minInverseEntropy;
	}

	public static abstract class Builder<T extends Builder<T>>{
		protected long duration = 5 * AlarmManager.INTERVAL_DAY;
		protected int minNumHistory = 3;
		protected double minLikelihood = 0;
		protected double minInverseEntropy = 0;
	
		protected Builder(){}

		public abstract T getThis();
		
//		public BaseMatcherConf build(){
//			return new BaseMatcherConf(this);
//		}
		
		public T setDuration(long duration) {
			this.duration = duration;
			return getThis();
		}

		public T setMinNumHistory(int minNumHistory) {
			this.minNumHistory = minNumHistory;
			return getThis();
		}

		public T setMinLikelihood(double minLikelihood) {
			this.minLikelihood = minLikelihood;
			return getThis();
		}

		public T setMinInverseEntropy(double minInverseEntropy) {
			this.minInverseEntropy = minInverseEntropy;
			return getThis();
		}
	}
}