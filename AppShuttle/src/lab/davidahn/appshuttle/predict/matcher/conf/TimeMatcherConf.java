package lab.davidahn.appshuttle.predict.matcher.conf;

import android.app.AlarmManager;

public class TimeMatcherConf extends BaseMatcherConf{
	private long period;
	private long tolerance;

	private TimeMatcherConf(Builder builder){
		super(builder);
		period = builder.period;
		tolerance = builder.tolerance;
	}
	
	public long getPeriod() {
		return period;
	}

	public long getTolerance() {
		return tolerance;
	}

	public static class Builder extends BaseMatcherConf.Builder<Builder> {
		private long period = AlarmManager.INTERVAL_DAY;
		private long tolerance = AlarmManager.INTERVAL_HOUR;

		public TimeMatcherConf build(){
			return new TimeMatcherConf(this);
		}
		
		@Override
		public Builder getThis(){
			return this;
		}
		
		public Builder setPeriod(long period) {
			this.period = period;
			return getThis();
		}

		public Builder setTolerance(long tolerance) {
			if(tolerance > AlarmManager.INTERVAL_DAY)
				throw new IllegalArgumentException("tolerance should not exceed 24 hours");

			this.tolerance = tolerance;
			return getThis();
		}
	}
}