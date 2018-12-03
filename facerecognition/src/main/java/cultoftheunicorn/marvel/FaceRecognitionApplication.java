package cultoftheunicorn.marvel;

import android.app.Application;

public class FaceRecognitionApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException (Thread thread, Throwable e) {
                handleUncaughtException (thread, e);
            }
        });
    }

    public void handleUncaughtException (Thread thread, Throwable e) {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically

        System.exit(1); // kill off the crashed app
    }
}
