package com.open.filebrowser.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5加密算法
 */
public class MD5Util {

	private static String byte2hex(byte[] md5Bytes) {
		StringBuffer hexValue = new StringBuffer();
		int val=0;
		for (int i = 0; i < md5Bytes.length; i++) {
			val = (md5Bytes[i]) & 0xff;
			if (val < 16)
				hexValue.append("0");
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
	}
	
	public static String encrypt(String input){
		String result = "";
		byte[] bs = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			bs = md.digest(input.getBytes());
			result = byte2hex(bs);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 适用于小数组
	 * @param input
	 * @return
	 */
	public static String encrypt(byte[] input){
		String result = "";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte []bs = md.digest(input);
			result = byte2hex(bs);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static String generateMD5(String path){
		
		InputStream inputStream = null;
	    MessageDigest md;
	    try {
	    	inputStream=new FileInputStream(path);
	    	
	        int read =0;
	        byte[] buf = new byte[4096];
	        md = MessageDigest.getInstance("MD5");
	        while((read = inputStream.read(buf))>0)
	        {
	            md.update(buf,0,read);
	        }
	        byte[] hashValue = md.digest();
	        return byte2hex(hashValue);
	    } catch (NoSuchAlgorithmException e) {
	        return null;
	    } catch (Exception e) {
	        return null;
	    }finally{
	        try {
	            if(null!=inputStream)
	            {
	            	inputStream.close();
	            	inputStream=null;
	            }
	        }catch (Exception e) {
				e.printStackTrace();
			}
	    }
	} 
	
	public static String generateMD5(InputStream inputStream){
	    if(inputStream==null){
	        return null;
	    }
	    MessageDigest md;
	    try {
	        int read =0;
	        byte[] buf = new byte[4096];
	        md = MessageDigest.getInstance("MD5");
	        while((read = inputStream.read(buf))>0)
	        {
	            md.update(buf,0,read);
	        }
	        byte[] hashValue = md.digest();
	        return byte2hex(hashValue);
	    } catch (NoSuchAlgorithmException e) {
	        return null;
	    } catch (IOException e) {
	        return null;
	    }finally{
	        try {
	            if(null!=inputStream)
	            {
	            	inputStream.close();
	            	inputStream=null;
	            }
	        }catch (Exception e) {
				e.printStackTrace();
			}
	    }
	}  
	
	public static String generateMD5(SequenceInputStream inputStream){
	    if(inputStream==null){
	        return null;
	    }
	    MessageDigest md;
	    try {
	        int read =0;
	        byte[] buf = new byte[4096];
	        md = MessageDigest.getInstance("MD5");
	        while((read = inputStream.read(buf))>0)
	        {
	            md.update(buf,0,read);
	        }
	        byte[] hashValue = md.digest();
	        
	        return byte2hex(hashValue);
	    } catch (NoSuchAlgorithmException e) {
	        return null;
	    } catch (IOException e) {
	        return null;
	    }finally{
	        try {
	            if(null!=inputStream)
	            {
	            	inputStream.close();
	            	inputStream=null;
	            }
	        }catch (Exception e) {
				e.printStackTrace();
			}
	    }
	}
}
