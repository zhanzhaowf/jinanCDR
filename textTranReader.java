package jinanCDR;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class textTranReader {
	BufferedReader br;
	private Transaction lastReadTran = null;
	//private static final SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-DD hh:mm:ss");
	private final ArrayList<Transaction> Trans = new ArrayList<Transaction>();
	
	public textTranReader(String path) {
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
	
	public Transaction nextTran() {
		String line = null;
		try {
			line = br.readLine();
		}
		catch (IOException ex) {
			lastReadTran = null;
			return null;
		}
		if (line == null) {
			close();
			lastReadTran = null;
			return null;
		}
		
		Transaction nextTran = new Transaction();
		StringTokenizer t = new StringTokenizer(line, ";");
		nextTran.id = t.nextToken();
		nextTran.date = (Integer.parseInt(t.nextToken()));
		nextTran.time = (Integer.parseInt(t.nextToken()));
		nextTran.lac = (Integer.parseInt(t.nextToken()));
		nextTran.cellid = (Integer.parseInt(t.nextToken()));
		nextTran.eventid = (Integer.parseInt(t.nextToken()));
		lastReadTran = nextTran;
		return nextTran;
	}
	
	public Transaction[] nextUserTransaction() {
		Trans.clear();
		if (lastReadTran == null) {
			if (nextTran() == null) {
				return null; // end of file
			}
		}
		Transaction firstTran = lastReadTran;
		Trans.add(firstTran);
		while (true) {
			String prevID = lastReadTran.id;
			Transaction next = nextTran();
			if (next != null && next.id.equals(prevID)) {
				Trans.add(next);
			}
			else {
				break;
			}
		}
		if (Trans.size() > 0) {
			return Trans.toArray(new Transaction[0]);
		}
		else {
			return null;
		}
	}
}
