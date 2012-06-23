package shira.android.paintdroid;

import android.app.Activity;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
//import android.view.ViewGroup;
import android.widget.*;

public class PaintDroidActivity extends Activity 
{
	private PaintBoardView paintBoardView;
	private View currentColorView;
	private Paint drawingPaint;
	private GradientDrawable colorBackground;
	
	private class ScrollingAction extends DifferencePaintAction
	{
		private int maxScrollX,maxScrollY;
		
		public ScrollingAction(int maxScrollX,int maxScrollY) 
		{ 
			if ((maxScrollX<0)||(maxScrollY<0))
				throw new IllegalArgumentException("The maximum value to " + 
						"scroll on an axis must be greater than or equal to 0!");
			this.maxScrollX=maxScrollX; this.maxScrollY=maxScrollY;
		}
		
		public void draw(Canvas canvas,Paint paint) { }
		public boolean usesLocalPoints() { return true; }
		public boolean isPermanentChange() { return false; }
		public RectF getLastAffectedArea() { return null; }
		
		@Override 
		public void actOnPoint(float pointX,float pointY,boolean isFinalPoint)
		{
			if ((lastPointX>-1)&&(lastPointY>-1))
			{
				//Log.i("Last",lastPointX + "," + lastPointY);
				int differenceX=(int)(pointX-lastPointX);
				int differenceY=(int)(pointY-lastPointY);
				Log.i("PaintDroid","Difference: " + differenceX + "," + 
						differenceY);
				//paintBoardView.scrollBy(deltaX,deltaY);
				int scrollX=paintBoardView.getScrollX()+differenceX;
				int scrollY=paintBoardView.getScrollY()+differenceY;
				Log.i("PaintDroid","Scroll: " + scrollX + "," + scrollY);
				if (scrollX<0) scrollX=0; if (scrollY<0) scrollY=0;
				Log.i("PaintDroid","Max: " + maxScrollX + "," + maxScrollY);
				if (scrollX>maxScrollX) scrollX=maxScrollX;
				if (scrollY>maxScrollY) scrollY=maxScrollY;
				Log.i("PaintDroid","Scroll: " + scrollX + "," + scrollY);
				paintBoardView.scrollTo(scrollX,scrollY);
			}
			super.actOnPoint(pointX,pointY,isFinalPoint);
		}
	}
	
	public PaintDroidActivity() { }
	
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.paint_main);
		//Paint board view
		int initialPaintColor=Color.BLACK,backgroundColor=Color.WHITE;
		drawingPaint=new Paint();
		drawingPaint.setColor(initialPaintColor);
		paintBoardView=(PaintBoardView)findViewById(R.id.paint_board_view);
		paintBoardView.setPaint(drawingPaint);
		paintBoardView.setBackgroundColor(backgroundColor);
		//paintBoardView.setDrawingCacheEnabled(true);
		
		//Paint actions grid
		final PaintAction[] paintActions=new PaintAction[10];
		DummyPaintAction dummyAction=DummyPaintAction.getInstance();
		for (int counter=0;counter<paintActions.length;counter++)
			paintActions[counter]=dummyAction;
		
		paintActions[2]=new FreeFormPaintAction();
		paintActions[3]=new EraserPaintAction(backgroundColor);
		paintActions[7]=new PaintRectangleAction();
		paintActions[8]=new PaintEllipseAction();
		
		int hScrollRange=paintBoardView.computeHorizontalScrollRange();
		int vScrollRange=paintBoardView.computeVerticalScrollRange();
		paintActions[0]=new ScrollingAction(hScrollRange,vScrollRange);
		
		BitmapInfo boardBitmapInfo=paintBoardView.getBoardBitmapInfo();
		PickColorAction pickColorAction=new PickColorAction(boardBitmapInfo);
		pickColorAction.setOnColorPickListener(new PickColorAction.OnColorPickListener() 
		{ public void onColorPick(int color) { changeCurrentColor(color); } });
		paintActions[4]=pickColorAction;
		
		GridView paintActionsGrid=(GridView)findViewById(R.id.paint_actions_grid);
		PaintActionsAdapter actionsAdapter=new PaintActionsAdapter(this,
				new OnListItemClickListener()
		{
			public void onListItemClick(View view,int position)
			{ paintBoardView.setPaintAction(paintActions[position]); }
		});
		paintActionsGrid.setAdapter(actionsAdapter);
		actionsAdapter.setSelectedListItem(0);
		paintBoardView.setPaintAction(paintActions[0]);
		
		//Paint colors views
		currentColorView=findViewById(R.id.current_color_view);
		colorBackground=(GradientDrawable)currentColorView.getBackground();
		colorBackground.setColor(initialPaintColor);
		GradientSelectorView colorSelectorView=(GradientSelectorView)
				findViewById(R.id.color_selector_view);
		//currentColorView.setOnClickListener(new View.OnClickListener()
		colorSelectorView.setOnColorSelectListener(new GradientSelectorView.
				OnColorSelectListener()
		{	
			//@Override public void onClick(View view)
			@Override public void onColorSelect(int color)
			{ changeCurrentColor(color); }
		});
	}
	
	private void changeCurrentColor(int color)
	{
		colorBackground.setColor(color);
		currentColorView.invalidate();
		drawingPaint.setColor(color);
	}
}
