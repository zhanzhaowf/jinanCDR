package jinanCDR;

import java.util.HashSet;

public class SystemOD {
	TAZ zones;
	int tazCount;
	Flow[] flows;
	int[] locNum;
	
	public SystemOD(TAZ tazs) {
		zones = tazs;
		tazCount = tazs.tazList.length;
		flows = new Flow[tazCount*tazCount];
		locNum = new int[tazCount];
		for (int i = 0; i < tazCount; i++) {
			locNum[i] = 0;
		}
		for (int i = 0; i < flows.length; i++) {
			flows[i] = new Flow();
			flows[i].oTAZ = i/tazCount;
			flows[i].dTAZ = i%tazCount;
			flows[i].tripVolume = 0;
			flows[i].userVolume = 0;
		}
	}
	
	public void add(UserOD od) {
		for (int i = 0; i < od.locList.length; i++) {
			if (od.locList[i].taz >= 0) {
				int tazIndex = od.locList[i].taz;
				locNum[tazIndex]++;
			}
		}
		
		if (od.tripList != null) {
			HashSet<Integer> odPairs = new HashSet<Integer>();
			for (int i = 0; i < od.tripList.length; i++) {
				int o = od.tripList[i].oTAZ;
				int d = od.tripList[i].dTAZ;
				if (o >= 0 && d >= 0) {
					int index = o * tazCount + d;
					flows[index].tripVolume++;
					if (!odPairs.contains(index)) {
						flows[index].userVolume++;
						odPairs.add(index);
					}
				}
			}
		}
	}
	
	public int[][] getTripODMatrix() {
		int[][] matrix = new int[tazCount][tazCount];
		for (int i = 0; i < flows.length; i++) {
			int o = i/tazCount;
			int d = i%tazCount;
			matrix[o][d] = flows[i].tripVolume;
		}
		return matrix;
	}
	
	public int[][] getUserODMatrix() {
		int[][] matrix = new int[tazCount][tazCount];
		for (int i = 0; i < flows.length; i++) {
			int o = i/tazCount;
			int d = i%tazCount;
			matrix[o][d] = flows[i].userVolume;
		}
		return matrix;
	}
	
	public void outputTripMatrix(String filename) {
		int[][] tripMatrix = getTripODMatrix();
		textWriter writer = new textWriter(filename);
		for (int i = 0; i < tazCount; i++) {
			for (int j = 0; j < tazCount; j++) {
				writer.write(tripMatrix[i][j] + "\t");
			}
			writer.write("\n");
		}
		writer.close();
	}
	
	public void outputUserMatrix(String filename) {
		int[][] userMatrix = getUserODMatrix();
		textWriter writer = new textWriter(filename);
		for (int i = 0; i < tazCount; i++) {
			for (int j = 0; j < tazCount; j++) {
				writer.write(userMatrix[i][j] + "\t");
			}
			writer.write("\n");
		}
		writer.close();
	}
	
	public void outputFlow(String filename) {
		textWriter writer = new textWriter(filename);
		writer.write("Source;Target;Weight;userVolume\n");
		for (int i = 0; i < flows.length; i++) {
			int o = flows[i].oTAZ + 1;
			int d = flows[i].dTAZ + 1;
			int tripVol = flows[i].tripVolume;
			int userVol = flows[i].userVolume;
			if (userVol > 10)
				writer.write(o+";"+d+";"+tripVol+";"+userVol+"\n");
		}
		writer.close();
	}
	
	public void outputTAZ(String filename) {
		textWriter writer = new textWriter(filename);
		writer.write("ID;LNG;LAT;locNum\n");
		for (int i = 0; i < tazCount; i++) {
			int N = zones.tazList[i].getVertexCount();
			int weight = 1;
			double lng = zones.tazList[i].getX(0);
			double lat = zones.tazList[i].getY(0);
			for (int j = 1; j < N; j++) {
				lng = (lng * weight + zones.tazList[i].getX(j)) / (weight + 1);
				lat = (lat * weight + zones.tazList[i].getY(j)) / (weight + 1);
				weight++;
			}
			int locCount = locNum[i];
			writer.write((i+1)+";"+lng+";"+lat+";"+locCount+"\n");
		}
		writer.close();
	}
	
	public int getTotalTrips() {
		int sum = 0;
		for (int i = 0; i < flows.length; i++) {
			sum = sum + flows[i].tripVolume;
		}
		return sum;
	}
	
	public class Flow {
		int oTAZ;
		int dTAZ;
		int tripVolume;
		int userVolume;
	}
}
