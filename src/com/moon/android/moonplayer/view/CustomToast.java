package com.moon.android.moonplayer.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.moon.android.moonplayer.R;

@SuppressLint({ "NewApi", "ViewConstructor" })
public class CustomToast extends Toast {

	public CustomToast(Context context, String message, int textsize) {
		super(context);
		if (context == null) {
			return;
		}
		LinearLayout mainLayout = new LinearLayout(context);
		LinearLayout.LayoutParams paramP = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		mainLayout.setLayoutParams(paramP);
		mainLayout.setBackgroundResource(R.drawable.show_mode_bg);
		mainLayout.setAlpha(0.4f);

		LinearLayout.LayoutParams contentP = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		contentP.setMargins(10, 10, 20, 10);
		contentP.gravity  = Gravity.CENTER;
		
//		LinearLayout.LayoutParams paramIcon = new LinearLayout.LayoutParams(35, 35);
//		paramIcon.setMargins(20, 10, 10, 10);
//		paramIcon.gravity  = Gravity.CENTER;
//		ImageView icon = new ImageView(context);
//		icon.setImageResource(R.drawable.p_icon_scale);
//		icon.setLayoutParams(paramIcon);
		TextView textView = new TextView(context);
		textView.setLayoutParams(contentP);
		textView.setText(message);
		textView.setTextSize(textsize);
		textView.setTextColor(Color.rgb(250, 235, 215));

//		mainLayout.addView(icon);
		mainLayout.addView(textView);

		setGravity(Gravity.BOTTOM, 0, 200);
		setDuration(Toast.LENGTH_SHORT);
		setView(mainLayout);
	}
}
