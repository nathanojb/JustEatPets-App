package me.jonathanburton.apps.justeatpets;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "JUSTEATYOURPETS";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Button shootButton;
    ImageView viewImage;
    EditText editText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shootButton = (Button) findViewById(R.id.launch_camera_button);
        viewImage = (ImageView) findViewById(R.id.imageView);
        editText = (EditText) findViewById(R.id.editText);
    }

    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            viewImage.setImageBitmap(imageBitmap);
            storeImage(imageBitmap);

            Log.d(TAG, "Finished Saving image, about to ask clarfai");

            clarifyShizzle c = new clarifyShizzle();
            c.execute();
        }
    }





    class clarifyShizzle extends AsyncTask<Void, Void, Integer> {


        List<RecognitionResult> results;

        @Override
        protected Integer doInBackground(Void... params) {
            Log.i(TAG, "do in background started");
            ClarifaiClient clarifai = new ClarifaiClient(credentials.CLIENT_ID, credentials.CLIENT_SECREt);
            File theImage = new File(Environment.getExternalStorageDirectory()
                    + "/Android/data/"
                    + getApplicationContext().getPackageName()
                    + "/Files" + File.separator + "test.jpg");

            Log.i(TAG, "Got image succesfully");
            try {
                results = clarifai.recognize(new RecognitionRequest(theImage));
            }

            catch (Exception e) {
                Log.i(TAG, "clarifi request caused exception");
                Log.i(TAG, e.getMessage());
            }


            Integer i = 1;
            return i;
        }

        protected void onProgressUpdate(Void... progress) {
            editText.setText(results.get(0).getStatusMessage());
        }

        protected void onPostExecute(Integer... ints) {
            editText.setText(results.get(0).getStatusMessage());
        }

    }


    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        File mediaFile;
        String mImageName = "temp" + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }


}
