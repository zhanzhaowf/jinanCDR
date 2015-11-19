package jinanCDR;

import java.util.ArrayList;

public class Demo {
	
	final static String inputCDR = "/Data/nov_freq_users.csv";
	final static TAZ zones = new TAZ("/Data/verticepoint.csv");
	
	static SystemOD jinanOD;
	static ArrayList<User> users = new ArrayList<User>();
	static ArrayList<UserOD.Gap> gaps = new ArrayList<>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long timeStart = System.currentTimeMillis();
		System.out.println("Initializing...");
		
		jinanOD = new SystemOD(zones);
		for (int i = 0; i < 100; i++) {
			readCDRfromFile(inputCDR);
			long timeTemp = System.currentTimeMillis();
			System.out.println(i + "\t" + (timeTemp - timeStart) / 1000.0);
		}
		//readCDRfromFile(inputCDR);
		
		//outputUsers("/Data/Users.txt");
		//outputSignalJumps("/Data/SignalJumps.txt");
		
		//outputUserHours("/Data/UserHours.txt");
		
		//readAndWrite(inputCDR, "/Data/Gaps2.txt");
		
		jinanOD.outputTripMatrix("/Data/Trip_Matrix_Scale.txt");
		jinanOD.outputUserMatrix("/Data/User_Matrix_Scale.txt");
		
		//jinanOD.outputFlow("/Data/Flows.csv");
		//jinanOD.outputTAZ("/Data/TAZs.csv");
		
		System.out.println("Total Number of Users Inferred: " + users.size());
		System.out.println("Total Number of Trips Identified: " + jinanOD.getTotalTrips());
		
		long timeEnd = System.currentTimeMillis();
		System.out.println("Completed! Computation Time: " + (timeEnd - timeStart) / 1000.0 + " seconds.");
	}
	
	private static void readCDRfromFile(String fileDirectory) {
		textReader reader = new textReader(fileDirectory);
		CDR[] CDRs = reader.nextUserCDR();
		int n = 0;
		do {
			//User newUser = new User(CDRs);
			UserOD newUserOD = new UserOD(CDRs, zones);
			//User newUser = new User(newUserOD);
			//Methods.printMemory();
			//users.add(newUser);
			jinanOD.add(newUserOD);
			// if (n <= 10) newUserOD.printTrips();
			n++;
//			if (n % 1000 == 0) {
//				System.out.println(n + " mobile phone users processed...");
//			}
			CDRs = reader.nextUserCDR();
			
		} while (CDRs != null);
		//users.trimToSize();
	}
	
	private static void readAndWrite(String readFilePath, String writeFilePath) {
		textReader reader = new textReader(readFilePath);
		textWriter writer = new textWriter(writeFilePath);
		writer.write("callNum;towerNum;start;end;preLocRank;sucLocRank;tripBefore;otherLoc;tripProb;dist;trip\n");
		CDR[] CDRs = reader.nextUserCDR();
		int n = 0;
		do {
			//User newUser = new User(CDRs);
			UserOD newUserOD = new UserOD(CDRs, zones);
			UserOD.Gap gap = newUserOD.pickRandomGap();
			//Methods.printMemory();
			
			if (gap != null) {
				int callNum = newUserOD.getCallRecords();
				int towerNum = newUserOD.getCallTowers();
				int start = gap.start;
				int end = gap.end;
				int preLocRank = gap.preLocRank;
				int sucLocRank = gap.sucLocRank;
				int tripBefore = gap.tripBefore;
				int otherLoc = gap.otherLoc;
				double tripProb = gap.tripProb;
				double dist = gap.dist;
				int trip = gap.trip;
				writer.write(callNum+";"+towerNum+";"+start+";"+end+";"+preLocRank+";"+sucLocRank+";"+tripBefore+";"+otherLoc+";"+tripProb+";"+dist+";"+trip+"\n");
			}
			
			//users.add(newUser);
			//jinanOD.add(newUserOD);
			// if (n <= 10) newUserOD.printTrips();
			n++;
			if (n % 100 == 0) {
				System.out.println(n + " mobile phone users processed...");
			}
			CDRs = reader.nextUserCDR();
			
		} while (CDRs != null);
		//users.trimToSize();
		writer.close();
	}
	
	private static void outputUsers(String filepath) {
		textWriter writer = new textWriter(filepath);
		writer.write("id;cdrCount;towerCount;dispCount;ROG;MFT_lng;MFT_lat\n");
		for (int i = 0; i < users.size(); i++) {
			String id = users.get(i).id;
			int cdrCount = users.get(i).cdrCount;
			int towerCount = users.get(i).towerCount;
			int dispCount = users.get(i).dispCount;
			double ROG = users.get(i).ROG;
			double lng = users.get(i).MFT_lng;
			double lat =users.get(i).MFT_lat;
			writer.write(id+";"+cdrCount+";"+towerCount+";"+dispCount+";"+ROG+";"+lng+";"+lat+"\n");
		}
		writer.close();
	}
	
	private static void outputUserHours(String filepath) {
		textWriter writer = new textWriter(filepath);
		writer.write("id;hour;cdrNum;callNum;dataNum;towerNum;dispNum;ROG;returnProb;eDist;nDist;wDist;sDist;o_lng;o_lat;d_lng;d_lat\n");
		for (int i = 0; i < users.size(); i++) {
			String id = users.get(i).id;
			for (int j = 0; j < 24; j++) {
				int cdrNum = users.get(i).hourlyCDRNum[j];
				int callNum = users.get(i).hourlyCallNum[j];
				int dataNum = users.get(i).hourlyDataNum[j];
				int towerNum = users.get(i).hourlyTowerNum[j];
				int dispNum = users.get(i).hourlyDispNum[j];
				double ROG = users.get(i).hourlyROG[j];
				double returnProb = users.get(i).hourlyReturn[j];
				
				double e = users.get(i).eDist[j];
				double n = users.get(i).nDist[j];
				double w = users.get(i).wDist[j];
				double s = users.get(i).sDist[j];
				double o_lng = users.get(i).hourlyO_lng[j];
				double o_lat = users.get(i).hourlyO_lat[j];
				double d_lng = users.get(i).hourlyD_lng[j];
				double d_lat = users.get(i).hourlyD_lat[j];
				writer.write(id+";"+j+";"+cdrNum+";"+callNum+";"+dataNum+";"+towerNum+";"+dispNum+";"+ROG+";"+returnProb+";"+
						e+";"+n+";"+w+";"+s+";"+o_lng+";"+o_lat+";"+d_lng+";"+d_lat+"\n");
			}
		}
		writer.close();
	}
	
	private static void outputGaps(String filepath) {
		textWriter writer = new textWriter(filepath);
		writer.write("start;end;preLocRank;sucLocRank;tripBefore;otherLoc;trip\n");
		for (int i = 0; i < gaps.size(); i++) {
			int start = gaps.get(i).start;
			int end = gaps.get(i).end;
			int preLocRank = gaps.get(i).preLocRank;
			int sucLocRank = gaps.get(i).sucLocRank;
			int tripBefore = gaps.get(i).tripBefore;
			int otherLoc = gaps.get(i).otherLoc;
			int trip = gaps.get(i).trip;
			writer.write(start+";"+end+";"+preLocRank+";"+sucLocRank+";"+tripBefore+";"+otherLoc+";"+trip+"\n");
		}
		writer.close();
	}
	
}
