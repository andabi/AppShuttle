package lab.davidahn.appshuttle.collect.env;

public enum EnvType {

	LOCATION(UserLoc.class),
	INVALID_LOCATION(InvalidUserLoc.class),
	
	PLACE(UserPlace.class),
	INVALID_PLACE(InvalidUserPlace.class),
	
	SPEED(UserSpeed.class),
	INVALID_SPEED(InvalidUserSpeed.class);
	
	private Class<? extends UserEnv> _clazz;
	
	EnvType(Class<? extends UserEnv> clazz){
		_clazz = clazz;
	}

	public Class<? extends UserEnv> getClazz() {
		return _clazz;
	}
}