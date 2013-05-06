package com.open.filebrowser.util;

import java.io.InputStream;

import com.open.filebrowser.ui.EduApp;

public class FileInitUitl {

	public static String logoPath=FileUtil.CACHE_IMAGE_LOGO+"logo.jpg";
	public static String configPath=FileUtil.CACHE_IMAGE_CONFIG+"config";
	public static void initLogo()
	{
		
		if(!FileUtil.isFileExist(logoPath))
		{
			try {
				FileUtil.writeFile(logoPath, EduApp.getInstance().getResources().getAssets().open("logo/logo.jpg"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void initConfig()
	{
		if(!FileUtil.isFileExist(configPath))
		{
			try {
				FileUtil.writeFile(configPath, EduApp.getInstance().getResources().getAssets().open("config/config"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void initIcon()
	{
		try {
				String []fileItems=EduApp.getInstance().getResources().getAssets().list("icon");
				for(int j=0;j<fileItems.length;j++)
				{
					String path=FileUtil.CACHE_IMAGE_ICON+fileItems[j];
					if(!FileUtil.isFileExist(path))
					{
						InputStream in=EduApp.getInstance().getResources().getAssets().open("icon/"+fileItems[j]);
						FileUtil.writeFile(path, in);
					}
				}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
