package shira.android.paintdroid;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

abstract class DraggingPaintAction extends DifferencePaintAction
{
	protected float startPointX=-1,startPointY=-1;
	
	protected DraggingPaintAction() { }
	
	@Override 
	public void actOnPoint(float pointX,float pointY,boolean isFinalPoint)
	{
		if (startPointX==-1)
		{
			startPointX=pointX;
			startPointY=pointY;
		}
		lastAffectedArea=new RectF(startPointX,startPointY,pointX,pointY);
		lastAffectedArea.sort();
		Log.i("PaintDroid",lastAffectedArea.toString());
		if (isFinalPoint) { startPointX=-1; startPointY=-1; }
		super.actOnPoint(pointX,pointY,isFinalPoint);
	}
	
	@Override public void finishWithLastPoint() 
	{ 
		lastAffectedArea=new RectF(startPointX,startPointY,lastPointX,lastPointY);
		lastAffectedArea.sort();
		startPointX=-1; startPointY=-1;
		super.finishWithLastPoint();
	}
	
	public boolean isPermanentChange() { return (startPointX==-1); }
	public boolean usesLocalPoints() { return false; }
}

class PaintRectangleAction extends DraggingPaintAction
{
	public void draw(Canvas canvas,Paint paint) 
	{ if (lastAffectedArea!=null) canvas.drawRect(lastAffectedArea,paint); }
}

class PaintEllipseAction extends DraggingPaintAction
{
	public void draw(Canvas canvas,Paint paint) 
	{ if (lastAffectedArea!=null) canvas.drawOval(lastAffectedArea,paint); }
}
