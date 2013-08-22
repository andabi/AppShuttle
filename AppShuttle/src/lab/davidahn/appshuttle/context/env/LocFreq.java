package lab.davidahn.appshuttle.context.env;

@Deprecated
public class LocFreq{
	private UserLoc uLoc;
	private int freq;
	
	LocFreq(UserLoc uLoc, int freq){
		this.uLoc = uLoc;
		this.freq = freq;
	}
	
	public UserLoc getULoc() {
		return uLoc;
	}

	public void setULoc(UserLoc uLoc) {
		this.uLoc = uLoc;
	}

	public int getFreq() {
		return freq;
	}

	public void setFreq(int freq) {
		this.freq = freq;
	}
	public String toString(){
		StringBuffer msg = new StringBuffer();
		msg.append(" (").append(uLoc.toString()).append(", ");
		msg.append(Integer.toString(freq)).append(") ");
		return msg.toString();
	}
}