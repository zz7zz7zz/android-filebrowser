package com.open.filebrowser.ui;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.open.filebrowser.R;
import com.open.filebrowser.util.FileInitUitl;
import com.open.filebrowser.util.FileUtil;
import com.open.filebrowser.util.ImageCacheMgr;
import com.open.filebrowser.util.ImageCacheMgr.IImageReponse;
import com.open.filebrowser.util.Util;

public class MainUI extends Activity {
	
	private GridView cloundGridView;
	private GridViewAdapter adapter;
	private ArrayList<FunBean> list=new ArrayList<FunBean>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//无标题 
		setContentView(R.layout.main);
		
		parse();
		initView();
	}

	private void initView()
	{
		cloundGridView=(GridView)findViewById(R.id.gridView);
//		cloundGridView.setSelector(android.R.color.transparent);
		cloundGridView.setHorizontalFadingEdgeEnabled(false);
		cloundGridView.setVerticalFadingEdgeEnabled(false);
		cloundGridView.setHorizontalScrollBarEnabled(false);
		cloundGridView.setVerticalScrollBarEnabled(true);
		cloundGridView.setVerticalSpacing(Util.dipTopx(this, 35));
		
		adapter=new GridViewAdapter(getApplication(), list);
		cloundGridView.setAdapter(adapter);
		cloundGridView.setOnItemClickListener(onItemClickListener);
	}
	
	private OnItemClickListener onItemClickListener= new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
			Intent mIntent=new Intent(getApplicationContext(),FileListUI.class);
			mIntent.putExtra("data", list.get(position).dir);
			startActivity(mIntent);

		}
	};
	
	
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			android.os.Process.killProcess(android.os.Process.myPid());
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}


	private class GridViewAdapter extends BaseAdapter
	{
		LayoutInflater mInflater;
		ArrayList<FunBean> list;
		
		public GridViewAdapter(Context context,ArrayList<FunBean> list)
		{
			this.mInflater=LayoutInflater.from(context);
			this.list=list;
		}
		
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if(null==convertView)
			{
				convertView=new ImageView(getBaseContext());
			}
			
			
			final ImageView mImageView=(ImageView)convertView;
			FunBean item=list.get(position);
			String url=FileUtil.CACHE_IMAGE_ICON+item.icon;
			mImageView.setImageBitmap(null);
			mImageView.setTag(url);
			Bundle bundle=new Bundle();
			bundle.putString(ImageCacheMgr.KEY_URL,url);
			ImageCacheMgr.getInstance().loadSmallImage(bundle,new IImageReponse(){
	
				@Override
				public void onImageReponse(String requestUri, final Bitmap retImage) {
					if(null!=retImage&&requestUri.equals(mImageView.getTag()))
					{
						runOnUiThread(new Runnable(){
								@Override
								public void run()
								{
									mImageView.setImageBitmap(retImage);
								}
							});
					}
				}
			 });
			return convertView;
		}
	}
	
	private ArrayList<FunBean>  parse()
	{
		byte[] configByte=FileUtil.readFile(FileInitUitl.configPath);
		if(null!=configByte)
		{
			String config=new String(configByte);
			try {
				JSONObject obj=new JSONObject(config);
				JSONArray jarray=obj.optJSONArray("items");
				for(int i=0;i<jarray.length();i++)
				{
					JSONObject item=(JSONObject) jarray.get(i);
					FunBean itemUser=new FunBean();
					itemUser.name=item.optString("name");
					itemUser.icon=item.optString("icon");
					itemUser.dir=item.optString("dir");
					list.add(itemUser);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return list;
	}
	
	
	public class FunBean
	{
		public String name;
		public String icon;
		public String dir;
	}
}
