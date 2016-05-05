package com.moon.android.moonplayer;

import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class OSDPause extends OSD{

	

	private View mView;
	private View mViewPause;
	private ImageView mAdImage;
	
	public OSDPause(View view,String adUrl){
		mView = view;
		mViewPause = mView.findViewById(R.id.container_big_pause);
		mAdImage=(ImageView) mViewPause.findViewById(R.id.pause_ad);
		if(adUrl!=null){
			ImageLoader.getInstance().displayImage(adUrl, mAdImage);
		}
		setProperity(PROPERITY_LEVEL_4);
	}
	
	@Override
	public void setVisibility(int visible) {
		mViewPause.setVisibility(visible);
	}
	
}
