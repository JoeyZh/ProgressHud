package com.example.progressloading;

import com.example.progressloading.ProgressHub2.ProgressLayout;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class JVProgressDialog extends Dialog{
	
	ProgressLayout layout ;

	private WindowManager wm;
	private final int WIDTH = 100;
	private final int HEIGHT = 100;
	int width = WIDTH;
	int height = HEIGHT;
	private Context context;
	
	private boolean isShowing = false;
	/**
	 * mark window style dark or light
	 */
	public final static int STYLE_DARK = 0;
	public final static int STYLE_LIGHT = 1;

	private int style;
	
	public JVProgressDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
		init(context,STYLE_LIGHT);

	}

	protected JVProgressDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init(context,STYLE_LIGHT);

		// TODO Auto-generated constructor stub
	}
	public JVProgressDialog(Context context) {
		super(context,R.style.progress_dlg_style);
		init(context,STYLE_LIGHT);
		// TODO Auto-generated constructor stub
	}

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        layout = new ProgressLayout(context);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width,height);
        layout.setLayoutParams(params);
        setContentView(layout);

    }

    @Override
    protected void onStart() {
      
     
    }

    @Override
    protected void onStop() {
       
    }
	
    @Override
	public void show() {
		// TODO Auto-generated method stub
		super.show();
		layout.loadingView.startLoading();
	}

    
    public void show(String msg)
    {
    	show();
    	layout.loadingText.setText(msg);
    }
    public void show(int id)
    {
    	show();
    	layout.loadingText.setText(id);
    }
	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		super.cancel();
		layout.loadingView.stopLoading();

	}

	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		super.dismiss();
		layout.loadingView.stopLoading();
	}


	/**
	 * 
	 * @param context
	 *            上下文
	 * 
	 * @param style
	 *            {@link #STYLE_DARK } = 0 {@link #STYLE_LIGHT } = 1
	 */

	public void init(Context context, int style) {

		Log.i("ProgressHub", "init");
		this.context = context.getApplicationContext();
		wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);

		float density = outMetrics.density;
		width = (int)(WIDTH*density);
		height = (int)(HEIGHT*density);

		Log.i("ProgressHub",
				String.format("width = %d,height= %d", width, height));
		setStyle(style);
	}

	/**
	 * 
	 * @param style
	 *            {@link #STYLE_DARK } = 0 {@link #STYLE_LIGHT } = 1
	 */
	public void setStyle(int style) {
		this.style = style;
		if (layout != null)
			layout.setStyle();
	}

	/**
	 * 
	 * 加载界面的布局定义
	 * 
	 * @author Joey
	 * 
	 */
	class ProgressLayout extends RelativeLayout {

		LoadingSurfaceViewCW loadingView;
		TextView loadingText;
		int backgroundColor;
		int shadowColor;
		final float SHADOW_D = 1.5f;
		final float SHADOW_DADIUS = 3.0f;
		Paint paint;
		private final float RX = 15.0f;

		public ProgressLayout(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			init(context);
		}

		public ProgressLayout(Context context, AttributeSet attrs) {
			super(context, attrs);
			init(context);
		}

		public ProgressLayout(Context context) {
			super(context);
			init(context);
		}

		private void init(Context context) {
			View.inflate(context, R.layout.progress_layout, this);
			loadingView = (LoadingSurfaceViewCW) findViewById(R.id.loadingView);
			loadingText = (TextView) findViewById(R.id.loadingText);
			setStyle();
		}

		private void setStyle() {
			if (style == STYLE_DARK) {
				loadingText.setTextColor(Color.WHITE);
				loadingView.setPaintColor(Color.DKGRAY);
				shadowColor = Color.WHITE;
				backgroundColor = Color.DKGRAY;
			} else if (style == STYLE_LIGHT) {
				loadingView.setPaintColor(Color.WHITE);
				loadingText.setTextColor(Color.DKGRAY);
				backgroundColor = Color.WHITE;
				shadowColor = Color.DKGRAY;
			}
			initPaint();
			
		}

		private void initPaint()
		{
			
			if(paint == null)
				paint = new Paint();
			paint.setColor(backgroundColor);
			paint.setAntiAlias(true);
			paint.setShadowLayer(SHADOW_DADIUS, SHADOW_D, SHADOW_D, shadowColor);
			paint.setStrokeJoin(Paint.Join.ROUND);
			paint.setStrokeCap(Paint.Cap.ROUND);
			setBackgroundColor(Color.TRANSPARENT);
		}
		
		@Override
		public void onDraw(Canvas canvas) {
			// TODO Auto-generated method stub
			super.onDraw(canvas);
			RectF rect = new RectF(SHADOW_DADIUS, SHADOW_DADIUS, width-SHADOW_DADIUS*2, height-SHADOW_DADIUS*2);
			canvas.drawRoundRect(rect, RX, RX, paint);
		}
		
		

	}

}
