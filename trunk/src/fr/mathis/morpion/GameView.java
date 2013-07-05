package fr.mathis.morpion;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import fr.mathis.morpion.tools.ColorHolder;

public class GameView extends View {

	public static int MODE_INTERACTIVE = 0;
	public static int MODE_NOT_INTERACTIVE = 1;

	public static int STYLE_TOP_VERTICAL = 4;
	public static int STYLE_CENTER_HORIZONTAL = 2;
	public static int STYLE_CENTER_VERTICAL = 3;
	public static int STYLE_CENTER_BOTH = 5;
	public static int STYLE_TOP_VERTICAL_CENTER_HORIZONTAL = 6;

	private int _viewHeight;
	private int _viewWidth;
	private int rHeight;
	private int rWidth;
	Context context;
	int barcolor = Color.LTGRAY;
	int bluepayercolor = Color.BLUE;
	int redplayercolor = Color.RED;
	Bitmap bluedrawable = null;
	Bitmap reddrawable = null;
	int[][] values = null;
	int currentMode = MODE_NOT_INTERACTIVE;
	int style = STYLE_TOP_VERTICAL;
	Paint _paint;
	boolean isDark = false;
	float strikeWidth = 2;
	int nextTurn = MainActivity.BLUE_PLAYER;
	boolean showWinner = false;
	boolean isHoveredMode = false;
	int alphaForSet = 255;

	GameHandler delegate;
	HoverHandler handler;

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		loadcolors();

		_paint = new Paint();
		_paint.setFilterBitmap(true);
		_paint.setAntiAlias(true);
	}

	public void loadcolors() {
		bluepayercolor = Color.parseColor(ColorHolder.getInstance(context).getColor(MainActivity.BLUE_PLAYER));
		redplayercolor = Color.parseColor(ColorHolder.getInstance(context).getColor(MainActivity.RED_PLAYER));
		bluedrawable = BitmapFactory.decodeResource(context.getResources(), ColorHolder.getInstance(context).getDrawable(MainActivity.BLUE_PLAYER));
		reddrawable = BitmapFactory.decodeResource(context.getResources(), ColorHolder.getInstance(context).getDrawable(MainActivity.RED_PLAYER));

	}

	public static int convertDpToPixel(float dp, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return (int) px;
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		int xOffset = 0;
		int yOffset = 0;

		if (rHeight < rWidth) {
			xOffset = rWidth - rHeight;
			_viewWidth = _viewHeight;

		} else {
			yOffset = rHeight - rWidth;
			_viewHeight = _viewWidth;
		}

		xOffset = xOffset / 2;
		yOffset = yOffset / 2;

		if (style == STYLE_CENTER_HORIZONTAL) {
			yOffset = 0;
		} else if (style == STYLE_CENTER_VERTICAL) {
			xOffset = 0;
		} else if (style == STYLE_CENTER_BOTH) {

		} else if (style == STYLE_TOP_VERTICAL_CENTER_HORIZONTAL) {
			yOffset = 0;
		} else {
			xOffset = 0;
			yOffset = 0;
		}

		_paint.setColor(barcolor);
		for (int i = 1; i < 3; i++) {
			canvas.drawRect(new Rect(xOffset + _viewWidth * i / 3 - (convertDpToPixel(strikeWidth, context) == 0 ? 1 : convertDpToPixel(strikeWidth, context)), yOffset + 0, xOffset + _viewWidth * i / 3 + (convertDpToPixel(strikeWidth, context) == 0 ? 1 : convertDpToPixel(strikeWidth, context)), yOffset + _viewHeight), _paint);
			canvas.drawRect(new Rect(xOffset + 0, yOffset + _viewHeight * i / 3 - (convertDpToPixel(strikeWidth, context) == 0 ? 1 : convertDpToPixel(strikeWidth, context)), xOffset + _viewWidth, yOffset + _viewHeight * i / 3 + (convertDpToPixel(strikeWidth, context) == 0 ? 1 : convertDpToPixel(strikeWidth, context))), _paint);
		}

		if (downI != -1 && downY != -1) {
			if (nextTurn == MainActivity.BLUE_PLAYER)
				_paint.setColor(bluepayercolor);
			else
				_paint.setColor(redplayercolor);
			_paint.setAlpha(isDark ? 80 : 40);
			int offset = (convertDpToPixel(strikeWidth, context) == 0 ? 1 : convertDpToPixel(strikeWidth, context));
			canvas.drawRect(new Rect(xOffset + _viewWidth * downI / 3 + (downI == 0 ? 0 : offset), yOffset + _viewHeight * downY / 3 + (downY == 0 ? 0 : offset), xOffset + _viewWidth * (downI + 1) / 3 - (downI == 2 ? 0 : offset), yOffset + _viewHeight * (downY + 1) / 3 - (downY == 2 ? 0 : offset)), _paint);
			_paint.setAlpha(255);
		}

		if (showWinner && !isHoveredMode) {

			ArrayList<Point> wins = checkWinner();

			for (Point p : wins) {
				int res = values[p.x][p.y];

				int v = p.y;
				p.y = p.x;
				p.x = v;

				if (res != MainActivity.NONE_PLAYER) {
					if (res == MainActivity.BLUE_PLAYER)
						_paint.setColor(bluepayercolor);
					else
						_paint.setColor(redplayercolor);
					_paint.setAlpha(isDark ? 160 : 80);
					int offset = (convertDpToPixel(strikeWidth, context) == 0 ? 1 : convertDpToPixel(strikeWidth, context));
					canvas.drawRect(new Rect(xOffset + _viewWidth * p.x / 3 + (p.x == 0 ? 0 : offset), yOffset + _viewHeight * p.y / 3 + (p.y == 0 ? 0 : offset), xOffset + _viewWidth * (p.x + 1) / 3 - (p.x == 2 ? 0 : offset), yOffset + _viewHeight * (p.y + 1) / 3 - (p.y == 2 ? 0 : offset)), _paint);
					_paint.setAlpha(255);
				}
			}
		}

		if (values != null) {
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					if (values[i][j] == MainActivity.BLUE_PLAYER) {
						canvas.drawBitmap(bluedrawable, new Rect(0, 0, bluedrawable.getWidth(), bluedrawable.getHeight()), new Rect(xOffset + _viewWidth * j / 3 + _viewHeight / (15), yOffset + _viewHeight * i / 3 + _viewHeight / (15), xOffset + _viewWidth * (j + 1) / 3 - _viewHeight / (15), yOffset + _viewHeight * (i + 1) / 3 - _viewHeight / (15)), _paint);
					}
					if (values[i][j] == MainActivity.RED_PLAYER) {
						canvas.drawBitmap(reddrawable, new Rect(0, 0, bluedrawable.getWidth(), bluedrawable.getHeight()), new Rect(xOffset + _viewWidth * j / 3 + _viewHeight / (15), yOffset + _viewHeight * i / 3 + _viewHeight / (15), xOffset + _viewWidth * (j + 1) / 3 - _viewHeight / (15), yOffset + _viewHeight * (i + 1) / 3 - _viewHeight / (15)), _paint);
					}
				}

			}
		}

		if (isHoveredMode) {
			_paint.setColor(Color.parseColor("#33B5E5"));
			_paint.setAlpha(isDark ? 160 : 80);
			canvas.drawRect(new Rect(xOffset, yOffset, -xOffset + _viewWidth, -yOffset + _viewHeight), _paint);
			_paint.setAlpha(255);
		}
	}

	private ArrayList<Point> checkWinner() {

		ArrayList<Point> res = new ArrayList<Point>();

		if (values[0][0] == values[0][1] && values[0][1] == values[0][2] && values[0][2] != MainActivity.NONE_PLAYER) {
			res.add(new Point(0, 0));
			res.add(new Point(0, 1));
			res.add(new Point(0, 2));
		} else if (values[1][0] == values[1][1] && values[1][1] == values[1][2] && values[1][2] != MainActivity.NONE_PLAYER) {
			res.add(new Point(1, 0));
			res.add(new Point(1, 1));
			res.add(new Point(1, 2));
		} else if (values[2][0] == values[2][1] && values[2][1] == values[2][2] && values[2][2] != MainActivity.NONE_PLAYER) {
			res.add(new Point(2, 0));
			res.add(new Point(2, 1));
			res.add(new Point(2, 2));
		} else if (values[0][0] == values[1][0] && values[1][0] == values[2][0] && values[2][0] != MainActivity.NONE_PLAYER) {
			res.add(new Point(0, 0));
			res.add(new Point(1, 0));
			res.add(new Point(2, 0));
		} else if (values[0][1] == values[1][1] && values[1][1] == values[2][1] && values[2][1] != MainActivity.NONE_PLAYER) {
			res.add(new Point(0, 1));
			res.add(new Point(1, 1));
			res.add(new Point(2, 1));
		} else if (values[0][2] == values[1][2] && values[1][2] == values[2][2] && values[2][2] != MainActivity.NONE_PLAYER) {
			res.add(new Point(0, 2));
			res.add(new Point(1, 2));
			res.add(new Point(2, 2));
		} else if (values[0][0] == values[1][1] && values[1][1] == values[2][2] && values[2][2] != MainActivity.NONE_PLAYER) {
			res.add(new Point(0, 0));
			res.add(new Point(1, 1));
			res.add(new Point(2, 2));
		} else if (values[2][0] == values[1][1] && values[1][1] == values[0][2] && values[0][2] != MainActivity.NONE_PLAYER) {
			res.add(new Point(2, 0));
			res.add(new Point(1, 1));
			res.add(new Point(0, 2));
		}
		return res;
	}

	public void setValues(int[][] values2, int nextTurn) {
		this.values = values2;
		this.nextTurn = nextTurn;
		this.invalidate();
	}

	public int[][] getValues() {
		return values;
	}

	public void setMode(int mode) {
		currentMode = mode;
	}

	public void setAlignement(int style) {
		this.style = style;
	}

	int downI = -1;
	int downY = -1;

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (currentMode == MODE_INTERACTIVE) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				int xOffset = 0;
				int yOffset = 0;

				if (rHeight < rWidth) {
					xOffset = rWidth - rHeight;
					_viewWidth = _viewHeight;

				} else {
					yOffset = rHeight - rWidth;
					_viewHeight = _viewWidth;
				}

				xOffset = xOffset / 2;
				yOffset = yOffset / 2;

				if (style == STYLE_CENTER_HORIZONTAL) {
					yOffset = 0;
				} else if (style == STYLE_CENTER_VERTICAL) {
					xOffset = 0;
				} else if (style == STYLE_CENTER_BOTH) {

				} else if (style == STYLE_TOP_VERTICAL_CENTER_HORIZONTAL) {
					yOffset = 0;
				} else {
					xOffset = 0;
					yOffset = 0;
				}

				int x = (int) event.getX();
				int y = (int) event.getY();
				x -= xOffset;
				y -= yOffset;

				if (x < _viewWidth && y < _viewHeight) {

					int i = 0;
					if (x < _viewWidth / 3)
						i = 0;
					else if (x < (_viewWidth / 3) * 2)
						i = 1;
					else if (x < _viewWidth)
						i = 2;

					int j = 0;
					if (y < _viewHeight / 3)
						j = 0;
					else if (y < (_viewHeight / 3) * 2)
						j = 1;
					else if (y < _viewHeight)
						j = 2;

					if (values == null || values[j][i] == MainActivity.NONE_PLAYER) {
						downY = j;
						downI = i;
					}

					this.invalidate();
				}
			}
			if (event.getAction() == MotionEvent.ACTION_MOVE) {

			}

			if (event.getAction() == MotionEvent.ACTION_UP) {
				int xOffset = 0;
				int yOffset = 0;

				if (rHeight < rWidth) {
					xOffset = rWidth - rHeight;
					_viewWidth = _viewHeight;

				} else {
					yOffset = rHeight - rWidth;
					_viewHeight = _viewWidth;
				}

				xOffset = xOffset / 2;
				yOffset = yOffset / 2;

				if (style == STYLE_CENTER_HORIZONTAL) {
					yOffset = 0;
				} else if (style == STYLE_CENTER_VERTICAL) {
					xOffset = 0;
				} else if (style == STYLE_CENTER_BOTH) {

				} else if (style == STYLE_TOP_VERTICAL_CENTER_HORIZONTAL) {
					yOffset = 0;
				} else {
					xOffset = 0;
					yOffset = 0;
				}

				int x = (int) event.getX();
				int y = (int) event.getY();
				x -= xOffset;
				y -= yOffset;

				if (x < _viewWidth && y < _viewHeight && x >= 0 && y >= 0) {

					int i = 0;
					if (x < _viewWidth / 3)
						i = 0;
					else if (x < (_viewWidth / 3) * 2)
						i = 1;
					else if (x < _viewWidth)
						i = 2;

					int j = 0;
					if (y < _viewHeight / 3)
						j = 0;
					else if (y < (_viewHeight / 3) * 2)
						j = 1;
					else if (y < _viewHeight)
						j = 2;

					if (downI == i && downY == j) {
						if (delegate != null) {
							delegate.handleTurn(j, i);
						}
					}
				}

				downI = -1;
				downY = -1;
				this.invalidate();
			}
			return true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public boolean isInEditMode() {
		return false;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int desiredWidth = (int) GameView.convertDpToPixel(48 * 3, context);
		int desiredHeight = desiredWidth;

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width;
		int height;

		if (widthMode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else if (widthMode == MeasureSpec.AT_MOST) {
			width = Math.min(desiredWidth, widthSize);
		} else {
			width = desiredWidth;
		}

		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else if (heightMode == MeasureSpec.AT_MOST) {
			height = Math.min(desiredHeight, heightSize);
		} else {
			height = desiredHeight;
		}

		_viewHeight = height;
		_viewWidth = width;
		rHeight = _viewHeight;
		rWidth = _viewWidth;
		setMeasuredDimension(width, height);
	}

	public boolean isDark() {
		return isDark;
	}

	public void setDark(boolean isDark) {
		this.isDark = isDark;
		if (isDark)
			barcolor = Color.DKGRAY;
		else
			barcolor = Color.LTGRAY;
	}

	public float getStrikeWidth() {
		return strikeWidth;
	}

	public void setStrikeWidth(float strikeWidth) {
		this.strikeWidth = strikeWidth;
	}

	public void setDelegate(GameHandler delegate) {
		this.delegate = delegate;
	}

	public boolean isShowWinner() {
		return showWinner;
	}

	public void setShowWinner(boolean showWinner) {
		this.showWinner = showWinner;
	}

	public interface GameHandler {
		public void handleTurn(int i, int j);
	}

	public void setHoverHandler(HoverHandler handler) {
		this.handler = handler;
	}

	public boolean isHoveredMode() {
		return isHoveredMode;
	}

	public void setHoveredMode(boolean isHoveredMode) {
		this.isHoveredMode = isHoveredMode;
		invalidate();
	}

	@Override
	public boolean dispatchGenericMotionEvent(MotionEvent ev) {
		if (handler != null) {
			handler.give(ev, this);
			return true;
		} else
			return true;
	}

}
