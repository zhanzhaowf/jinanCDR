package jinanCDR;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import diva.util.java2d.Polygon2D;
//import math.geom2d.Point2D;
//import math.geom2d.polygon.SimplePolygon2D;

public class TAZ {
	final int count = 607;
	Polygon2D.Double[] tazList = new Polygon2D.Double[count];
	
	public TAZ(String filename) {
		try {
			FileReader fin= new FileReader(filename);
			BufferedReader in= new BufferedReader(fin);
			tazList= readData(in);
			in.close();
		} catch(IOException e) {
			System.out.println(e);
		}
	}
	
	private Polygon2D.Double[] readData(BufferedReader in) throws IOException {
		String header = in.readLine();
		Polygon2D.Double[] tazList = new Polygon2D.Double[count];
		for (String str; (str = in.readLine()) != null;) {
			StringTokenizer t = new StringTokenizer(str, ",");
			int id = (Integer.parseInt(t.nextToken()));
			double lng = (Double.parseDouble(t.nextToken()));
			double lat = (Double.parseDouble(t.nextToken()));
			if (tazList[id-1] != null)
				tazList[id-1].lineTo(lng, lat);
			else
				tazList[id-1] = new Polygon2D.Double(lng, lat);
		}
		return tazList;
	}
	
}
