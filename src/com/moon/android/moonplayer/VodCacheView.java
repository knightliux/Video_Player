package com.moon.android.moonplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("NewApi")
public class VodCacheView extends RelativeLayout{
	
	private ImageView mImgVod;
	private TextView mTextTraffic;
	private TextView mTextTrafficBKS;
	public static final String KBS = " kb/s";
	public VodCacheView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		View view = LayoutInflater.from(context).inflate(R.layout.p_vod_cache, this);
		Animation operatingAnim = AnimationUtils.loadAnimation(context, R.anim.p_vod_anima);  
		LinearInterpolator lin = new LinearInterpolator();  
		operatingAnim.setInterpolator(lin);  
		
		mImgVod = (ImageView) view.findViewById(R.id.rotate_image);
		mTextTraffic = (TextView) view.findViewById(R.id.text_traffic);
		mTextTrafficBKS = (TextView) view.findViewById(R.id.text_traffic_kbs);
		mImgVod.startAnimation(operatingAnim);
	}

	public VodCacheView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public VodCacheView(Context context) {
		this(context, null);
	}
	
	public void stopAnimation(){
		mImgVod.clearAnimation();
	}

	public void setText(String traffic){
//		String trafficText = new StringBuilder(traffic)
//								.append(KBS).toString();
		mTextTraffic.setText(traffic);
		mTextTrafficBKS.setText(KBS);
	}
	
}
