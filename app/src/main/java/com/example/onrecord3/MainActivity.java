package com.example.onrecord3;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.*;
import android.os.AsyncTask;
import android.view.View;
import android.graphics.Color;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.developer_cloud.android.library.camera.CameraHelper;
import com.ibm.watson.developer_cloud.android.library.camera.GalleryHelper;
import com.ibm.watson.developer_cloud.language_translator.v3.LanguageTranslator;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslationResult;
import com.ibm.watson.developer_cloud.language_translator.v3.util.Language;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.SynthesizeOptions;



import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private String Tag = "MainActivity";

    private EditText input;
    private ImageButton mic;
    private Button translate;
    private ImageButton play;
    private EditText translatedText;
    private ImageView loadedImage;


    private RecognizeOptions recognizeOptions;

    private SpeechToText speechService;
    private TextToSpeech textService;
    private LanguageTranslator translatorService;
    private String selectedTargetLanguage = Language.SPANISH;

    private StreamPlayer player = new StreamPlayer();

    private CameraHelper cameraHelper;
    private GalleryHelper galleryHelper;
    private MicrophoneHelper microphoneHelper;

    private MicrophoneInputStream capture;
    private boolean listening = false;

    private ArrayList<Card> myValues;
    private MyAdapter adapter;
    private RecyclerView myView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);


        myValues = new ArrayList<Card>();
        final Card current = new Card();

        myView = (RecyclerView) findViewById(R.id.recyclerView);
        myView.setHasFixedSize(true);

        final LinearLayoutManager llm;
        llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(RecyclerView.VERTICAL);
        myView.setLayoutManager(llm);

        FloatingActionButton myFab = (FloatingActionButton)  findViewById(R.id.fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myValues.add(current);
                adapter.notifyDataSetChanged();
            }
        });


        adapter = new MyAdapter(this, myValues);
        myView.setAdapter(adapter);

        adapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position){
                /*Card card = myValues.get(position);
                if (card.getTranscribe() == null && card.getCv() == null) {
                    recordMessage();
                    adapter.notifyDataSetChanged();

                }*/
            }
            @Override
            public void onItemRecord(int position) {
                Card card = myValues.get(position);
                if (card.getTranscribe() == null && card.getCv() == null) {
                    recordMessage();
                    adapter.notifyDataSetChanged();

                }
            }
        });
    }

    // Record a message via Watson Speech-to-Text
    private void recordMessage() {

        cameraHelper = new CameraHelper(this);
        galleryHelper = new GalleryHelper(this);
        microphoneHelper = new MicrophoneHelper(this);


        speechService = new SpeechToText();
        speechService = initSpeechToTextService();
        input = findViewById(R.id.cardViewText);
        mic = findViewById(R.id.mic);

        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!listening) {
                    // Update the icon background
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mic.setBackgroundColor(Color.WHITE);
                        }
                    });
                    capture = microphoneHelper.getInputStream(true);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                speechService.recognizeUsingWebSocket(getRecognizeOptions(capture),
                                        new MicrophoneRecognizeDelegate());
                            } catch (Exception e) {
                                showError(e);
                            }
                        }
                    }).start();
                    listening = true;
                    Toast.makeText(MainActivity.this, "Listening...Click to Stop", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        microphoneHelper.closeInputStream();
                        listening = false;
                        Toast.makeText(MainActivity.this, "Stopped Listening...Click to Start", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    private void showTranslation(final String translation) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                translatedText.setText(translation);
            }
        });
    }

    private void showError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                // Update the icon background
                mic.setBackgroundColor(Color.LTGRAY);
            }
        });
    }

    private void showMicText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                input.setText(text);
            }
        });
    }

    private void enableMicButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mic.setEnabled(true);
            }
        });
    }

    private SpeechToText initSpeechToTextService() {
        SpeechToText service = new SpeechToText();
        String username = getString(R.string.apikey);
        String password = getString(R.string.speech_text_apiKey);
        service.setUsernameAndPassword(username, password);
        service.setEndPoint(getString(R.string.speech_text_url));
        return service;
    }

    /*private TextToSpeech initTextToSpeechService() {
        TextToSpeech service = new TextToSpeech();
        String username = getString(R.string.text_speech_username);
        String password = getString(R.string.text_speech_password);
        service.setUsernameAndPassword(username, password);
        service.setEndPoint(getString(R.string.text_speech_url));
        return service;
    }*/

    /*private LanguageTranslator initLanguageTranslatorService() {
        LanguageTranslator service = new LanguageTranslator("2018-05-01");
        String username = getString(R.string.language_translator_username);
        String password = getString(R.string.language_translator_password);
        service.setUsernameAndPassword(username, password);
        service.setEndPoint(getString(R.string.language_translator_url));
        return service;
    }*/

    private RecognizeOptions getRecognizeOptions(InputStream captureStream) {
        return new RecognizeOptions.Builder()
                .audio(captureStream)
                .contentType(ContentType.OPUS.toString())
                .model("en-US_BroadbandModel")
                .interimResults(true)
                .inactivityTimeout(2000)
                .build();
    }

private abstract class EmptyTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    // assumes text is initially empty
    private boolean isEmpty = true;

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() == 0) {
            isEmpty = true;
            onEmpty(true);
        } else if (isEmpty) {
            isEmpty = false;
            onEmpty(false);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    public abstract void onEmpty(boolean empty);
}

private class MicrophoneRecognizeDelegate extends BaseRecognizeCallback {
    @Override
    public void onTranscription(SpeechRecognitionResults speechResults) {
        System.out.println(speechResults);
        if (speechResults.getResults() != null && !speechResults.getResults().isEmpty()) {
            String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
            showMicText(text);
        }
    }

    @Override
    public void onError(Exception e) {
        try {
            // This is critical to avoid hangs
            // (see https://github.com/watson-developer-cloud/android-sdk/issues/59)
            capture.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        showError(e);
        enableMicButton();
    }

    @Override
    public void onDisconnected() {
        enableMicButton();
    }
}

    /*private class TranslationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            TranslateOptions translateOptions = new TranslateOptions.Builder()
                    .addText(params[0])
                    .source(Language.ENGLISH)
                    .target(selectedTargetLanguage)
                    .build();
            TranslationResult result = translationService.translate(translateOptions).execute();
            String firstTranslation = result.getTranslations().get(0).getTranslationOutput();
            showTranslation(firstTranslation);
            return "Did translate";
        }
    }*/

private class SynthesisTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
        SynthesizeOptions synthesizeOptions = new SynthesizeOptions.Builder()
                .text(params[0])
                .voice(SynthesizeOptions.Voice.EN_US_LISAVOICE)
                .accept(SynthesizeOptions.Accept.AUDIO_WAV)
                .build();
        player.playStream(textService.synthesize(synthesizeOptions).execute());
        return "Did synthesize";
    }
}

    /**
     * On request permissions result.
     *
     * @param requestCode  the request code
     * @param permissions  the permissions
     * @param grantResults the grant results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case CameraHelper.REQUEST_PERMISSION: {
                // permission granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraHelper.dispatchTakePictureIntent();
                }
            }
            case MicrophoneHelper.REQUEST_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission to record audio denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * On activity result.
     *
     * @param requestCode the request code
     * @param resultCode  the result code
     * @param data        the data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CameraHelper.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            loadedImage.setImageBitmap(cameraHelper.getBitmap(resultCode));
        }

        if (requestCode == GalleryHelper.PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            loadedImage.setImageBitmap(galleryHelper.getBitmap(resultCode, data));
        }
    }
}



//implementation 'com.ibm.watson.developer_cloud:speech-to-text:3.7.0'
//implementation 'com.ibm.watson.developer_cloud:android-sdk:0.5.0'