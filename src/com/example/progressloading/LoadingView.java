package com.example.progressloading;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * 
 * 自定义的云存储加载动画定义
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
public class LoadingView extends ImageView {

	int width;
	int height;
	int frame;
	Paint paint;
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
	final int DISTANCE = 5;
	int startTop;
	final float LEFT_MAX_SWEEP = 270.0f;
	final float LEFT_START_ANGLE = 90.0f;
	final float RIGHT_START_ANGLE = -90.0f;
	final float TOP_START_ANGLE = 190.0f;
	final float TOP_MAX_SWEEP = 180.0f;
	final float RIGHT_MAX_SWEEP = 180.0f;

	float leftSweep = LEFT_MAX_SWEEP;
	float rightSweep = RIGHT_MAX_SWEEP;
	float topSweep = RIGHT_MAX_SWEEP;
	float lineStartX;
	float lineMaxLength;
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

	public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public LoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LoadingView(Context context) {
		super(context);
		init();
	}

	private void init() {
		setBackgroundColor(Color.WHITE);
		paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(PAINT_STOKE_WIDTH);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		// drawRect(canvas);
		super.onDraw(canvas);
		if (isAnimating)
			drawFrame(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		width = getWidth();
		height = getHeight();
		resetFrameData();
		Log.i("", String.format("width = %d,height = %d", width, height));
	}

	/**
	 * 绘制帧动画
	 * 
	 * @param canvas
	 */
	private void drawFrame(Canvas canvas) {
		if (leftSweep > 0) {
			drawLeft(canvas);
		} else if (drawingPart == PART_LEFT) {
			drawingPart = PART_LINE;
		}
		if (lineStartX < lineMaxLength) {
			drawLine(canvas);
		} else if (drawingPart == PART_LINE) {
			drawingPart = PART_RIGHT;
		}
		if (rightSweep > 0) {
			drawRight(canvas);
		} else if (drawingPart == PART_RIGHT) {
			drawingPart = PART_TOP;
		}
		if (topSweep > 0) {
			drawTop(canvas);
		} else if (drawingPart == PART_TOP) {
			waitCount++;
			if (waitCount > MAX_WAIT_COUNT) {
				resetFrameData();
			}
		}
		removeCallbacks(invalidateRunnable);
		postDelayed(invalidateRunnable, DELAY_MILLIS);
	}

	private void drawRectFrame(Canvas canvas) {
		RectF rect;
		if (height - startTop < DISTANCE) {
			rect = new RectF(0, DISTANCE, width, height - DISTANCE);
		} else {
			rect = new RectF(0, height - startTop, width, height - DISTANCE);
		}
		canvas.drawRect(rect, paint);
		startTop += DISTANCE;
		startTop %= (height + 5 * DISTANCE);
	}

	/**
	 * 左边部分动画
	 * 
	 * @param canvas
	 */
	private void drawLeft(Canvas canvas) {
		RectF rect = new RectF(0, height - width / 2 - PAINT_STOKE_WIDTH / 2,
				width / 2, height - PAINT_STOKE_WIDTH / 2);
		canvas.drawArc(rect, LEFT_START_ANGLE, leftSweep, true, paint);
		if (drawingPart == PART_LEFT)
			leftSweep -= OFFSET_ANGLE;
	}

	/**
	 * 底部动画
	 * 
	 * @param canvas
	 */
	private void drawLine(Canvas canvas) {
		if (drawingPart == PART_LINE)
			lineStartX += DISTANCE;
		canvas.drawLine(lineStartX, height - PAINT_STOKE_WIDTH / 2,
				lineMaxLength, height - PAINT_STOKE_WIDTH / 2, paint);

	}

	/**
	 * 右测不封的动画
	 * 
	 * @param canvas
	 */
	private void drawRight(Canvas canvas) {
		if (drawingPart == PART_RIGHT)
			rightSweep -= OFFSET_ANGLE;
		RectF rect = new RectF(width - height / 2, height / 2
				- PAINT_STOKE_WIDTH / 2, width, height - PAINT_STOKE_WIDTH / 2);
		canvas.drawArc(rect, RIGHT_START_ANGLE, rightSweep, true, paint);

	}

	/**
	 * 顶部动画
	 * 
	 * @param canvas
	 */
	private void drawTop(Canvas canvas) {
		if (drawingPart == PART_TOP)
			topSweep -= OFFSET_ANGLE;
		RectF rect = new RectF(width / 4, 0, width - height / 4, 3 * width / 4
				- height / 4);
		canvas.drawArc(rect, TOP_START_ANGLE , topSweep, true,
				paint);

	}

	/**
	 * 重设绘制参数
	 */
	private void resetFrameData() {
		leftSweep = LEFT_MAX_SWEEP;
		rightSweep = RIGHT_MAX_SWEEP;
		topSweep = RIGHT_MAX_SWEEP;
		lineStartX = width / 4 - DISTANCE;
		lineMaxLength = width - height / 4 + DISTANCE;
		drawingPart = PART_LEFT;
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

	}
}
