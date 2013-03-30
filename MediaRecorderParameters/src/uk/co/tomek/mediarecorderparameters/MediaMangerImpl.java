package uk.co.tomek.mediarecorderparameters;

import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

public final class MediaMangerImpl implements MediaManager {

	private static final int RECORDING_BITRATE = 12200;
	private MediaRecorder mMediaRecorder;
	private MediaPlayer mMediaPlayer;
	private int mPausedPosition;
	private final String TAG = "MediaMangerImpl";

	private MediaMangerImpl() {
	}

	/**
	 * Creates new instance of MEdiaManger.
	 * @param mOutputFile
	 * @return MediaMangerImpl
	 */
	public static MediaMangerImpl newInstance() {
		return new MediaMangerImpl();
	}
	
	@Override
	public void recordGreeting(String fileName) {
		Log.d(TAG , String.format("Recording a file:%s", fileName));
		// reset any previous paused position 
		mPausedPosition = 0;

		// initialise MediaRecorder
		if (mMediaRecorder == null) {
			mMediaRecorder = new MediaRecorder();
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
			mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mMediaRecorder.setAudioEncodingBitRate(RECORDING_BITRATE);
			mMediaRecorder.setOnErrorListener(new RecorderErrorListener());
		} else {
			mMediaRecorder.stop();
			mMediaRecorder.reset();
		}
		
		mMediaRecorder.setOutputFile(fileName);
		try {
			mMediaRecorder.prepare();
			mMediaRecorder.start();
		} catch (IllegalStateException e) {
			Log.e(TAG, "IllegalStateException thrown while trying to record a greeting");
			e.printStackTrace();
			mMediaRecorder.release();
			mMediaRecorder = null;
		} catch (IOException e) {
			Log.e(TAG, "IOException thrown while trying to record a greeting");
			e.printStackTrace();
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
	}

	@Override
	public void playGreeting(String fileName, boolean isRestartRequired) {
		Log.d(TAG , String.format("Playing a file:%s", fileName));
		if (mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnErrorListener(new PlayerErrorListener());
		} else {
			mMediaPlayer.stop();
			mMediaPlayer.reset();
		}
		
		try {
			mMediaPlayer.setDataSource(fileName);
			mMediaPlayer.prepare();
			if (isRestartRequired) {
				mMediaPlayer.seekTo(0);
			} else { // probably paused before, so use paused position
				mMediaPlayer.seekTo(mPausedPosition);
			}
			// reset any previous paused position 
			mPausedPosition = 0;
			mMediaPlayer.start();
		} catch (IOException e) {
			Log.e(TAG, "IOException thrown while trying to play a greeting");
			e.printStackTrace();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}

	}

	@Override
	public void stopRecording() {
		if (mMediaRecorder != null) {
			Log.d(TAG, "Stopping recording");
			mMediaRecorder.stop();
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
	}

	@Override
	public void stopPlayback() {
		if (mMediaPlayer != null) {
			Log.d(TAG, "Stopping playback");
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}
	
	@Override
	public void pausePlayback() {
		if (mMediaPlayer != null) {
			mMediaPlayer.pause();
			mPausedPosition = mMediaPlayer.getCurrentPosition();
		}
		
	}
	
	@Override
	public int getPlaybackDuration() {
		int duration = 0;
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			duration = mMediaPlayer.getDuration();
		}
		return duration;
	}
	
	@Override
	public int getCurrentPlaybackPosition() {
		int position = 0;
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			position = mMediaPlayer.getCurrentPosition();
		}
		return position;
	}


	@Override
	public void setPlayPosition(int progress) {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.seekTo(progress);
		}
		mPausedPosition = progress;
		
	}

	@Override
	public MediaPlayer getMediaPlayer() {
		return mMediaPlayer;
	}

	@Override
	public MediaRecorder getMediaRecorder() {
		return mMediaRecorder;
	}
	
	/**
	 * Listener for the MediaRecorder error messages. 
	 */
	public class RecorderErrorListener implements android.media.MediaRecorder.OnErrorListener {

		@Override
		public void onError(MediaRecorder mp, int what, int extra) {
			
			String whatDescription = "";
			
			switch (what) {
			case MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN:
				whatDescription = "MEDIA_RECORDER_ERROR_UNKNOWN";
				break;
			default:
				whatDescription = Integer.toString(what);
				break;

			}
			
			Log.e(TAG, String.format("MediaRecorder error occured: %s,%d", whatDescription, extra));
		}

	}
	
	/**
	 * Listener for the MediaPlayer error messages. 
	 */
	public class PlayerErrorListener implements android.media.MediaPlayer.OnErrorListener {

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			
			String whatDescription = "";
			
			switch (what) {
			case MediaPlayer.MEDIA_ERROR_UNKNOWN:
				whatDescription = "MEDIA_ERROR_UNKNOWN";
				break;
			case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
				whatDescription = "MEDIA_ERROR_SERVER_DIED";
				break;
			default:
				whatDescription = Integer.toString(what);
				break;
			}
			
			Log.e(TAG, String.format("MediaPlayer error occured: %s:%d", whatDescription, extra));
			return false;
		}

	}

}
