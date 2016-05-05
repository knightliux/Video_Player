package com.moon.android.moonplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SoundView extends View
{
  private static final int HEIGHT = 11;
  public static final int MY_HEIGHT = 163;
  public static final int MY_WIDTH = 44;
  public static final String TAG = "SoundView";
  private int bitmapHeight;
  private int bitmapWidth;
  private Bitmap bm;
  private Bitmap bm1;
  private int index;
  private Context mContext;
  private OnVolumeChangedListener mOnVolumeChangedListener;

  public SoundView(Context paramContext)
  {
    super(paramContext);
    this.mContext = paramContext;
    init();
  }

  public SoundView(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    this.mContext = paramContext;
    init();
  }

  public SoundView(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    super(paramContext, paramAttributeSet, paramInt);
    this.mContext = paramContext;
    init();
  }

  private void init()
  {
    this.bm = BitmapFactory.decodeResource(this.mContext.getResources(), 2130837663);
    this.bm1 = BitmapFactory.decodeResource(this.mContext.getResources(), 2130837664);
    this.bitmapWidth = this.bm.getWidth();
    this.bitmapHeight = this.bm.getHeight();
    setIndex(((AudioManager)this.mContext.getSystemService("audio")).getStreamVolume(3));
  }

  private void setIndex(int paramInt)
  {
    if (paramInt > 15)
      paramInt = 15;
    if (paramInt < 0)
      paramInt = 0;
      if (this.index != paramInt)
      {
        this.index = paramInt;
        if (this.mOnVolumeChangedListener != null)
          this.mOnVolumeChangedListener.setYourVolume(paramInt);
      }
      invalidate();
      return;
  }

  protected void onDraw(Canvas paramCanvas)
  {
    int i = 15 - this.index;
    for(int j= 0;j!=i;j++){
        paramCanvas.drawBitmap(this.bm1, new Rect(0, 0, this.bitmapWidth, this.bitmapHeight), new Rect(0, j * 11, this.bitmapWidth, j * 11 + this.bitmapHeight), null);
        j++;
    }
    for (int k = i;k!=15 ; k++)
    {
      paramCanvas.drawBitmap(this.bm, new Rect(0, 0, this.bitmapWidth, this.bitmapHeight), new Rect(0, k * 11, this.bitmapWidth, k * 11 + this.bitmapHeight), null);
    }
    super.onDraw(paramCanvas);
    return;
  }

  public boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    int i = 15 * (int)paramMotionEvent.getY() / 163;
    setIndex(15 - i);
    return true;
  }

  public void setOnVolumeChangeListener(OnVolumeChangedListener paramOnVolumeChangedListener)
  {
    this.mOnVolumeChangedListener = paramOnVolumeChangedListener;
  }

  public static abstract interface OnVolumeChangedListener
  {
    public abstract void setYourVolume(int paramInt);
  }
}