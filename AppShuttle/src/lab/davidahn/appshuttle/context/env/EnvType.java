package lab.davidahn.appshuttle.context.env;

public enum EnvType {
//	LOCATION,
//	PLACE;

	LOCATION(UserLoc.class),
	PLACE(UserPlace.class);
	
	private Class<? extends UserEnv> clazz;
	
	EnvType(Class<? extends UserEnv> clazz){
		this.clazz = clazz;
	}

	public Class<? extends UserEnv> getClazz() {
		return clazz;
	}
}