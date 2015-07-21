package com.example.progressloading;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

/**
 * 
 * 自定义弹出的加载框，使用前需要先调用{@link #init(Context, int)},初始化
 * 
 * 
 * @author Joey
 * 
 */
public class ProgressHub2 {

	private static ProgressHub2 hub;
	private Context context;

	private WindowManager wm;
	private ProgressLayout layout;
	private final int WIDTH = 100;
	private final int HEIGHT = 100;
	int width = WIDTH;
	int height = HEIGHT;
	private PopupWindow popWin;
	private ViewGroup parent;

	private boolean isShowing = false;
	/**
	 * mark window style dark or light
	 */
	public final static int STYLE_DARK = 0;
	public final static int STYLE_LIGHT = 1;

	private int style;

	private ProgressHub2() {

	}

	/**
	 * 获取全局显示的对象
	 * 
	 * @return
	 */
	public static ProgressHub2 getInstance() {
		if (hub != null) {
			return hub;
		}
		synchronized (ProgressHub2.class) {
			if (hub != null)
				return hub;
			hub = new ProgressHub2();
			return hub;
		}
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
	 * 显示loading视图,
	 * 
	 * @param text
	 *            要显示的文字
	 */
	public void show(String text,ViewGroup root) {
	
		if(isShowing)
			return;
		if(root == null)
			return;
		parent = root;
		if (layout == null) {
			layout = new ProgressLayout(context);
		}
		if(parent instanceof LinearLayout)
		{
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
			params.gravity = Gravity.CENTER;
			parent.addView(layout, params);
		}
		else if(parent instanceof FrameLayout)
		{
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width,height);
			params.gravity = Gravity.CENTER;
			parent.addView(layout, params);
		}
		else if(parent instanceof RelativeLayout)
		{
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			parent.addView(layout, params);
		}
		else
		{
			parent.addView(layout, width, height);
		}
		layout.clearAnimation();
		layout.setVisibility(View.VISIBLE);
		layout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.pop_in));
		if(text == null||text.equals(""))
			text = context.getResources().getString(R.string.waiting);
		layout.loadingText.setText(text);
	
		layout.loadingView.startLoading();
		isShowing = true;
		
	}

	/**
	 * 加载完毕，关闭View显示
	 */
	public void dismiss() {
		if (!isShowing)
			return;
		if(layout == null)
			return;
		layout.loadingView.stopLoading();
		layout.setVisibility(View.GONE);
		if(parent != null)
			parent.removeView(layout);
		layout = null;

		isShowing = false;
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
//			Log.i("progressHub", "onDraw :"+ rect.toShortString());
			canvas.drawRoundRect(rect, RX, RX, paint);
		}
		
		

	}

}
