package jinanCDR;

import java.util.ArrayList;
import java.util.HashSet;

public class Tower {
	int lac;
	int cellid;
	double lng;
	double lat;
	ArrayList<CDR> cdrList;

	public Tower(CDR call) {
		lac = call.lac;
		cellid = call.cellid;
		lng = call.lng;
		lat = call.lat;
		cdrList = new ArrayList<>();
		cdrList.add(call);
	}
	
	public Tower(Transaction call) {
		lac = call.lac;
		cellid = call.cellid;
	}
	
	public int getDays() {
		HashSet<Integer> days = new HashSet<>();
		for (int i = 0; i < cdrList.size(); i++) {
			if (!days.contains(cdrList.get(i).date)) {
				days.add(cdrList.get(i).date);
			}
		}
		return days.size();
	}
	
	public int getLac() {
		return lac;
	}

	public void setLac(int lac) {
		this.lac = lac;
	}

	public int getCellid() {
		return cellid;
	}

	public void setCellid(int cellid) {
		this.cellid = cellid;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

}
