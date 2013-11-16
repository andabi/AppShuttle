package lab.davidahn.appshuttle.predict.matcher.conf;

public class PositionMatcherConf extends BaseMatcherConf{
	private final int toleranceInMeter;
	
	private PositionMatcherConf(Builder builder){
		super(builder);
		toleranceInMeter = builder.toleranceInMeter;
	}
	
	public int getToleranceInMeter() {
		return toleranceInMeter;
	}

	public static class Builder extends BaseMatcherConf.Builder<Builder> {
		private int toleranceInMeter = 2000;

		public PositionMatcherConf build(){
			return new PositionMatcherConf(this);
		}
		
		@Override
		public Builder getThis(){
			return this;
		}
		
		public Builder setToleranceInMeter(int toleranceInMeter) {
			this.toleranceInMeter = toleranceInMeter;
			return getThis();
		}
	}
}