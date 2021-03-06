package com.gunmachan.game;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import android.content.pm.PackageManager;

import com.badlogic.gdx.Gdx;
import com.github.zagum.speechrecognitionview.RecognitionProgressView;
import com.github.zagum.speechrecognitionview.adapters.RecognitionListenerAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.gunmachan.SQLite.*;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import asu.gunma.GunmaChan;
import asu.gunma.speech.ActionResolver;
import com.badlogic.gdx.assets.AssetManager;

import asu.gunma.DatabaseInterface.DbInterface;
import asu.gunma.DbContainers.VocabWord;
import asu.gunma.ui.screen.menu.SettingsScreen;

    public class AndroidLauncher extends AndroidApplication {

        /*public VocabDb androidDB;
        Button buttonStart, buttonStop;
        String AudioSavePathInDevice = null;
        MediaRecorder mediaRecorder;
        MediaPlayer mediaPlayer;*/
        public AssetManager assetManager;
        public VocabDb androidDB;
        protected DbInterface dbInterface;
        public View view;
        public static final int REQUEST_SPEECH = 0;
        public SpeechRecognizer speechRecognizer;
        public ActionResolver callback;
        protected String sendWord;
        public View decorView;
        public int uiOptions;
        private AndroidApplicationConfiguration config;
        private static final int RC_SIGN_IN = 100;
        private Context context;

        // add permission to hide navigation bar?
        // create button to exit to home screen under instructor menu
        String[] perms = {"android.permission.RECORD_AUDIO", "android.permission.INTERNET",
                "android.permission.WRITE_EXTERNAL_STORAGE"};
        int permsRequestCode = 200;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            hideNavigationBar();
            config = new AndroidApplicationConfiguration();

            //DEFAULT_SIGN_IN will request user ID, email address, and profile
            GoogleSignInOptions gso =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();

            //creating sign in object with options specified by gso
            final GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


            AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();


            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

            final RecognitionProgressView recognitionProgressView = (RecognitionProgressView) findViewById(R.id.recognition_view);

            recognitionProgressView.setSpeechRecognizer(speechRecognizer);

            recognitionProgressView.setRecognitionListener(new RecognitionListenerAdapter() {
                @Override
                public void onResults(Bundle results) {
                    showResults(results);
                }
            });

            callback = new ActionResolver() {
                @Override

                //method that starts the Google login client
                public void signIn()
                {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                }

                public void startRecognition() {

                    try {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("Start Recognition");
                                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");
                                speechRecognizer.startListening(intent);
                                System.out.println("End Recognition");
                            }
                        });
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }

                public String getWord() {
                    return sendWord;
                }
            };

            dbInterface = new DbInterface() {
                public List<VocabWord> getDbVocab(){return androidDB.viewDb();}
            };
            initialize(new GunmaChan(callback, dbInterface),config);
            if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M){
                    requestPermissions(perms, permsRequestCode);
            }
            androidDB = newDb();
            test(androidDB);
            androidDB.viewDb();
            if(isFinishing()){
                System.out.println("Hit2");
                this.deleteDatabase("AppDb");
            }
        }

        public void showResults(Bundle results) {
            ArrayList<String> matches = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            Toast.makeText(this, matches.get(0), Toast.LENGTH_LONG).show();
            sendWord = matches.get(0);
        }

        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_SPEECH && resultCode == RESULT_OK) {
                // Get the spoken sentence..
                ArrayList<String> thingsYouSaid =
                        data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                // ..and pass it to the textField:
                //speechGDX.setTextFieldText(thingsYouSaid.get(0));
                Gdx.app.log("you said: ", thingsYouSaid.get(0));
            }
        }

        @Override
        public void onResume(){
            super.onResume();
            hideNavigationBar();
        }

        @Override
        public void onPause(){
            super.onPause();
            this.onResume();
            hideNavigationBar();
        }

        @Override
        public void onBackPressed() {
            hideNavigationBar();
        }


        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_HOME) {
                hideNavigationBar();
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }

        /*
         * Called as part of the activity lifecycle when an activity is about to go into the background
         * as the result of user choice. For example, when the user presses the Home key, onUserLeaveHint() will be called,
         * but when an incoming phone call causes the in-call Activity to be automatically brought to the foreground,
         * onUserLeaveHint() will not be called on the activity being interrupted.
         * In cases when it is invoked, this method is called right before the activity's onPause() callback.
         */
        @Override
        protected void onUserLeaveHint() {
            super.onUserLeaveHint();
            /*setContentView(R.layout.activity_main);
            initialize(new GunmaChan(callback, dbInterface),config);
            if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M){
                requestPermissions(perms, permsRequestCode);
            }*/
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
        }

        @Override
        public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
            switch (permsRequestCode) {
                case 200:
                    boolean RecordingAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean InternetAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean ExternalAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    break;
            }
        }

        public void test(VocabDb vDB) {
            try {
                vDB.importCSV("Numbers.csv");
                vDB.importCSV("Colors-Shapes.csv");
                vDB.importCSV("Countries.csv");
                vDB.importCSV("Days-Months.csv");
                vDB.importCSV("Feelings.csv");
                vDB.importCSV("Subjects.csv");
                vDB.importCSV("Fruits-Foods.csv");
                vDB.importCSV("Professions.csv");
                vDB.importCSV("Places.csv");
                vDB.importCSV("Time.csv");
            } catch(Exception e){
                System.out.println(e);
            }
            List<VocabWord> currentDb = vDB.viewDb();
            for(VocabWord element : currentDb){
                System.out.println("KANJI: " + element.getKanjiSpelling());
                System.out.println("KANA: " + element.getKanaSpelling());
                System.out.println("ENG: " + element.getEngSpelling());
                System.out.println("Module: " + element.getModuleCategory());
                System.out.println("Correct Word: " + element.getCorrectWords());
            }
        }

        public VocabDb newDb() {
            VocabDb testDb = new VocabDb(AndroidLauncher.this);
            return testDb;
        }


        private void hideNavigationBar(){
            decorView = getWindow().getDecorView();
            uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

