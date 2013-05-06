package com.open.filebrowser.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

import com.open.filebrowser.ui.EduApp;

import android.os.Environment;


/**
 * 
 * 涓庢枃浠剁浉鍏崇殑绫�涓昏璐熻矗鏂囦欢鐨勮鍐�
 * 
 * @author 鏉ㄩ緳杈�2012.04.07
 * 
 */
public final class FileUtil
{

	// ------------------------------ 鎵嬫満绯荤粺鐩稿叧 ------------------------------
	public static final String NEWLINE = System.getProperty("line.separator");// 绯荤粺鐨勬崲琛岀
	public static final String APPROOT = "edu";// 绋嬪簭鐨勬牴鐩綍
	public static final String ASSERT_PATH="file:///android_asset";//apk鐨刟ssert鐩綍
	public static final String RES_PATH="file:///android_res";//apk鐨刟ssert鐩綍
	
	//----------------------------------瀛樻斁鏂囦欢鐨勮矾寰勫悗缂�-----------------------------------
	public static final String CACHE_File_SUFFIX=File.separator + APPROOT+ File.separator ;

	// ------------------------------------鏁版嵁鐨勭紦瀛樼洰褰�------------------------------------------------------
	public static String SDCARD_PAHT ;// SD鍗¤矾寰�
	public static String LOCAL_PATH ;// 鏈湴璺緞,鍗�data/data/鐩綍涓嬬殑绋嬪簭绉佹湁鐩綍
	public static String CURRENT_PATH = "";// 褰撳墠鐨勮矾寰�濡傛灉鏈塖D鍗＄殑鏃跺�褰撳墠璺緞涓篠D鍗★紝濡傛灉娌℃湁鐨勮瘽鍒欎负绋嬪簭鐨勭鏈夌洰褰�
	
	public static String CACHE_IMAGE;
	public static String CACHE_IMAGE_LOGO;
	public static String CACHE_IMAGE_ICON;
	public static String CACHE_IMAGE_CONFIG;
	public static String CACHE_IMAGE_RESOURCE;
	public static String CACHE_IMAGE_COMMON;//甯哥敤缂撳瓨
	
	static
	{
		init();
	}

	public static void init()
	{
		SDCARD_PAHT = Environment.getExternalStorageDirectory().getPath();// SD鍗¤矾寰�
		LOCAL_PATH = EduApp.getInstance().getApplicationContext().getFilesDir().getAbsolutePath();// 鏈湴璺緞,鍗�data/data/鐩綍涓嬬殑绋嬪簭绉佹湁鐩綍
		
		if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
		{
			CURRENT_PATH = SDCARD_PAHT;
		} 
		else
		{
			CURRENT_PATH = LOCAL_PATH;
		}

		CACHE_IMAGE = CURRENT_PATH + CACHE_File_SUFFIX;
		CACHE_IMAGE_LOGO = CACHE_IMAGE +"logo"+File.separator;
		CACHE_IMAGE_ICON = CACHE_IMAGE +"icon"+File.separator;
		CACHE_IMAGE_CONFIG= CACHE_IMAGE+"config"+File.separator;
		CACHE_IMAGE_RESOURCE= CACHE_IMAGE+"resource"+File.separator;
		CACHE_IMAGE_COMMON= CACHE_IMAGE+"common"+File.separator;
	}

	/**
	 * 寰楀埌涓庡綋鍓嶅瓨鍌ㄨ矾寰勭浉鍙嶇殑璺緞(褰撳墠涓�data/data鐩綍锛屽垯杩斿洖/sdcard鐩綍;褰撳墠涓�sdcard锛屽垯杩斿洖/data/data鐩綍)
	 * @return
	 */
	public static String getDiffPath()
	{
		if(CURRENT_PATH.equals(SDCARD_PAHT))
		{
			return LOCAL_PATH;
		}
		return SDCARD_PAHT;
	}
	
	
	public static String getDiffPath(String pathIn)
	{
		return pathIn.replace(CURRENT_PATH, getDiffPath());
	}

	// ------------------------------------鏂囦欢鐨勭浉鍏虫柟娉�-------------------------------------------
	/**
	 * 灏嗘暟鎹啓鍏ヤ竴涓枃浠�
	 * 
	 * @param destFilePath
	 *            瑕佸垱寤虹殑鏂囦欢鐨勮矾寰�
	 * @param data
	 *            寰呭啓鍏ョ殑鏂囦欢鏁版嵁
	 * @param startPos
	 *            璧峰鍋忕Щ閲�
	 * @param length
	 *            瑕佸啓鍏ョ殑鏁版嵁闀垮害
	 * @return 鎴愬姛鍐欏叆鏂囦欢杩斿洖true,澶辫触杩斿洖false
	 */
	public static boolean writeFile(String destFilePath, byte[] data, int startPos, int length)
	{
		try
		{
			if (!createFile(destFilePath))
			{
				return false;
			}
			FileOutputStream fos = new FileOutputStream(destFilePath);
			fos.write(data, startPos, length);
			fos.flush();
			if (null != fos)
			{
				fos.close();
				fos = null;
			}
			return true;

		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 浠庝竴涓緭鍏ユ祦閲屽啓鏂囦欢
	 * 
	 * @param destFilePath
	 *            瑕佸垱寤虹殑鏂囦欢鐨勮矾寰�
	 * @param in
	 *            瑕佽鍙栫殑杈撳叆娴�
	 * @return 鍐欏叆鎴愬姛杩斿洖true,鍐欏叆澶辫触杩斿洖false
	 */
	public static boolean writeFile(String destFilePath, InputStream in)
	{
		try
		{
			if (!createFile(destFilePath))
			{
				return false;
			}
			FileOutputStream fos = new FileOutputStream(destFilePath);
			int readCount = 0;
			int len = 1024;
			byte[] buffer = new byte[len];
			while ((readCount = in.read(buffer)) != -1)
			{
				fos.write(buffer, 0, readCount);
			}
			fos.flush();
			if (null != fos)
			{
				fos.close();
				fos = null;
			}
			if (null != in)
			{
				in.close();
				in = null;
			}
			return true;
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		return false;
	}
	
	public static boolean appendFile(String filename,byte[]data,int datapos,int datalength)
	{
		try {
			
			createFile(filename);
			
			RandomAccessFile rf= new RandomAccessFile(filename, "rw");
			rf.seek(rf.length());
			rf.write(data, datapos, datalength);
			if(rf!=null)
			{
				rf.close();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * 璇诲彇鏂囦欢锛岃繑鍥炰互byte鏁扮粍褰㈠紡鐨勬暟鎹�
	 * 
	 * @param filePath
	 *            瑕佽鍙栫殑鏂囦欢璺緞鍚�
	 * @return
	 */
	public static byte[] readFile(String filePath)
	{
		try
		{
			if (isFileExist(filePath))
			{
				FileInputStream fi = new FileInputStream(filePath);
				return readInputStream(fi);
			}
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 浠庝竴涓暟閲忔祦閲岃鍙栨暟鎹�杩斿洖浠yte鏁扮粍褰㈠紡鐨勬暟鎹�
	 * </br></br>
	 * 闇�娉ㄦ剰鐨勬槸锛屽鏋滆繖涓柟娉曠敤鍦ㄤ粠鏈湴鏂囦欢璇诲彇鏁版嵁鏃讹紝涓�埇涓嶄細閬囧埌闂锛屼絾濡傛灉鏄敤浜庣綉缁滄搷浣滐紝灏辩粡甯镐細閬囧埌涓�簺楹荤儲(available()鏂规硶鐨勯棶棰�銆傛墍浠ュ鏋滄槸缃戠粶娴佷笉搴旇浣跨敤杩欎釜鏂规硶銆�
	 * @param in
	 *            瑕佽鍙栫殑杈撳叆娴�
	 * @return
	 * @throws IOException
	 */
	public static byte[] readInputStream(InputStream in)
	{
		try
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			byte[] b = new byte[in.available()];
			int length = 0;
			while ((length = in.read(b)) != -1)
			{
				os.write(b, 0, length);
			}

			b = os.toByteArray();

			in.close();
			in = null;

			os.close();
			os = null;

			return b;

		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 璇诲彇缃戠粶娴�
	 * @param in
	 * @return
	 */
	public static byte[] readNetWorkInputStream(InputStream in)
	{
		ByteArrayOutputStream os=null;
		try
		{
			os = new ByteArrayOutputStream();
			
			int readCount = 0;
			int len = 1024;
			byte[] buffer = new byte[len];
			while ((readCount = in.read(buffer)) != -1)
			{
				os.write(buffer, 0, readCount);
			}

			in.close();
			in = null;

			return os.toByteArray();

		} catch (IOException e)
		{
			e.printStackTrace();
		}finally{
			if(null!=os)
			{
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				os = null;
			}
		}
		return null;
	}

	/**
	 * 灏嗕竴涓枃浠舵嫹璐濆埌鍙﹀涓�釜鍦版柟
	 * @param sourceFile 婧愭枃浠跺湴鍧�
	 * @param destFile 鐩殑鍦板潃
	 * @param shouldOverlay 鏄惁瑕嗙洊
	 * @return
	 */
	public static boolean copyFiles(String sourceFile, String destFile,boolean shouldOverlay)
	{
		try
		{
			if(shouldOverlay)
			{
				deleteFile(destFile);
			}
			FileInputStream fi = new FileInputStream(sourceFile);
			writeFile(destFile, fi);
			return true;
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 鍒ゆ柇鏂囦欢鏄惁瀛樺湪
	 * 
	 * @param filePath
	 *            璺緞鍚�
	 * @return
	 */
	public static boolean isFileExist(String filePath)
	{
		File file = new File(filePath);
		return file.exists();
	}

	/**
	 * 鍒涘缓涓�釜鏂囦欢锛屽垱寤烘垚鍔熻繑鍥瀟rue
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean createFile(String filePath)
	{
		try
		{
			File file = new File(filePath);
			if (!file.exists())
			{
				if (!file.getParentFile().exists())
				{
					file.getParentFile().mkdirs();
				}

				return file.createNewFile();
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	
	public static boolean createDir(String filePath)
	{
		try
		{
			File file = new File(filePath);
			file.mkdirs();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 鍒犻櫎涓�釜鏂囦欢
	 * 
	 * @param filePath
	 *            瑕佸垹闄ょ殑鏂囦欢璺緞鍚�
	 * @return true if this file was deleted, false otherwise
	 */
	public static boolean deleteFile(String filePath)
	{
		try {
			File file = new File(filePath);
			if (file.exists())
			{
				return file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 鍒犻櫎 directoryPath鐩綍涓嬬殑鎵�湁鏂囦欢锛屽寘鎷垹闄ゅ垹闄ゆ枃浠跺す
	 * @param directoryPath
	 */
	public static void deleteDirectory(File dir)
	{
		if (dir.isDirectory())  
	    {  
	        File[] listFiles = dir.listFiles();  
	        for (int i = 0; i < listFiles.length ; i++)  
	        {  
	        	deleteDirectory(listFiles[i]);
	        }  
	    }
	    dir.delete();  
	}

	/**
	 * 瀛楃涓茶浆娴�
	 * @param str
	 * @return
	 */
	public static InputStream String2InputStream(String str)
	{
		ByteArrayInputStream stream = new ByteArrayInputStream(str.getBytes());
		return stream;
	}

	/**
	 * 娴佽浆瀛楃涓�
	 * @param is
	 * @return
	 */
	public static String inputStream2String(InputStream is)
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		StringBuffer buffer = new StringBuffer();
		String line = "";

		try
		{
			while ((line = in.readLine()) != null)
			{
				buffer.append(line);
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return buffer.toString();
	}
	
	//鎵归噺鏇存敼鏂囦欢鍚庣紑
	public static void reNameSuffix(File dir,String oldSuffix,String newSuffix)
	{
		if (dir.isDirectory())  
	    {  
	        File[] listFiles = dir.listFiles();  
	        for (int i = 0; i < listFiles.length ; i++)  
	        {  
	        	reNameSuffix(listFiles[i],oldSuffix,newSuffix);
	        }  
	    }
		else
		{
			dir.renameTo(new File(dir.getPath().replace(oldSuffix, newSuffix)));
		}
	}
	
	
	public static String getMIMEType(String name) {
		String type = "";
		String end = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
		if (end.equals("apk")) {
			return "application/vnd.android.package-archive";
		} else if (end.equals("mp4") || end.equals("avi") || end.equals("3gp")
				|| end.equals("rmvb")) {
			type = "video";
		} else if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") || end.equals("xmf")
				|| end.equals("ogg") || end.equals("wav")) {
			type = "audio";
		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp")) {
			type = "image";
		} else if (end.equals("txt") || end.equals("log")) {
			type = "text";
		} else {
			type = "*";
		}
		type += "/*";
		return type;
	}
	
}
