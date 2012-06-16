package shira.android.paintdroid;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
//import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class PaintBoardView extends View 
{
	private float DENSITY_FACTOR;
	private int SCROLLBAR_SIZE;
	
	private Bitmap boardBitmap;
	private Rect prevAffectedArea;
	private PaintAction paintAction;
	private int prevWidth=0,prevHeight=0;
	private int pointerID=-1;
	
	public PaintBoardView(Context context) 
	{ 
		super(context);
		initialize(context);
	}
	
	public PaintBoardView(Context context,AttributeSet attributeSet)
	{
		super(context,attributeSet);
		initialize(context);
	}
	
	private void initialize(Context context)
	{
		setBackgroundResource(android.R.color.white);
		setScrollBarStyle(SCROLLBARS_OUTSIDE_INSET);
		setHorizontalScrollBarEnabled(true);
		setVerticalScrollBarEnabled(true);
		TypedArray styledAttributes=context.obtainStyledAttributes(
				R.styleable.View);
		initializeScrollbars(styledAttributes);
		styledAttributes.recycle();
		DENSITY_FACTOR=context.getResources().getDisplayMetrics().density;
		SCROLLBAR_SIZE=(int)(Math.ceil(5*DENSITY_FACTOR));
		Log.i("PaintDroid","Scroll bar: " + SCROLLBAR_SIZE);
		//setDrawingCacheEnabled(true);
	}
	
	@Override protected void finalize() throws Throwable
	{
		setDrawingCacheEnabled(false);
		super.finalize();
	}
	
	public PaintAction getPaintAction() { return paintAction; }
	
	public void setPaintAction(PaintAction paintAction)
	{ this.paintAction=paintAction; }
	
	@Override public int computeHorizontalScrollRange() 
	{ return (int)(500*DENSITY_FACTOR); }
	
	@Override public int computeVerticalScrollRange() 
	{ return (int)(800*DENSITY_FACTOR); }
	
	@Override public boolean onTouchEvent(MotionEvent event)
	{
		if (paintAction!=null)
		{
			int actionMask=event.getAction() & MotionEvent.ACTION_MASK;
			if (actionMask!=MotionEvent.ACTION_CANCEL)
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
					if (paintAction.actsOnIntermediatePoints())
					{
						int historySize=event.getHistorySize();
						for (int historyPoint=0;historyPoint<historySize;
								historyPoint++)
						{
							float positionX=event.getHistoricalX(pointerIndex,
									historyPoint);
							float positionY=event.getHistoricalY(pointerIndex,
									historyPoint);
							PointF adaptedPosition=adaptCoordinates(positionX,
									positionY);
							positionX=adaptedPosition.x; 
							positionY=adaptedPosition.y;
							paintAction.actOnPoint(positionX,positionY,false);
						}
					}
					float positionX=event.getX(pointerIndex);
					float positionY=event.getY(pointerIndex);
					PointF adaptedPosition=adaptCoordinates(positionX,positionY);
					positionX=adaptedPosition.x; positionY=adaptedPosition.y;
					paintAction.actOnPoint(positionX,positionY,(actionMask==
							MotionEvent.ACTION_UP));
				} //end if pointerIndex>-1
				if (actionMask==MotionEvent.ACTION_UP)
				{
					if (pointerIndex==-1) paintAction.finishWithLastPoint();
					pointerID=-1;
				}
			} //end if (actionMask...
			else
			{
				if (paintAction.supportsCancel()) paintAction.cancelAction();
				else paintAction.finishWithLastPoint();
			}
			Rect affectedArea=null;
			RectF affectedAreaF=paintAction.getLastAffectedArea();
			if (affectedAreaF!=null)
			{
				affectedArea=convertRectFToInt(affectedAreaF);
				if (!paintAction.usesLocalPoints()) 
					convertLocalGlobalRect(affectedArea,false);
				if (prevAffectedArea!=null) affectedArea.union(prevAffectedArea);
			}
			else if (prevAffectedArea!=null) 
				affectedArea=new Rect(prevAffectedArea);
			if (affectedArea!=null)
			{
				setDrawingCacheEnabled(false);
				invalidate();
			}
			return true;
		} //end if paintAction!=null
		else return false;
	}
	
	@Override protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		int width=getWidth(),height=getHeight();
		if ((width!=prevWidth)||(height!=prevHeight))
		{
			if (boardBitmap!=null)
			{
				boardBitmap.recycle();
				boardBitmap=null;
			}
			prevWidth=width; prevHeight=height;
			width=(int)(width*DENSITY_FACTOR);
			height=(int)(height*DENSITY_FACTOR);
			Log.i("PaintDroid","Dimensions: " + width + "," + height);
			int boardWidth=computeHorizontalScrollRange()+width-SCROLLBAR_SIZE;
			int boardHeight=computeVerticalScrollRange()+height-SCROLLBAR_SIZE;
			Log.i("PaintDroid","Bitmap: " + boardWidth + "," + boardHeight);
			boardBitmap=Bitmap.createBitmap(boardWidth,boardHeight,Bitmap.
					Config.ARGB_8888);
		}
		/*if (prevAffectedArea!=null) //canvas.restore();
			canvas.drawBitmap(prevStateBitmap,null,prevAffectedArea,null);*/
		/*if (prevStateBitmap!=null)
		{*/
		Rect boardDrawingRect=new Rect();
		getDrawingRect(boardDrawingRect);
		boardDrawingRect.right-=SCROLLBAR_SIZE; 
		boardDrawingRect.bottom-=SCROLLBAR_SIZE;
		Log.i("PaintDroid","Drawing: " + boardDrawingRect.left + "," + 
				boardDrawingRect.top);
		/*Bitmap partialBitmap=Bitmap.createBitmap(boardDrawingRect.width(),
				boardDrawingRect.height(),Bitmap.Config.ARGB_8888);
		partialBitmap.setDensity(getContext().getResources().getDisplayMetrics().
				densityDpi);*/
		//canvas.setBitmap(partialBitmap);
		/*canvas.save();
		canvas.translate(getScrollX(),getScrollY());*/
		canvas.drawBitmap(boardBitmap,boardDrawingRect,boardDrawingRect,null);
		//canvas.restore();
		//}
		if (paintAction!=null)
		{
			//setDrawingCacheEnabled(true);
			paintAction.draw(canvas);
			if (paintAction.isPermanentChange())
			{
				setDrawingCacheEnabled(true);
				Bitmap partialBitmap=getDrawingCache();
				//.copy(Bitmap.Config.ARGB_8888,false);
				int numColored=0;
				/*for (int index1=0;index1<partialBitmap.getWidth();index1++)
				{
					for (int index2=0;index2<partialBitmap.getHeight();index2++)
					{
						if (partialBitmap.getPixel(index1,index2)==0xFF000000) 
							numColored++;
					}
				}
				Log.i("PaintDroid","Bitmap pixels: " + numColored);*/
				int areaWidth=partialBitmap.getWidth()-SCROLLBAR_SIZE;
				int areaHeight=partialBitmap.getHeight()-SCROLLBAR_SIZE;
				int[] pixels=new int[areaWidth*areaHeight];
				partialBitmap.getPixels(pixels,0,areaWidth,0,0,areaWidth,
						areaHeight);	
				numColored=0;
				for (int index=0;index<pixels.length;index++)
					if (pixels[index]==0xFF000000) numColored++;
				Log.i("PaintDroid","Pixels: " + numColored);
				/*Log.i("PaintDroid","Bitmap: " + boardBitmap.getWidth() + "," + 
						boardBitmap.getHeight());*/
				boardBitmap.setPixels(pixels,0,areaWidth,boardDrawingRect.
						left,boardDrawingRect.top,areaWidth,areaHeight);
				prevAffectedArea=null;
				//partialBitmap.recycle();
				//setDrawingCacheEnabled(false);
			}
			//setDrawingCacheEnabled(false);
			//else //canvas.saveLayer(null,null,Canvas.ALL_SAVE_FLAG);
		} //end if paintAction!=null
	}
	
	private PointF adaptCoordinates(float coordinateX,float coordinateY)
	{
		if (coordinateX<0) coordinateX=0; if (coordinateY<0) coordinateY=0;
		int maxWidth=getWidth()-SCROLLBAR_SIZE;
		int maxHeight=getHeight()-SCROLLBAR_SIZE;
		if (coordinateX>maxWidth) coordinateX=maxWidth;
		if (coordinateY>maxHeight) coordinateY=maxHeight;
		if (!paintAction.usesLocalPoints())
		{ coordinateX+=getScrollX(); coordinateY+=getScrollY(); }
		return new PointF(coordinateX,coordinateY);
	}
	
	private static Rect convertRectFToInt(RectF rectangleF)
	{
		return new Rect((int)rectangleF.left,(int)rectangleF.top,(int)
				rectangleF.right,(int)rectangleF.bottom);
	}
	
	private Rect convertLocalGlobalRect(Rect rectangle,boolean toGlobal)
	{
		int factor=(toGlobal?1:-1);
		int scrollX=getScrollX(),scrollY=getScrollY(); 
		rectangle.left+=scrollX*factor; rectangle.right+=scrollX*factor;
		rectangle.top+=scrollY*factor; rectangle.bottom+=scrollY*factor;
		return rectangle;
	}
}
