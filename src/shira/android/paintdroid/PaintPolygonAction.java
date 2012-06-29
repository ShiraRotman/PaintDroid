package shira.android.paintdroid;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

public class PaintPolygonAction extends DifferencePaintAction 
{
	private final float MAX_DIFFERENCE;
	
	private float firstPointX=-1,firstPointY=-1;
	private float edgePointX=-1,edgePointY=-1;
	private boolean finishedAction;
	
	public PaintPolygonAction(float densityFactor)
	{ 
		if (densityFactor<=0)
			throw new IllegalArgumentException("The screen's density factor " +
					"must be positive!");
		MAX_DIFFERENCE=5*densityFactor;
	}
	
	@Override 
	public void actOnPoint(float pointX,float pointY,boolean isFinalPoint)
	{
		if ((firstPointX==-1)||(finishedAction))
		{
			firstPointX=pointX; firstPointY=pointY;
			edgePointX=pointX; edgePointY=pointY;
			finishedAction=false;
			Log.i("PaintDroid","First1: " + firstPointX + "," + firstPointY);
		}
		else if ((edgePointX==-1)||(this.isFinalPoint))
		{
			edgePointX=lastPointX;
			edgePointY=lastPointY;
		}
		if (isFinalPoint)
		{
			Log.i("PaintDroid","First2: " + firstPointX + "," + firstPointY);
			Log.i("PaintDroid","Point: " + pointX + "," + pointY);
			if ((Math.abs(pointX-firstPointX)<MAX_DIFFERENCE)&&
				(Math.abs(pointY-firstPointY)<MAX_DIFFERENCE))
				finishedAction=true;
		}
		updateAffectedArea();
		super.actOnPoint(pointX,pointY,isFinalPoint);
	}
	
	public boolean isPermanentChange() { return isFinalPoint; }
	public boolean usesLocalPoints() { return false; }
	public void finishWithLastPoint() { updateAffectedArea(); }
	
	@Override public void resetState()
	{
		super.resetState();
		firstPointX=-1; firstPointY=-1; edgePointX=-1; edgePointY=-1;
		finishedAction=false;
		Log.i("PaintDroid","First: " + firstPointX + "," + firstPointY);
	}
	
	@Override public boolean needsToFinishAction() { return true; }
	
	@Override public void finishAction()
	{
		if (!finishedAction)
		{
			edgePointX=lastPointX; edgePointY=lastPointY;
			lastPointX=firstPointX; lastPointY=firstPointY;
			isFinalPoint=true;
			updateAffectedArea();
			finishedAction=true;
		}
	}
	
	private void updateAffectedArea()
	{
		lastAffectedArea=new RectF(edgePointX,edgePointY,lastPointX,lastPointY);
		lastAffectedArea.sort();
	}
	
	public void draw(Canvas canvas,Paint paint)
	{
		if (edgePointX>-1)
			canvas.drawLine(edgePointX,edgePointY,lastPointX,lastPointY,paint);
	}
}
