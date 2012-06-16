package shira.android.paintdroid;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

class PaintActionsAdapter extends BaseAdapter
{
	private static final int PADDING=1;
	
	private Context context;
	private View[] listItemsViews;
	private OnListItemClickListener itemClickListener;
	private int[] actionsImagesResources;
	private int lastSelectedPosition=-1;
	private int pendingSelectedPosition=-1;
	private final int padding;
	
	private class ActionButtonClickListener implements View.OnClickListener
	{
		private int position;
		
		public ActionButtonClickListener(int position)
		{ this.position=position; }
		
		public void onClick(View view)
		{
			changeSelectedListItem(position);
			if (itemClickListener!=null)
				itemClickListener.onListItemClick(view,position);
		}
	}
	
	public PaintActionsAdapter(Context context,OnListItemClickListener 
			itemClickListener)
	{
		if (context==null)
			throw new IllegalArgumentException("The context must be non-null!");
		this.context=context;
		this.itemClickListener=itemClickListener;
		TypedArray imagesResourcesNames=context.getResources().obtainTypedArray(
				R.array.actions_images);
		actionsImagesResources=new int[imagesResourcesNames.length()];
		for (int counter=0;counter<actionsImagesResources.length;counter++)
		{
			actionsImagesResources[counter]=imagesResourcesNames.getResourceId(
					counter,-1);
		}
		imagesResourcesNames.recycle();
		listItemsViews=new View[actionsImagesResources.length];
		padding=(int)(PADDING*context.getResources().getDisplayMetrics().density);
	}
	
	public int getCount() { return actionsImagesResources.length; }
	public long getItemId(int position)
	{ return actionsImagesResources[position]; }
	public Object getItem(int position) 
	{ return actionsImagesResources[position]; }
	
	public View getView(int position,View convertView,ViewGroup parent)
	{
		ImageView actionImageView;
		if (convertView==null)
		{
			actionImageView=new ImageView(context);
			/*actionToggleButton=new ToggleButton(context);
			actionToggleButton.setTextOn(null);
			actionToggleButton.setTextOff(null);
			actionToggleButton.setText(null);*/
			AbsListView.LayoutParams layoutParams=new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.
					WRAP_CONTENT);
			actionImageView.setLayoutParams(layoutParams);
			actionImageView.setPadding(padding,0,padding,0);
		}
		else actionImageView=(ImageView)convertView;
		actionImageView.setImageResource(actionsImagesResources[position]);
		actionImageView.setOnClickListener(new ActionButtonClickListener(position));
		listItemsViews[position]=actionImageView;
		if (pendingSelectedPosition==position)
		{
			changeSelectedListItem(position);
			pendingSelectedPosition=-1;
		}
		return actionImageView;
	}
	
	public void setSelectedListItem(int position)
	{
		if ((position<0)||(position>actionsImagesResources.length))
			throw new IndexOutOfBoundsException("The position of the list " +
					"item to select must be between 0 and " + actionsImagesResources.
					length + "! The value supplied was " + position);
		if (listItemsViews[position]==null) pendingSelectedPosition=position;
		else changeSelectedListItem(position);
	}
		
	private void changeSelectedListItem(int position)
	{
		listItemsViews[position].setBackgroundResource(R.drawable.
				actions_list_selector);
		if (lastSelectedPosition>-1) 
		{
			listItemsViews[lastSelectedPosition].setBackgroundResource(0);
			listItemsViews[lastSelectedPosition].invalidate();
		}
		lastSelectedPosition=position;
	}
}
