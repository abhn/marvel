package cultoftheunicorn.marvel.utilities;

import org.opencv.android.OpenCVLoader;

/**
 * Methods to frequently performed actions with regards to OpenCV library
 */
public class OpenCV {

    /**
     * Checks if the opencv library has loaded successfully
     * @return true if loaded, false otherwise
     *
     */
    public static boolean checkOpenCVLoaded() {
        return OpenCVLoader.initDebug();
    }

}
