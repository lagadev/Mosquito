package com.mosquitohunter.game;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class MainActivity extends Activity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        gameView = findViewById(R.id.gameView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.onActivityResume();
    }

    @Override
    protected void onPause() {
        gameView.onActivityPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        gameView.release();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!gameView.handleBack()) super.onBackPressed();
    }
}
