package com.open.filebrowser.ui;

import android.app.Application;

import com.open.filebrowser.util.FileInitUitl;

public class EduApp extends Application
{
	private static EduApp instance;
	
    public static int SCREEN_WIDTH = 480;
    public static int SCREEN_HEIGHT = 800;
    public static int SCREEN_STATUS_HEIGHT=35;
	public static int PHOTOW = 480;
	public static int PHOTOH = 640;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		instance = this;
		initData();
		
		FileInitUitl.initLogo();
		FileInitUitl.initConfig();
		FileInitUitl.initIcon();
	}

	public static EduApp getInstance()
	{
		return instance;
	}
	
	private void initData()
	{
		SCREEN_WIDTH =getResources().getDisplayMetrics().widthPixels; 	
		SCREEN_HEIGHT =getResources().getDisplayMetrics().heightPixels; 
		SCREEN_WIDTH=Math.min(SCREEN_WIDTH, SCREEN_HEIGHT);
		SCREEN_HEIGHT=Math.max(SCREEN_WIDTH, SCREEN_HEIGHT);
		
		PHOTOW = SCREEN_WIDTH;
		PHOTOH = PHOTOW*4/3;
	}
}
