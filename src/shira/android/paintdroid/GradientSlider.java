package shira.android.paintdroid;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class GradientSlider extends View 
{
	private static final float MARKER_BASE_EDGE_PERCENT=0.1f;
	private static final float MARKER_HEIGHT_PERCENT=0.3f;
	
	private OnValueChangeListener listener;
	private GradientDrawable gradientDrawable;
	private int[] gradientColors;
	private float value,minimum,maximum,step;
	private float markerWidth,markerHeight;
	private int pointerID=0;
	
	public static interface OnValueChangeListener
	{
		public abstract void onValueChange(GradientSlider gradientSlider,
				float value);
	}
	
	public GradientSlider(Context context) 
	{ this(context,1,1,10,1,0xFF000000,0xFFFFFFFF); }
	
	public GradientSlider(Context context,float value,float minimum,float 
			maximum,float step,int minColor,int maxColor)
	{ this(context,value,minimum,maximum,step,new int[] {minColor,maxColor}); }
	
	public GradientSlider(Context context,float value,float minimum,float 
			maximum,float step,int[] colors)
	{
		super(context);
		if (minimum>maximum) minimum=maximum;
		this.minimum=minimum; this.maximum=maximum;
		setValue(value); setStep(step);
		setColors(colors);
		setBackgroundColor(Color.TRANSPARENT);
	}
	
	public GradientSlider(Context context,AttributeSet attributes)
	{
		super(context,attributes);
		TypedArray attributesArray=context.obtainStyledAttributes(
				attributes,R.styleable.GradientSlider);
		minimum=attributesArray.getFloat(R.styleable.GradientSlider_minimum,1);
		maximum=attributesArray.getFloat(R.styleable.GradientSlider_maximum,10);
		float value=attributesArray.getFloat(R.styleable.GradientSlider_value,1);
		float step=attributesArray.getFloat(R.styleable.GradientSlider_step,1);
		int minColor=attributesArray.getColor(R.styleable.GradientSlider_minColor,
				0xFF000000);
		int maxColor=attributesArray.getColor(R.styleable.GradientSlider_maxColor,
				0xFFFFFFFF);
		if (minimum>maximum) minimum=maximum;
		setValue(value); setStep(step);
		setColors(new int[] {minColor,maxColor});
		setBackgroundColor(Color.TRANSPARENT);
	}
	
	public float getValue() { return value; }

	public void setValue(float value) 
	{
		if (value<minimum) value=minimum;
		else if (value>maximum) value=maximum;
		this.value=value;
		fixValue();
		invalidate();
	}

	public float getMinimum() { return minimum; }

	public void setMinimum(float minimum) 
	{
		if (minimum>maximum) minimum=maximum;
		this.minimum=minimum;
		invalidate();
	}

	public float getMaximum() { return maximum; }

	public void setMaximum(float maximum) 
	{
		if (maximum<minimum) maximum=minimum;
		this.maximum=maximum;
		invalidate();
	}

	public float getStep() { return step; }

	public void setStep(float step) 
	{
		if ((step<=0)||(step>maximum-minimum)) step=maximum-minimum;
		this.step=step;
		fixValue();
		invalidate();
	}
	
	private void fixValue()
	{
		float range=maximum-minimum;
		int valuePercent=Math.round(value/range*100);
		int stepPercent=Math.round(step/range*100);
		if (stepPercent>0)
		{
			int residue=valuePercent%stepPercent;
			if (residue>0) value=range*(valuePercent-residue)/100+minimum;
		}
	}
	
	public int getMinColor() { return gradientColors[0]; }
	
	public void setMinColor(int minColor)
	{
		gradientColors[0]=minColor;
		invalidate();
	}
	
	public int getMaxColor() { return gradientColors[gradientColors.length]; }
	
	public void setMaxColor(int maxColor)
	{
		gradientColors[gradientColors.length-1]=maxColor;
		invalidate();
	}
	
	public int[] getColors() { return gradientColors.clone(); }
	
	public void setColors(int[] colors)
	{
		if (colors!=null) gradientColors=colors.clone();
		else gradientColors=new int[] {0xFF000000,0xFFFFFFFF};
		gradientDrawable=new GradientDrawable(GradientDrawable.Orientation.
				LEFT_RIGHT,gradientColors);
		invalidate();
	}
	
	public OnValueChangeListener getOnValueChangeListener() { return listener; }
	public void setOnValueChangeListener(OnValueChangeListener listener) 
	{ this.listener=listener; }
	
	@Override protected void onSizeChanged(int w,int h,int oldw,int oldh)
	{
		super.onSizeChanged(w,h,oldw,oldh);
		markerWidth=getWidth()*MARKER_BASE_EDGE_PERCENT;
		markerHeight=getHeight()*MARKER_HEIGHT_PERCENT;
	}
	
	@Override public boolean onTouchEvent(MotionEvent event)
	{
		int actionMask=event.getAction() & MotionEvent.ACTION_MASK;
		if (actionMask==MotionEvent.ACTION_CANCEL) pointerID=-1;
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
				float position=event.getX(pointerIndex);
				float gradientMargin=markerWidth/2;
				if (position<gradientMargin) position=gradientMargin;
				else if (position>getWidth()-gradientMargin) 
					position=getWidth()-gradientMargin;
				float percent=(position-gradientMargin)/(getWidth()-markerWidth);
				value=(maximum-minimum)*percent+minimum;
				fixValue();
				if (listener!=null) listener.onValueChange(this,value);
				invalidate();
			}
			if (actionMask==MotionEvent.ACTION_UP) pointerID=-1;
		} //end else
		return true;
	} //end onTouchEvent
	
	@Override protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		float halfMarkerWidth=markerWidth/2,height=getHeight();
		gradientDrawable.setBounds(Math.round(halfMarkerWidth),0,Math.round(
				getWidth()-halfMarkerWidth),Math.round(getHeight()-markerHeight/3));
		gradientDrawable.draw(canvas);
		Paint markerPaint=new Paint();
		markerPaint.setColor(Color.BLACK);
		//markerPaint.setStyle(Paint.Style.FILL);
		float valuePercent=value/(maximum-minimum);
		float markerTopPointX=(getWidth()-markerWidth)*valuePercent+halfMarkerWidth;
		float markerTopPointY=height-markerHeight;
		/*canvas.drawLine(markerTopPointX,markerTopPointY,markerTopPointX-
				halfMarkerWidth,height,markerPaint);
		canvas.drawLine(markerTopPointX,markerTopPointY,markerTopPointX+
				halfMarkerWidth,height,markerPaint);
		canvas.drawLine(markerTopPointX-halfMarkerWidth,height,markerTopPointX+
				halfMarkerWidth,height,markerPaint);*/
		float a=(height-markerTopPointY)/-halfMarkerWidth;
		float b1=markerTopPointY-a*markerTopPointX;
		float b2=markerTopPointY+a*markerTopPointX;
		for (float markerPointY=height;markerPointY>markerTopPointY;markerPointY--)
		{
			float startPointX=(markerPointY-b1)/a;
			float endPointX=(markerPointY-b2)/-a;
			canvas.drawLine(startPointX,markerPointY,endPointX,markerPointY,
					markerPaint);
		}
	}
}
