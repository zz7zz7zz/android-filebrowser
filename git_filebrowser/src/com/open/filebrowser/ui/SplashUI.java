package com.open.filebrowser.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.ImageView;

import com.open.filebrowser.R;
import com.open.filebrowser.util.FileInitUitl;
import com.open.filebrowser.util.ImageCacheMgr;

public class SplashUI extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//无标题 
        setContentView(R.layout.splash);
        
        ((ImageView)findViewById(R.id.splashImg)).setImageBitmap(ImageCacheMgr.getInstance().loadSync(FileInitUitl.logoPath, "", "", false, EduApp.PHOTOW, EduApp.PHOTOH));
        new Handler().postDelayed(jumpRunnable , 2500);	
	}
	
    private Runnable jumpRunnable = new Runnable() 
    {
		public void run() 
		{
			Intent mIntent = new Intent(SplashUI.this, MainUI.class);
			startActivity(mIntent);
			finish();
		}
	};

}
