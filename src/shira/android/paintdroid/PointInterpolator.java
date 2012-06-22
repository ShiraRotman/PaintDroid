package shira.android.paintdroid;

import flanagan.interpolation.CubicSpline;

interface PointInterpolator 
{
	public abstract float interpolate(float x);
}

class LinearPointInterpolator implements PointInterpolator
{
	//public static final double EPSILON=0.000001; 
	
	private float a,b;
	
	public LinearPointInterpolator(float x1,float y1,float x2,float y2)
	{
		int comparisonResult=Float.compare(x1,x2);
		if (comparisonResult==0)
		{
			if (Float.compare(y1,y2)!=0)
				throw new IllegalArgumentException("The points supplied " +
						"represent a line of the form x=a, which is undefined " +
						"for any other values of x, and thus can't be used " +
						"for interpolation!");
			else
				throw new IllegalArgumentException("The points supplied are " + 
						"identical!");
		}
		else if (comparisonResult>0) //Substitute the points
		{ float temp=x1; x1=x2; x2=temp; temp=y1; y1=y2; y2=temp; }
		a=(y2-y1)/(x2-x1); b=y1-a*x1;
	}
	
	public float interpolate(float x) { return a*x+b; }
}

//Not used
class CubicSplinePointInterpolator implements PointInterpolator
{
	private CubicSpline spline;
	
	public CubicSplinePointInterpolator(float[] pointsX,float[] pointsY)
	{ 
		double[] pointsXDouble=new double[pointsX.length];
		double[] pointsYDouble=new double[pointsY.length];
		for (int index=0;index<pointsX.length;index++)
		{
			pointsXDouble[index]=pointsX[index];
			pointsYDouble[index]=pointsY[index];
		}
		spline=new CubicSpline(pointsXDouble,pointsYDouble);
	}
	
	public float interpolate(float x) 
	{ return (float)(spline.interpolate(x)); }
}
