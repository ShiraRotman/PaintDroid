package shira.android.paintdroid;

import android.graphics.*;
import android.text.*;
import android.util.Log;

interface TextInputController
{ 
	public abstract boolean isInTextInputMode();
	public abstract void setTextInputMode(boolean isInTextInputMode);
	public abstract String getTextInput();
	public abstract void setTextInput(String textInput);
}

class PaintTextAction extends AbstractPaintAction
{
	/*Since I can't measure the text prior to the drawing operation, because
	 *only then I get access to the parameters which affect the text's 
	 *dimensions, such as font and size (through the paint object), I have to  
	 *tell the mechanism that this action affects the whole drawing area in any 
	 *case, which of course hurts performance. In future versions I'll try to 
	 *find a solution to this issue.*/
	private static final RectF LAST_AFFECTED_AREA=new RectF(0,0,0,0);
	private static final String MEASURE_TEXT="Ag";
	
	private TextInputController inputController;
	private String lastTextInput="";
	private float startTextPointX=-1,startTextPointY=-1;
	private float lastTextPointX,lastTextPointY;
	private boolean isPermanentDrawing=false;
	private boolean positioningStarted=false,isLastDrawing=false;
	
	public PaintTextAction(TextInputController inputController)
	{
		if (inputController==null)
			throw new IllegalArgumentException("The interface object through " +
					"which to control the text input component must be non-null!");  
		this.inputController=inputController;
	}
	
	@Override
	public void actOnPoint(float pointX,float pointY,boolean isFinalPoint)
	{
		if ((!isPermanentDrawing)&&(!positioningStarted))
		{
			isPermanentDrawing=true;
			positioningStarted=true;
			lastTextPointX=startTextPointX;
			lastTextPointY=startTextPointY;
		}
		else
		{
			if (isPermanentDrawing)
			{
				isPermanentDrawing=false;
				inputController.setTextInput("");
			}
			if (isFinalPoint) positioningStarted=false;
		}
		startTextPointX=pointX;
		startTextPointY=pointY;
		super.actOnPoint(pointX,pointY,isFinalPoint);
	}
	
	public boolean usesLocalPoints() { return false; }
	public boolean isPermanentChange() { return isPermanentDrawing; }
	@Override public boolean needsToFinishAction() { return true; }
	public void finishWithLastPoint() { } 
	//{ startTextPointX=lastPointX; startTextPointY=lastPointY; }
	
	@Override public void resetState()
	{
		super.resetState();
		lastAffectedArea=LAST_AFFECTED_AREA;
		isPermanentDrawing=false; positioningStarted=false; isLastDrawing=false;
		startTextPointX=-1; startTextPointY=-1;
		inputController.setTextInputMode(true);
		inputController.setTextInput("");
	}
	
	@Override public void finishAction() 
	{
		super.finishAction();
		isPermanentDrawing=true; 
		isLastDrawing=true;
		lastTextPointX=startTextPointX;
		lastTextPointY=startTextPointY;
	}
	
	public void draw(Canvas canvas,Paint paint)
	{
		//Log.i("PaintDroid","Start: " + startTextPointX + "," + startTextPointY);
		if ((startTextPointX==-1)||(startTextPointY==-1)) return;
		paint.setTextSize(50); //Temporary
		String textInput=inputController.getTextInput();
		if ((textInput.equals(""))&&(!isPermanentDrawing))
		{
			/*Rect bounds=new Rect();
			paint.getTextBounds(MEASURE_TEXT,0,MEASURE_TEXT.length(),bounds);*/
			float measureTextWidth=paint.measureText(MEASURE_TEXT);
			TextPaint textPaint=new TextPaint(paint);
			StaticLayout staticLayout=new StaticLayout(MEASURE_TEXT,textPaint,
					(int)measureTextWidth,Layout.Alignment.ALIGN_NORMAL,
					1f,0f,false);
			int originalColor=paint.getColor();
			paint.setColor(Color.BLACK); //Temporary
			int measureTextHeight=staticLayout.getHeight();
			canvas.drawLine(startTextPointX,startTextPointY-measureTextHeight,
					startTextPointX,startTextPointY,paint);
			paint.setColor(originalColor);
			/*if (!inputController.isInTextInputMode()) 
				inputController.setTextInputMode(true);*/
		}
		else
		{
			String textForDrawing; float basePointX,basePointY;
			if (isPermanentDrawing)
			{
				textForDrawing=lastTextInput;
				basePointX=lastTextPointX; basePointY=lastTextPointY;
			}
			else
			{
				textForDrawing=textInput;
				basePointX=startTextPointX; basePointY=startTextPointY;
			}
			Log.i("PaintDroid","Text: " + textForDrawing);
			Log.i("PaintDroid","Permanent: " + isPermanentDrawing);
			canvas.drawText(textForDrawing,basePointX,basePointY,paint);
			if (isLastDrawing)
			{
				isLastDrawing=false;
				inputController.setTextInputMode(false);
			}
		}
		lastTextInput=textInput;
	}
}
