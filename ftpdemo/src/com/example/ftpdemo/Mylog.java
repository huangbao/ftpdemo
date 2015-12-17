package com.example.ftpdemo;

import android.text.TextUtils;
import android.util.Log;

public class Mylog {

	
	
	public void i(String txt){
		if(!TextUtils.isEmpty(txt)){
			Log.i(MainActivity.TAG, txt);
		}
	}

}
