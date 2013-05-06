package com.open.filebrowser.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.open.filebrowser.R;
import com.open.filebrowser.util.FileUtil;

public class FileListUI extends ListActivity {
	
	private String dir;
	private ArrayList<FileInfo> dataList=new ArrayList<FileInfo>();
	private String currentPath=dir;
	
	private ListViewAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		dir=getIntent().getStringExtra("data");
		dir=FileUtil.CACHE_IMAGE_RESOURCE+dir;
		FileUtil.createDir(dir);
		currentPath=dir;
		
//		getListView().setSelector(android.R.color.transparent);
		getListView().setHorizontalFadingEdgeEnabled(false);
		getListView().setVerticalFadingEdgeEnabled(false);
		getListView().setHorizontalScrollBarEnabled(false);
		getListView().setVerticalScrollBarEnabled(true);
		getListView().setOnItemClickListener(onItemClickListener);
		
		adapter=new ListViewAdapter(getBaseContext(), dataList);
		setListAdapter(adapter);
		
		viewFiles(dir);
	}
	
	private void viewFiles(String filePath) 
	{
		ArrayList<FileInfo> ret = getFiles(filePath);
		if (ret != null) 
		{
			dataList.clear();
			dataList.addAll(ret);
			ret.clear();

			currentPath = filePath;
			adapter.notifyDataSetChanged();
		}
	}
	
	private void openFile(String path)
	{
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		File f = new File(path);
		String type = FileUtil.getMIMEType(f.getName());
		intent.setDataAndType(Uri.fromFile(f), type);
		startActivity(intent);
	}
	
	private OnItemClickListener onItemClickListener= new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
			if(dataList.get(position).IsDirectory)
			{
				viewFiles(dataList.get(position).Path);
			}
			else
			{
				openFile(dataList.get(position).Path);
			}
		}
	};
	
	

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			File f = new File(currentPath);
			String parentPath = f.getParent();
			if (parentPath != null&&parentPath.length()>=dir.length()) 
			{
				viewFiles(parentPath);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private class ListViewAdapter extends BaseAdapter
	{
		LayoutInflater mInflater;
		ArrayList<FileInfo> list;
		
		public ListViewAdapter(Context context,ArrayList<FileInfo> names)
		{
			this.mInflater=LayoutInflater.from(context);
			this.list=names;
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
			ViewHolder holder = null;
			if(null==convertView)
			{
				convertView = mInflater.inflate(R.layout.file_item, null);
				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.file_name);
				holder.icon = (ImageView) convertView.findViewById(R.id.file_icon);
				convertView.setTag(holder);
			}
			else 
			{
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.name.setText(list.get(position).Name);
			holder.icon.setImageResource(getMIMEType(list.get(position)));
			return convertView;
		}
		
		private class ViewHolder {
			TextView name;
			ImageView icon;
		}
	}
	
	private  int getMIMEType(FileInfo item) {
		String end = item.Name.substring(item.Name.lastIndexOf(".") + 1, item.Name.length()).toLowerCase();
		if (end.equals("apk")) 
		{
			return R.drawable.icon_apk;
		} 
		else if (end.equals("mp4") || end.equals("avi") || end.equals("3gp")|| end.equals("rmvb")) 
		{
			return R.drawable.icon_video;
		} 
		else if (end.equals("m4a") || end.equals("mp3") || end.equals("mid") || end.equals("xmf")
				|| end.equals("ogg") || end.equals("wav")) 
		{
			return R.drawable.icon_audio;
		} 
		else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp")) 
		{
			return R.drawable.icon_img;
		} 
		else if (end.equals("txt") || end.equals("log")) 
		{
			return R.drawable.icon_txt;
		}
		else if (end.equals("pdf")) 
		{
			return R.drawable.icon_pdf;
		}
		else if (end.equals("excel")) 
		{
			return R.drawable.icon_excel;
		}
		else if (end.equals("word")) 
		{
			return R.drawable.icon_word;
		}
		else if (end.equals("zip")) 
		{
			return R.drawable.icon_zip;
		}
		else if (end.equals("html")) 
		{
			return R.drawable.icon_html;
		}
		else if(item.IsDirectory)
		{
			return R.drawable.icon_folder;
		}
		return R.drawable.icon_default;
	}
	
	
	public static class FileInfo
	{
		public String Name;
		public String Path;
		public boolean IsDirectory = false;
		
		public static class FileComparator implements Comparator<FileInfo> {

			public int compare(FileInfo file1, FileInfo file2) 
			{
				if (file1.IsDirectory && !file2.IsDirectory)
				{
					return -1000;
				} 
				else if (!file1.IsDirectory && file2.IsDirectory) 
				{
					return 1000;
				}
				return file1.Name.compareTo(file2.Name);
			}
		}
	}

	public ArrayList<FileInfo> getFiles(String path) 
	{
		File f = new File(path);
		File[] files = f.listFiles();
		if (files == null||files.length==0) 
		{
			Toast.makeText(FileListUI.this,R.string.fileListUI_nofile,Toast.LENGTH_SHORT).show();
			return null;
		}

		ArrayList<FileInfo> fileList = new ArrayList<FileInfo>();
		for (int i = 0; i < files.length; i++) 
		{
			File file = files[i];
			FileInfo fileInfo = new FileInfo();
			fileInfo.Name = file.getName();
			fileInfo.IsDirectory = file.isDirectory();
			fileInfo.Path = file.getPath();
			fileList.add(fileInfo);
		}

		Collections.sort(fileList, new FileInfo.FileComparator());
		return fileList;
	}
}
