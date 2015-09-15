package com.pinplanet.pintact.utility;

import com.google.gson.annotations.SerializedName;

public class DataProfile {

	@SerializedName("accessToken")
	String userName;
	
	@SerializedName("userId")
	String passWord;
	
	public DataProfile (String name, String pwd) 
	{
		userName = name;
		passWord = pwd;
	}
}
