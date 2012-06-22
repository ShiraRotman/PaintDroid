package shira.android.paintdroid;

import java.util.LinkedList;

//import android.util.Log;

public final class PaintUtils 
{
	private PaintUtils() { }
	
	public static final float[] calcIntermediatePoints(float pointX1,float pointY1,
			float pointX2,float pointY2)
	{
		int comparisonResultX=Float.compare(pointX1,pointX2);
		int comparisonResultY=Float.compare(pointY1,pointY2);
		if ((comparisonResultX==0)&&(comparisonResultY==0))
			return new float[0];		
		LinkedList<Float> intermediatePoints=new LinkedList<Float>();
		if (comparisonResultX!=0)
		{
			LinearPointInterpolator interpolator=new LinearPointInterpolator(
					pointX1,pointY1,pointX2,pointY2); 
			float prevPointX=pointX1,prevPointY=pointY1;
			int differenceX=-comparisonResultX;
			boolean isLastPoint=false;
			//Log.i("PaintDroid","Last: " + pointX2);
			for (float interPointX=pointX1+differenceX;((Math.signum(
					Float.compare(pointX2,interPointX))==differenceX)||
					(!isLastPoint));interPointX+=differenceX)
			{
				/*Log.i("PaintDroid","Inter: " + interPointX);
				Log.i("PaintDroid","Last: " + pointX2);*/
				float interPointY=interpolator.interpolate(interPointX);
				int comparisonResultInterY=Float.compare(prevPointY,interPointY);
				if (comparisonResultInterY!=0)
				{
					float diffMiddle=(Math.abs(interPointY-prevPointY)-1)/2+
							(comparisonResultInterY<0?prevPointY:interPointY);
					//Log.i("PaintDroid","Middle: " + diffMiddle);
					int differenceY=-comparisonResultInterY;
					for (float diffPointY=prevPointY+differenceY;Math.signum(
							Float.compare(diffMiddle,diffPointY))==differenceY;
							diffPointY+=differenceY)
					{
						//Log.i("PaintDroid","Diff: " + diffPointY);
						intermediatePoints.add(prevPointX);
						intermediatePoints.add(diffPointY);
					}
					for (float diffPointY=diffMiddle;Math.signum(Float.compare(
							interPointY,diffPointY))==differenceY;diffPointY+=
							differenceY)
					{
						intermediatePoints.add(interPointX);
						intermediatePoints.add(diffPointY);
					}
				} //end if (comparisonResultInterY!=0)
				if (Math.signum(Float.compare(pointX2,interPointX))!=differenceX)
					isLastPoint=true;
				else 
				{ 
					intermediatePoints.add(interPointX);
					intermediatePoints.add(interPointY);
				}	
				prevPointX=interPointX; prevPointY=interPointY;
			} //end for
		} //end if (comparisonResultX!=0)
		else
		{
			int differenceY=-comparisonResultY;
			for (float interPointY=pointY1+differenceY;Math.signum(Float.compare(
					pointY2,interPointY))==differenceY;interPointY+=differenceY)
			{
				intermediatePoints.add(pointX1); 
				intermediatePoints.add(interPointY);
			}
		}
		float[] interPointsArray=new float[intermediatePoints.size()];
		int index=0;
		for (Float interPointWrapper : intermediatePoints)
			interPointsArray[index++]=interPointWrapper;
		return interPointsArray;
	}
}
