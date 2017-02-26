package cultoftheunicorn.marvel;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;

import cultoftheunicorn.marvel.utilities.*;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(OpenCV.checkOpenCVLoaded()) {
            Log.d(TAG, "Loaded");
        } else {
            Log.d(TAG, "Not Loaded");
        }

        AppCompatButton btn = (AppCompatButton) findViewById(R.id.openCamera);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ShowCamera.class);
                startActivity(intent);
            }
        });

    }
}
