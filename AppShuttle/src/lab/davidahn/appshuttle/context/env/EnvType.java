package lab.davidahn.appshuttle.context.env;

@SuppressWarnings("rawtypes")
public enum EnvType {
	LOCATION(LocUserEnv.class),
	PLACE(PlaceUserEnv.class);
	
	private Class clazz;
	
	EnvType(Class clazz){
		this.clazz = clazz;
	}

	public Class getClazz() {
		return clazz;
	}
}