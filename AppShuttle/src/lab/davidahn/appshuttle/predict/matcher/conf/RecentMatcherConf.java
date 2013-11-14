package lab.davidahn.appshuttle.predict.matcher.conf;

import android.app.AlarmManager;



public class RecentMatcherConf extends BaseMatcherConf{
	private long acceptanceDelay;

	private RecentMatcherConf(Builder builder){
		super(builder);
		acceptanceDelay = builder.acceptanceDelay;
	}
	
	public long getAcceptanceDelay() {
		return acceptanceDelay;
	}

	public static class Builder extends BaseMatcherConf.Builder<Builder> {
		private long acceptanceDelay = AlarmManager.INTERVAL_FIFTEEN_MINUTES / 3;

		public RecentMatcherConf build(){
			return new RecentMatcherConf(this);
		}
		
		@Override
		public Builder getThis(){
			return this;
		}

		public Builder setAcceptanceDelay(long acceptanceDelay) {
			this.acceptanceDelay = acceptanceDelay;
			return getThis();
		}
	}
}