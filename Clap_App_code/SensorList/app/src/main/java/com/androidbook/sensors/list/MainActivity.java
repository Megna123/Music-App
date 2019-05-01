package com.androidbook.sensors.list;

//This file is MainActivity.java
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	Button playBtn;
	SeekBar positionBar;
	SeekBar volumeBar;
	TextView elapsedTimeLabel;
	TextView remainingTimeLabel;
	MediaPlayer mp;
	int totalTime;
	private SensorManager sensorManager;
	private Sensor proximitySensor;
	private SensorEventListener proximitySensorListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		playBtn = (Button) findViewById(R.id.playBtn);
		elapsedTimeLabel = (TextView) findViewById(R.id.elapsedTimeLabel);
		remainingTimeLabel = (TextView) findViewById(R.id.remainingTimeLabel);

		// Media Player
		mp = MediaPlayer.create(this, R.raw.music);
		mp.setLooping(true);
		mp.seekTo(0);
		mp.setVolume(0.5f, 0.5f);
		totalTime = mp.getDuration();
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		// Position Bar
		positionBar = (SeekBar) findViewById(R.id.positionBar);
		positionBar.setMax(totalTime);
		positionBar.setOnSeekBarChangeListener(
				new SeekBar.OnSeekBarChangeListener() {
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						if (fromUser) {
							mp.seekTo(progress);
							positionBar.setProgress(progress);
						}
					}

					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					public void onStopTrackingTouch(SeekBar seekBar) {

					}
				}
		);

		if (proximitySensor == null) {
			Toast.makeText(this, "Proximity sensor is not available !", Toast.LENGTH_LONG).show();
			finish();
		}

		proximitySensorListener = new SensorEventListener() {
			public void onSensorChanged(SensorEvent sensorEvent) {
				float volumeNum = sensorEvent.values[0]*10 / 100f;
				mp.setVolume(volumeNum, volumeNum);
				volumeBar.setProgress((int)sensorEvent.values[0]*10);

			}
			public void onAccuracyChanged(Sensor sensor, int i) {
			}
		};


		// Volume Bar
		volumeBar = (SeekBar) findViewById(R.id.volumeBar);


		// Thread (Update positionBar & timeLabel)
		new Thread(new Runnable() {
			public void run() {
				while (mp != null) {
					try {
						Message msg = new Message();
						msg.what = mp.getCurrentPosition();
						handler.sendMessage(msg);
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
				}
			}
		}).start();

	}




	@Override
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(proximitySensorListener, proximitySensor,
				2 * 1000 * 1000);
	}

	@Override
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(proximitySensorListener);
	}


	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int currentPosition = msg.what;
			// Update positionBar.
			positionBar.setProgress(currentPosition);

			// Update Labels.
			String elapsedTime = createTimeLabel(currentPosition);
			elapsedTimeLabel.setText(elapsedTime);

			String remainingTime = createTimeLabel(totalTime-currentPosition);
			remainingTimeLabel.setText("- " + remainingTime);
		}
	};

	public String createTimeLabel(int time) {
		String timeLabel = "";
		int min = time / 1000 / 60;
		int sec = time / 1000 % 60;

		timeLabel = min + ":";
		if (sec < 10) timeLabel += "0";
		timeLabel += sec;

		return timeLabel;
	}

	public void playBtnClick(View view) {

		if (!mp.isPlaying()) {
			// Stopping
			mp.start();
			//playBtn.setBackgroundResource(R.drawable.stop);

		} else {
			// Playing
			mp.pause();
			//playBtn.setBackgroundResource(R.drawable.play);
		}

	}
}