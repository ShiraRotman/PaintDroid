package shira.android.paintdroid;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
//import android.view.ViewGroup;
import android.widget.*;

public class PaintDroidActivity extends Activity 
{
	private PaintBoardView paintBoardView;
	
	private class ScrollingAction extends AbstractPaintAction
	{
		private int maxScrollX,maxScrollY;
		
		public ScrollingAction(int maxScrollX,int maxScrollY) 
		{ 
			if ((maxScrollX<0)||(maxScrollY<0))
				throw new IllegalArgumentException("The maximum value to " + 
						"scroll on an axis must be greater than or equal to 0!");
			this.maxScrollX=maxScrollX; this.maxScrollY=maxScrollY;
		}
		
		public void draw(Canvas canvas) { }
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
		/*paintBoardView=new PaintBoardView(this);
		LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(
				0,ViewGroup.LayoutParams.FILL_PARENT,(float)0.8);
		paintBoardView.setLayoutParams(layoutParams);
		ViewGroup boardToolsContainer=(ViewGroup)findViewById(R.id.board_tools_container);
		boardToolsContainer.addView(paintBoardView);*/
		paintBoardView=(PaintBoardView)findViewById(R.id.paint_board_view);
		//paintBoardView.setDrawingCacheEnabled(true);
		
		//Paint actions grid
		final PaintAction[] paintActions=new PaintAction[8];
		paintActions[0]=new ScrollingAction(paintBoardView.computeHorizontalScrollRange(),
				paintBoardView.computeVerticalScrollRange());
		DummyPaintAction dummyAction=DummyPaintAction.getInstance();
		for (int counter=1;counter<paintActions.length;counter++)
			paintActions[counter]=dummyAction;
		paintActions[5]=new PaintRectangleAction();
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
		final View currentColorView=findViewById(R.id.current_color_view);
		final GradientDrawable colorBackground=(GradientDrawable)currentColorView.
				getBackground();
		GradientSelectorView colorSelectorView=(GradientSelectorView)
				findViewById(R.id.color_selector_view);
		//currentColorView.setOnClickListener(new View.OnClickListener()
		colorSelectorView.setOnColorSelectListener(new GradientSelectorView.
				OnColorSelectListener()
		{	
			//@Override public void onClick(View view)
			@Override public void onColorSelect(int color)
			{ 
				colorBackground.setColor(color);
				currentColorView.invalidate();
			}
		});
	}
}
