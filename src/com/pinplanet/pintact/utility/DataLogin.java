package com.pinplanet.pintact.utility;

import com.google.gson.annotations.SerializedName;

public class DataLogin {

	@SerializedName("login")
	String userName;
	
	@SerializedName("password")
	String passWord;
	
	public DataLogin (String name, String pwd) 
	{
		userName = name;
		passWord = pwd;
	}
}
