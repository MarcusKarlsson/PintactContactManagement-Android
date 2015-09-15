package com.pinplanet.pintact.profile;

public class ProfileContentData {
	private String type;
	private String content;
	private int firstIcon;
	private int secondIcon;
	
	public ProfileContentData(String t, String c, int f, int s){
		this.type = t;
		this.content = c;
		this.firstIcon = f;
		this.secondIcon = s;
	}
	
	String getType() {
		return type;
	}

	String getContent() {
		return content;
	}

	int getFirstIcon() {
		return firstIcon;
	}

	int getSecondIcon() {
		return secondIcon;
	}
}
