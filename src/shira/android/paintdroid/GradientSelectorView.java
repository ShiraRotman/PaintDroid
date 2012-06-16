package shira.android.paintdroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class GradientSelectorView extends View 
{
	private Bitmap gradientBitmap;
	private GradientDrawable gradient;
	private OnColorSelectListener colorSelectListener;
	private int pointerID=-1; //For API 5 and above
	
	public static interface OnColorSelectListener
	{
		public abstract void onColorSelect(int color);
	}
	
	private class GradientTouchListener implements OnTouchListener
	{
		public boolean onTouch(View view,MotionEvent event) 
		{
			int action=event.getAction();
			//For API 4 and below:
			//switch (action)
			//For API 5-7:
			int actionMask=action & MotionEvent.ACTION_MASK;
			//switch (actionMask)
			//For API 8 and above:
			//switch (event.getActionMasked)
			//(Here I'm using "if")
			if (actionMask==MotionEvent.ACTION_CANCEL)
				pointerID=-1;
			else
			{
				int pointerIndex;
				if (actionMask==MotionEvent.ACTION_DOWN)
				{
					pointerID=event.getPointerId(0);
					pointerIndex=0;
				}
				else pointerIndex=event.findPointerIndex(pointerID);				
				if (pointerIndex>-1)
				{
					int coordinateX=(int)event.getX(pointerIndex);
					int coordinateY=(int)event.getY(pointerIndex);
					if ((coordinateX>=0)&&(coordinateY>=0)&&(coordinateX<
							gradientBitmap.getWidth())&&(coordinateY<
							gradientBitmap.getHeight()))
					{
						int color=gradientBitmap.getPixel(coordinateX,coordinateY);
						colorSelectListener.onColorSelect(color);
					}
				}
				if (actionMask==MotionEvent.ACTION_UP) pointerID=-1;
			} //end else
			return true;
		} //end onTouch
	} //end GradientTouchListener
	
	public GradientSelectorView(Context context)
	{ 
		super(context); 
		initialize();
	}
	
	public GradientSelectorView(Context context,AttributeSet attributeSet)
	{
		super(context,attributeSet);
		initialize();
	}
	
	private void initialize()
	{ 
		//setBackgroundResource(R.drawable.gradient_view_background);
		int[] gradientColors=new int[] { 0xFFFF0000,0xFFFFFF00,0xFF00FF00,
				0xFF00FFFF,0xFF0000FF,0xFFFF00FF,0xFFFF0000 };
		gradient=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
				gradientColors);
		setBackgroundDrawable(gradient);
	}
	
	@Override protected void finalize() throws Throwable
	{
		setDrawingCacheEnabled(false);
		super.finalize();
	}
	
	public OnColorSelectListener getOnColorSelectListener()
	{ return colorSelectListener; }
	
	public void setOnColorSelectListener(OnColorSelectListener listener)
	{ 
		this.colorSelectListener=listener;
		if (listener!=null) setOnTouchListener(new GradientTouchListener());
		else setOnTouchListener(null);
	}
	
	@Override protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		if (gradientBitmap!=null) gradientBitmap.recycle();
		setDrawingCacheEnabled(true);
		gradientBitmap=Bitmap.createBitmap(getDrawingCache());
	}
}
