package jinanCDR;

public class Demo2 {
	final static String inputCDR = "/Data/cdr_sample_11.csv";
	final static String outputCallers = "/Data/CallPattern.txt";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long timeStart = System.currentTimeMillis();
		System.out.println("Initializing...");
		
		readAndWrite(inputCDR, outputCallers);
		
		long timeEnd = System.currentTimeMillis();
		System.out.println("Completed! Computation Time: " + (timeEnd - timeStart) / 1000.0 + " seconds.");
	}
	
	private static void readAndWrite(String inputFilePath, String outputFilePath) {
		textTranReader reader = new textTranReader(inputFilePath);
		textWriter writer = new textWriter(outputFilePath);
		//writer.write("id;cdrCount;towerCount;dispCount;callHours;dataHours;mixedHours;callCont;dataCont\n");
		Transaction[] CDRs = reader.nextUserTransaction();
		int n = 0;
		do {
			Caller newCaller = new Caller(CDRs);
			
//			String id = newCaller.id;
//			int cdrCount = newCaller.callCount;
//			int towerCount = newCaller.towerCount;
//			int dispCount = newCaller.dispCount;
//			int callHours = newCaller.activeCallHours;
//			int dataHours = newCaller.activeDataHours;
//			int mixedHours = newCaller.activeMixedHours;
//			double callCont = newCaller.callContinuity;
//			double dataCont = newCaller.dataContinuity;
//			writer.write(id+";"+cdrCount+";"+towerCount+";"+dispCount+";"+callHours+";"+dataHours+";"+mixedHours+";"+callCont+";"+dataCont+"\n");
			
			for (int i = 0; i < 720; i++) {
				writer.write(newCaller.callHours[i]+";");
			}
			writer.write("\n");
			
			n++;
			if (n % 1000 == 0) {
				System.out.println(n + " mobile phone users processed...");
				Methods.printMemory();
			}
			CDRs = reader.nextUserTransaction();
			
		} while (CDRs != null);
		writer.close();
	}
	
}
