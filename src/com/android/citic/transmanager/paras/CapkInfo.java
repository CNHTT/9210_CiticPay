package com.android.citic.transmanager.paras;

import com.pos.device.emv.CAPublicKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CapkInfo implements Serializable {
	
	public static final String FILENAME = "capk.dat";
	private List<CAPublicKey> capkList = new ArrayList<>();

	public List<CAPublicKey> getCapkList() {
		return capkList;
	}

	public void setCapkList(List<CAPublicKey> capkList) {
		this.capkList = capkList;
	}
}
