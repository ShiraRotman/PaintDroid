package shira.android.paintdroid;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
//import android.util.Log;

import java.util.LinkedList;

abstract class ImmediatePaintAction extends AbstractPaintAction
{
	protected LinkedList<Float> affectedPoints=new LinkedList<Float>();
	protected boolean isFinalPoint=false;
	
	protected ImmediatePaintAction() { lastAffectedArea=new RectF(); }
	
	@Override 
	public void actOnPoint(float pointX,float pointY,boolean isFinalPoint)
	{
		//if (affectedPoints.size()==0)
		if (this.isFinalPoint)
		{
			affectedPoints.clear();
			lastAffectedArea=new RectF(pointX,pointY,pointX,pointY);
		}
		else
		{
			if (lastAffectedArea.left>pointX) lastAffectedArea.left=pointX;
			else if (lastAffectedArea.right<pointX) lastAffectedArea.right=pointX;
			if (lastAffectedArea.top>pointY) lastAffectedArea.top=pointY;
			else if (lastAffectedArea.bottom<pointY) lastAffectedArea.bottom=pointY;
			//Log.i("PaintDroid","Immediate: " + lastAffectedArea);
		}
		affectedPoints.add(pointX);
		affectedPoints.add(pointY);
		this.isFinalPoint=isFinalPoint;
	}
	
	@Override public void finishWithLastPoint() { isFinalPoint=true; } 
	public PointSensitivityLevel getPointsSensitivityLevel() 
	{ return PointSensitivityLevel.INTERMEDIATE; }
	public boolean isPermanentChange() { return isFinalPoint; }
	public boolean usesLocalPoints() { return false; }
	
	//public void draw(Canvas canvas,Paint paint) //{ affectedPoints.clear(); }
}

class FreeFormPaintAction extends ImmediatePaintAction
{
	public void draw(Canvas canvas,Paint paint)
	{
		float[] affectedPointsArray=new float[affectedPoints.size()];
		int index=0;
		for (Float affectedPointWrapper : affectedPoints)
			affectedPointsArray[index++]=affectedPointWrapper;
		float strokeWidth=paint.getStrokeWidth();
		paint.setStrokeWidth(5); //Temporary
		canvas.drawPoints(affectedPointsArray,paint);
		paint.setStrokeWidth(strokeWidth);
		//if (isFinalPoint) affectedPoints.clear();
		//super.draw(canvas,paint);
	}
}

class EraserPaintAction extends FreeFormPaintAction
{
	private int backgroundColor;
	
	public EraserPaintAction() { this(Color.WHITE); }
	
	public EraserPaintAction(int backgroundColor)
	{ setBackgroundColor(backgroundColor); }

	public int getBackgroundColor() { return backgroundColor; }

	public void setBackgroundColor(int backgroundColor) 
	{ this.backgroundColor=backgroundColor; }
	
	@Override public void draw(Canvas canvas,Paint paint)
	{
		int origBackgroundColor=paint.getColor();
		paint.setColor(backgroundColor);
		super.draw(canvas,paint);
		paint.setColor(origBackgroundColor);
	}
}
