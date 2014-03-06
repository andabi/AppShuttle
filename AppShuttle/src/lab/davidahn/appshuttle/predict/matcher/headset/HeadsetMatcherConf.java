package lab.davidahn.appshuttle.predict.matcher.headset;

import lab.davidahn.appshuttle.predict.matcher.BaseMatcherConf;


public class HeadsetMatcherConf extends BaseMatcherConf{

	private HeadsetMatcherConf(Builder builder){
		super(builder);
	}
	
	public static class Builder extends BaseMatcherConf.Builder<Builder> {
		public HeadsetMatcherConf build(){
			return new HeadsetMatcherConf(this);
		}
		
		@Override
		public Builder getThis(){
			return this;
		}
	}
}