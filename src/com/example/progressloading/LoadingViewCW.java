package com.example.progressloading;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * 
 * 自定义的云存储加载动画定义,顺时针绘制云的轨迹
 * <dl>
 * 开始loading需要调用{@link #startLoading()}启动动画
 * </dl>
 * <dl>
 * 结束动画需要调用{@link #stopLoading()}停止动画
 * </dl>
 * 
 * @author Joey
 * 
 */
public class LoadingViewCW extends ImageView {

	int width;
	int height;
	int frame;
	Paint paint;
	private Context context;
	/**
	 * 画笔宽度
	 */
	final int PAINT_STOKE_WIDTH = 10;
	/**
	 * 角度递增量
	 */
	final float OFFSET_ANGLE = 10.0f;
	/**
	 * 
	 */
	float startAngle = 0.0f;
	/**
	 * 位置递增量，画直线或者矩形动画使用
	 */
	final int DISTANCE = 10;
	int startTop;
	final float LEFT_MAX_SWEEP = 280.0f;
	final float LEFT_START_ANGLE = 10.0f;
	final float RIGHT_START_ANGLE = 80.0f;
	final float TOP_START_ANGLE = 10.0f;
	final float TOP_MAX_SWEEP = 200.0f;
	final float RIGHT_MAX_SWEEP = 180.0f;

	float leftSweep = LEFT_MAX_SWEEP;
	float rightSweep = RIGHT_MAX_SWEEP;
	float topSweep = RIGHT_MAX_SWEEP;
	float lineStartX;
	float lineMaxLength;
	float lineSweep;
	/**
	 * 标记正在绘制的部分
	 */
	private int drawingPart;
	private final int PART_LEFT = 0;
	private final int PART_LINE = 1;
	private final int PART_RIGHT = 2;
	private final int PART_TOP = 3;

	private boolean isAnimating = true;
	private final int MAX_WAIT_COUNT = 10;
	private int waitCount = 0;
	private final int DELAY_MILLIS = 10;
	private Runnable invalidateRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			invalidate();
		}
	};

	public LoadingViewCW(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public LoadingViewCW(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public LoadingViewCW(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		this.context = context;
		Drawable drawable = context.getResources().getDrawable(R.drawable.loadingbg);
		width = drawable.getIntrinsicWidth();
		height = drawable.getIntrinsicHeight();
//		MyLog.i(getClass().getName(), String.format("width = %d,height = %d", width,height));
		resetFrameData();
		setBackgroundColor(Color.WHITE);
		initPaint();
	}

	private void initPaint()
	{
		paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStyle(Paint.Style.FILL);
	}
	public void setPaintColor(int color)
	{
		if(paint == null)
			initPaint();
		paint.setColor(color);
		setBackgroundColor(color);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
//		MyLog.i("LoadingView", "onDraw");
//		MyLog.i("LoadingView", String.format("width = %d,height = %d", width,height));

		if (isAnimating)
			drawFrame(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	/**
	 * 绘制帧动画
	 * 
	 * @param canvas
	 */
	private void drawFrame(Canvas canvas) {
		if(width == 0 ||height == 0)
			return;
		if (topSweep > 0) {
			drawTop(canvas);
		} else if (drawingPart == PART_TOP) {
			drawingPart = PART_RIGHT;
		}
		if (rightSweep > 0) {
			drawRight(canvas);
		} else if (drawingPart == PART_RIGHT) {
			drawingPart = PART_LINE;
		}
		if (lineSweep > lineStartX) {
			drawLine(canvas);
		} else if (drawingPart == PART_LINE) {
			drawingPart = PART_LEFT;
		}

		if (leftSweep > 0) {
			drawLeft(canvas);
		} else if (drawingPart == PART_LEFT) {
			waitCount++;
			if (waitCount > MAX_WAIT_COUNT) {
				resetFrameData();
			}
		}
//		 removeCallbacks(invalidateRunnable);
		 postInvalidate();
	}

	/**
	 * 左边部分动画
	 * 
	 * @param canvas
	 */
	private void drawLeft(Canvas canvas) {
		paint.setStrokeWidth(1);

		RectF rect = new RectF(- PAINT_STOKE_WIDTH / 2,
				height - width / 2 - PAINT_STOKE_WIDTH / 2, width / 2
						+ PAINT_STOKE_WIDTH / 2, height
						+ PAINT_STOKE_WIDTH / 2);
		canvas.drawArc(rect, LEFT_START_ANGLE, -leftSweep, true, paint);
		if (drawingPart == PART_LEFT)
			leftSweep -= OFFSET_ANGLE;
	}

	/**
	 * 底部动画
	 * 
	 * @param canvas
	 */
	private void drawLine(Canvas canvas) {
		paint.setStrokeWidth(PAINT_STOKE_WIDTH);

		if (drawingPart == PART_LINE)
			lineSweep -= DISTANCE;
		canvas.drawLine(lineStartX, height - PAINT_STOKE_WIDTH / 2, lineSweep,
				height - PAINT_STOKE_WIDTH / 2, paint);

	}

	/**
	 * 右测部分的动画
	 * 
	 * @param canvas
	 */
	private void drawRight(Canvas canvas) {
		paint.setStrokeWidth(1);
		if (drawingPart == PART_RIGHT)
			rightSweep -= OFFSET_ANGLE;
		RectF rect = new RectF(width - height / 2 - PAINT_STOKE_WIDTH, height
				/ 2 - PAINT_STOKE_WIDTH, width, height);
		canvas.drawArc(rect, RIGHT_START_ANGLE, -rightSweep, true, paint);

	}

	/**
	 * 顶部动画
	 * 
	 * @param canvas
	 */
	private void drawTop(Canvas canvas) {
		paint.setStrokeWidth(1);
		if (drawingPart == PART_TOP)
			topSweep -= OFFSET_ANGLE;
		RectF rect = new RectF(width / 4 - PAINT_STOKE_WIDTH / 2,
				-PAINT_STOKE_WIDTH / 2, width - height / 4 + PAINT_STOKE_WIDTH,
				3 * width / 4 - height / 4 + PAINT_STOKE_WIDTH );
		canvas.drawArc(rect, TOP_START_ANGLE, -topSweep, true, paint);

	}
	
	private void drawBack(Canvas canvas)
	{
		paint.setStrokeWidth(1);
		RectF rect = new RectF(width / 4 - PAINT_STOKE_WIDTH / 2,
				height - width/2, width - height / 4 + PAINT_STOKE_WIDTH,
				height );
		canvas.drawRect(rect, paint);
	}

	/**
	 * 重设绘制参数
	 */
	private void resetFrameData() {
		leftSweep = LEFT_MAX_SWEEP;
		rightSweep = RIGHT_MAX_SWEEP;
		topSweep = TOP_MAX_SWEEP;
		lineMaxLength = width - height / 4;
		lineStartX = width / 4  ;
		lineSweep = lineMaxLength;
		drawingPart = PART_TOP;
		waitCount = 0;
	}

	/**
	 * 结束动画的时候需要调用
	 */
	public void stopLoading() {
		isAnimating = false;
		removeCallbacks(invalidateRunnable);
		postDelayed(invalidateRunnable, DELAY_MILLIS);
	}

	/**
	 * 开始加载的时候需要调用该方法
	 */
	public void startLoading() {
		isAnimating = true;
		resetFrameData();
		removeCallbacks(invalidateRunnable);
		postDelayed(invalidateRunnable, DELAY_MILLIS);
	}
}
