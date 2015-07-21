package com.example.progressloading;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class MainActivity extends Activity {

	Button start, stop,light,dark;
	LoadingSurfaceViewCW loadingView;
	JVProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		start = (Button) findViewById(R.id.button_start);
		stop = (Button) findViewById(R.id.button_stop);
		light = (Button) findViewById(R.id.button_light);
		dark = (Button) findViewById(R.id.button_dark);
//		loadingView = (LoadingSurfaceViewCW) findViewById(R.id.loading);
		stop.setOnClickListener(listener);
		start.setOnClickListener(listener);
		light.setOnClickListener(listener);
		dark.setOnClickListener(listener);
		ProgressHub2.getInstance().init(MainActivity.this, ProgressHub2.STYLE_LIGHT);
		dialog = new JVProgressDialog(MainActivity.this);
	}

	OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.button_start:
//				loadingView.startLoading();
				ProgressHub2.getInstance().show("测试啦啦啦！",(ViewGroup)getRootView());
				break;
			case R.id.button_stop:
//				loadingView.stopLoading();
				ProgressHub2.getInstance().dismiss();
				break;
			case R.id.button_light:
//				ProgressHub2.getInstance().setStyle(ProgressHub2.STYLE_LIGHT);
				dialog.show("啦啦啦");
				break;
			case R.id.button_dark:
//				ProgressHub2.getInstance().setStyle(ProgressHub2.STYLE_DARK);
				dialog.dismiss();

				break;
			default:
				break;
			}
		}
	};
	
	public View getRootView()
	{
		return ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		ProgressHub2.getInstance().dismiss();
	}
	

}
