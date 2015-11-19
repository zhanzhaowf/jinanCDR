package jinanCDR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

public class UserOD {
	String id;
	CDR[] cdrList;
	Tower[] towerList;
	Location[] locList;
	Presence[] presenceList;
	Trip[] tripList;
	
	public UserOD (CDR[] CDRs, TAZ zones) {
		id = CDRs[0].id;
		cdrList = CDRs;
		towerList = getTowers();
		locList = leaderClusteringTowers(1.5);
		locationAggregate(zones);
		locationRank();
		presenceList = getPresences(10);
		tripList = tripInference(120);
	}
	
	private Tower[] getTowers() {
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
					}
				}
				towers.add(t);
			}
		}
		towers.trimToSize();
		Collections.sort(towers);
		return towers.toArray(new Tower[0]);
	}
	
	private Location[] leaderClusteringTowers(double d) {
		ArrayList<Location> locs = new ArrayList<Location>();
		final double distThreshold = d; // radius parameter
		
		int n = towerList.length;
		
		for (int i = 0; i < n; i++) {
			if (towerList[i].locIndex == -1) {
				int index = locs.size();
				towerList[i].setLocIndex(index);
				Location loc = new Location(index, towerList[i]);
				for (int j = i+1; j < n; j++) {
					if (towerList[j].locIndex == -1 && Methods.distance(loc.lng, loc.lat, towerList[j].lng, towerList[j].lat) < distThreshold) {
						towerList[j].setLocIndex(index);
						loc.include(towerList[j]);
					}
				}
				locs.add(loc);
			}
		}
		locs.trimToSize();
		return locs.toArray(new Location[0]);
	}
	
	private void locationAggregate(TAZ zones) {
		for (int i = 0; i < locList.length; i++) {
			for (int j = 0; j < zones.count; j++) {
				if (zones.tazList[j].contains(locList[i].lng, locList[i].lat)) {
					locList[i].taz = j;
					break;
				}
			}
		}
	}
	
	private void locationRank() {
		ArrayList<Location> locs = new ArrayList<>(locList.length);
		for (int i = 0; i < locList.length; i++) {
			locs.add(locList[i]);
		}
		Collections.sort(locs);
		for (int i = 0; i < locList.length; i++) {
			int index = locs.get(i).locIndex;
			locList[index].rank = i + 1;
		}
	}
	
	private Presence[] getPresences(int t) {
		ArrayList<Presence> presences = new ArrayList<Presence>();
		final int timeThreshold = t; // the maximum time during which a person will not move
		
		CDR prevCDR = cdrList[0];
		Presence tempPresence = new Presence(prevCDR);
		int n = cdrList.length;
		for (int i = 1; i < n; i++) {
			CDR currCDR = cdrList[i];
			if (currCDR.date == prevCDR.date && currCDR.time <= (prevCDR.time + timeThreshold) && 
					currCDR.locIndex == prevCDR.locIndex) {
				tempPresence.extend(currCDR);
			}
			else {
				presences.add(tempPresence);
				tempPresence = new Presence(currCDR);
			}
			prevCDR = currCDR;
		}
		presences.add(tempPresence);
		presences.trimToSize();
		return presences.toArray(new Presence[0]);
	}
	
	private Trip[] tripInference(int t) {
		ArrayList<Trip> trips = new ArrayList<Trip>();
		final int timeThreshold = t; // the maximum time during which a trip can be determined
		
		Presence prevPresence = presenceList[0];
		for (int i = 1; i < presenceList.length; i++) {
			Presence currPresence = presenceList[i];
			if (currPresence.date == prevPresence.date && currPresence.startTime <= (prevPresence.endTime + timeThreshold) &&
					currPresence.locIndex != prevPresence.locIndex) {
				trips.add(new Trip(prevPresence, currPresence));
			}
			prevPresence = currPresence;
		}
		if (trips.size() == 0) {
			return null;
		}
		trips.trimToSize();
		return trips.toArray(new Trip[0]);
	}
	
	public void printTrips() {
		if (tripList != null) {
			int N = tripList.length;
			System.out.println(id + ": " + N +" trips");
			for (int i = 0; i < N; i++) {
				int o = tripList[i].oTAZ;
				int d = tripList[i].dTAZ;
				int date = tripList[i].date;
				int start = tripList[i].startTime;
				int end = tripList[i].endTime;
				double dist = tripList[i].dist;
				System.out.println("\t" + o + "\t" + d + "\t" + date + "\t" + start + "\t" + end + "\t" + dist);
			}
		}
	}
	
	public class Tower implements Comparable<Tower> {
		int lac;
		int cellid;
		double lng;
		double lat;
		int locIndex;
		
		public Tower(CDR call) {
			lac = call.lac;
			cellid = call.cellid;
			lng = call.lng;
			lat = call.lat;
			locIndex = -1;
		}
		
		public int compareTo(Tower t) {
			int egoDays = this.getDays();
			int alterDays = t.getDays();
			int egoHours = this.getHours();
			int alterHours = t.getHours();
			if (egoDays > alterDays)
				return -2;
			else if (egoDays == alterDays && egoHours > alterHours)
				return -1;
			else if (egoDays == alterDays && egoHours < alterHours)
				return 1;
			else if (egoDays < alterDays)
				return 2;
			else
				return 0;
		}
		
		public void setLocIndex(int index) {
			this.locIndex = index;
			for (int i = 0; i < cdrList.length; i++) {
				if (this.lac == cdrList[i].lac && this.cellid == cdrList[i].cellid) {
					cdrList[i].locIndex = index;
				}
			}
		}
		
		public int getHours() {
			int hours = 0;
			int lastHour = 0;
			for (int i = 0; i < cdrList.length; i++) {
				if (this.lac == cdrList[i].lac && this.cellid == cdrList[i].cellid) {
					int currentHour = cdrList[i].date*24 + cdrList[i].time/60;
					if (currentHour > lastHour) {
						hours++;
						lastHour = currentHour;
					}
				}
			}
			return hours;
		}
		
		public int getDays() {
			int days = 0;
			int lastDay = 0;
			for (int i = 0; i < cdrList.length; i++) {
				if (this.lac == cdrList[i].lac && this.cellid == cdrList[i].cellid) {
					int currentDay = cdrList[i].date;
					if (currentDay > lastDay) {
						days++;
						lastDay = currentDay;
					}
				}
			}
			return days;
		}
	}
	
	public class Location implements Comparable<Location> {
		int locIndex;
		double lng;
		double lat;
		int taz;
		int rank;
		
		public Location (int index, Tower t) {
			locIndex = index;
			lng = t.lng;
			lat = t.lat;
			taz = -1;
		}
		
		public void include(Tower t) {
			int egoHours = this.getHours();
			int alterHours = t.getHours();
			this.lng = (this.lng * egoHours + t.lng * alterHours)/(egoHours + alterHours);
			this.lat = (this.lat * egoHours + t.lat * alterHours)/(egoHours + alterHours);
		}
		
		public int getHours() {
			int hours = 0;
			int lastHour = 0;
			for (int i = 0; i < cdrList.length; i++) {
				if (this.locIndex == cdrList[i].locIndex) {
					int currentHour = cdrList[i].date*24 + cdrList[i].time/60;
					if (currentHour > lastHour) {
						hours++;
						lastHour = currentHour;
					}
				}
			}
			return hours;
		}
		
		public int compareTo(Location loc) {
			int egoDays = this.getCallDays();
			int alterDays = loc.getCallDays();
			int egoHours = this.getCallHours();
			int alterHours = loc.getCallHours();
			if (egoDays > alterDays)
				return -2;
			else if (egoDays == alterDays && egoHours > alterHours)
				return -1;
			else if (egoDays == alterDays && egoHours < alterHours)
				return 1;
			else if (egoDays < alterDays)
				return 2;
			else
				return 0;
		}
		
		public int getCallHours() {
			int hours = 0;
			int lastHour = 0;
			for (int i = 0; i < cdrList.length; i++) {
				if (this.locIndex == cdrList[i].locIndex && cdrList[i].eventid <= 6) {
					int currentHour = cdrList[i].date*24 + cdrList[i].time/60;
					if (currentHour > lastHour) {
						hours++;
						lastHour = currentHour;
					}
				}
			}
			return hours;
		}
		
		public int getCallDays() {
			int days = 0;
			int lastDay = 0;
			for (int i = 0; i < cdrList.length; i++) {
				if (this.locIndex == cdrList[i].locIndex && cdrList[i].eventid <= 6) {
					int currentDay = cdrList[i].date;
					if (currentDay > lastDay) {
						days++;
						lastDay = currentDay;
					}
				}
			}
			return days;
		}
		
		public int getCallRank() {
			return rank;
		}

	}
	
	public class Presence {
		int date;
		int startTime;
		int endTime;
		int locIndex;
		
		public Presence(CDR call) {
			date = call.date;
			startTime = call.time;
			endTime = call.time;
			locIndex = call.locIndex;
		}
		
		public double getLongitude() {
			return locList[locIndex].lng;
		}
		
		public double getLatitude() {
			return locList[locIndex].lat;
		}
		
		public int getDuration() {
			int dur = endTime - startTime;
			if (dur > 0) {
				return dur;
			}
			else {
				return 1;
			}
		}
		
		public void extend(CDR call) {
			if (call.time < this.startTime) {
				this.endTime = call.time + 24*60; // Next day(?)
			}
			else {
				this.endTime = call.time;
			}
		}
		
	}

	public class Trip {
		int date;
		int startTime;
		int endTime;
		int oTAZ;
		int dTAZ;
		double dist;
		
		public Trip (Presence p1, Presence p2) {
			date = p1.date;
			startTime = p1.endTime;
			endTime = p2.startTime;
			oTAZ = locList[p1.locIndex].taz;
			dTAZ = locList[p2.locIndex].taz;
			dist = Methods.distance(p1.getLongitude(), p1.getLatitude(), p2.getLongitude(), p2.getLatitude());
		}
	}
	
	private int[] getTrajectory(CDR[] CDRs) {
		int[] trajectory = new int[720];
		for (int i = 0; i < trajectory.length; i++) {
			trajectory[i] = -1;
		}
		for (int i = 0; i < CDRs.length; i++) {
			int hour = (CDRs[i].date-1)*24 + CDRs[i].time/60;
			if (trajectory[hour] == -1 || (hour > 0 && trajectory[hour-1] >= 0)) {
				trajectory[hour] = CDRs[i].locIndex;
			}
		}
		return trajectory;
	}
	
	private int[] getCallTrajectory(CDR[] CDRs) {
		int[] trajectory = new int[720];
		for (int i = 0; i < trajectory.length; i++) {
			trajectory[i] = -1;
		}
		for (int i = 0; i < CDRs.length; i++) {
			if (CDRs[i].eventid <= 6) {
				int hour = (CDRs[i].date-1)*24 + CDRs[i].time/60;
				if (trajectory[hour] == -1 || (hour > 0 && trajectory[hour-1] >= 0)) {
					trajectory[hour] = CDRs[i].locIndex;
				}
			}
		}
		return trajectory;
	}
	
	public int getActiveCallHours() {
		CDR[] CDRs = this.cdrList;
		HashSet<Integer> activeCallHours = new HashSet<>();
		for (int i = 0; i < CDRs.length; i++) {
			if (CDRs[i].eventid <= 6) {
				int hour = (CDRs[i].date-1)*24 + CDRs[i].time/60;
				if (!activeCallHours.contains(hour)) {
					activeCallHours.add(hour);
				}
			}
		}
		return activeCallHours.size();
	}
	
	public int getCallLocs() {
		CDR[] CDRs = this.cdrList;
		int[] traj = this.getCallTrajectory(CDRs);
		HashSet<Integer> callLocs = new HashSet<>();
		for (int i = 0; i < traj.length; i++) {
			if (traj[i]>=0 && !callLocs.contains(traj[i])) {
				callLocs.add(traj[i]);
			}
		}
		return callLocs.size();
	}
	
	public int getCallDisps() {
		CDR[] CDRs = this.cdrList;
		int[] traj = this.getCallTrajectory(CDRs);
		int dispCount = 0;
		int prevLoc = -1;
		for (int i = 0; i < traj.length; i++) {
			if (prevLoc == -1 && traj[i] >= 0) {
				prevLoc = traj[i];
			}
			if (prevLoc >= 0 && traj[i] >= 0 && prevLoc != traj[i]) {
				dispCount++;
				prevLoc = traj[i];
			}
		}
		return dispCount;
	}
	
	private int[] getDataTrajectory(CDR[] CDRs) {
		int[] trajectory = new int[720];
		for (int i = 0; i < trajectory.length; i++) {
			trajectory[i] = -1;
		}
		for (int i = 0; i < CDRs.length; i++) {
			if (CDRs[i].eventid == 16) {
				int hour = (CDRs[i].date-1)*24 + CDRs[i].time/60;
				if (trajectory[hour] == -1 || (hour > 0 && trajectory[hour-1] >= 0)) {
					trajectory[hour] = CDRs[i].locIndex;
				}
			}
		}
		return trajectory;
	}
	
	public int[] getCallPattern() {
		CDR[] CDRs = this.cdrList;
		int[] pattern = new int[720];
		for (int i = 0; i < pattern.length; i++) {
			pattern[i] = 0;
		}
		for (int i = 0; i < CDRs.length; i++) {
			if (CDRs[i].eventid <= 6) {
				int hour = (CDRs[i].date-1)*24 + CDRs[i].time/60;
				if (pattern[hour] == 0) {
					pattern[hour] = 1;
				}
			}
		}
		return pattern;
	}
	
	private Gap[] getGaps() {
		int[] callTraj = getCallTrajectory(this.cdrList);
		int[] dataTraj = getDataTrajectory(this.cdrList);
		
//		for (int i = 0; i < 720; i++) {
//			double d = -1.0;
//			if (callTraj[i]>=0 && dataTraj[i]>=0) {
//				d = Methods.distance(locList[callTraj[i]].lng, locList[callTraj[i]].lat, locList[dataTraj[i]].lng, locList[dataTraj[i]].lat);
//			}
//			System.out.println(i+";"+callTraj[i]+";"+dataTraj[i]+";"+d);
//		}
		
		int start = 0;
		int end = 0;
		int check = 0;
		int trip = 1;
		ArrayList<Gap> gapList = new ArrayList<>();
		for (int i = 1; i < 719; i++) {
			if (callTraj[i] == -1 && callTraj[i-1] >= 0) {
				start = i;
			}
			if (callTraj[i] == -1 && callTraj[i+1] >= 0) {
				end = i;
			}
			if (start > 0 && end >= start) {
				if (callTraj[start-1] != callTraj[end+1]) {
					check = 0;
					trip = 1;
					for (int j = start; j < end + 1; j++) {
						if (dataTraj[j] == -1) {
							check = 1;
							break;
						}
						if (dataTraj[j] != callTraj[start-1] && dataTraj[j] != callTraj[end+1]) {
							trip = 0;
						}
					}
					if (check == 0) {
						int preLoc = callTraj[start-1];
						int sucLoc = callTraj[end+1];
						int tripBefore = 0;
						int totalTrips = 0;
						int sameTrips = 0;
						for (int j = 1; j < 720; j++) {
							if (callTraj[j-1] >= 0 && callTraj[j] >= 0 && callTraj[j-1] != callTraj[j]) {
								totalTrips++;
								if (callTraj[j-1] == preLoc && callTraj[j] == sucLoc) {
									tripBefore = 1;
									sameTrips++;
								}
							}
						}
						double tripProb = 0.0;
						if (totalTrips > 0) {tripProb = sameTrips * 1.0 / totalTrips;}
						int startHour = start % 24;
						int endHour = startHour + (end - start);
						int otherLoc = 0;
						int counter = 0;
						HashSet<Integer> otherLocs = new HashSet<>();
						while (endHour + counter * 24 < 720) {
							for (int h = (startHour+counter*24); h < (endHour+counter*24+1); h++) {
								if (callTraj[h] >= 0 && callTraj[h] != preLoc && callTraj[h] != sucLoc) {
									if (!otherLocs.contains(callTraj[h])) {
										otherLocs.add(callTraj[h]);
									}
								}
							}
							counter++;
						}
						otherLoc = otherLocs.size();
						gapList.add(new Gap(start,end,preLoc,sucLoc,tripBefore,otherLoc,tripProb,trip));
						
					}
				}
				start = 0;
				end = 0;
			}
		}
		if (gapList.size() > 0) {
			return gapList.toArray(new Gap[0]);
		}
		else {
			return null;
		}
	}
	
	public Gap pickRandomGap() {
		Gap[] gaps = getGaps();
		if (gaps != null) {
			Random rn = new Random();
			int index = rn.nextInt(gaps.length);
			return gaps[index];
		}
		else {
			return null;
		}
	}
	
	public int getCallRecords() {
		int count = 0;
		for (int i = 0; i < cdrList.length; i++) {
			if (cdrList[i].eventid <= 6) {
				count++;
			}
		}
		return count;
	}
	
	public int getCallTowers() {
		HashSet<Integer> towers = new HashSet<>();
		for (int i = 0; i < cdrList.length; i++) {
			if (cdrList[i].eventid <= 6) {
				int towerid = cdrList[i].lac * 100000 + cdrList[i].cellid;
				if (!towers.contains(towerid)) {
					towers.add(towerid);
				}
			}
		}
		return towers.size();
	}
	
	public class Gap {
		int start;
		int end;
		int preLocRank;
		int sucLocRank;
		int tripBefore;
		int otherLoc;
		double dist;
		double tripProb;
		int trip;
		
		public Gap(int s, int e, int pre, int suc, int tB, int o, double tP, int t) {
			start = s;
			end = e;
			preLocRank = locList[pre].getCallRank();
			sucLocRank = locList[suc].getCallRank();
			tripBefore = tB;
			otherLoc = o;
			tripProb = tP;
			dist = Methods.distance(locList[pre].lng, locList[pre].lat, locList[suc].lng, locList[suc].lat);
			trip = t;
		}
	}

}
