package jinanCDR;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;


public class textReader {
	BufferedReader br;
	private CDR lastReadCDR = null;
	//private static final SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-DD hh:mm:ss");
	private final ArrayList<CDR> CDRs = new ArrayList<CDR>();
	
	public textReader(String path) {
		try {
			br = new BufferedReader(new java.io.FileReader(path));
			
			// By default the first line of the input file is header. If not, the system will issue a warning.
			String header = br.readLine();
			StringTokenizer t = new StringTokenizer(header, ";");
			if (t.nextToken().length() == 32) {
				System.out.println("WARNING: There is no header! Please revise textReader accordingly!");
			}
			
		}
		catch (FileNotFoundException nf) {
			System.err.println(nf);
			System.exit(1);
		}
		catch (IOException ex) {
			System.err.println(ex);
			System.exit(1);
		}
	}
	
	public void close() {
		try {
			br.close();
		}
		catch (IOException ex) {
			System.err.println(ex);
			System.exit(1);
		}
	}
	
	public CDR nextCDR() {
		String line = null;
		try {
			line = br.readLine();
		}
		catch (IOException ex) {
			lastReadCDR = null;
			return null;
		}
		if (line == null) {
			close();
			lastReadCDR = null;
			return null;
		}
		
		CDR nextCDR = new CDR();
		StringTokenizer t = new StringTokenizer(line, ";");
		nextCDR.id = t.nextToken();
		nextCDR.date = (Integer.parseInt(t.nextToken()));
		nextCDR.time = (Integer.parseInt(t.nextToken()));
		nextCDR.lac = (Integer.parseInt(t.nextToken()));
		nextCDR.cellid = (Integer.parseInt(t.nextToken()));
		nextCDR.lng = (Double.parseDouble(t.nextToken()));
		nextCDR.lat = (Double.parseDouble(t.nextToken()));
		nextCDR.eventid = (Integer.parseInt(t.nextToken()));
		lastReadCDR = nextCDR;
		return nextCDR;
	}
	
	public CDR[] nextUserCDR() {
		CDRs.clear();
		if (lastReadCDR == null) {
			if (nextCDR() == null) {
				return null; // end of file
			}
		}
		CDR firstCDR = lastReadCDR;
		CDRs.add(firstCDR);
		while (true) {
			String prevID = lastReadCDR.id;
			CDR next = nextCDR();
			if (next != null && next.id.equals(prevID)) {
				CDRs.add(next);
			}
			else {
				break;
			}
		}
		if (CDRs.size() > 0) {
			return CDRs.toArray(new CDR[0]);
		}
		else {
			return null;
		}
	}
	
}
