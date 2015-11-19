package jinanCDR;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Methods {
	
	public static Location getCenterLocation(CDR[] CDRs) {
		double centerLng;
		double centerLat;
		int weight = 0;
		
		centerLng = CDRs[0].lng;
		centerLat = CDRs[0].lat;
		weight++;
		
		for (int i = 1; i < CDRs.length; i++) {
			if (CDRs[i].lac != CDRs[i-1].lac || CDRs[i].cellid != CDRs[i-1].cellid) {
				centerLng = (centerLng * weight + CDRs[i].lng)/(weight + 1);
				centerLat = (centerLat * weight + CDRs[i].lat)/(weight + 1);
				weight++;
			}
		}
		
		Location loc = new Location(centerLng, centerLat);
		return loc;
	}
	
	public static void printMemory() {
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		long memory = runtime.totalMemory() - runtime.freeMemory();
		System.out.println(memory/1000/1000 + "MB");
	}
	
	public static int countLines(String filePath) {
		int n = 0;
		try (BufferedReader br = new BufferedReader(new java.io.FileReader(filePath))) {
			while (br.readLine() != null) {
				n++;
			}
		}
		catch (IOException ex) {
			System.out.println(ex);
		}
		return n;
	}
	
	// Calculate the distance between two points
	public static double distance(double lon1, double lat1, double lon2, double lat2) {
		double dist;
		double theta = lon1 - lon2;
		dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515 * 1.609344;
		return (dist);
	}

	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::  This function converts decimal degrees to radians             :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::  This function converts radians to decimal degrees             :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	private static double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}
	
	// Round the double number into a double number with specific decimal places
	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
}
}
