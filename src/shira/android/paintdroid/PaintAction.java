package shira.android.paintdroid;

//import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

//enum PointType { CONTINUAL,INTERMEDIATE,FINAL };

interface PaintAction 
{
	public abstract void actOnPoint(float pointX,float pointY,boolean isFinalPoint);
	public abstract boolean actsOnIntermediatePoints();
	public abstract boolean requiresContinualPoints();
	public abstract boolean usesLocalPoints();
	public abstract boolean isPermanentChange();
	public abstract RectF getLastAffectedArea();
	public abstract boolean supportsCancel();
	public abstract void cancelAction();
	public abstract void finishWithLastPoint();
	public abstract void draw(Canvas canvas,Paint paint);
}

abstract class AbstractPaintAction implements PaintAction
{
	protected RectF lastAffectedArea;
	
	protected AbstractPaintAction() { }
	
	public RectF getLastAffectedArea() 
	{ 
		if (lastAffectedArea!=null) return lastAffectedArea;
		else return new RectF(0,0,0,0);
	}
	
	public boolean actsOnIntermediatePoints() { return false; }
	public boolean requiresContinualPoints() { return false; }
	public boolean supportsCancel() { return false; }
	
	public void cancelAction()
	{ 
		throw new UnsupportedOperationException("Cancel not supported for " + 
				"this action!");
	}
}

abstract class DifferencePaintAction extends AbstractPaintAction
{
	protected float lastPointX=-1,lastPointY=-1;
	
	public void actOnPoint(float pointX,float pointY,boolean isFinalPoint)
	{
		if (isFinalPoint) { lastPointX=-1; lastPointY=-1; }
		else { lastPointX=pointX; lastPointY=pointY; }
	}
	
	public void finishWithLastPoint() { lastPointX=-1; lastPointY=-1; }
}

class DummyPaintAction implements PaintAction
{
	private static DummyPaintAction instance=null;
	private DummyPaintAction() { }
	
	public static DummyPaintAction getInstance()
	{
		if (instance==null) instance=new DummyPaintAction();
		return instance;
	}
	
	public void actOnPoint(float pointX,float pointY,boolean isFinalPoint) { }
	public boolean actsOnIntermediatePoints() { return false; }
	public boolean requiresContinualPoints() { return false; }
	public boolean usesLocalPoints() { return false; }
	public boolean isPermanentChange() { return false; }
	public RectF getLastAffectedArea() { return null; }
	public boolean supportsCancel() { return false; }
	public void cancelAction() { }
	public void finishWithLastPoint() { }
	public void draw(Canvas canvas,Paint paint) { }
}