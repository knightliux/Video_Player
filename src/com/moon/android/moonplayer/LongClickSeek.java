package com.moon.android.moonplayer;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.SeekBar;

public class LongClickSeek extends SeekBar implements OnTouchListener{

	 private onLong longClick;

	    /**
	     * 长按接口
	     * @author terry
	     *
	     */
	    public interface onLong {
	        public boolean onLongClick(View v);
	    }

	    private onChange SeekBarChange;

	    /**
	     * 进度改变接口
	     * @author terry
	     *
	     */
	    public interface onChange {
	        public void onStopTrackingTouch(LongClickSeek seekBar);

	        public void onStartTrackingTouch(LongClickSeek seekBar);

	        public void onProgressChanged(LongClickSeek seekBar, int progress,
	                boolean fromUser);
	    }

	     
	    private Handler hand;
	    private Runnable runable;
	    private Thread th;
	    public static int i = 0;
	    private boolean isStop = false;
	    public static int pp = 0;
	    public int index = 0;

	    public LongClickSeek(Context context) {
	        this(context, null);
	        // TODO Auto-generated constructor stub
	    }

	    public LongClickSeek(Context context, AttributeSet attrs) {
	        super(context, attrs);
	        this.setOnTouchListener(this);
	        this.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

	            @Override
	            public void onStopTrackingTouch(SeekBar seekBar) {
	                // TODO Auto-generated method stub
	                if (SeekBarChange != null) {
	                    SeekBarChange.onStopTrackingTouch(LongClickSeek.this);
	                }
	            }

	            @Override
	            public void onStartTrackingTouch(SeekBar seekBar) {
	                // TODO Auto-generated method stub
	                if (SeekBarChange != null) {
	                    SeekBarChange.onStartTrackingTouch(LongClickSeek.this);
	                }
	            }

	            @Override
	            public void onProgressChanged(final SeekBar seekBar,
	                    final int progress, boolean fromUser) {
	                if (SeekBarChange != null) {
	                    SeekBarChange.onProgressChanged(LongClickSeek.this, progress,
	                            fromUser);
	                }
	                hand = getHandler(1, LongClickSeek.this, progress);
	            }
	        });
	        /**
	         * 为runable 赋�?
	         */
	        runable = new Runnable() {
	            @Override
	            public void run() {
	                // TODO Auto-generated method stub
	                do {
	                    i++;
	                    try {
	                        Thread.sleep(400);
	                        Message msg = hand.obtainMessage();
	                        msg.arg1 = i;
	                        msg.sendToTarget();
	                    } catch (InterruptedException e) {
	                        // TODO Auto-generated catch block
	                        e.printStackTrace();
	                    }
	                } while (isStop);
	            }
	        };
	    }

	    /**
	     * 获取�?��handler 对象
	     * @param 0代表onTouch 1代表onChange
	     * @param 视图对象
	     * @param 进度
	     * @return 返回�?��handler对象
	     */
	    public Handler getHandler(final int j, final View v, final int progress) {
	        Handler h = new Handler() {
	            @Override
	            public void handleMessage(Message msg) {
	                // TODO Auto-generated method stub
	                switch (j) {
	                case 0:
	                    if (msg.arg1 == 3) {
	                        if (longClick != null) {
	                            longClick.onLongClick(v);
	                        }
	                    }
	                    break;
	                case 1:
	                    if (msg.arg1 == 1) {
	                        pp = progress;
	                    }
	                    if (msg.arg1 == 2) {
	                        if (pp != progress) {
	                            i = 0;
	                        }
	                    }
	                    if (msg.arg1 == 3) {
	                        i = 0;
	                        if (pp == progress) {
	                            if (longClick != null) {
	                                longClick.onLongClick(LongClickSeek.this);
	                            }
	                        }
	                    }
	                    break;
	                }
	                super.handleMessage(msg);
	            }
	        };
	        return h;
	    }

	    /**
	     * 设置长按事件
	     * @param longClick
	     */
	    public void setOnLongSeekBarClick(onLong longClick) {
	        this.longClick = longClick;
	    }

	    /**
	     * 设置进度改变事件
	     * @param change
	     */
	    public void setOnSeekBarChange(onChange change) {
	        this.SeekBarChange = change;
	    }

	    @Override
	    public boolean onTouch(final View v, MotionEvent event) {
	        // TODO Auto-generated method stub
	        switch (event.getAction()) {
	        case MotionEvent.ACTION_DOWN:
	            isStop = true;
	            th = new Thread(runable);
	            th.start();
	            i = 0;
	            hand = getHandler(0, v, 0);
	            break;
	        case MotionEvent.ACTION_UP:
	            isStop = false;
	            break;
	        }
	        return false;
	    }
}
