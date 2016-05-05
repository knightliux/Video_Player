package com.moon.android.moonplayer.history;

public class HistoryItem {

	private int _id;
	private String subcatid;
	private String playIndex;
	private String playPos;
	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
	}
	public String getSubcatid() {
		return subcatid;
	}
	public void setSubcatid(String subcatid) {
		this.subcatid = subcatid;
	}
	public String getPlayIndex() {
		return playIndex;
	}
	public void setPlayIndex(String playIndex) {
		this.playIndex = playIndex;
	}
	public String getPlayPos() {
		return playPos;
	}
	public void setPlayPos(String playPos) {
		this.playPos = playPos;
	}
	
	public HistoryItem(){}
	
	public HistoryItem(int _id, String subcatid, String playIndex,String playPos){
		this._id = _id;
		this.subcatid = subcatid;
		this.playIndex = playIndex;
		this.playPos = playPos;
	}
}
