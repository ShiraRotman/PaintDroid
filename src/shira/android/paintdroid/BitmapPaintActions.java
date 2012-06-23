package shira.android.paintdroid;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

interface BitmapInfo
{
	public abstract int getWidth();
	public abstract int getHeight();
	public abstract int getColor(int pointX,int pointY);
}

abstract class BitmapPaintAction extends AbstractPaintAction
{
	protected BitmapInfo bitmapInfo;
	
	protected BitmapPaintAction(BitmapInfo bitmapInfo)
	{
		if (bitmapInfo==null)
			throw new IllegalArgumentException("The bitmap info object must " + 
					"be non-null!");
		this.bitmapInfo=bitmapInfo;
	}
	
	public BitmapInfo getBitmapInfo() { return bitmapInfo; }
	
	public boolean usesLocalPoints() { return false; }
}

class PickColorAction extends BitmapPaintAction
{
	private OnColorPickListener listener;
	private int pickedColor;
	
	public static interface OnColorPickListener
	{
		public abstract void onColorPick(int color);
	}
	
	public PickColorAction(BitmapInfo bitmapInfo) { super(bitmapInfo); }
	
	public OnColorPickListener getOnColorPickListener() { return listener; }
	public void setOnColorPickListener(OnColorPickListener listener)
	{ this.listener=listener; }
	
	public boolean isPermanentChange() { return false; } 
	@Override public RectF getLastAffectedArea() { return null; }
	public void draw(Canvas canvas,Paint paint) { }
	
	public void actOnPoint(float pointX,float pointY,boolean isFinalPoint)
	{
		pickedColor=bitmapInfo.getColor(Math.round(pointX),Math.round(pointY));
		//TODO: Update content view
		if ((isFinalPoint)&&(listener!=null)) listener.onColorPick(pickedColor); 
	}
	
	public void finishWithLastPoint() 
	{ if (listener!=null) listener.onColorPick(pickedColor); }
}
