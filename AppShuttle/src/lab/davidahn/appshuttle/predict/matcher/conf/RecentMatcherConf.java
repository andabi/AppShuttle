package lab.davidahn.appshuttle.predict.matcher.conf;


public class RecentMatcherConf extends BaseMatcherConf{

	private RecentMatcherConf(Builder builder){
		super(builder);
	}
	
	public static class Builder extends BaseMatcherConf.Builder<Builder> {
		public RecentMatcherConf build(){
			return new RecentMatcherConf(this);
		}
		
		@Override
		public Builder getThis(){
			return this;
		}
	}
}