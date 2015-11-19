package jinanCDR;

import java.util.ArrayList;
import java.util.HashSet;

//import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class User {
	String id;
	int cdrCount;
	int towerCount;
	int dispCount;
	double ROG;
	double MFT_lng;
	double MFT_lat;
	
	int locCount;
	int tripCount;
	
	int[] hourlyCDRNum;
	int[] hourlyCallNum;
	int[] hourlyDataNum;
	int[] hourlyDispNum;
	int[] hourlyTowerNum;
	double[] hourlyROG;
	double[] hourlyReturn;
	
	double[] hourlyO_lng;
	double[] hourlyO_lat;
	double[] hourlyD_lng;
	double[] hourlyD_lat;
	double[] eDist;
	double[] nDist;
	double[] wDist;
	double[] sDist;
	
	UserOD.Gap randomGap;
	
	
	User (String ID) {
		id = ID;
	}
	
	User (CDR[] CDRs) {
		id = CDRs[0].id;
		cdrCount = CDRs.length;
		towerCount = getTowerCount(CDRs);
		dispCount = getDispCount(CDRs);
		ROG = getROG(CDRs);
		Tower[] towers = getTowers(CDRs);
		Tower mft = getMFT(towers);
		MFT_lng = mft.lng;
		MFT_lat = mft.lat;
		computeHourlyPattern(CDRs);
	}
	
	User (UserOD od) {
		id = od.id;
		UserOD.Gap gap = od.pickRandomGap();
		if (gap != null) randomGap = gap;
	}
	
	private int getTowerCount(CDR[] CDRs) {
		Tower[] towers = getTowers(CDRs);
		return towers.length;
	}
	
	private double getROG(CDR[] CDRs) {
		Location center = Methods.getCenterLocation(CDRs);
		double lng = center.lng;
		double lat = center.lat;
		int count = 0;
		double sumSquareDist;
		
		sumSquareDist = Math.pow(Methods.distance(CDRs[0].lng, CDRs[0].lat, lng, lat), 2);
		count++;
		
		for (int i = 1; i < CDRs.length; i++) {
			if (CDRs[i].lac != CDRs[i-1].lac || CDRs[i].cellid != CDRs[i-1].cellid) {
				sumSquareDist = sumSquareDist + Math.pow(Methods.distance(CDRs[i].lng, CDRs[i].lat, lng, lat), 2);
				count++;
			}
		}
		double rg = Math.sqrt(sumSquareDist/count);
		return rg;
	}
	
	private int getDispCount(CDR[] CDRs) {
		Displacement[] disps = getDisplacement(CDRs);
		if (disps != null) {
			return disps.length;
		}
		else {
			return 0;
		}
	}
	
	private Displacement[] getDisplacement(CDR[] CDRs) {
		ArrayList<Displacement> disps = new  ArrayList<Displacement>();
		for (int i = 1; i < CDRs.length; i++) {
			if (CDRs[i].lac != CDRs[i-1].lac || CDRs[i].cellid != CDRs[i-1].cellid) {
				disps.add(new Displacement(CDRs[i-1],CDRs[i]));
			}
		}
		return disps.toArray(new Displacement[0]);
	}
	
	private void computeHourlyPattern(CDR[] CDRs) {
		Displacement[] disps = getDisplacement(CDRs);
		if (disps != null) {
			
			hourlyCDRNum = new int[24];
			hourlyCallNum = new int[24];
			hourlyDataNum = new int[24];
			hourlyDispNum = new int[24];
			hourlyTowerNum = new int[24];
			hourlyROG = new double[24];
			hourlyReturn = new double[24];
			
			hourlyO_lng = new double[24];
			hourlyO_lat = new double[24];
			hourlyD_lng = new double[24];
			hourlyD_lat = new double[24];
			eDist = new double[24];
			nDist = new double[24];
			wDist = new double[24];
			sDist = new double[24];
			
			ArrayList<ArrayList<CDR>> allCDR = new ArrayList<>(30);
			ArrayList<ArrayList<Displacement>> allDisp = new ArrayList<>(30);
			for (int n = 0; n < 24; n++) {
				ArrayList<CDR> hourlyCDR = new ArrayList<>();
				allCDR.add(hourlyCDR);
				ArrayList<Displacement> hourlyDisp = new ArrayList<>();
				allDisp.add(hourlyDisp);
			}
			for (int i = 0; i < CDRs.length; i++) {
				int hour = CDRs[i].time/60;
				allCDR.get(hour).add(CDRs[i]);
			}
			for (int i = 0; i < disps.length; i++) {
				int interval = (disps[i].getEndDate() - disps[i].getStartDate()) * 1440 + disps[i].getEndTime() - disps[i].getStartTime();
				if (interval < 120) {
					int midpoint = (disps[i].getEndTime() + (disps[i].getEndDate() - disps[i].getStartDate()) * 1440 + disps[i].getStartTime())/2 % 1440;
					int midhour = midpoint/60;
					allDisp.get(midhour).add(disps[i]);
				}
			}
			for (int n = 0; n < 24; n++) {
				CDR[] hourlyCDRs = allCDR.get(n).toArray(new CDR[0]);
				if (hourlyCDRs != null && hourlyCDRs.length > 0) {
					hourlyCDRNum[n] = hourlyCDRs.length;
					HashSet<Integer> days = new HashSet<>();
					for (int i = 0; i < hourlyCDRs.length; i++) {
						if (hourlyCDRs[i].eventid == 16) {
							hourlyDataNum[n]++;
						}
						else {
							hourlyCallNum[n]++;
						}
						if (!days.contains(hourlyCDRs[i].date)) {
							days.add(hourlyCDRs[i].date);
						}
					}
					Tower[] towers = getTowers(hourlyCDRs);
					hourlyTowerNum[n] = towers.length;
					Tower hourlyMFT = getMFT(towers);
					hourlyReturn[n] = hourlyMFT.getDays() * 1.0 / days.size();
					hourlyROG[n] = getROG(hourlyCDRs);
					
					Displacement[] hourlyDisp = allDisp.get(n).toArray(new Displacement[0]);
					if (hourlyDisp != null) {
						hourlyDispNum[n] = hourlyDisp.length;
						double sumO_lng = 0.0;
						double sumO_lat = 0.0;
						double sumD_lng = 0.0;
						double sumD_lat = 0.0;
						for (int i = 0; i < hourlyDisp.length; i++) {
							double o_lng = hourlyDisp[i].getStartLng();
							double o_lat = hourlyDisp[i].getStartLat();
							double d_lng = hourlyDisp[i].getEndLng();
							double d_lat = hourlyDisp[i].getEndLat();
							
							if (d_lng > o_lng) {
								eDist[n] = eDist[n] + Methods.distance(o_lng, o_lat, d_lng, o_lat);
							}
							else {
								wDist[n] = wDist[n] + Methods.distance(o_lng, o_lat, d_lng, o_lat);
							}
							
							if (d_lat > o_lat) {
								nDist[n] = nDist[n] + Methods.distance(o_lng, o_lat, o_lng, d_lat);
							}
							else {
								sDist[n] = sDist[n] + Methods.distance(o_lng, o_lat, o_lng, d_lat);
							}
							
							sumO_lng = sumO_lng + o_lng;
							sumO_lat = sumO_lat + o_lat;
							sumD_lng = sumD_lng + d_lng;
							sumD_lat = sumD_lat + d_lat;
						}
						hourlyO_lng[n] = sumO_lng/hourlyDisp.length;
						hourlyO_lat[n] = sumO_lat/hourlyDisp.length;
						hourlyD_lng[n] = sumD_lng/hourlyDisp.length;
						hourlyD_lat[n] = sumD_lat/hourlyDisp.length;
					}
				}
			}
		}
	}
	
	private Tower[] getTowers(CDR[] cdrList) {
		ArrayList<Tower> towers = new ArrayList<Tower>();
		int n = cdrList.length;
		int [] cluster = new int[n];
		for (int i = 0; i < n; i++) {
			cluster[i] = -1;
		}
		for (int i = 0; i < n; i++) {
			if (cluster[i] == -1) {
				cluster[i] = i;
				Tower t = new Tower(cdrList[i]);
				for (int j = i+1; j < n; j++) {
					if (cluster[j] == -1 && cdrList[i].lac == cdrList[j].lac && cdrList[i].cellid == cdrList[j].cellid) {
						cluster[j] = i;
						t.cdrList.add(cdrList[j]);
					}
				}
				towers.add(t);
			}
		}
		towers.trimToSize();
		return towers.toArray(new Tower[0]);
	}
	
	private Tower getMFT(Tower[] towers) {
		
		Tower MFT = towers[0];
		for (int i = 0; i < towers.length; i++) {
			if (towers[i].getDays() > MFT.getDays()) {
				MFT = towers[i];
			}
		}
		return MFT;
	}
	
	
	
	
	public class Displacement {
		final CDR startCDR;
		final CDR endCDR;
		
		public Displacement(CDR cdr1, CDR cdr2) {
			startCDR = cdr1;
			endCDR = cdr2;
		}
		
		public int getStartDate() {
			return startCDR.date;
		}
		
		public int getEndDate() {
			return endCDR.date;
		}
		
		public int getStartTime() {
			return startCDR.time;
		}
		
		public int getEndTime() {
			return endCDR.time;
		}
		
		public double getStartLng() {
			return startCDR.lng;
		}
		
		public double getStartLat() {
			return startCDR.lat;
		}
		
		public double getEndLng() {
			return endCDR.lng;
		}
		
		public double getEndLat() {
			return endCDR.lat;
		}
	}
	
	
}
