package com.moon.android.moonplayer;

public abstract class OSD{
	
	public static final int PROPERITY_LEVEL_1 = 1;
	public static final int PROPERITY_LEVEL_2 = 2;
	public static final int PROPERITY_LEVEL_3 = 3;
	public static final int PROPERITY_LEVEL_4 = 4;
	public static final int PROPERITY_LEVEL_5 = 5;
	public static final int PROPERITY_LEVEL_6 = 6;
	
	/*
	 * OSD显示时间为5S，超过5S OSD未操作，则OSD隐藏
	 */
	public static final int OSD_SHOW_TIME = 5;   
	 
	/*
	 * OSD的默认等级为2
	 */
	private int mProperity = PROPERITY_LEVEL_2;
	/*
	 * 检查两个具有相同权限等级的OSD是否可以兼容，如果等级相同的话
	 * 则不兼容，否则则兼容
	 */
	private int mCompatibility = 0;
	
	public static int COMPATIBILITY_01 = 1;
	public static int COMPATIBILITY_02 = 2;
	public static int COMPATIBILITY_03 = 3;
	
	public void setProperity(int properity){
		mProperity = properity;
	};

	public int getProperity(){
		return mProperity;
	}
	
	public abstract void setVisibility(int visibility);
	
	public void setCompatibility(int compatibility){
		mCompatibility = compatibility;
	}
	
	public int getCompatibility(){
		return mCompatibility;
	}
	
}
