package jinanCDR;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.util.ArrayList;

public class Demo_UserClassification {
	
	final static String inputCDR = "/Data/nov_freq_user_sample.csv";
	final static TAZ zones = new TAZ("/Data/verticepoint.csv");
	final static String inputUserClasses = "/Data/UserClassCentroids.csv";
	
	static SystemOD jinanOD;
	static ArrayList<User> users = new ArrayList<User>();
	static ArrayList<UserOD.Gap> gaps = new ArrayList<>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
			long timeStart = System.currentTimeMillis();
			System.out.println("Initializing...");
			
			jinanOD = new SystemOD(zones);
			readAndWriteGaps(inputCDR, inputUserClasses, "/Data/Gaps_Clustered_v2.txt");
			
			System.out.println("Total Number of Users Inferred: " + users.size());
			System.out.println("Total Number of Trips Identified: " + jinanOD.getTotalTrips());
			
			long timeEnd = System.currentTimeMillis();
			System.out.println("Completed! Computation Time: " + (timeEnd - timeStart) / 1000.0 + " seconds.");
	}
	
	private static void readAndWriteGaps(String readFilePath, String inputUserClassesFilePath, String writeFilePath) {
		ArrayList<double[]> userClasses = new ArrayList<double[]>();
		userClasses = readUserClasses(inputUserClassesFilePath);
		textReader reader = new textReader(readFilePath);
		textWriter writer = new textWriter(writeFilePath);
		writer.write("userClass;callNum;towerNum;activeHours;locNum;dispNum;start;end;preLocRank;sucLocRank;tripBefore;otherLoc;tripProb;dist;trip\n");
		CDR[] CDRs = reader.nextUserCDR();
		int n = 0;
		int[] classSize = new int[3];
		do {
			//User newUser = new User(CDRs);
			UserOD newUserOD = new UserOD(CDRs, zones);
			int userClass = userClassAssignment(newUserOD.getCallPattern(), userClasses);
			UserOD.Gap gap = newUserOD.pickRandomGap();
			//Methods.printMemory();
			
			if (gap != null) {
				int callNum = newUserOD.getCallRecords();
				int towerNum = newUserOD.getCallTowers();
				int activeHours = newUserOD.getActiveCallHours();
				int locNum = newUserOD.getCallLocs();
				int dispNum = newUserOD.getCallDisps();
				int start = gap.start;
				int end = gap.end;
				int preLocRank = gap.preLocRank;
				int sucLocRank = gap.sucLocRank;
				int tripBefore = gap.tripBefore;
				int otherLoc = gap.otherLoc;
				double tripProb = gap.tripProb;
				double dist = gap.dist;
				int trip = gap.trip;
				writer.write(userClass+";"+callNum+";"+towerNum+";"+activeHours+";"+locNum+";"+dispNum+";"+start+";"+end+";"+preLocRank+";"+sucLocRank+";"+tripBefore+";"+otherLoc+";"+tripProb+";"+dist+";"+trip+"\n");
				classSize[userClass-1]++;
			}
			
			//users.add(newUser);
			//jinanOD.add(newUserOD);
			// if (n <= 10) newUserOD.printTrips();
			n++;
			if (n % 1000 == 0) {
				System.out.println(n + " mobile phone users processed...");
				System.out.println("Cluster 1: "+classSize[0]+"; Cluster 2: "+classSize[1]+"; Cluster 3: "+classSize[2]);
			}
			CDRs = reader.nextUserCDR();
			
		} while (CDRs != null);
		//users.trimToSize();
		writer.close();
		System.out.println("Cluster 1: "+classSize[0]+"; Cluster 2: "+classSize[1]+"; Cluster 3: "+classSize[2]);
	}
	
	private static ArrayList<double[]> readUserClasses(String inputFilePath) {
		ArrayList<double[]> classes = new ArrayList<double[]>();
		
		BufferedReader br = null;
		String line = "";
		String csvSplitBy = ",";
		int classCounter = 0;
		
		try {
			br = new BufferedReader(new FileReader(inputFilePath));
			while ((line = br.readLine()) != null) {
				String[] centroid = line.split(csvSplitBy);
				int N = centroid.length;
				double[] newClass = new double[N];
				for (int i = 0; i < N; i++) {
					newClass[i] = Double.parseDouble(centroid[i]);
					// System.out.println(newClass[i]);
				}
				classes.add(newClass);
				classCounter++;
			}
			System.out.println(classCounter + " user classes imported...");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return classes;
	}
	
	private static int userClassAssignment(int[] pattern, ArrayList<double[]> classes) {
		if (pattern == null || classes == null || pattern.length != classes.get(0).length) {
			return -9999;
		}
		int nDimen = pattern.length;
		int nClass = classes.size();
		double minDist = Double.MAX_VALUE;
		int Assignment = -1;
		for (int k = 0; k < nClass; k++) {
			double dist = 0.0;
			for (int d = 0; d < nDimen; d++) {
				double diff = pattern[d] - classes.get(k)[d];
				dist += diff * diff;
			}
			if (dist < minDist) {
				minDist = dist;
				Assignment = k + 1;
			}
		}
		return Assignment;
	}

}
