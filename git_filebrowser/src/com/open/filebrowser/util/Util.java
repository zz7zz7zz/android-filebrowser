package com.open.filebrowser.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.media.ExifInterface;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

public class Util
{
	
	public static byte[] intToByteArray(int a) {
		byte[] ret = new byte[4];
		ret[3] = (byte) (a & 0xFF);
		ret[2] = (byte) ((a >> 8) & 0xFF);
		ret[1] = (byte) ((a >> 16) & 0xFF);
		ret[0] = (byte) ((a >> 24) & 0xFF);
		return ret;
	}

	public static int bytesToInt(byte[] bytes, int pos) {
		int num = bytes[pos + 3] & 0xFF;
		num |= ((bytes[pos + 2] << 8) & 0xFF00);
		num |= ((bytes[pos + 1] << 16) & 0xFF0000);
		num |= ((bytes[pos] << 24) & 0xFF000000);
		return num;
	}
	

	private static SoftReference<Date> date_sof;
	private static SoftReference<SimpleDateFormat> dateFotmate_sof;

	/**
	 * 格式化时间
	 * 
	 * @param time
	 * @param formate
	 *            需要格式化的样式 eg: yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String transforTime(long time, String formate)
	{

		Date date = null;
		if (date_sof != null) {
			date = date_sof.get();
		}
		if (date == null) {
			date = new Date();
			date_sof = null;
			date_sof = new SoftReference<Date>(date);
		}
		date.setTime(time);
		SimpleDateFormat formatter = getFormat();
		formatter.applyPattern(formate);
		// new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String dateString = formatter.format(date);
		return dateString;
	}

	/**
	 * 根据传入参数, 格式化当前时间
	 * 
	 * @param formate
	 * @return
	 */
	public static String transforCurTime(String formate)
	{
		return transforTime(System.currentTimeMillis(), formate);
	}
	
	/**
	 * 解析时间
	 * @param strDate 
	 * @param str
	 * @return 毫秒
	 */
	public static long paserDate(String strDate,String str)
	{
		long result = 0;
		try {
			SimpleDateFormat format = getFormat();
			format.applyPattern(str);
			Date date = format.parse(strDate);
			result =  date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 得到一个时间格式化实例
	 * 
	 * @return
	 */
	public static SimpleDateFormat getFormat()
	{
		SimpleDateFormat formatter = null;
		if (dateFotmate_sof != null) {
			formatter = dateFotmate_sof.get();
		}
		if (formatter == null) {
			formatter = new SimpleDateFormat();
			dateFotmate_sof = null;
			dateFotmate_sof = new SoftReference<SimpleDateFormat>(formatter);
		}
		return formatter;
	}

	/**
	 * 得到当前手机sdcard路径
	 * 
	 * @return null/路径
	 */
	public static String getExternalStoragePath()
	{
		String state = android.os.Environment.getExternalStorageState();
		if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {
			return android.os.Environment.getExternalStorageDirectory().getPath();
		}
		return null;
	}


	/**
	 * 连接字符串
	 * 
	 * @param str
	 * @return
	 */
//	public static String appendString(CharSequence... str)
//	{
//		String result = null;
//		if (str != null && str.length > 0) {
//			StringBuilder builder = null;
//			if (STR_BUILDER_sof != null)
//				builder = STR_BUILDER_sof.get();
//			if (builder == null) {
//				builder = new StringBuilder();
//				STR_BUILDER_sof = null;
//				STR_BUILDER_sof = new SoftReference<StringBuilder>(builder);
//			}
//
//			for (CharSequence s : str) {
//				builder.append(s);
//			}
//			result = builder.toString();
//			builder.delete(0, builder.length());
//		}
//		return result;
//	}
	public static String appendString(CharSequence... str)
	{
		String result = null;
		if (str != null && str.length > 0) 
		{
			StringBuilder builder = new StringBuilder();

			for (CharSequence s : str) 
			{
				builder.append(s);
			}
			result = builder.toString();
		}
		return result;
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static byte[] MD5(CharSequence... value)
	{
		byte[] result = null;
		String tmp = null;
		if (value != null && value.length > 0) {
			tmp = appendString(value);
		}
		if (!TextUtils.isEmpty(tmp)) {
			try {
				final MessageDigest md5 = MessageDigest.getInstance("MD5");
				md5.update(tmp.getBytes());
				result = md5.digest();
			} catch (NoSuchAlgorithmException e) {
			}
		}
		return result;
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static String MD5String(String... value)
	{
		String result = null;
		byte[] data = MD5(value);
		if (data != null) {
			final char hexDigits[] = { // 用来将字节转换成 16 进制表示的字符
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
			char str[] = new char[16 * 2]; // 每个字节用 16 进制表示的话，使用两个字符，
			// 所以表示成 16 进制需要 32 个字符
			int k = 0; // 表示转换结果中对应的字符位置
			for (int i = 0; i < 16; i++) { // 从第一个字节开始，对 MD5 的每一个字节
				// 转换成 16 进制字符的转换
				byte byte0 = data[i]; // 取第 i 个字节
				str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换,
				// >>> 为逻辑右移，将符号位一起右移
				str[k++] = hexDigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
			}
			result = new String(str);
		}
		return result;
	}

	/** 执行Linux命令，并返回执行结果。 */
	public static String exec(String[] args)
	{
		String result = "";
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		Process process = null;
		InputStream errIs = null;
		InputStream inIs = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int read = -1;
			process = processBuilder.start();
			errIs = process.getErrorStream();
			while ((read = errIs.read()) != -1) {
				baos.write(read);
			}
			baos.write('\n');
			inIs = process.getInputStream();
			while ((read = inIs.read()) != -1) {
				baos.write(read);
			}
			byte[] data = baos.toByteArray();
			result = new String(data);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (errIs != null) {
					errIs.close();
				}
				if (inIs != null) {
					inIs.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (process != null) {
				process.destroy();
			}
		}
		return result;
	}
    
    public static void cancelDialog(Dialog dialog){
    	if(dialog!=null)
    	{
    		dialog.cancel();
    		dialog=null;
    	}
    }	

    public static final int NO_STORAGE_ERROR = -1;
    public static final int CANNOT_STAT_ERROR = -2;
    public static void setExifOrientation(String filepath,int orientation){
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            Log.e("setExifOrientation", "cannot read exif", ex);
        }
        if (exif != null) 
        {
            switch(orientation) 
            {
                case 0:{
                	exif.setAttribute(ExifInterface.TAG_ORIENTATION,String.valueOf(ExifInterface.ORIENTATION_NORMAL));
                }break;
                case 90:{
                	exif.setAttribute(ExifInterface.TAG_ORIENTATION,String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
                }break;
                case 180:{
                	exif.setAttribute(ExifInterface.TAG_ORIENTATION,String.valueOf(ExifInterface.ORIENTATION_ROTATE_180));
                }break;
                case 270:{
                	exif.setAttribute(ExifInterface.TAG_ORIENTATION,String.valueOf(ExifInterface.ORIENTATION_ROTATE_270));
                }break;
                default:{
                	exif.setAttribute(ExifInterface.TAG_ORIENTATION,String.valueOf(ExifInterface.ORIENTATION_NORMAL));
                }
            }
            try {
                exif.saveAttributes();
            } catch (IOException e) {
                 Log.e("setExifOrientation", "cannot save exif", e);
            }
        }
    }
    public static int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            Log.e("getExifOrientation", "cannot read exif", ex);
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                // We only recognize a subset of orientation tag values.
                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }

            }
        }
        return degree;
    }
    public static int roundOrientation(int orientation) {
        return ((orientation + 45) / 90 * 90) % 360;
    }
    public static int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        switch (rotation) {
            case Surface.ROTATION_0: return 0;
            case Surface.ROTATION_90: return 90;
            case Surface.ROTATION_180: return 180;
            case Surface.ROTATION_270: return 270;
        }
        return 0;
    }
    public static int calculatePicturesRemaining() {
        try {
            if (!hasStorage()) {
                return NO_STORAGE_ERROR;
            } else {
                String storageDirectory =
                        Environment.getExternalStorageDirectory().toString();
                StatFs stat = new StatFs(storageDirectory);
                final int PICTURE_BYTES = 1024*1024;
                float remaining = ((float) stat.getAvailableBlocks()
                        * (float) stat.getBlockSize()) / PICTURE_BYTES;
                return (int) remaining;
            }
        } catch (Exception ex) {
            return CANNOT_STAT_ERROR;
        }
    }

    public static boolean hasStorage() {
        return hasStorage(true);
    }
    public static boolean hasStorage(boolean requireWriteAccess) {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (requireWriteAccess) {
                boolean writable = checkFsWritable();
                return writable;
            } else {
                return true;
            }
        } else if (!requireWriteAccess
                && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
    private static boolean checkFsWritable() {
        // Create a temporary file to see whether a volume is really writeable.
        // It's important not to put it in the root directory which may have a
        // limit on the number of files.
        String directoryName =
                Environment.getExternalStorageDirectory().toString()+"/DCIM";
        File directory = new File(directoryName);
        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                return false;
            }
        }
        return directory.canWrite();
    }
	/** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
    public static int pxTodip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    }  
    /** 
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
     */  
        public static int dipTopx(Context context, float dpValue) {  
            final float scale = context.getResources().getDisplayMetrics().density;  
           return (int) (dpValue * scale + 0.5f);  
        }
    /**
     * 递归扫描指定文件夹文件
     * @param filePath  目标文件夹
     * @param list 目标文件添加列表
     * @param filter 文件名过滤器
     */
	public static void scaneFiles(File filePath,ArrayList<File> list,FilenameFilter fileFilter,FilenameFilter dirFilter){
		if(filePath==null||(!filePath.exists())||list==null)return ;
		File[] files = filePath.listFiles();
		for(int i=0;i<files.length;i++){
			if(files[i].isDirectory())
			{
				if(dirFilter!=null){
				    if(dirFilter.accept(files[i],files[i].getName()))
				    scaneFiles(files[i], list, fileFilter,dirFilter);
				}else{
				    scaneFiles(files[i], list, fileFilter,dirFilter);
				}
			}
			else{
				if(fileFilter!=null){
					if(fileFilter.accept(filePath, files[i].getName()))
						list.add(files[i]);
				}else {
					list.add(files[i]);
				}
			}
		}
	}
	/**
	 * 保存图片
	 * @param data
	 * @param start
	 * @param length
	 * @param path
	 * @return true 成功/失败
	 */
	public static boolean save(byte[] data, int start , int length , String path, boolean append)
	{
		try {
			int lastSpe = path.lastIndexOf(File.separator);
			String dirPath = path.substring(0, lastSpe);
			// 确认父文件夹是否创建
			File f = new File(dirPath);
			if (!f.exists()) {
				f.mkdirs();
			}
			f = null;
			f = new File(path);
			FileOutputStream os = new FileOutputStream(f,append);
			os.write(data, start, length);
			os.flush();
			os.getFD().sync();
			os.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * 默认缩略图路径 /sdcard/UIPAI_Thumn/
	 */
	public static final String THUM_PATH = "UIPAI_Thumn";
	
	
}
