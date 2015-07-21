package com.example.progressloading;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

/**
 * 自定义的云存储加载动画定义,顺时针绘制云的轨迹
 * <dl>
 * 开始loading需要调用{@link #startLoading()}启动动画
 * </dl>
 * <dl>
 * 结束动画需要调用{@link #stopLoading()}停止动画
 * </dl>
 * <dl>
 * 通过调用 {@link #setSpeed(int)} 调整加载速率
 * </dl>
 * 
 * @author Joey
 * 
 */
public class LoadingSurfaceViewCW extends SurfaceView implements
		SurfaceHolder.Callback {

	Bitmap bitmap;
	SurfaceHolder sfh;
	int width;
	int height;
	int frame;
	Paint paint;
	/**
	 * 画笔宽度
	 */
	float PAINT_STOKE_WIDTH = 10.0f;
	/**
	 * 角度递增量
	 */
	float OFFSET_ANGLE = 10.0f;
	/**
	 * 位置递增量，画直线或者矩形动画使用
	 */
	final int DISTANCE = 5;
	/**
	 * 底部线的绘制速度
	 */
	float speed = DISTANCE;
	float angleSpeed = OFFSET_ANGLE;
	final float LEFT_MAX_SWEEP = 280.0f;
	final float LEFT_START_ANGLE = 10.0f;
	final float RIGHT_START_ANGLE = 90.0f;
	final float TOP_START_ANGLE = 10.0f;
	final float TOP_MAX_SWEEP = 200.0f;
	final float RIGHT_MAX_SWEEP = 200.0f;

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
	private final int PART_LEFT = 0; // 左侧
	private final int PART_BOTTOM = 1;
	private final int PART_RIGHT = 2;
	private final int PART_TOP = 3;

	private boolean isAnimating = true;
	private final int MAX_WAIT_COUNT = 10;
	private int waitCount = 0;
	private Context context;
	private DrawLooper drawRunnable;
	private Thread animationThread;
	/**
	 * 屏幕密度
	 */
	float density;

	public LoadingSurfaceViewCW(Context context) {
		super(context);
		init(context);
	}

	public LoadingSurfaceViewCW(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public LoadingSurfaceViewCW(Context context, AttributeSet attrs,
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
		setZOrderOnTop(true);
		sfh.setFormat(PixelFormat.TRANSPARENT);
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);

		density = outMetrics.density;
		PAINT_STOKE_WIDTH *= density;
		initPaint();
		setSpeed(1);
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

	/**
	 * 设置绘制速率
	 * 
	 * @param speed
	 */
	public void setSpeed(int speed) {
		if (speed <= 0)
			speed = 1;
		this.speed = density * speed * DISTANCE;
		angleSpeed = density * OFFSET_ANGLE * speed;
	}

	/**
	 * 设置画笔颜色
	 * 
	 * @param color
	 */
	public void setPaintColor(int color) {
		if (paint == null)
			initPaint();
		paint.setColor(color);
		setBackgroundColor(color);
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
		width = bitmap.getWidth();
		height = bitmap.getHeight();
		Log.i("SurfaceView", "imgWidth =" + width + ",imgHeight = " + height);
		resetFrameData();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i("Surface:", "Destroy");
		stopLoading();
		bitmap.recycle();
	}

	/**
	 * 读取并显示图片的线程
	 */
	class DrawLooper implements Runnable {
		private int count = 0;

		public void reset() {
			count = 0;
		}

		public void run() {
			Log.i("SurfaceView", "Load_DrawImage run");
			// 为了避免先绘制云图片的问题，所以延时处理，等待背景画出来之后，再开始动画效果
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while (isAnimating) {
				if (!isShown()) {
					Log.i("surfaceView", "not showing");
					break;
				}
				Canvas c = sfh.lockCanvas();
				if (c == null) {
					Log.i("surfaceView", "canvas null");
					break;
				}
				Log.i("SurfaceView", String.format("width = %d,height = %d ",
						getHeight(), getWidth()));
				/**
				 * 清屏
				 */
				c.drawColor(paint.getColor());
				/**
				 * 绘制内容
				 */
				c.save();
				int tx = getWidth() / 2 - width / 2;
				int ty = getHeight() / 2 - height / 2;
				// Log.i("SurfaceView",
				// String.format("tx = %d,ty = %d ", tx, ty));
				c.translate(tx, ty);
				if (bitmap.isRecycled()) {
					Log.i("surfaceView", "bitmap recycled");
					break;
				}
				c.drawBitmap(bitmap, 0, 0, paint);
				drawFrame(c);
				c.restore();

				count++;
				sfh.unlockCanvasAndPost(c);// 更新屏幕显示内容
			}
			clearScreen();

			Log.i("SurfaceView", "end run");

		}
	};

	private void clearScreen() {
		Canvas c = sfh.lockCanvas();
		if (c == null)
			return;
		/**
		 * 清屏
		 */
		c.drawColor(Color.TRANSPARENT);
		// c.save();
		// int tx = getWidth() / 2 - width / 2;
		// int ty = getHeight() / 2 - height / 2;
		// c.translate(tx, ty);
		// if(bitmap.isRecycled())
		// return;
		// c.drawBitmap(bitmap, 0, 0, paint);
		// c.restore();
		/**
		 * 绘制内容
		 */
		sfh.unlockCanvasAndPost(c);// 更新屏幕显示内容
	}

	public void startLoading() {
		drawRunnable.reset();
		resetFrameData();
		isAnimating = true;
		if (animationThread != null && animationThread.isAlive()) {
			return;
		}
		animationThread = new Thread(drawRunnable);
		animationThread.setName("drawRunnable");
		animationThread.start();// 开始绘图
	}

	public void stopLoading() {
		isAnimating = false;
	}

	private Runnable stopAnimRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			isAnimating = false;
		}
	};

	/**
	 * 绘制帧动画
	 * 
	 * @param canvas
	 */
	private void drawFrame(Canvas canvas) {
		if (width == 0 || height == 0)
			return;
		if (topSweep >= 0) {
			drawTop(canvas);
		} else if (drawingPart == PART_TOP) {
			drawingPart = PART_RIGHT;
		}
		if (rightSweep >= 0) {
			drawRight(canvas);
		} else if (drawingPart == PART_RIGHT) {
			drawingPart = PART_BOTTOM;
		}
		if (lineSweep >= lineStartX) {
			drawBottom(canvas);
		} else if (drawingPart == PART_BOTTOM) {
			drawingPart = PART_LEFT;
		}

		if (leftSweep >= 0) {
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
			leftSweep -= angleSpeed;
	}

	/**
	 * 底部动画
	 * 
	 * @param canvas
	 */
	private void drawBottom(Canvas canvas) {
		if (drawingPart == PART_BOTTOM)
			lineSweep -= speed;
		canvas.drawRect(lineStartX, height - 2 * PAINT_STOKE_WIDTH, lineSweep,
				height + PAINT_STOKE_WIDTH, paint);

	}

	/**
	 * 右测部分的动画
	 * 
	 * @param canvas
	 */
	private void drawRight(Canvas canvas) {
		paint.setStrokeWidth(1);
		if (drawingPart == PART_RIGHT)
			rightSweep -= angleSpeed;
		RectF rect = new RectF(width - height / 2 - PAINT_STOKE_WIDTH, height
				/ 2 - PAINT_STOKE_WIDTH, width + PAINT_STOKE_WIDTH, height
				+ PAINT_STOKE_WIDTH);
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
			topSweep -= angleSpeed;
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
