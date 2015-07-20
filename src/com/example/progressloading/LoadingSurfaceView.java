package com.example.progressloading;

import java.lang.reflect.Field;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;

public class LoadingSurfaceView extends SurfaceView implements
		SurfaceHolder.Callback {

	int imgWidth;
	int imgHeight;
	Bitmap bitmap;
	SurfaceHolder sfh;
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
	final float OFFSET_ANGLE = 20.0f;
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
	private Context context;
	private Bitmap markBmp;
	private DrawLooper drawRunnable;

	public LoadingSurfaceView(Context context) {
		super(context);
		init(context);
	}

	public LoadingSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public LoadingSurfaceView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		this.context = context;
		bitmap = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.loadingbg);
		sfh = getHolder();
		sfh.addCallback(this);
		initPaint();
		drawRunnable = new DrawLooper();
		resetFrameData();

	}

	private void initPaint() {
		paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		paint.setTextSize(50);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStyle(Paint.Style.FILL);
		paint.setStrokeWidth(PAINT_STOKE_WIDTH);
	}

	public void setPaintColor(int color) {
		if (paint == null)
			initPaint();
		paint.setColor(color);
//		setBackgroundColor(color);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.i("SurfaceView:", "Change");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i("SurfaceView:", "Create");
		// 取得图像大小
		bitmap = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.loadingbg);
		imgWidth = bitmap.getWidth();
		imgHeight = bitmap.getHeight();
		Log.i("SurfaceView", "imgWidth =" + imgWidth + ",imgHeight = "
				+ imgHeight);
		isAnimating = true;
		new Thread(drawRunnable).start();// 开始绘图
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i("Surface:", "Destroy");
		isAnimating = false;
	}

	/**
	 * 读取并显示图片的线程
	 */
	class DrawLooper implements Runnable {
		private int count = 0;

		public void reset() {
			count = 0;

		}

		public DrawLooper() {

		}

		public void run() {
			Log.i("SurfaceView", "Load_DrawImage run");
			while (isAnimating) {
				Canvas c = sfh.lockCanvas();
				if (c == null)
					break;
				Log.i("SurfaceView", String.format("width = %d,height = %d ",
						getHeight(), getWidth()));
				paint.setColor(Color.WHITE);

				c.drawColor(paint.getColor());
				c.save();
				int tx = getWidth() / 2 - imgWidth / 2;
				int ty = getHeight() / 2 - imgHeight / 2;
				Log.i("SurfaceView",
						String.format("tx = %d,ty = %d ", tx, ty));
				c.translate(tx, ty);
				c.drawBitmap(bitmap, 0, 0, paint);
				width = imgWidth;
				height = imgHeight;

				// if(count>5)
				drawFrame(c);
				c.restore();
				paint.setColor(Color.BLACK);

				c.drawText(count + "%", 0, 0, paint);

				count++;
				sfh.unlockCanvasAndPost(c);// 更新屏幕显示内容
			}
			Log.i("SurfaceView", "end run");

		}
	};

	public void startLoading() {
		drawRunnable.reset();
	}

	public void stopLoading() {
		isAnimating = false;
	}

	/**
	 * 绘制帧动画
	 * 
	 * @param canvas
	 */
	private void drawFrame(Canvas canvas) {
		if (width == 0 || height == 0)
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
	}

	/**
	 * 左边部分动画
	 * 
	 * @param canvas
	 */
	private void drawLeft(Canvas canvas) {
		paint.setStrokeWidth(1);

		RectF rect = new RectF(-PAINT_STOKE_WIDTH / 2, height - width / 2
				- PAINT_STOKE_WIDTH / 2, width / 2 + PAINT_STOKE_WIDTH / 2,
				height + PAINT_STOKE_WIDTH / 2);
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
		Log.i("surfaceView", "drawTop, topSweep = " + topSweep + ",time = "
				+ System.currentTimeMillis());
		if (drawingPart == PART_TOP)
			topSweep -= OFFSET_ANGLE;
		RectF rect = new RectF(width / 4 - PAINT_STOKE_WIDTH / 2,
				-PAINT_STOKE_WIDTH / 2, width - height / 4 + PAINT_STOKE_WIDTH,
				3 * width / 4 - height / 4 + PAINT_STOKE_WIDTH);
		canvas.drawArc(rect, TOP_START_ANGLE, -topSweep, true, paint);

	}

	/**
	 * 重设绘制参数
	 */
	private void resetFrameData() {
		leftSweep = LEFT_MAX_SWEEP;
		rightSweep = RIGHT_MAX_SWEEP;
		topSweep = TOP_MAX_SWEEP;
		lineMaxLength = width - height / 4;
		lineStartX = width / 4;
		lineSweep = lineMaxLength;
		drawingPart = PART_TOP;
		waitCount = 0;
	}
}
