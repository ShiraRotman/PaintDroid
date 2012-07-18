package shira.android.paintdroid;

import android.content.Context;
import android.graphics.*;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
//import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.EditText;

public class PaintBoardView extends View 
{
	private float DENSITY_FACTOR;
	private int SCROLLBAR_SIZE;
	
	private Bitmap boardBitmap;
	private Rect prevAffectedArea;
	private PaintAction paintAction,tempAction;
	private Paint paint=new Paint();
	private BoardBitmapInfo boardBitmapInfo=null;
	private BoardTextInputController textInputController;
	private EditText softKeysReceiver;
	private float lastPointX=-1,lastPointY=-1;
	private int prevWidth=0,prevHeight=0;
	private int pointerID=-1,backgroundColor;
	private boolean isInTextInputMode=false;
	
	private class BoardBitmapInfo implements BitmapInfo
	{
		public int getWidth() 
		{ return (boardBitmap!=null?boardBitmap.getWidth():0); }
		
		public int getHeight() 
		{ return (boardBitmap!=null?boardBitmap.getHeight():0); }
		
		public int getColor(int pointX,int pointY) 
		{ 
			if (boardBitmap!=null)
			{
				int color=boardBitmap.getPixel(pointX,pointY);
				if (color==0) color=backgroundColor;
				return color;
			}
			else return Color.WHITE;
		}
	}
	
	private class BoardTextInputController implements TextInputController
	{
		public boolean isInTextInputMode() { return isInTextInputMode; }
		
		public void setTextInputMode(boolean isInTextInputMode)
		{
			PaintBoardView.this.isInTextInputMode=isInTextInputMode;
			if (!isInTextInputMode)
			{
				InputMethodManager inputManager=(InputMethodManager)getContext().
						getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(getWindowToken(),0);
			}
		}
		
		public String getTextInput() 
		{ return softKeysReceiver.getText().toString(); }
		
		public void setTextInput(String textInput) 
		{ softKeysReceiver.setText(textInput); }
	}
	
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
		setBackgroundColor(Color.WHITE);
		DENSITY_FACTOR=context.getResources().getDisplayMetrics().density;
		SCROLLBAR_SIZE=ViewConfiguration.get(context).getScaledScrollBarSize();
		boardBitmapInfo=new BoardBitmapInfo();
		textInputController=new BoardTextInputController();
		softKeysReceiver=new EditText(context);
		//softKeysReceiver.setVisibility(View.INVISIBLE);
		softKeysReceiver.addTextChangedListener(new TextWatcher() 
		{
			public void beforeTextChanged(CharSequence s,int start,int count,
					int after) { }
			
			public void onTextChanged(CharSequence s,int start,int before,
					int count) { } 
			
			public void afterTextChanged(Editable s) 
			{ PaintBoardView.this.invalidate(); } //TODO: Optimize
		});
	}
	
	@Override protected void finalize() throws Throwable
	{
		setDrawingCacheEnabled(false);
		super.finalize();
	}
	
	public PaintAction getPaintAction() { return paintAction; }
	
	public void setPaintAction(PaintAction paintAction)
	{ 
		if ((this.paintAction!=null)&&(this.paintAction.needsToFinishAction()))
		{
			tempAction=paintAction;
			this.paintAction.finishAction();
			invalidateBoard();
		}
		else setNewPaintAction(paintAction);
	}
	
	private void setNewPaintAction(PaintAction paintAction)
	{
		this.paintAction=paintAction;
		paintAction.resetState();
		lastPointX=-1; lastPointY=-1;
		prevAffectedArea=null;
	}
	
	public Paint getPaint() { return paint; }
	
	public void setPaint(Paint paint)
	{
		if (paint==null)
			throw new IllegalArgumentException("The paint to use for drawing " +
					"must be non-null!");
		this.paint=paint;
	}
	
	public BitmapInfo getBoardBitmapInfo() { return boardBitmapInfo; }
	public TextInputController getTextInputController() 
	{ return textInputController; }
	
	@Override public void setBackgroundColor(int color)
	{
		super.setBackgroundColor(color);
		backgroundColor=color;
	}
	
	public void clearBoard()
	{
		if (boardBitmap!=null)
		{
			boardBitmap.eraseColor(backgroundColor);
			if (paintAction!=null) paintAction.resetState();
			invalidate();
		}
	}
	
	@Override public int computeHorizontalScrollRange() 
	{ return (int)(500*DENSITY_FACTOR); }
	
	@Override public int computeVerticalScrollRange() 
	{ return (int)(800*DENSITY_FACTOR); }
	
	@Override public boolean onCheckIsTextEditor() { return isInTextInputMode; }
	
	@Override public InputConnection onCreateInputConnection(EditorInfo outAttrs)
	{
		if (isInTextInputMode)
		{
			outAttrs.inputType=InputType.TYPE_CLASS_TEXT;
			return new BaseInputConnection(this,true)
			{	
				@Override public Editable getEditable() 
				{ return softKeysReceiver.getText(); }
			};
		}
		else return null;
	}
	
	/*@Override public boolean onKeyPreIme(int keyCode,KeyEvent event)
	{
		char pressedKey=event.getDisplayLabel();
		if (pressedKey!=0)
		{
			inputText.append(pressedKey);
			invalidate();
			return true;
		}
		else return false;
	}*/
	
	@Override public boolean onTouchEvent(MotionEvent event)
	{
		InputMethodManager inputManager=null;
		if (isInTextInputMode)
		{
			requestFocus();
			inputManager=(InputMethodManager)getContext().getSystemService(
					Context.INPUT_METHOD_SERVICE);
		}
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
					//inputManager.toggleSoftInputFromWindow(getWindowToken(),0,0);
					//inputManager.showSoftInput(this,0,new ShowInputResultReceiver());
					if (isInTextInputMode)
						inputManager.hideSoftInputFromWindow(getWindowToken(),0);
				}
				else pointerIndex=event.findPointerIndex(pointerID);
				if (pointerIndex>-1)
				{
					if (paintAction.getPointsSensitivityLevel()!=
							PointSensitivityLevel.CURRENT)
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
							handleContinualPoints(positionX,positionY);	
							/*Log.i("PaintDroid","History: " + positionX + "," + 
									positionY);*/
							paintAction.actOnPoint(positionX,positionY,false);
						}
					}
					float positionX=event.getX(pointerIndex);
					float positionY=event.getY(pointerIndex);
					PointF adaptedPosition=adaptCoordinates(positionX,positionY);
					positionX=adaptedPosition.x; positionY=adaptedPosition.y;
					handleContinualPoints(positionX,positionY);
					boolean isFinalPoint=(actionMask==MotionEvent.ACTION_UP);
					paintAction.actOnPoint(positionX,positionY,isFinalPoint);
					if (isFinalPoint) { lastPointX=-1; lastPointY=-1; }
				} //end if pointerIndex>-1
				if (actionMask==MotionEvent.ACTION_UP)
				{
					if (pointerIndex==-1) paintAction.finishWithLastPoint();
					pointerID=-1;
					if (isInTextInputMode) inputManager.showSoftInput(this,0);
				}
			} //end if (actionMask...
			else
			{
				if (paintAction.supportsCancel()) paintAction.cancelAction();
				else paintAction.finishWithLastPoint();
			}
			invalidateBoard();
			return true;
		} //end if paintAction!=null
		else return false;
	}
	
	private void invalidateBoard()
	{
		Rect affectedArea=null;
		RectF affectedAreaF=paintAction.getLastAffectedArea();
		if (affectedAreaF!=null)
		{
			affectedArea=new Rect();
			affectedAreaF.round(affectedArea);
			if (paintAction.usesLocalPoints()) 
				convertLocalGlobalRect(affectedArea,true);
			if (prevAffectedArea!=null) affectedArea.union(prevAffectedArea);
		}
		else if (prevAffectedArea!=null) 
			affectedArea=new Rect(prevAffectedArea);
		if (affectedArea!=null)
		{
			setDrawingCacheEnabled(false);
			if ((affectedArea.width()==0)&&(affectedArea.height()==0))
				invalidate();
			else invalidate(affectedArea);
		}
		prevAffectedArea=affectedArea;
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
		Rect boardDrawingRect=new Rect();
		getDrawingRect(boardDrawingRect);
		boardDrawingRect.right-=SCROLLBAR_SIZE; 
		boardDrawingRect.bottom-=SCROLLBAR_SIZE;
		canvas.drawBitmap(boardBitmap,boardDrawingRect,boardDrawingRect,null);
		/*paint.setTextSize(16);
		canvas.drawText(inputText.toString(),boardDrawingRect.right-20,20,paint);*/
		if (paintAction!=null)
		{
			//setDrawingCacheEnabled(true);
			paintAction.draw(canvas,paint);
			if (paintAction.isPermanentChange())
			{
				setDrawingCacheEnabled(true);
				Bitmap partialBitmap=getDrawingCache();
				//.copy(Bitmap.Config.ARGB_8888,false);
				RectF affectedAreaF=paintAction.getLastAffectedArea();
				if (affectedAreaF!=null)
				{
					Rect localAffectedArea=new Rect();
					affectedAreaF.round(localAffectedArea);
					Rect globalAffectedArea=new Rect(localAffectedArea);
					if ((localAffectedArea.width()==0)&&(localAffectedArea.
							height()==0))
					{
						localAffectedArea.left=0; localAffectedArea.top=0;
						localAffectedArea.right=partialBitmap.getWidth();
						localAffectedArea.bottom=partialBitmap.getHeight();
						convertLocalGlobalRect(globalAffectedArea,true);
					}
					else
					{
						if (paintAction.usesLocalPoints())
							convertLocalGlobalRect(globalAffectedArea,true);
						else convertLocalGlobalRect(localAffectedArea,false);
					}
					//Log.i("PaintDroid","Local: " + localAffectedArea);
					int areaWidth=localAffectedArea.width();
					int areaHeight=localAffectedArea.height();
					/*int areaWidth=partialBitmap.getWidth()-SCROLLBAR_SIZE;
					int areaHeight=partialBitmap.getHeight()-SCROLLBAR_SIZE;*/
					int[] pixels=new int[areaWidth*areaHeight];
					partialBitmap.getPixels(pixels,0,areaWidth,localAffectedArea.
							left,localAffectedArea.top,areaWidth,areaHeight);
					/*partialBitmap.getPixels(pixels,0,areaWidth,0,0,areaWidth,
							areaHeight);*/
					/*int numColored=0;
					for (int index=0;index<pixels.length;index++)
						if (pixels[index]!=Color.WHITE) numColored++;
					Log.i("PaintDroid","Pixels: " + numColored);*/
					boardBitmap.setPixels(pixels,0,areaWidth,globalAffectedArea.
							left,globalAffectedArea.top,areaWidth,areaHeight);
				} //end if (affectedAreaF!=null)
				prevAffectedArea=null;
				//partialBitmap.recycle();
				//setDrawingCacheEnabled(false);
			}
			//setDrawingCacheEnabled(false);
			if (tempAction!=null)
			{
				setNewPaintAction(tempAction);
				tempAction=null;
			}
		} //end if paintAction!=null
	}
	
	private void handleContinualPoints(float pointX,float pointY)
	{
		if (paintAction.getPointsSensitivityLevel()==PointSensitivityLevel.
				INTERMEDIATE)
		{
			if (lastPointX>-1)
			{
				float[] continualPoints=PaintUtils.calcIntermediatePoints(
						lastPointX,lastPointY,pointX,pointY);
				for (int index=0;index<continualPoints.length;index+=2)
				{
					paintAction.actOnPoint(continualPoints[index],
							continualPoints[index+1],false);
				}
			}
			lastPointX=pointX; lastPointY=pointY;
		}
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
	
	private Rect convertLocalGlobalRect(Rect rectangle,boolean toGlobal)
	{
		int factor=(toGlobal?1:-1);
		int scrollX=getScrollX(),scrollY=getScrollY(); 
		rectangle.left+=scrollX*factor; rectangle.right+=scrollX*factor;
		rectangle.top+=scrollY*factor; rectangle.bottom+=scrollY*factor;
		return rectangle;
	}
}
