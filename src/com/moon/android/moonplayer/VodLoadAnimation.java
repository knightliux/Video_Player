package com.moon.android.moonplayer;

import com.moon.android.moonplayer.util.Logger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint("NewApi")
public class VodLoadAnimation extends LinearLayout {

	private Logger logger = Logger.getLogger();

	private TextView mTextPrompt;

	public VodLoadAnimation(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public VodLoadAnimation(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VodLoadAnimation(Context context) {
		this(context, null);
	}

	private void init(Context context) {
		logger.i("视频资源正在加载中...");
		View view = LayoutInflater.from(context).inflate(
				R.layout.vod_load_anim, this);
		ImageView progressImageView = (ImageView) view.findViewById(R.id.vod_load_img);
		mTextPrompt = (TextView) view.findViewById(R.id.load_prompt);
		AnimationDrawable ad = (AnimationDrawable) progressImageView
				.getDrawable();
		ad.start();
	}

	public void setPromptText(int traffic) {
		String promptText = getString(R.string.load_prompt) + "(" + traffic
				+ getString(R.string.kbs) + ")";
		mTextPrompt.setText(promptText);
	}

	private String getString(int resId) {
		return getResources().getText(resId).toString();
	}
}
