/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.open.filebrowser.util;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;


/**
 * Collection of utility functions used in this package.
 */
public class BitmapUtil {
    // If a new bitmap is created, the original bitmap is recycled.
    public static Bitmap rotate(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees,
                    (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(
                        b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                // We have no memory to rotate. Return the original bitmap.
            }
        }
        return b;
    }

    public static boolean equals(String a, String b) {
        // return true if both string are null or the content equals
        return a == b || a.equals(b);
    }
    
    /**
     * 将byte[]转化为Bitmap
     * @param jpegData
     * @param 采样率
     * @return
     */
    public static Bitmap getBitmapFromByte(byte[] jpegData,int CompressRate){
    	if(CompressRate>=0 && CompressRate<=100 && jpegData!=null)
    	{
    		BitmapFactory.Options opts=new BitmapFactory.Options();
    		opts.inSampleSize=Math.round((float)100/CompressRate);
    		return BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, opts);
    	}
    	return null;
    }
    /**
     * 
     * 保存图片
     * @param title 标题名称
     * @param dateTaken 拍照时间
     * @param tLocation 地理信息
     * @param directory 文件夹
     * @param filename 文件名
     * @param source Bitmap数据源
     * @param jpegData byte数据源
     * @param compress 压缩率
     * @param format 压缩格式
     * @return
     */
    public static boolean addImage( String directory, String filename,
            Bitmap source, byte[] jpegData,int compress,CompressFormat format) {
        OutputStream outputStream = null;
        boolean result = false;
        try {
            File dir = new File(directory);
            if (!dir.exists()) dir.mkdirs();
            File file = new File(directory, filename);
            outputStream = new FileOutputStream(file);
            if (source != null) {
                source.compress(format, compress, outputStream);
            } else {
            	if(jpegData == null)result = false;
                outputStream.write(jpegData);
            }
            Util.setExifOrientation(file.getAbsolutePath(),0);
            result = true;
        } catch (FileNotFoundException ex) {
            Log.w("addImage", ex);
            result = false;
        } catch (IOException ex) {
            Log.w("addImage", ex);
            result = false;
        } finally {
        	if(outputStream!=null)
        	{
                try
    			{
                	
    				outputStream.close();
    			}
    			catch (IOException e)
    			{
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
        	}
        }
        return result;
    }    
    /**
     * 保存图片
     * @param title 标题名称
     * @param dateTaken 拍照时间
     * @param tLocation 地理信息
     * @param directory 文件夹
     * @param filename 文件名
     * @param source Bitmap数据源
     * @param jpegData byte数据源
     * @param degree 角度
     * @return
     */
    public static boolean addImage( String directory, String filename,
            Bitmap source, byte[] jpegData,CompressFormat format) {
    	return addImage(directory,filename,source,jpegData,100,format);
    }    
    /**
     * 保存图片
     * @param cr 内容提供类
     * @param title 标题名称
     * @param dateTaken 拍照时间
     * @param location 地理信息
     * @param directory 文件夹
     * @param filename 文件名
     * @param source Bitmap数据源
     * @param jpegData byte数据源
     * @param degree 保存角度
     * @param compress 压缩率
     * @param format 保存格式
     * @return
     */
    public static Uri addImage(ContentResolver cr, String title, long dateTaken,
            Location location, String directory, String filename,
            Bitmap source, byte[] jpegData, int[] degree,int compress,CompressFormat format) {
    	
        OutputStream outputStream = null;
        String filePath = directory + "/" + filename;
        try {
            File dir = new File(directory);
            if (!dir.exists()) dir.mkdirs();
            File file = new File(directory, filename);
            outputStream = new FileOutputStream(file);
            if (source != null) {
                source.compress(format, compress, outputStream);
            } else {
                outputStream.write(jpegData);
            }
        } catch (FileNotFoundException ex) {
            Log.w("addImage", ex);
            return null;
        } catch (IOException ex) {
            Log.w("addImage", ex);
            return null;
        } finally {
        	if(outputStream!=null)
        	{
                try
    			{
                	
    				outputStream.close();
    			}
    			catch (IOException e)
    			{
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
        	}
        }
        ContentValues values = new ContentValues(8);
        values.put(Images.Media.TITLE, title);
        values.put(Images.Media.DISPLAY_NAME, filename);
        values.put(Images.Media.DATE_TAKEN, dateTaken);
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        if(degree!=null){
        	Util.setExifOrientation(filePath, degree[0]);
            values.put(Images.Media.ORIENTATION,degree[0]);
        }
        else {
        	values.put(Images.Media.ORIENTATION,0);
		}
        values.put(MediaColumns.DATA, filePath);
       
        if (location != null) {
            values.put(Images.Media.LATITUDE, location.getLatitude());
            values.put(Images.Media.LONGITUDE, location.getLongitude());
        }
        else {
            values.put(Images.Media.LATITUDE,0.0);
            values.put(Images.Media.LONGITUDE,0.0);			
		}
        
        return cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
    /**
     * 保存图片
     * @param cr 内容提供类
     * @param title 标题名称
     * @param dateTaken 拍照时间
     * @param location 地理信息
     * @param directory 文件夹
     * @param filename 文件名
     * @param source Bitmap数据源
     * @param jpegData byte数据源
     * @param degree 保存角度
     * @param format 保存格式
     * @return
     */
    public static Uri addImage(ContentResolver cr, String title, long dateTaken,
            Location location, String directory, String filename,
            Bitmap source, byte[] jpegData, int[] degree,CompressFormat format) {
    	return addImage(cr, title, dateTaken, location, directory, filename, source, jpegData, degree,100, format);
    }
    
    /**
     * 拍照图片屏幕显示处理
     * */
    public static Bitmap createFromJPEGData(byte[] data,int jpegW,int jpegH, int outW, int outH, float rotation, boolean isMirror)
    {
    	if(data==null)return null;
    	int tempW, tempH;
    	int finalW,finalH;
    	if(jpegW > jpegH){
    		int t = Math.max(outW,outH);
    		outH = Math.min(outW,outH);
    		outW = t;
    	}
    	else{
    		int t = Math.min(outW,outH);
    		outH = Math.max(outW,outH);
    		outW = t;
    	}
    	
    	float ratio  = Math.max(jpegW/(float)outW, jpegH/(float)outH);
		BitmapFactory.Options opts = new BitmapFactory.Options();
		if (ratio > 1.0f) opts.inSampleSize = (int) (ratio);
		Bitmap temp = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
		tempW = temp.getWidth();
		tempH = temp.getHeight();
		
    	if(tempW > tempH){
    		int t = Math.max(outW,outH);
    		outH = Math.min(outW,outH);
    		outW = t;
    	}
    	else{
    		int t = Math.min(outW,outH);
    		outH = Math.max(outW,outH);
    		outW = t;
    	}
    	
		float scale = Math.max(outW/(float)tempW, outH/(float)tempH);
		if(scale>1.0f)scale=1.0f;
		
		if(Math.abs(rotation-90.0f)<0.5f||Math.abs(rotation-270)<0.5f){
			finalW = (int)(tempH*scale);
			finalH = (int)(tempW*scale);
		}
		else{
			finalW = (int)(tempW*scale);
			finalH = (int)(tempH*scale);
		}
		
		Matrix matrix = new Matrix();

		//Rotation
		matrix.postRotate(rotation, tempW/2,tempH/2);
		if(rotation==90||rotation==270)
			matrix.postTranslate((tempH-tempW)/2, (tempW-tempH)/2);
		//Scale
		matrix.postScale(scale, scale);
		
		//Mirror
		if(isMirror)
		{
			float[] mirror={ -1, 0, 0,  0, 1, 0,  0, 0, 1};
			Matrix m = new Matrix();
			m.setValues(mirror);
			m.postTranslate(finalW, 0);
			matrix.postConcat(m);
		}
		Bitmap bitmap = Bitmap.createBitmap(finalW, finalH, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setDither(true);
		paint.setFilterBitmap(true);
		canvas.drawARGB(255, 255, 255, 255);
		canvas.drawBitmap(temp,matrix,paint);
		temp.recycle();
		//Log.d("createFromJPEGData","jpegWxH:"+jpegW+"x"+jpegH+" outWxH"+outW+"x"+outH+" Rotation:"+rotation+" isMirror:"+isMirror);
		//Log.d("createFromJPEGData","tempWxH"+tempW+"x"+tempH+" finalWxH"+finalW+"x"+finalH);
    	return bitmap;
    }
   
    public static final Bitmap createFromLocal(Context context,String uri,int w,int h,String[] outpath) throws URISyntaxException, IOException{
		Bitmap bitmap = null;
		int degree = 0;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inDither = false;
        options.inJustDecodeBounds = true;
        if (uri.startsWith(ContentResolver.SCHEME_CONTENT))
        {
        	BufferedInputStream bufferedInput=null;
	            options.inJustDecodeBounds = true;
	            bufferedInput = new BufferedInputStream(context.getContentResolver()
	                    .openInputStream(Uri.parse(uri)), 16384);
	            bufferedInput.mark(Integer.MAX_VALUE);
	            BitmapFactory.decodeStream(bufferedInput, null, options);
	            int width = options.outWidth;
	            int height = options.outHeight;
	            if(width<=0 || height<=0)
	            {
	            	bufferedInput.close();
	            	return null;
	            }
	            float maxResX = width>height?
	            		Math.max(w,h):
	            		Math.min(w,h);
	            float maxResY = (maxResX == w) ? h : w;
	            float ratio = Math.max(width / maxResX, height / maxResY); 
	            options.inSampleSize = (int)ratio;
	            options.inDither = false;
	            options.inJustDecodeBounds = false;	
            Thread timeoutThread = new Thread("BitmapTimeoutThread") 
            {
                @Override
				public void run()
                {
                    try 
                    {
                        Thread.sleep(6000);
                        options.requestCancelDecode();
                    } 
                    catch (InterruptedException e)
                    {

                    }
                }
            };
            timeoutThread.start();
            bufferedInput.close();
            bufferedInput = new BufferedInputStream(context.getContentResolver().openInputStream(Uri.parse(uri)), 16384);
    		Bitmap temp = BitmapFactory.decodeStream(bufferedInput, null, options);
            bufferedInput.close();
            if(temp == null) return null;
			int tempW = temp.getWidth();
			int tempH = temp.getHeight();
			ratio = Math.min(maxResX/tempW,maxResY/tempH);
            if(ratio<1f){
    			//Matrix matrix = new Matrix();
        		//matrix.setScale(ratio, ratio);
        		//bitmap = Bitmap.createBitmap(temp, 0, 0,tempW,tempH, matrix, true);
            	
    			int bW = (int) (tempW * ratio);
    			int bH = (int) (tempH * ratio);
    			bitmap = Bitmap.createBitmap(bW, bH, Config.ARGB_8888);
    			Canvas canvas = new Canvas(bitmap);
    			Paint paint = new Paint();
    			paint.setDither(true);
    			paint.setFilterBitmap(true);
    			canvas.drawBitmap(temp, new Rect(0, 0,tempW , tempH), new Rect(0,0,bW,bH), paint);
    			
            	temp.recycle();
            	temp =null;
            }else{
            	bitmap = temp;
            	temp = null;
            }
            
            String[] proj = { MediaColumns.DATA };   
            Uri pUri = Uri.parse(uri);       
            Cursor actualimagecursor = context.getContentResolver().query(pUri, proj, null, null, null);
            int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaColumns.DATA);   
            actualimagecursor.moveToFirst();   
            String img_path = actualimagecursor.getString(actual_image_column_index);  
            if(outpath!=null)
            outpath[0] = img_path;
            degree = Util.getExifOrientation(img_path);
        }
        else{
        	File f;
        	if(uri.startsWith(ContentResolver.SCHEME_FILE))
        		f =new File(new URI(uri));
        	else
        		f = new File(uri);
        	if(f.exists())
        	{	
        		String path = f.getAbsolutePath();
        		if(outpath!=null)
        		outpath[0] = path;
        		degree = Util.getExifOrientation(path);
                BitmapFactory.decodeFile(path, options);
                int width = options.outWidth;
                int height = options.outHeight;
                if(width<=0 || height<=0)return null;
                float maxResX = width>height?
                		Math.max(w,h):
                		Math.min(w,h);
                float maxResY = (maxResX == w) ? h : w;
	            float ratio = Math.max(width / maxResX, height / maxResY); 
      			Bitmap temp= null;
                options.inDither = false;
                options.inJustDecodeBounds = false;	 
      			try
				{
     	            options.inSampleSize = (int)ratio;            
                    temp=BitmapFactory.decodeFile(path,options);  
				}
				catch (OutOfMemoryError e)
				{
					ratio+=1;
     	            options.inSampleSize = (int)ratio;            
                    temp=BitmapFactory.decodeFile(path,options);  
				}
  
                if(temp == null) return null;
    			int tempW = temp.getWidth();
    			int tempH = temp.getHeight();
    			ratio = Math.min(maxResX/tempW,maxResY/tempH);
    			int bW = (int) (tempW * ratio);
    			int bH = (int) (tempH * ratio);
                if(ratio<1f && bW > 0 && bH>0){
        			bitmap = Bitmap.createBitmap(bW, bH, Config.ARGB_8888);
        			Canvas canvas = new Canvas(bitmap);
        			Paint paint = new Paint();
        			paint.setDither(true);
        			paint.setFilterBitmap(true);
        			canvas.drawBitmap(temp, new Rect(0, 0,tempW , tempH), new Rect(0,0,bW,bH), paint);
        			
                	temp.recycle();
                	temp =null;
                }else{
                	bitmap = temp;
                	temp = null;
                }
        	}
        }

    	return rotate(bitmap, degree);
    }
    
    
    
	public static void writeImage(Bitmap bitmap,String destPath,int quality)
	{
		try {
			FileUtil.deleteFile(destPath);
			if (FileUtil.createFile(destPath))
			{
				FileOutputStream out = new FileOutputStream(destPath);
				if (bitmap.compress(Bitmap.CompressFormat.JPEG,quality, out))
				{
					out.flush();
					out.close();
					out = null;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeImage(Bitmap bitmap,String destPath,int quality,long limitSize)
	{
		writeImage(bitmap,destPath,quality);
		
		int compressCount=1;
		while (new File(destPath).length()>limitSize&&(100-5*compressCount>0))
		{
			writeImage(bitmap, destPath, 100-5*compressCount);
			compressCount++;
		};
	}
	
	/**
	 * 放回图片的宽和高,第一个元素代表图片宽度，第二个元素代表图片高度
	 * @param pathName
	 * @return
	 */
	public static int[] getBitmapWidthHeight(String pathName)
	{
		int [] ret=new int[2];
		try {
			
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(pathName, opts);

			ret[0] = opts.outWidth;
			ret[1] = opts.outHeight;
		
		} catch (Exception e) {
			e.printStackTrace();
		}
LogUtil.v("getBitmapWidthHeight", "Width:"+ret[0]+"Height:"+ret[1]);
		return ret;
	}
	
	public static int[] getBitmapWidthHeight(Context context,int resourceID)
	{
		int [] ret=new int[2];
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(context.getResources(),resourceID, opts);

		ret[0] = opts.outWidth;
		ret[1] = opts.outHeight;
		
LogUtil.v("getBitmapWidthHeight", "Width:"+ret[0]+"Height:"+ret[1]);
		return ret;
	}

	public static void recycleBitmap(Bitmap bitmap)
	{
		if(null!=bitmap&&!bitmap.isRecycled())
		{
			bitmap.recycle();
		}
		bitmap=null;
	}
	
	
	public static Bitmap createHorizontalIcon(Context context,ArrayList<Integer> iconIDList,int gapWidth) 
	{
		if(iconIDList.size()==1)
		{
			return BitmapFactory.decodeResource(context.getResources(),iconIDList.get(0));
		}
		
		Bitmap tmpBitmap=BitmapFactory.decodeResource(context.getResources(),iconIDList.get(0));
		int []widthHeightArray ={tmpBitmap.getWidth(),tmpBitmap.getHeight()};
		recycleBitmap(tmpBitmap);
		
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		
		Bitmap destBitmap = Bitmap.createBitmap(iconIDList.size()*widthHeightArray[0]+(iconIDList.size()-1)*gapWidth, widthHeightArray[1], Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(destBitmap);

//		paint.setColor(Color.BLUE);
//		canvas.drawRect(new Rect(0, 0, iconIDList.size()*(widthHeightArray[0]+gapWidth), widthHeightArray[1]), paint);
		
		for(int i=0;i<iconIDList.size();i++)
		{
			Bitmap bitmap=BitmapFactory.decodeResource(context.getResources(),iconIDList.get(i));
			canvas.drawBitmap(bitmap, i*(widthHeightArray[0]+gapWidth), 0, paint);
			recycleBitmap(bitmap);
		}
		return destBitmap;
	}
	
	public static Bitmap getBitmap(String path, int dstWidth, int dstHeight) {
		/*File file = new File(path);
		if(!file.exists())
			return null;*/
		
		System.gc();
		System.gc();
		
		/*BitmapFactory.Options op = new BitmapFactory.Options();
		op.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, op);
		
		int h = op.outHeight;
		int w = op.outWidth;
		
		if (w > h) {
			if (w > dstWidth || h > dstHeight) {
				float scaleWidth = w / ((float) dstWidth);
				float scaleHeight = h / ((float) dstHeight);
				op.inSampleSize = Math.round(Math.min(scaleWidth, scaleHeight));		
			} 
		} else {
			if (w > dstWidth || h > dstHeight) {
				float scaleWidth = w / ((float) dstHeight);
				float scaleHeight = h / ((float) dstWidth);
				op.inSampleSize = Math.round(Math.min(scaleWidth, scaleHeight));	
			} 
		}
		op.inJustDecodeBounds = false;*/
		BitmapFactory.Options op = new BitmapFactory.Options();
		op.inJustDecodeBounds = false;
		//op.inInputShareable = true;
		//op.inPurgeable = true;
		int scale = calcSampleSize(path, dstWidth, dstHeight);
		if(scale <= 0)
			return null;
		op.inSampleSize = scale;
		
		Bitmap bm = BitmapFactory.decodeFile(path, op);
		
		//int degree = getExifOrientation(path);
		//if(degree != 0)
			//bm = rotate(bm, degree);
		return bm;
		
	}
	
	public static int calcSampleSize(String path, int dstWidth, int dstHeight) {
		File file = new File(path);
		if(!file.exists())
			return -1;
		
		BitmapFactory.Options op = new BitmapFactory.Options();
		op.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, op);
		
		int h = op.outHeight;
		int w = op.outWidth;
		
		int scale = 1;
		if (w > h) {
			if (w > dstWidth || h > dstHeight) {
				float scaleWidth = w / ((float) dstWidth);
				float scaleHeight = h / ((float) dstHeight);
				scale = Math.round(Math.min(scaleWidth, scaleHeight));		
			} 
		} else {
			if (w > dstWidth || h > dstHeight) {
				float scaleWidth = w / ((float) dstHeight);
				float scaleHeight = h / ((float) dstWidth);
				scale = Math.round(Math.min(scaleWidth, scaleHeight));	
			} 
		}
		
		return scale;
	}
	
	
	public static String saveBitmap(Bitmap bitmap, String path, Bitmap.CompressFormat format) {
		if(path == null || bitmap == null)
			return null;
		
		try {			
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
			bitmap.compress(format, 100, bos);
			bos.flush();
			bos.close();
			return path;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	public static Bitmap getBitmapFromUrl(String url) {
		InputStream is = null;
		try {
			URL u = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setRequestMethod("GET");
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				is = conn.getInputStream();
				return BitmapFactory.decodeStream(is);
			} else
				conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if(is != null)
					is.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} finally {
		}
		return null;
	
	}
}
