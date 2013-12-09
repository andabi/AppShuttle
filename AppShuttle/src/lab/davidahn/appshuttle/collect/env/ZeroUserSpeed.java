package lab.davidahn.appshuttle.collect.env;

public class ZeroUserSpeed extends UserSpeed {
	private static ZeroUserSpeed zeroUserSpeed = new ZeroUserSpeed();
	
	private ZeroUserSpeed() {
		super(0.0);
	}
	
	public static ZeroUserSpeed getInstance(){
		return zeroUserSpeed;
	}
}