package shira.android.paintdroid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.*;
//import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.*;

public class ColorSelectionActivity extends Activity
{
	public static final String SELECTED_COLOR_KEY_NAME="selected_color";
	private static final Pattern colorHexPattern=Pattern.compile("[0-9a-f]*",
			Pattern.CASE_INSENSITIVE);
	
	private GradientSlider redComponentSlider,greenComponentSlider;
	private GradientSlider blueComponentSlider;
	private EditText redComponentEdit,greenComponentEdit,blueComponentEdit;
	private EditText colorHexValue;
	private View selectedColorView;
	private int selectedColor;
	private boolean isUpdatingColor;
	
	private class ColorComponentTextWatcher implements TextWatcher
	{
		private TextView textView;
		
		public ColorComponentTextWatcher(TextView textView)
		{ this.textView=textView; }
		
		public void beforeTextChanged(CharSequence s,int start,int count,
				int after) { }
		public void onTextChanged(CharSequence s,int start,int before,int count) { }
		
		public void afterTextChanged(Editable s) 
		{
			if (isUpdatingColor) return;
			int colorComponent=(s.length()>0?Integer.parseInt(s.toString()):0);
			if (colorComponent>255)
			{
				colorComponent=255;
				s.clear();
				s.append(String.valueOf(colorComponent));
			}
			switch (textView.getId())
			{
				case R.id.red_component_edit:
					redComponentSlider.setValue(colorComponent);
					selectedColor=Color.rgb(colorComponent,Color.green(
							selectedColor),Color.blue(selectedColor));
					break;
				case R.id.green_component_edit:
					greenComponentSlider.setValue(colorComponent);
					selectedColor=Color.rgb(Color.red(selectedColor),
							 colorComponent,Color.blue(selectedColor));
					break;
				case R.id.blue_component_edit:
					blueComponentSlider.setValue(colorComponent);
					selectedColor=Color.rgb(Color.red(selectedColor),Color.
							green(selectedColor),colorComponent);
					break;
			}
			updateColor();
		}
	}
	
	private class ColorHexTextWatcher implements TextWatcher
	{
		private String textToReplace;
		private int start,count;
		
		public void beforeTextChanged(CharSequence s,int start,int count,
				int after) 
		{
			if (isUpdatingColor) return;
			textToReplace=s.subSequence(start,start+count).toString();
			this.start=start;
		}
		
		public void onTextChanged(CharSequence s,int start,int before,
				int count) 
		{
			if (isUpdatingColor) return;
			if (s.length()<=6)
			{
				String newText=s.subSequence(start,start+count).toString();
				if (colorHexPattern.matcher(newText).matches())
				{
					String upperCaseText=newText.toUpperCase();
					if (!newText.equals(upperCaseText))
						textToReplace=upperCaseText;
					else textToReplace=null;
				}
				//else restore original text
			}
			//else restore original text
			this.count=count;
		}
		
		public void afterTextChanged(Editable s) 
		{
			if (isUpdatingColor) return;
			//String originalText=s.toString();
			if (textToReplace!=null)
				s.replace(start,start+count,textToReplace);
			/*if (s.length()<6)
			{
				StringBuilder filler=new StringBuilder();
				for (int index=0;index<6-s.length();index++)
					filler.append("0");
				s.insert(0,filler);
			}*/
			/*if (!s.toString().equals(originalText))
			{*/
			selectedColor=Integer.parseInt(s.toString(),16) | 0xFF000000;
			updateColor();
			//}
		}
	}
	
	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.color_selection);
		if (savedInstanceState!=null)
		{
			selectedColor=savedInstanceState.getInt(SELECTED_COLOR_KEY_NAME,
					Color.BLACK);
		}
		else selectedColor=Color.BLACK;
		selectedColorView=findViewById(R.id.selected_color_view);
		colorHexValue=(EditText)findViewById(R.id.color_hex_value);
		
		//Set color sliders and editors 
		redComponentSlider=(GradientSlider)findViewById(R.id.red_component_slider);
		greenComponentSlider=(GradientSlider)findViewById(R.id.green_component_slider);
		blueComponentSlider=(GradientSlider)findViewById(R.id.blue_component_slider);
		redComponentEdit=(EditText)findViewById(R.id.red_component_edit);
		greenComponentEdit=(EditText)findViewById(R.id.green_component_edit);
		blueComponentEdit=(EditText)findViewById(R.id.blue_component_edit);
		
		//Set slide listeners
		GradientSlider.OnValueChangeListener slideListener=new GradientSlider.
				OnValueChangeListener() 
		{
			@Override 
			public void onValueChange(GradientSlider gradientSlider,float value) 
			{
				int colorComponent=Math.round(gradientSlider.getValue());
				switch (gradientSlider.getId())
				{
					case R.id.red_component_slider:
						selectedColor=Color.rgb(colorComponent,Color.green(
								selectedColor),Color.blue(selectedColor));
						break;
					case R.id.green_component_slider:
						selectedColor=Color.rgb(Color.red(selectedColor),
								colorComponent,Color.blue(selectedColor));
						break;
					case R.id.blue_component_slider:
						selectedColor=Color.rgb(Color.red(selectedColor),
								Color.green(selectedColor),colorComponent);
						break;
				}
				updateColor();
			}
		}; //end listener inner class
		redComponentSlider.setOnValueChangeListener(slideListener);
		blueComponentSlider.setOnValueChangeListener(slideListener);
		greenComponentSlider.setOnValueChangeListener(slideListener);
		
		//Set edit listeners
		redComponentEdit.addTextChangedListener(new ColorComponentTextWatcher(
				redComponentEdit));
		greenComponentEdit.addTextChangedListener(new ColorComponentTextWatcher(
				greenComponentEdit));
		blueComponentEdit.addTextChangedListener(new ColorComponentTextWatcher(
				blueComponentEdit));
		colorHexValue.addTextChangedListener(new ColorHexTextWatcher());
		
		View button=findViewById(R.id.ok_button);
		button.setOnClickListener(new View.OnClickListener() 
		{	
			@Override public void onClick(View v) 
			{
				Intent resultIntent=new Intent();
				resultIntent.putExtra(SELECTED_COLOR_KEY_NAME,selectedColor);
				setResult(RESULT_OK,resultIntent);
				finish();
			}
		});
		button=findViewById(R.id.cancel_button);
		button.setOnClickListener(new View.OnClickListener() 
		{
			@Override public void onClick(View v) 
			{ 
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		
		updateColor();
	}
	
	@Override protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt(SELECTED_COLOR_KEY_NAME,selectedColor);
	}
	
	private void updateColor()
	{
		isUpdatingColor=true;
		selectedColorView.setBackgroundColor(selectedColor);
		int redComponent=Color.red(selectedColor);
		int greenComponent=Color.green(selectedColor);
		int blueComponent=Color.blue(selectedColor);
		redComponentSlider.setValue(redComponent);
		greenComponentSlider.setValue(greenComponent);
		blueComponentSlider.setValue(blueComponent);
		int minColor=Color.rgb(0,greenComponent,blueComponent);
		int maxColor=Color.rgb(0xFF,greenComponent,blueComponent);
		redComponentSlider.setColors(new int[] {minColor,maxColor});
		minColor=Color.rgb(redComponent,0,blueComponent);
		maxColor=Color.rgb(redComponent,0xFF,blueComponent);
		greenComponentSlider.setColors(new int[] {minColor,maxColor});
		minColor=Color.rgb(redComponent,greenComponent,0);
		maxColor=Color.rgb(redComponent,greenComponent,0xFF);
		blueComponentSlider.setColors(new int[] {minColor,maxColor});
		redComponentEdit.setText(String.valueOf(redComponent));
		greenComponentEdit.setText(String.valueOf(greenComponent));
		blueComponentEdit.setText(String.valueOf(blueComponent));
		int colorHex=selectedColor & 0x00FFFFFF;
		String colorHexStr=Integer.toHexString(colorHex).toUpperCase();
		/*if (colorHexStr.length()<6)
		{
			StringBuilder colorHexBuilder=new StringBuilder();
			for (int index=0;index<6-colorHexStr.length();index++)
				colorHexBuilder.append("0");
			colorHexBuilder.append(colorHexStr);
			colorHexStr=colorHexBuilder.toString();
		}*/
		colorHexValue.setText(colorHexStr);
		isUpdatingColor=false;
	}
}
