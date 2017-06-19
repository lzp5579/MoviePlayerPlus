package com.bfy.movieplayerplus.view;

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.MediaController;

import com.bfy.movieplayerplus.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
 
/**
 * Displays a video file.  The VideoView class
 * can load images from various sources (such as resources or content
 * providers), takes care of computing its measurement from the video so that
 * it can be used in any layout manager, and provides various display options
 * such as scaling and tinting.
 */
public class VideoView extends SurfaceView implements MediaPlayerController{

	private static final boolean DEBUG = LogUtils.isDebug;
    private static final String TAG = "VideoView";

    private Context mContext;
    private ArrayList<String> mMediaList;
    private Uri         mCurrentUri;
    private int         mDuration;
    private int			mCurrentIndex;

    // All the stuff we need for playing and showing a video
    private SurfaceHolder mSurfaceHolder = null;
    private MediaPlayer mMediaPlayer = null;
    private MediaController mMediaController = null;
    
    private boolean     mIsPrepared;
    private int         mVideoWidth;
    private int         mVideoHeight;
    private int         mSurfaceWidth;
    private int         mSurfaceHeight;
//  private int         mCurrentBufferPercentage;
    private boolean     mStartWhenPrepared;
    private int         mSeekWhenPrepared;

    private OnChangeListener mOnChangeListener;
    
    public VideoView(Context context) {
	    this(context,null,0);
	}

	public VideoView(Context context, AttributeSet attrs) {
	    this(context, attrs, 0);
	}

	public VideoView(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    mContext = context;
	    initVideoView();
	}

    
    
    private MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
	    new MediaPlayer.OnVideoSizeChangedListener() {
	        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
	        	if(DEBUG) Log.i(TAG, "OnVideoSizeChanged.......................");
	            mVideoWidth = mp.getVideoWidth();
	            mVideoHeight = mp.getVideoHeight();
	            
	            /*if(mMyChangeLinstener!=null){
	            	mMyChangeLinstener.doMyThings();
	            }*/
	            
	            /*if (mVideoWidth != 0 && mVideoHeight != 0) {
	                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
	            }*/
	        }
	};
	
	private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
	        public void onPrepared(MediaPlayer mp) {
	            // briefly show the mediacontroller
	        	if(DEBUG) Log.i(TAG, "begin prepare....................");
	            mIsPrepared = true;
	           
	            if (mMediaController != null) {
	                mMediaController.setEnabled(true);
	            }
	            mVideoWidth = mp.getVideoWidth();
	            mVideoHeight = mp.getVideoHeight();
	            if (mVideoWidth != 0 && mVideoHeight != 0) {
	                if(DEBUG) Log.i(TAG, "video size: " + mVideoWidth +"/"+ mVideoHeight);
	                setVideoScale(SCREEN_FULL, SCALE_MODE_DEFAULT);
//	                if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
	                    // We didn't actually change the size (it was already at the size
	                    // we need), so we won't get a "surface changed" callback, so
	                    // start the video here instead of in the callback.
//	                	if(DEBUG) Log.i(TAG, " if mStartWhenPrepared : " + mStartWhenPrepared);
	                    if (mSeekWhenPrepared != 0) {
	                        mMediaPlayer.seekTo(mSeekWhenPrepared);
	                        mSeekWhenPrepared = 0;
	                    }
	                   
	                   if (mStartWhenPrepared) {
	                        mMediaPlayer.start();
	                        mStartWhenPrepared = false;
	                        
	                    }/* else if (!isPlaying() &&
	                            (mSeekWhenPrepared != 0 || getTime() > 0)) {
	                       if (mMediaController != null) {
	                           mMediaController.show(0);
	                       }
	                   	}*/
		                if (mMediaController != null) {
	                         mMediaController.show();
	                    }
//	                }
	            } else {
	                // We don't know the video size yet, but should start anyway.
	                // The video size might be reported to us later.
	            	if(DEBUG) Log.i(TAG, " else mStartWhenPrepared : " + mStartWhenPrepared);
	                if (mSeekWhenPrepared != 0) {
	                    mMediaPlayer.seekTo(mSeekWhenPrepared);
	                    mSeekWhenPrepared = 0;
	                }
	                
	                if (mStartWhenPrepared) {
	                    mMediaPlayer.start();
	                    mStartWhenPrepared = false;
	                }
	            }
	            
	            /*if (mOnPreparedListener != null) {
	                mOnPreparedListener.onPrepared(mMediaPlayer);
	            }*/
	        }
	    };
	    
	private MediaPlayer.OnCompletionListener mCompletionListener =
	  new MediaPlayer.OnCompletionListener() {
	    public void onCompletion(MediaPlayer mp) {
	        if (mMediaController != null) {
	            mMediaController.hide();
	        }
	     if(DEBUG)  Log.i(TAG, "playing complete!!!!!!!!!!!!");
	     	stop();
	     	//recordPosition(0);
	        if (mOnChangeListener != null) {
	        	mOnChangeListener.onEnd();;
	        }
	    }
	
	};
	
	private MediaPlayer.OnErrorListener mErrorListener =
	        new MediaPlayer.OnErrorListener() {
	        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
	            if(DEBUG) Log.d(TAG, "Error: " + framework_err + "," + impl_err);
	            if (mMediaController != null) {
	                mMediaController.hide();
	            }
	
	            /* If an error handler has been supplied, use it and finish. */
	            if (mOnChangeListener != null) {
	            	mOnChangeListener.onError();
	            }
	            return true;
	        }
	    };
	
	private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
	  new MediaPlayer.OnBufferingUpdateListener() {
	    public void onBufferingUpdate(MediaPlayer mp, int percent) {
//	        mCurrentBufferPercentage = percent;
	        if(mOnChangeListener != null){
	        	mOnChangeListener.onBufferChanged(percent);;
	        }
	    }
	};
	
	/*private MediaPlayer.OnTimedTextListener mTimeChangedListener = new MediaPlayer.OnTimedTextListener() {
			
			@Override
			public void onTimedText(MediaPlayer mp, TimedText text) {
				if(mOnChangeListener != null){
					mOnChangeListener.onPositionChanged(mMediaPlayer.getCurrentPosition());
				}
				
			}
	};*/
	    
	
	private SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback(){
		public void surfaceChanged(SurfaceHolder holder, int format,int w, int h){
		    	if(DEBUG) Log.i(TAG, "serface change...width = " + w + "height = " + h );
		        mSurfaceWidth = w;
		        mSurfaceHeight = h;
	//	        if (mMediaPlayer != null && mIsPrepared && mVideoWidth == w && mVideoHeight == h) {
	//	            if (mSeekWhenPrepared != 0) {
	//	            	//Log.i(TAG, "read Position : " + mSeekWhenPrepared);
	//	                mMediaPlayer.seekTo(mSeekWhenPrepared);
	//	                mSeekWhenPrepared = 0;
	//	            }
	//	          //  mMediaPlayer.start();
	//	            if (mMediaController != null) {
	//	                mMediaController.show();
	//	            }
	//	        }  
		    }
		
		public void surfaceCreated(SurfaceHolder holder){
			if(DEBUG) Log.i(TAG, "callback Create!.......................");
			mSurfaceHolder = holder;
			openVideo();
		}
		
		public void surfaceDestroyed(SurfaceHolder holder){
			// after we return from this we can't use the surface any more
		    mSurfaceHolder = null;
		    if(DEBUG)  Log.w(TAG, "the surface destroy..............");
		    if(mMediaController != null) mMediaController.hide();
		    stop(); 
		}
	};



	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//	    int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
//	    int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
//	    setMeasuredDimension(width,height);
	}

	private void initVideoView() {
	        mVideoWidth = 0;
	        mVideoHeight = 0;
	        getHolder().addCallback(mSHCallback);
//	        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	        setFocusable(true);
	        setFocusableInTouchMode(true);
	        requestFocus();
	}

	private void initPlayer(){
		 openVideo();
		 requestLayout();
		 invalidate();
	}

	private void openVideo() {
	
	        if (mCurrentUri == null || mSurfaceHolder == null) {
	            return;
	        }
	        if(DEBUG) Log.i(TAG, "Uri Scheme : " + mCurrentUri.getScheme()
	        				+ "      ParentPath : " + new File(mCurrentUri.getPath()).getParent());
	        
	        // Tell the music playback service to pause
	        Intent i = new Intent("com.android.music.musicservicecommand");
	        i.putExtra("command", "pause");
	        mContext.sendBroadcast(i);
	        if (mMediaPlayer != null) {
	            mMediaPlayer.reset();
	            mMediaPlayer.release();
	            mMediaPlayer = null;
	        }
	        try {
//	        	begin playing video.........................................
	        	mDuration = -1;
	        	mIsPrepared = false;
	            mMediaPlayer = new MediaPlayer();
	            mMediaPlayer.setOnPreparedListener(mPreparedListener);
	            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
	            mMediaPlayer.setOnCompletionListener(mCompletionListener);
	            mMediaPlayer.setOnErrorListener(mErrorListener);
	            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
	            //mMediaPlayer.setOnTimedTextListener(null);
	            mMediaPlayer.setDataSource(mContext, mCurrentUri);
	            mMediaPlayer.setDisplay(mSurfaceHolder);
	            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	            //mMediaPlayer.setVolume(1f, 1f);
	            mMediaPlayer.setScreenOnWhilePlaying(true);
	            mMediaPlayer.prepareAsync();
	        } catch (IOException ex) {
	            Log.w(TAG, "Unable to open content: " + mCurrentUri, ex);
	            return;
	        } catch (IllegalArgumentException ex) {
	            Log.w(TAG, "Unable to open content: " + mCurrentUri, ex);
	            return;
	        }
	    }

	private void setScale(int width , int height){
//			getHolder().setFixedSize(width, height);
			LayoutParams lp = getLayoutParams();
			lp.height = height;
			lp.width = width;
			setLayoutParams(lp);
	}
	
	private int[] adjustScale(int cw,int ch,int vw,int vh){
    	int[] scale = new int[]{cw,ch};
    	if(vh <= 0 || vw <= 0) return scale;
    	if(ch * vw > cw * vh){
    		//Log.i(TAG, "image too tall, correcting");
    		scale[1] = cw * vh / vw;
    	}else if(ch * vw < cw * vh){
    		//Log.i(TAG, "image too wide, correcting");
    		scale[0] = ch * vw / vh;
    	}
    	return scale;
    	
    }

	/*private void toggleMediaControlsVisiblity() {
	    if (mMediaController.isShowing()) {
	        mMediaController.hide();
	    } else {
	        mMediaController.show();
	    }
	}*/

	/*private void attachMediaController() {
	     if (mMediaPlayer != null && mMediaController != null) {
	          mMediaController.setMediaPlayer(this);
	          View anchorView = this.getParent() instanceof View ?
	                (View)this.getParent() : this;
	          mMediaController.setAnchorView(anchorView);
	          mMediaController.setEnabled(mIsPrepared);
	     }
	}*/

	/*public void setMediaController(MediaController controller) {
	     if (mMediaController != null) {
	         mMediaController.hide();
	     }
	     mMediaController = controller;
	     attachMediaController();
	}*/

	public int getVideoWidth(){
    	return mVideoWidth;
    }
    
    public int getVideoHeight(){
    	return mVideoHeight;
    }
    
    @Override
    public void setVideoScale(int flag,int scalMode){
    	if(!(mContext instanceof Activity)) return;
    	switch(flag){
    		case SCREEN_FULL: {
				((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				((Activity) mContext).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE | View.GONE);
//				setScale(r.width(), r.height());
				break;
			}
    		case SCREEN_DEFAULT: {
				//end by haoxiangtt
				((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				((Activity) mContext).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

				break;
			}
    	}
		Rect r = new Rect();
		View p = ((View)getParent());
		if(p != null) p.getGlobalVisibleRect(r);

		int mWidth = r.width();
		int mHeight = r.height();
		if (DEBUG) Log.i(TAG, "the parent screen width/height =(" + mWidth + ":" + mHeight + ")");
		if (DEBUG) Log.i(TAG, "the video width/height =(" + mVideoWidth + ":" + mVideoHeight + ")");
		//add by haoxiangtt 2014.4.2 for change scale
		int[] scale = null;
		switch (scalMode) {
			case SCALE_MODE_DEFAULT: {
				//原比例
				scale = adjustScale(mWidth, mHeight, mVideoWidth, mVideoHeight);
				break;
			}
			case SCALE_MODE_16_9: {
				//16:9
				scale = adjustScale(mWidth, mHeight, 16, 9);
				break;
			}
			case SCALE_MODE_4_3: {
				//4:3
				scale = adjustScale(mWidth, mHeight, 4, 3);
				break;
			}
			case SCALE_MODE_FULL: {
				scale = adjustScale(mWidth, mHeight, 0, 0);
			}
		}
		if (DEBUG) LogUtils.i(TAG, "the scale =(" + scale[0] + ":" + scale[1] + ")");
		setScale(scale[0], scale[1]);
    }

    
	public int resolveAdjustedSize(int desiredSize, int measureSpec) {
		int result = desiredSize;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize =  MeasureSpec.getSize(measureSpec);
		switch (specMode) {
		    case MeasureSpec.UNSPECIFIED:
		       /* Parent says we can be as big as we want. Just don't be larger
		        * than max size imposed on ourselves.*/
		      result = desiredSize;
		      break;
		   case MeasureSpec.AT_MOST:
		       /* Parent says we can be as big as we want, up to specSize.
		        * Don't be larger than specSize, and don't be larger than
		        * the max size imposed on ourselves.*/
		       result = Math.min(desiredSize, specSize);
		        break;
		
		   case MeasureSpec.EXACTLY:
		       // No choice. Do what we are told.
		      result = specSize;
		      break;
		}
		return result;
	}

    
    public boolean takeScreenShot(){
    	return false;
    }

	@Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mIsPrepared && mMediaPlayer != null && mMediaController != null) {
           // toggleMediaControlsVisiblity();
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (mIsPrepared && mMediaPlayer != null && mMediaController != null) {
            //toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (mIsPrepared &&
                keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL &&
                mMediaPlayer != null &&
                mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    && mMediaPlayer.isPlaying()) {
                pause();
                mMediaController.show();
            } else {
               // toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void setOnChangeListener(OnChangeListener listener) {
    	mOnChangeListener = listener;
    }
    
    public void initPlayer(Uri uri) {
//			LibVLC.getInstance().init(mContext);
		mMediaList = new ArrayList<String>();
		mCurrentUri = uri;
		mCurrentIndex = 0;
		mMediaList.clear();
		mMediaList.add(uri.toString());
		initPlayer();
	    
	}
    
   
    
    @Override
	public void initPlayer(String path) {
		if(path != null &&  !path.equals("")){
			initPlayer(Uri.parse(path));
		}
	}

	
	@Override
	public void initPlayer(ArrayList<String> list){
		initPlayer(list, 0);
	}
	
	@Override
	public void initPlayer(ArrayList<String> list,int index){
		mMediaList = list;
		mCurrentUri = Uri.parse(mMediaList.get(index));
		mCurrentIndex = index;
		initPlayer();
	}
	
	@Override
    public void start() {
//    	Log.i(TAG, "start: mMediaplayer = " + mMediaPlayer);
        if (mMediaPlayer != null && mIsPrepared) {
        	//Log.i(TAG, " out start le..................");
                mMediaPlayer.start();
                mStartWhenPrepared = false;
        } else {
            mStartWhenPrepared = true;
        }
    }
	
	@Override
	public void play() {
		mMediaPlayer.start();
	}
	
    @Override
    public void pause() {
//    	Log.i(TAG, "pause: mMediaplayer = " + mMediaPlayer);
        if (mMediaPlayer != null && mIsPrepared) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
//        mStartWhenPrepared = false;
    }
    
    @Override
    public void stop() {
		if(DEBUG) Log.i(TAG, "enter Stop method:"+mMediaPlayer);
	    if (mMediaPlayer != null) {
	    	if(DEBUG) Log.i(TAG, "stop media play................");
	    	//long t = getTime();
	    	//recordPosition(t);
	        mMediaPlayer.stop();
	    }
	}
    
    @Override
    public void destroy(){
    	if(mMediaPlayer != null){
	    	mMediaPlayer.reset();
	    	mMediaPlayer.release();
		    //this.invalidate();
		    mMediaPlayer = null;
    	}
    }

	@Override
    public long getDuration() {
        if (mMediaPlayer != null && mIsPrepared) {
            if (mDuration > 0) {
                return mDuration;
            }
            mDuration = mMediaPlayer.getDuration();
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
    }
	
    @Override
    public long getTime() {
        if (mMediaPlayer != null && mIsPrepared) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }
    
    @Override
	public void setTime(long time) {
		if (mMediaPlayer != null && mIsPrepared) {
            mMediaPlayer.seekTo((int)time);
        } else {
            mSeekWhenPrepared = (int)time;
        }
		
	}
    
    @Override
    public void seekTo(int delta) {
    	int seek = mMediaPlayer.getCurrentPosition() + delta;
        if (mMediaPlayer != null && mIsPrepared) {
            mMediaPlayer.seekTo(seek);
        } else {
            mSeekWhenPrepared = seek;
        }
    }
    
    @Override
    public boolean isPlaying() {
        if (mMediaPlayer != null && mIsPrepared) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

	@Override
	public boolean canSeekble() {
		return true;
	}

	

	@Override
	public boolean playNext() {
		if(mMediaList == null || mMediaList.size() == 0){ return false; }
		if(this.mCurrentIndex >= mMediaList.size() - 1){ return false; }
		mCurrentIndex++;
		mCurrentUri = Uri.parse(mMediaList.get(mCurrentIndex));
		try {
			if(mMediaPlayer.isPlaying()){ mMediaPlayer.stop(); }
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(mContext, mCurrentUri);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		}catch (IOException e) {
			if(DEBUG){ e.printStackTrace(); }
			return false;
		}
		return false;
	}

	@Override
	public boolean playBack() {
		if(mMediaList == null || mMediaList.size() == 0){ return false; }
		if(this.mCurrentIndex <= 0){ return false; }
		mCurrentIndex--;
		mCurrentUri = Uri.parse(mMediaList.get(mCurrentIndex));
		try {
			if(mMediaPlayer.isPlaying()){ mMediaPlayer.stop(); }
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(mContext, mCurrentUri);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		}catch (IOException e) {
			if(DEBUG){ e.printStackTrace(); }
			return false;
		}
		return false;
	}


	@Override
	public String getCurrentPlayUrl() {
		return mCurrentUri != null ? mCurrentUri.toString() : "";
	}

	@Override
	public int getCurrentPlayIndex() {
		return mCurrentIndex;
	}


}
