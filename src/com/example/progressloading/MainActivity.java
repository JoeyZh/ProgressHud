package com.example.progressloading;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	Button start, stop,light,dark;
	LoadingSurfaceViewCW loadingView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		start = (Button) findViewById(R.id.button_start);
		stop = (Button) findViewById(R.id.button_stop);
		light = (Button) findViewById(R.id.button_light);
		dark = (Button) findViewById(R.id.button_dark);
		loadingView = (LoadingSurfaceViewCW) findViewById(R.id.loading);
		stop.setOnClickListener(listener);
		start.setOnClickListener(listener);
		light.setOnClickListener(listener);
		dark.setOnClickListener(listener);
		ProgressHub.getInstance().init(MainActivity.this, ProgressHub.STYLE_LIGHT);
	}

	OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.button_start:
//				loadingView.startLoading();
				ProgressHub.getInstance().show("测试啦啦啦！");
				break;
			case R.id.button_stop:
//				loadingView.stopLoading();
				ProgressHub.getInstance().dismiss();

				break;
			case R.id.button_light:
				ProgressHub.getInstance().setStyle(ProgressHub.STYLE_LIGHT);
				break;
			case R.id.button_dark:
				ProgressHub.getInstance().setStyle(ProgressHub.STYLE_DARK);

				break;
			default:
				break;
			}
		}
	};

}
