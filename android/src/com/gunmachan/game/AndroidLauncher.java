package com.gunmachan.game;

import android.os.Bundle;


import android.media.MediaPlayer;
import android.media.MediaRecorder;

import android.os.Environment;
import android.view.View;

import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;

import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import asu.gunma.GunmaChan;
import asu.gunma.speech.SpeechInputHandler;

import android.util.Log;



public class AndroidLauncher extends AndroidApplication {

	@Override
	public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){

		switch(permsRequestCode){

			case 200:

				boolean RecordingAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
				boolean InternetAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;

				break;

		}

	}

	Button buttonStart, buttonStop;
	String AudioSavePathInDevice = null;
	MediaRecorder mediaRecorder;
	MediaPlayer mediaPlayer;
	String[] perms = {"android.permission.RECORD_AUDIO", "android.permission.INTERNET" };
	int permsRequestCode = 200;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new GunmaChan(), config);
		requestPermissions(perms, permsRequestCode);
	}

}
