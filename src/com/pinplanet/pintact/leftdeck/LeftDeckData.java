package com.pinplanet.pintact.leftdeck;

public class LeftDeckData {
	private int firstIcon;
	private String Option;
	private int secondIcon;
	
	public LeftDeckData(int f, String o, int s){
		this.firstIcon = f;
		this.Option = o;
		this.secondIcon = s;
	}
	
	String getOption() {
		return Option;
	}

	int getFirstIcon() {
		return firstIcon;
	}

	int getSecondIcon() {
		return secondIcon;
	}
}
