package lab.davidahn.appshuttle.model;

public class Area {
	double x;
	double y;
	double radius;

	public Area(double x, double y, double radius) {
		super();
		this.x = x;
		this.y = y;
		this.radius = radius;
	}
	
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public double getRadius() {
		return radius;
	}
	public void setRadius(double radius) {
		this.radius = radius;
	}
}
