package com.moon.android.moonplayer;

import android.view.View;

public class OSDCache extends OSD{
	
	public static final String TAG = "OSDCache";
	private View mContainer;
	private VodCacheView mVodCacheView;
	public OSDCache(View view){
		setProperity(PROPERITY_LEVEL_3);
		mContainer = view;
		mVodCacheView = (VodCacheView) mContainer.findViewById(R.id.vod_cache_animation);
	}
	
	public int getVisibility(){
		return mVodCacheView.getVisibility();
	}
	
	@Override
	public void setVisibility(int visible) {
		mVodCacheView.setVisibility(visible);
	}
	
	public void setTraffic(String trffiac){
		mVodCacheView.setText(trffiac);
	}
	
	public void setTraffic(long traffic){
		String trafficStr = String.valueOf(traffic);
		mVodCacheView.setText(trafficStr);
	}
}
