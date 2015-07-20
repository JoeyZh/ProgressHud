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
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 
 * 自定义弹出的加载框，使用前需要先调用{@link #init(Context, int)},初始化
 * 
 * 
 * @author Joey
 * 
 */
public class ProgressHub {

	private static ProgressHub hub;
	private Context context;

	private WindowManager wm;
	private ProgressLayout layout;
	int width = 150;
	int height = 150;

	private boolean isShowing = false;
	/**
	 * mark window style dark or light
	 */
	public final static int STYLE_DARK = 0;
	public final static int STYLE_LIGHT = 1;

	private int style;

	private ProgressHub() {

	}
	/**
	 * 获取全局显示的对象
	 * @return
	 */
	public static ProgressHub getInstance() {
		if (hub != null) {
			return hub;
		}
		synchronized (ProgressHub.class) {
			if (hub != null)
				return hub;
			hub = new ProgressHub();
			return hub;
		}
	}
	/**
	 * 
	 * @param context 上下文
	 * 
	 * @param style
	 *            {@link #STYLE_DARK } = 0 {@link #STYLE_LIGHT } = 1
	 */
	 
	public void init(Context context, int style) {

		Log.i("ProgressHub", "init");
		this.context = context;
		wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);

		float density = outMetrics.density;
		width *= density;
		height *= density;

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
	public void show(String text) {
		if (isShowing)
		{
			dismiss();
		}
		WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
		wmParams.type = 2002; // type是关键，这里的2002表示系统级窗口，你也可以试试2003。
		wmParams.format = 1;
		wmParams.flags = 40;
		wmParams.width = width;
		wmParams.height = height;
		wmParams.gravity = Gravity.CENTER;
		if (layout == null) {
			layout = new ProgressLayout(context);
		}

		layout.loadingText.setText(text);
		wm.addView(layout, wmParams);
		layout.clearAnimation();
		isShowing = true;
	}

	/**
	 * 加载完毕，关闭View显示
	 */
	public void dismiss() {
		if (!isShowing)
			return;
		wm.removeView(layout);
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

		LoadingSurfaceView loadingView;
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
			loadingView = (LoadingSurfaceView) findViewById(R.id.loadingView);
			loadingText = (TextView) findViewById(R.id.loadingText);
			setStyle();
		}

		private void setStyle() {
			if (style == STYLE_DARK) {
				loadingText.setTextColor(Color.WHITE);
				loadingView.setPaintColor(Color.DKGRAY);
				shadowColor = Color.LTGRAY;
				backgroundColor = Color.DKGRAY;
			} else if (style == STYLE_LIGHT) {
				loadingView.setPaintColor(Color.WHITE);
				loadingText.setTextColor(Color.BLACK);
				backgroundColor = Color.WHITE;
				shadowColor = Color.LTGRAY;
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
