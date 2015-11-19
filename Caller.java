package jinanCDR;

import java.util.ArrayList;
import java.util.HashSet;

public class Caller {
	String id;
	int callCount;
	int towerCount;
	int dispCount;
	int activeDataHours;
	int activeCallHours;
	int activeMixedHours;
	double callContinuity;
	double dataContinuity;
	
	Transaction[] calls;
	Transaction[] datas;
	
	int[] callHours;
	int[] dataHours;
	
	public Caller (Transaction[] trans) {
		id = trans[0].id;
		ArrayList<Transaction> callList = new ArrayList<>();
		ArrayList<Transaction> dataList = new ArrayList<>();
		for (int i = 0; i < trans.length; i++) {
			Transaction temp = trans[i];
			if (temp.eventid == 16) {
				dataList.add(temp);
			}
			else if (temp.eventid == 5 || temp.eventid == 6) {
				callList.add(temp);
			}
		}
		calls = callList.toArray(new Transaction[0]);
		datas = dataList.toArray(new Transaction[0]);
		callCount = calls.length;
		towerCount = getTowerCount(calls);
		dispCount = getDispCount(calls);
		callHours = getHourlyPattern(calls);
		dataHours = getHourlyPattern(datas);
		activeCallHours = arraySum(callHours);
		activeDataHours = arraySum(dataHours);
		activeMixedHours = arrayCombine(callHours, dataHours);
		callContinuity = getContinuity(callHours);
		dataContinuity = getContinuity(dataHours);
	}
	
	private int getTowerCount(Transaction[] tranList) {
		HashSet<String> towers = new HashSet<>();
		for (int i = 0; i < tranList.length; i++) {
			String str = tranList[i].lac + "-" + tranList[i].cellid;
			if (!towers.contains(str)) {
				towers.add(str);
			}
		}
		return towers.size();
	}
	
	private int getDispCount(Transaction[] Trans) {
		signalMove[] disps = getSignalMove(Trans);
		if (disps != null) {
			return disps.length;
		}
		else {
			return 0;
		}
	}
	
	private signalMove[] getSignalMove(Transaction[] Trans) {
		ArrayList<signalMove> disps = new  ArrayList<>();
		for (int i = 1; i < Trans.length; i++) {
			if (Trans[i].lac != Trans[i-1].lac || Trans[i].cellid != Trans[i-1].cellid) {
				disps.add(new signalMove(Trans[i-1],Trans[i]));
			}
		}
		return disps.toArray(new signalMove[0]);
	}
	
	private int[] getHourlyPattern(Transaction[] Trans) {
		int[] pattern = new int[720];
		for (int i = 0; i < pattern.length; i++) {
			pattern[i] = 0;
		}
		for (int i = 0; i < Trans.length; i++) {
			int hour = (Trans[i].date-1)*24 + Trans[i].time/60;
			if (pattern[hour] == 0) {
				pattern[hour] = 1;
			}
		}
		return pattern;
	}
	
	private int arraySum(int[] series) {
		int sum = 0;
		for (int i = 0; i < series.length; i++) {
			sum = sum + series[i];
		}
		return sum;
	}
	
	private int arrayCombine(int[] series1, int[] series2) {
		int sum = 0;
		for (int i = 0; i < series1.length; i++) {
			if (series1[i] == 1 && series2[i] == 1) {
				sum++;
			}
		}
		return sum;
	}
	
	private double getContinuity(int[] series) {
		double continuity = 0.0;
		int count = 0;
		for (int i = 1; i < series.length-1; i++) {
			if (series[i] == 1 && series[i-1] == 1 && series[i+1] == 1) {
				count++;
			}
		}
		int sum = arraySum(series);
		if (sum > 2) {
			continuity = count * 1.0 /(sum-2);
		}
		return continuity;
	}
	
	public class signalMove {
		final Transaction startCDR;
		final Transaction endCDR;
		
		public signalMove(Transaction cdr1, Transaction cdr2) {
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
	}
}
