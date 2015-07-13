package com.example.progressloading;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	Button start, stop;
	LoadingView loadingView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		start = (Button) findViewById(R.id.button_start);
		stop = (Button) findViewById(R.id.button_stop);
		loadingView = (LoadingView) findViewById(R.id.loading);
	}

	OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.button_start:
				loadingView.startLoading();
				break;
			case R.id.button_stop:
				loadingView.stopLoading();

				break;
			default:
				break;
			}
		}
	};

}
