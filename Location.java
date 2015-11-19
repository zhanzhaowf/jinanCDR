package jinanCDR;


public class Location {
	double lng;
	double lat;
	
	public Location () {}
	
	public Location (Tower t) {
		lng = t.lng;
		lat = t.lat;
	}
	
	public Location(double LNG, double LAT) {
		lng = LNG;
		lat = LAT;
	}
	
	public void include(Tower t) {
		
	}
	
}
