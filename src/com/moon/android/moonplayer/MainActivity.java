package com.moon.android.moonplayer;

import com.moon.android.moonplayer.util.Logger;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	public static final Logger log = Logger.getLogger();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
		log.i("Into moon player main activity");
		finish();
//		ComponentName componetName = new ComponentName(  
//                "com.moon.android.moonplayer","com.moon.android.moonplayer.VodPlayerActivity");  
//		Intent intent = new Intent();
//		List<VodVideo> list = new ArrayList<VodVideo>();
//		VodVideo v =new VodVideo();
//		v.setChannelId("52fc9cf6000465170c59de93490590c8");
//		v.setLink("ZCM5P27nqPBuNQdI");
//		v.setUrl("niulist.itvpad.co:9906");
////		forcetv://niulist.itvpad.co:9906/52fc9cf6000465170c59de93490590c8
//		list.add(v);
//		intent.putExtra("videolist", (Serializable)list);
//		intent.setComponent(componetName);  
//        startActivity(intent);  
	}

}
