package com.moon.android.moonplayer;

import android.view.View;

public class OSDLoading extends OSD{

	private VodLoadAnimation mVodLoadAnimation;
	private View mParentView;
	public OSDLoading(View parentView){
		setProperity(PROPERITY_LEVEL_6);
		mParentView = parentView;
		mVodLoadAnimation = (VodLoadAnimation) mParentView.findViewById(R.id.vod_load_animation);
	}
	
	@Override
	public void setVisibility(int visible) {
		mVodLoadAnimation.setVisibility(visible);
	}
	
	
	public void setPromptText(int traffic){
		mVodLoadAnimation.setPromptText(traffic);
	}
}
