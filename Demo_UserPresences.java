package jinanCDR;

public class Demo3 {
	
	final static String inputCDR = "/Data/nov_freq_users.csv";
	final static TAZ zones = new TAZ("/Data/verticepoint.csv");
	final static String outputUserPresences = "/Data/UserPresences.txt";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long timeStart = System.currentTimeMillis();
		System.out.println("Initializing...");
		
		readAndWrite(inputCDR, outputUserPresences);
		long timeEnd = System.currentTimeMillis();
		System.out.println("Completed! Computation Time: " + (timeEnd - timeStart) / 1000.0 + " seconds.");
	}
	
	private static void readAndWrite(String readFilePath, String writeFilePath) {
		textReader reader = new textReader(readFilePath);
		textWriter writer = new textWriter(writeFilePath);
		writer.write("PersonID;StopID;Date;Start;End;Location;Longitude;Latitude;Distance\n");
		CDR[] CDRs = reader.nextUserCDR();
		int n = 0;
		do {
			UserOD newUserOD = new UserOD(CDRs, zones);
			if (newUserOD != null) {
				int preNum = newUserOD.presenceList.length;
				for (int i = 0; i < preNum; i++) {
					String id = newUserOD.id;
					int date = newUserOD.presenceList[i].date;
					int start = newUserOD.presenceList[i].startTime;
					int end = newUserOD.presenceList[i].endTime;
					int loc = newUserOD.presenceList[i].locIndex;
					double lng = newUserOD.presenceList[i].getLongitude();
					double lat = newUserOD.presenceList[i].getLatitude();
					double dist = 0.0;
					if (i > 0) dist = Methods.distance(lng, lat, newUserOD.presenceList[i-1].getLongitude(),  newUserOD.presenceList[i-1].getLatitude());
					writer.write(id+";"+i+";"+date+";"+start+";"+end+";"+loc+";"+lng+";"+lat+";"+dist+"\n");
				}
			}
			
			n++;
			if (n % 100 == 0) {
				System.out.println(n + " mobile phone users processed...");
			}
			CDRs = reader.nextUserCDR();
			
		} while (CDRs != null);
		//users.trimToSize();
		writer.close();
	}

}
