package jinanCDR;

import java.io.BufferedWriter;
import java.io.IOException;

public class textWriter {
BufferedWriter bw;
	
	public textWriter(String path) {
		try {
			bw = new BufferedWriter(new java.io.FileWriter(path));
			
		}
		catch (IOException ex) {
			System.err.println(ex);
			System.exit(1);
		}
	}
	
	public void close() {
		try {
			bw.close();
		}
		catch (IOException ex) {
			System.err.println(ex);
			System.exit(1);
		}
	}
	
	public void write(String str) {
		try {
			bw.write(str);
		}
		catch (IOException ex) {
			System.err.println(ex);
			System.exit(1);
		}
	}
}
