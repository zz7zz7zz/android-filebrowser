package com.open.filebrowser.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.open.filebrowser.ui.EduApp;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.widget.ImageView;

/**
 * 图片缓存类，这个类需要优化,改写成LRUImageCacheMgr
 * 
 */
public class ImageCacheMgr {
	
	 private final String TAG="ImageCache";
	
	 private final Object _cacheLock = new Object();
	 private HashMap<String, WeakReference<Bitmap>> _cache = new HashMap<String, WeakReference<Bitmap>>();
	 private HashMap<String, List<IImageReponse>> _callbacks = new HashMap<String, List<IImageReponse>>();
	 
	 private final Object mTaskLock=new Object();
	 private ArrayList<ImgRunnable> mTaskList=new ArrayList<ImgRunnable>();
    
     private ExecutorService executorService = Executors.newFixedThreadPool(5); 
     private static ImageCacheMgr instance = null;

	 public static final String KEY_URL="reqUrl";//请求的URL
	 public static final String KEY_SAVADir="savaDir";//保存的目录
	 public static final String KEY_SAVAFileName="savaFileName";//保存的文件名，通常为空(为空的话，保存的文件名通常是MD5(reqUrl))
	 public static final String KEY_MaxWidth="maxWidth";//生存图片的最大宽度
	 public static final String KEY_MaxHeight="maxHeight";//生成图片的最大高度
	 public static final String KEY_DefaultImg="defaultImg";//是否展示下载进度
	 public static final String KEY_ISSHOWPROGRESS="isShowProgress";//是否展示下载进度
	
	 public static ImageCacheMgr getInstance() 
	 {
	    if (null==instance||(null!=instance&&instance.executorService.isShutdown())) 
	    {
	        instance = new ImageCacheMgr();
	    }
	    return instance;
	 }
	 
	 public Bitmap getBitmapFromMemoryCache(String url) 
	 {
        Bitmap bitmap = null;
        synchronized (_cacheLock) 
        {
            if (_cache.containsKey(url)) 
            {
            	WeakReference<Bitmap> ref = _cache.get(url);
                if ( ref != null ) 
                {
                    bitmap =ref.get();
                    if (bitmap == null)
                    {
                    	 _cache.remove(url);
                    }
                    else if(bitmap.isRecycled())
                    {
                    	bitmap=null;
                    	_cache.remove(url);
                    }
                }
            }
        }
        return bitmap;
	 }
	 
	 public Bitmap getBitmapFormLocal(String url,int maxWidth,int maxHeight)
	 {
		 Bitmap bitmap = getProtectedBitmap(url,maxWidth,maxHeight);
		 if(null!=bitmap)
		 {
			 putCache(url, bitmap);
		 }
		 return bitmap;
	 }
	 
	 public Bitmap getBitmapFromNetInLocal(String url,String savaDirectory,String savaFileName,int maxWidth,int maxHeight)
	 {
		 String localUri="";
		 if(!TextUtils.isEmpty(savaFileName))
		 {
			 localUri=savaDirectory+savaFileName;
		 }
		 else
		 {
			 localUri=savaDirectory+MD5Util.encrypt(url);
		 }

		 Bitmap bitmap = getProtectedBitmap(localUri,maxWidth,maxHeight);
		 if(null==bitmap)
		 {
			 localUri=FileUtil.getDiffPath(localUri);
			 bitmap = getProtectedBitmap(localUri,maxWidth,maxHeight);
		 }

		 if(null!=bitmap)
		 {
			 putCache(url, bitmap);
		 }
		 return bitmap;
	 }
	 
	 private Bitmap getAssesBitmap(String url)
	 {
		 try {
				String tmpUrl=url.substring(FileUtil.ASSERT_PATH.length()+1);
				InputStream in=EduApp.getInstance().getResources().getAssets().open(tmpUrl);
				Bitmap bitmap=BitmapFactory.decodeStream(in);
				
				if(null!=bitmap)
				{
					 putCache(url, bitmap);
				}
				
				in.close();
				in=null;
				return bitmap;
		} catch (IOException e) {
			e.printStackTrace();
		}
		 return null;
	 }
	 
	 private Bitmap getResBitmap(String url)
	 {
		 try {
				int resourceID=Integer.valueOf(url.substring(FileUtil.RES_PATH.length()+1));
				Bitmap bitmap=BitmapFactory.decodeResource(EduApp.getInstance().getResources(), resourceID);
				
				if(null!=bitmap)
				{
					 putCache(url, bitmap);
				}
				
				return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		 return null;
	 }
	 
	 private Bitmap getContentBitmap(String url,int maxWidth,int maxHeight)
	 {
		 String[] proj = { MediaColumns.DATA };   
		 Cursor mCursor = EduApp.getInstance().getContentResolver().query(Uri.parse(url),proj,null,null,null);  
		 if(null!=mCursor)
		 {
			 int tmpImgPath = mCursor.getColumnIndexOrThrow(MediaColumns.DATA);   
			 mCursor.moveToFirst();   
			 
			 String imgPath= mCursor.getString(tmpImgPath);  
			 
			 mCursor.close();
			 mCursor=null;
			 
			 return getBitmapFormLocal(imgPath,maxWidth,maxHeight);
		 }
		 
		 return null;
	 }
	 
	 public Bitmap getOrgBtimap(String url){
		 if (FileUtil.isFileExist(url)) {
			try {
				InputStream IS = new  FileInputStream(new File(url));
				Bitmap bitmap = BitmapFactory.decodeStream(IS);
				return bitmap;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		 
		 return null;
	 }
	 
	 public Bitmap getBitmapFromNet(String url,String savaDirectory,String saveFileName,boolean isShowProgress,int maxWidth,int maxHeight)
	 {
LogUtil.v(TAG, "getBitmapFromNet():"+url);
		 Bitmap bitmap=null;
		 
		 String savaPath="";
		 if(!TextUtils.isEmpty(saveFileName))
		 {
			 savaPath=savaDirectory+saveFileName;
		 }
		 else
		 {
			 savaPath=savaDirectory+MD5Util.encrypt(url);
		 }
		 
		 HttpURLConnection conn=null;
		 try {
				 URL imgURL=new URL(url);
				 conn=(HttpURLConnection) imgURL.openConnection();
				 conn.setConnectTimeout(20*1000);
				 conn.setReadTimeout(20*1000);
				 conn.setDoInput(true);
				 conn.setRequestMethod("GET");
				 conn.connect();
				
				int contentLength=conn.getContentLength();
				if(contentLength>0)
				{
					long originalFileLength=contentLength;//原始文件的长度
					InputStream in=conn.getInputStream();
					
					int preProgress=0;
					int totalReadCount=0;
					int readCount = 0;
					int len = 32*1024;
					byte[] buffer = new byte[len];
					while ((readCount = in.read(buffer)) != -1)
					{
							totalReadCount+=readCount;
							FileUtil.appendFile(savaPath, buffer, 0, readCount);
							
							if(isShowProgress)
							{
								if(originalFileLength>0)
								{
		LogUtil.v("getBitmapFromNet readCount:"+readCount+"totalReadCount:"+totalReadCount, "originalFileLength:"+originalFileLength+"-% is :"+(totalReadCount*100/originalFileLength));
									int progress=(int) (totalReadCount*100/originalFileLength);
									if(progress-preProgress>=10||progress==100)//每10%回调一次
									{
										preProgress=progress;
										
										List<IImageReponse> callbacks;
							            synchronized (_cacheLock) 
							            {
							                callbacks = _callbacks.get(url);
							            }
		
						                for (IImageReponse iter : callbacks) 
						                {
						                    iter.onImageProgress(url, progress);
						                }
									}
								}
								
								Thread.sleep(100);
							}
					}

					in.close();
					in = null;
				}
					
				 bitmap=getProtectedBitmap(savaPath,maxWidth,maxHeight);//BitmapFactory.decodeFile(savaPath);
				 if(null!=bitmap)
				 {
					 putCache(url, bitmap);
				 }
				 else
				 {
					 FileUtil.deleteFile(savaPath);
				 }
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			try {
	            if (conn != null) {
	            	conn.disconnect();
	            }
			} catch (Exception e2) {
				e2.printStackTrace();
			}

		}
		 
		 return bitmap;
	 }
	 
	 private Bitmap getProtectedBitmap(String url,int w,int h)
	 {
//LogUtil.v("getProtectedBitmap-start",Thread.currentThread().getId()+ " Size:" + (android.os.Debug.getNativeHeapAllocatedSize()/(float)(1024*1024))+"MB");
		Bitmap bitmap =null;
		try {
			
	        final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inScaled = false;
	        options.inDither = false;
	        options.inJustDecodeBounds = true;
	        BitmapFactory.decodeFile(url, options);
	        int width = options.outWidth;
	        int height = options.outHeight;
	        if(width<=0||height<=0)return null;
	        float maxResX = width>height?Math.max(w,h):Math.min(w,h);
	        float maxResY = (maxResX == w) ? h : w;
	        float ratio = Math.max(width / maxResX, height / maxResY);
	        options.inDither = false;
	        options.inJustDecodeBounds = false;	
	        options.inSampleSize = (int)ratio;
	        Bitmap temp = null;
	        try
			{
	        	 temp = BitmapFactory.decodeFile(url, options);
			}
	        catch (OutOfMemoryError o) {
	        	options.inSampleSize = (int)(ratio+1);
				temp = BitmapFactory.decodeFile(url, options);
			}
			catch (Exception e)
			{
				options.inSampleSize = (int)(ratio+1);
				temp = BitmapFactory.decodeFile(url, options);
			}

			if(temp == null) return null;
			int tempW = temp.getWidth();
			int tempH = temp.getHeight();
			ratio = Math.min(maxResX/tempW,maxResY/tempH);
	        if(ratio<1f)
	        {
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
	        }
	        else
	        {
	        	bitmap = temp;
	        	temp = null;
	        }   

	        int degrees=Util.getExifOrientation(url);
	        bitmap=BitmapUtil.rotate(bitmap, degrees);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.gc();
			LogUtil.v(TAG, "getProtectedBitmap() Exception");
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.gc();
			LogUtil.v(TAG, "getProtectedBitmap() OutOfMemoryError");
		}
		
// LogUtil.v("getProtectedBitmap-end",Thread.currentThread().getId()+ " Size:" + (android.os.Debug.getNativeHeapAllocatedSize()/(float)(1024*1024))+"MB");
        return bitmap;
	 }
	 
	 private void putCache(String url,Bitmap bitmap)
	 {
		 synchronized (_cacheLock) 
		 {
             _cache.put(url, new WeakReference<Bitmap>(bitmap));
         }
	 }
	 
	 /**
	  * 1./mnt/sdcard/a.png,/data/data/www.um.pro/a.png,本来就来自本地</br>
	  * 2.file:///android_asset --->apk的assert目录</br>
      * 3.http://www.baidu.com/img/baidu_sylogo1.gif,来自网络,但是下载后存储在本地</br>
	  * @param url
	  * @param savaDirectory
	  * @param saveFileName
	  * @return
	  */
	 public Bitmap loadSync(String url, String savaDirectory,String saveFileName,boolean isShowProgress,int maxWidth,int maxHeight)
	 {
		 if(url.startsWith("http:"))//来自网络，继续下载
		 {
			 Bitmap bitmap=getBitmapFromNetInLocal(url,savaDirectory,saveFileName,maxWidth,maxHeight);
			 if(null==bitmap)
			 {
				 bitmap=getBitmapFromNet(url,savaDirectory,saveFileName,isShowProgress,maxWidth,maxHeight);//从网络里获取
			 }
			 return bitmap;
		 }
		 else if(url.startsWith(FileUtil.SDCARD_PAHT)||url.startsWith(FileUtil.LOCAL_PATH))//如果图片本身来自本地,直接返回
		 {
			 return getBitmapFormLocal(url,maxWidth,maxHeight);
		 }
		 else if(url.startsWith(FileUtil.ASSERT_PATH))//apk的assert目录file:///android_asset/a/b.png(在子目录下);file:///android_asset/b.png无子目录
		 {
			 return getAssesBitmap(url);
		 }
		 else if(url.startsWith(FileUtil.RES_PATH))
		 {
			 return getResBitmap(url);
		 }
		 else if(url.startsWith(ContentResolver.SCHEME_FILE))
		 {
			 String imgPath= Uri.parse(url).getPath();
			 return getBitmapFormLocal(imgPath,maxWidth,maxHeight);
		 }
		 else if(url.startsWith(ContentResolver.SCHEME_CONTENT))
		 {
			 return getContentBitmap(url,maxWidth,maxHeight);
		 }
		 return null;
	 }
	 
	 public void loadLargeImage(Bundle params,final IImageReponse callback)
	 {
		 final String requestUrl=params.getString(KEY_URL);
		 final String savaDirectory=(!TextUtils.isEmpty(params.getString(KEY_SAVADir)))?params.getString(KEY_SAVADir):FileUtil.CACHE_IMAGE_COMMON;
		 final String savaFileName=params.getString(KEY_SAVAFileName);
		 final int maxWidth=params.getInt(KEY_MaxWidth,EduApp.PHOTOW);
		 final int maxHeight=params.getInt(KEY_MaxHeight, EduApp.PHOTOH);
		 final boolean isShowProgress=params.getBoolean(KEY_ISSHOWPROGRESS);
		 Bitmap defaultImg=params.getParcelable(KEY_DefaultImg);
		 
		 try {
			
			 	if(TextUtils.isEmpty(requestUrl))//如果地址为空的情况下
				{
			 		if(null!=callback&&null!=defaultImg)
			 		{
			 			 callback.onImageReponse(requestUrl, defaultImg);//地址不正确的时候回调一下
			 		}
					return;
				}
				 
		        synchronized (_cacheLock)
		        {
		            List<IImageReponse> callbacks = _callbacks.get(requestUrl);
		            if (callbacks != null) 
		            {
		                if (callback != null)
		                {
		                	callbacks.add(callback);
		                }
		                
		                callback.onImageReponse(requestUrl, defaultImg);//回调一次，不会产生很大的影响
		                return;
		            }

		            callbacks = new ArrayList<IImageReponse>();
		            if (callback != null)
		            {
		            	callbacks.add(callback);
		            }
		            _callbacks.put(requestUrl, callbacks);
		        }

				 Bitmap bitmap = getBitmapFromMemoryCache(requestUrl);//step1:从内存缓存区里获取
				 if(null!=bitmap)//有图片说明回调完了，不需要再次进行网络请求了，直接返回即可；没有图片，说明没有进行回调,需要去本地寻找或者网络进行下载
				 {
					 List<IImageReponse> callbacks;
		             synchronized (_cacheLock) 
		             {
	            		 callbacks = _callbacks.remove(requestUrl);
		             }
    	             if(null!=callbacks)
    	             {
    	                 for (IImageReponse iter : callbacks) 
    	                 {
    	                	 iter.onImageReponse(requestUrl,bitmap);
    	                 }
    	             }
					 return;
				 }
				 else
				 {
					 	ImgRunnable taskRun=new ImgRunnable(requestUrl) //step2:
				        {
				            @Override
				            public void run() 
				            {
				            	Bitmap retImage=null;
				            	try {
					            	if(!this.isCanceled())
					            	{
					            		 retImage = loadSync(requestUrl,savaDirectory,savaFileName,isShowProgress,maxWidth,maxHeight);
					            	}
								} catch (Exception e) {
									e.printStackTrace();
								}finally{
									
									try {
						                if(!this.isCanceled())
						                {
							                List<IImageReponse> callbacks;
							                synchronized (_cacheLock) 
							                {
							                    callbacks = _callbacks.remove(requestUrl);
							                }
							                
							                if(null!=callbacks)
							                {
								                for (IImageReponse iter : callbacks) 
								                {
								                    iter.onImageReponse(requestUrl,retImage);
								                }
							                }
						                }
									} catch (Exception e2) {
										e2.printStackTrace();
									}

					                cancelTaskByUrl(this.getRequestUrl());
								}
				            }
				        };
				        
				        synchronized (mTaskLock) 
				        {
				        	 mTaskList.add(taskRun);
						}
				        executorService.execute(taskRun);
				 }
			 
		} catch (Exception e) {
			e.printStackTrace();
		}
			
	 }
	 
	 public void loadSmallImage(Bundle params,final IImageReponse callback)
	 {
		 final String requestUrl=params.getString(KEY_URL);
		 final String savaDirectory=params.getString(KEY_SAVADir);
		 final String savaFileName=params.getString(KEY_SAVAFileName);
		 final int maxWidth=params.getInt(KEY_MaxWidth,EduApp.PHOTOW);
		 final int maxHeight=params.getInt(KEY_MaxHeight, EduApp.PHOTOH);
		 final boolean isShowProgress=params.getBoolean(KEY_ISSHOWPROGRESS);
		 Bitmap defaultImg=params.getParcelable(KEY_DefaultImg);
		 
		 try {
			
			 	if(TextUtils.isEmpty(requestUrl))
				{
			 		if(null!=callback)
			 		{
			 			 callback.onImageReponse(requestUrl, defaultImg);//地址不正确的时候回调一下
			 		}
					return;
				}
				 
		        synchronized (_cacheLock)
		        {
		            List<IImageReponse> callbacks = _callbacks.get(requestUrl);
		            if (callbacks != null) 
		            {
		                if (callback != null)
		                {
		                	callbacks.add(callback);
		                }
		                
		                callback.onImageReponse(requestUrl, defaultImg);//回调一次，不会产生很大的影响
		                return;
		            }

		            callbacks = new ArrayList<IImageReponse>();
		            if (callback != null)
		            {
		            	callbacks.add(callback);
		            }
		            _callbacks.put(requestUrl, callbacks);
		        }

				 Bitmap bitmap = getBitmapFromMemoryCache(requestUrl);//从内存缓存区里获取
				 if(null==bitmap)
				 {
					 if(requestUrl.startsWith(FileUtil.SDCARD_PAHT)||requestUrl.startsWith(FileUtil.LOCAL_PATH))//如果图片本身来自本地,直接返回
					 {
						 bitmap=getBitmapFormLocal(requestUrl,maxWidth,maxHeight);
					 }
					 else if(requestUrl.startsWith(FileUtil.ASSERT_PATH))//apk的assert目录
					 {
						 bitmap=getAssesBitmap(requestUrl);
					 }
					 else if(requestUrl.startsWith(FileUtil.RES_PATH))//来自网络，继续下载
					 {
						 bitmap=getResBitmap(requestUrl);
					 }
					 else if(requestUrl.startsWith(ContentResolver.SCHEME_FILE))
					 {
						 String imgPath= Uri.parse(requestUrl).getPath();
						 bitmap=getBitmapFormLocal(imgPath,maxWidth,maxHeight);
					 }
					 else if(requestUrl.startsWith(ContentResolver.SCHEME_CONTENT))
					 {
						 bitmap=getContentBitmap(requestUrl,maxWidth,maxHeight);
					 }
					 else if(requestUrl.startsWith("http:"))//来自网络，继续下载
					 {
						 bitmap=getBitmapFromNetInLocal(requestUrl,savaDirectory,savaFileName,maxWidth,maxHeight);
					 }
				 }

				 if(null!=bitmap)//有图片说明回调完了，不需要再次进行网络请求了，直接返回即可
				 {
					 List<IImageReponse> callbacks;
		             synchronized (_cacheLock) 
		             {
	            		 callbacks = _callbacks.remove(requestUrl);
		             }
    	             if(null!=callbacks)
    	             {
    	                 for (IImageReponse iter : callbacks) 
    	                 {
    	                	 iter.onImageReponse(requestUrl,bitmap);
    	                 }
    	             }
					 return;
				 }
				 else
				 {
					 	ImgRunnable taskRun=new ImgRunnable(requestUrl) 
				        {
				            @Override
				            public void run() 
				            {
				            	Bitmap retImage=null;
				            	if(!this.isCanceled())
				            	{
				            		 retImage=getBitmapFromNet(requestUrl,savaDirectory,savaFileName,isShowProgress,maxWidth,maxHeight);//从网络里获取
				            	}
				               
				                List<IImageReponse> callbacks;
				                synchronized (_cacheLock) 
				                {
				                    callbacks = _callbacks.remove(requestUrl);
				                }

				                if(!this.isCanceled())
				                {
				                	if(null!=callbacks)
				                	{
						                for (IImageReponse iter : callbacks) 
						                {
						                    iter.onImageReponse(requestUrl,retImage);
						                }
				                	}
				                }
				                
				                cancelTaskByUrl(this.getRequestUrl());
				            }
				        };
				        
				        synchronized (mTaskLock) 
				        {
				        	 mTaskList.add(taskRun);
						}
				        executorService.execute(taskRun);
				 }
		} catch (Exception e) {
			e.printStackTrace();
		}
			
	 
	 }
	 
	 
	 public void loadImg(final Activity activity,final ImageView mImageView,Bundle params)
	 {
		 mImageView.setTag(params.getString(KEY_URL));
		 mImageView.setImageBitmap(null);
		 loadLargeImage(params,new IImageReponse(){

			@Override
			public void onImageReponse(String requestUri, final Bitmap retImage) {
				if(null!=retImage&&requestUri.equals(mImageView.getTag()))
				{
					activity.runOnUiThread(new Runnable(){
							@Override
							public void run()
							{
								mImageView.setImageBitmap(retImage);
							}
						});
				}
			}

		 });
	 }
	 
	 
	 public boolean cancelAllTask()
	 {
		 synchronized (mTaskLock)
		 {
				Iterator<ImgRunnable> it = mTaskList.iterator();
				while (it.hasNext()) 
				{
					ImgRunnable itemTask = it.next();
					itemTask.setCanceled(true);
					it.remove();
				}
				mTaskList.clear();
		 }

		 synchronized (_cacheLock) 
		 {
			 _callbacks.clear();
		 }
 
		return true;
	 }
	 
	 public boolean cancelTaskByUrl(String imgUrl)
	 {
		 if(null==imgUrl)
		 {
			 return false;
		 }
		 
		 synchronized (mTaskLock)
		 {
				Iterator<ImgRunnable> it = mTaskList.iterator();
				while (it.hasNext())
				{
					ImgRunnable itemTask = it.next();
					if (imgUrl.equals(itemTask.getRequestUrl()))
					{
						itemTask.setCanceled(true);
						it.remove();
						break;
					}
				}
		 }
		 
		 synchronized (_cacheLock) 
		 {
			 Iterator<Entry<String, List<IImageReponse>>> iter = _callbacks.entrySet().iterator();
			 while (iter.hasNext()) 
			 {
				 Entry<String, List<IImageReponse>>  entry = ( Entry<String, List<IImageReponse>>) iter.next();
				 String key = entry.getKey();
				 if(imgUrl.equals(key))
				 {
					 iter.remove();
					 break;
				 }
			 }
		 }
		return false;
	 }
	 
	 public boolean cancelTaskByUrls(ArrayList<String> urlList)
	 {
		 synchronized (mTaskLock)
		 {
				Iterator<ImgRunnable> it = mTaskList.iterator();
				while (it.hasNext())
				{
					ImgRunnable itemTask = it.next();
					
					for(String itemUrl:urlList)
					{
						if (null!=itemUrl&&itemUrl.equals(itemTask.getRequestUrl()))
						{
							itemTask.setCanceled(true);
							it.remove();
							break;
						}
					}
				}
		 }
		 
		 synchronized (_cacheLock) 
		 {
			 Iterator<Entry<String, List<IImageReponse>>> iter = _callbacks.entrySet().iterator();
			 while (iter.hasNext()) 
			 {
				 Entry<String, List<IImageReponse>>  entry = ( Entry<String, List<IImageReponse>>) iter.next();
				 String key = entry.getKey();
				 
				 for(String itemUrl:urlList)
				 {
					if (null!=itemUrl&&itemUrl.equals(key))
					{
						iter.remove();
						break;
					}
				 }
			 }
		 }
		return false;
	 }
	 
	 public void clearCache(String url)
	 {
        synchronized (_cacheLock) 
        {
            if (_cache.containsKey(url)) 
            {
            	WeakReference<Bitmap> ref = _cache.get(url);
                if ( ref != null ) 
                {
                    _cache.remove(url);
                }
            }
        }
	 }
	 
	 public void clearCache()
	 {
		 synchronized (_cacheLock) 
		 {
			 Iterator<java.util.Map.Entry<String, WeakReference<Bitmap>>> iter = _cache.entrySet().iterator();
			 while (iter.hasNext()) 
			 {
				 Entry<String, WeakReference<Bitmap>> item=iter.next();
				
				 WeakReference<Bitmap> itemVaule=item.getValue();
                 if (itemVaule != null ) 
                 {
                	 Bitmap bitmap =itemVaule.get();
                	 if (null!=bitmap&&!bitmap.isRecycled())
                	 {
                    	bitmap.recycle();
                	 }
                	 bitmap=null;
                 }
				 
			     iter.remove();
			 } 
		 }

		 System.gc();
		 System.runFinalization();
	 }
		
	 public static void destory()
	 {
		 if(null!=instance)
		 {
			 instance._cache.clear();
			 instance.cancelAllTask();
			 if(!instance.executorService.isShutdown())
			 {
				 instance.executorService.shutdown();
			 }
			 instance=null;
		 }
	 }
	 
	 private static class ImgRunnable implements Runnable
	 {
		private boolean canceled=false;//是否取消了消息的执行
		private String requestUrl="";
		
		public ImgRunnable(String requestUrl)
		{
			this.requestUrl=requestUrl;
		}
		 
		public String getRequestUrl() {
			return requestUrl;
		}

		public boolean isCanceled() {
			return canceled;
		}

		public void setCanceled(boolean canceled) {
			this.canceled = canceled;
		}

		@Override
		public void run() {
			
		}
	 }
	 
	 public static class IImageReponse
	 {
		public void onImageReponse(String requestUri, Bitmap retImage) {}

		public void onImageProgress(String requestUri, int progress) {}
	 }
}