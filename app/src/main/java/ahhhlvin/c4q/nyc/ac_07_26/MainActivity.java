package ahhhlvin.c4q.nyc.ac_07_26;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;




public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    SensorManager mSensorManager;
    Button photoButton;
    Button videoButton;
    Button recordButton;
    Button playBackButton;
    TextView locationView;
    ImageView photoView;
    boolean isNotRecording;
    int CAPTURE_IMAGE = 0;
    int CAPTURE_VIDEO = 1;
    ContentResolver contentResolver;
    Uri uri;
    File mediaFile, audioFile;
    GoogleApiClient googleApiClient;
    Location location;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private static final float THRESHOLD = 2f;
    private float[] oldValues;
    private long oldTimestamp;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String mediaStorageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC).getPath();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        audioFile = new File(mediaStorageDir + File.separator + "AUD_" + timeStamp + ".wav");



        contentResolver = getContentResolver();


        photoButton = (Button) findViewById(R.id.photoButton);
        videoButton = (Button) findViewById(R.id.videoButton);
        recordButton = (Button) findViewById(R.id.recordButton);
        playBackButton = (Button) findViewById(R.id.playBack);
        locationView = (TextView) findViewById(R.id.locationView);
        photoView = (ImageView) findViewById(R.id.photoView);

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCameraPictureIntent();
            }
        });


        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCameraVideoIntent();
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNotRecording = true) {
                    mediaRecorder = new MediaRecorder();
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                    mediaRecorder.setOutputFile(audioFile.getPath());
                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    isNotRecording = false;
                } else {
                    mediaRecorder.stop();
//                    mediaRecorder.release();
                    isNotRecording = true;

                }
            }
        });



        playBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPlaying();
            }
        });

        mSensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(sensorEventListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);



    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        mSensorManager.unregisterListener(sensorEventListener);
//    }

    public void startPlaying() {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(audioFile.getPath()));
        mediaPlayer.setScreenOnWhilePlaying(true);
        mediaPlayer.start();
//        mediaPlayer.resume();
    }


    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // Handle sensor event

            float[] values = event.values;
            if (values[1] > THRESHOLD) {
                // Tilted up so scroll down
                Toast.makeText(getApplicationContext(), "SCROLLING DOWN!!!", Toast.LENGTH_SHORT).show();

            } else if (values[1] < -THRESHOLD) {
                // Tilted down so scroll up
                Toast.makeText(getApplicationContext(), "SCROLLING UP!!!", Toast.LENGTH_SHORT).show();

            } else {
                // Stop scrolling
            }



    }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Handle accuracy change event
        }
    };



    protected synchronized void connectGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        googleApiClient.connect();
        Log.d("Map", "Connected to Google API Client");
    }


    @Override
    public void onConnected(Bundle bundle) {
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        Log.d("OnConnected", "Performing handlenewlocation");
        if (location == null)
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, createLocationRequest(), new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            handleNewLocation(location);
                        }

                    });
        else
            handleNewLocation(location);
    }

    private LocationRequest createLocationRequest() {
        return new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(1);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("Map", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private void handleNewLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        geocodeTask.execute();
    }





    AsyncTask<Void, Void, Address> geocodeTask = new AsyncTask<Void, Void, Address>() {
        @Override
        protected Address doInBackground(Void... params) {
            Address address = null;

            Geocoder geocoder = new Geocoder(MainActivity.this);
            try {
                List<Address> locations = geocoder.getFromLocation(
                        location.getLatitude(), location.getLongitude(), 1 /* maxResults */);
                address = locations.get(0);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return address;
        }

        @Override
        protected void onPostExecute(Address address) {
            locationView.setText(address.getAddressLine(0) + ", " + address.getSubAdminArea() + " "
                    + address.getAdminArea() + " " + address.getPostalCode());
        }
    };






    //////////////////////////////////////////////////////////////////////////

    private void startCameraPictureIntent() {

        // Make destination image URI
        String mediaStorageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).getPath();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        mediaFile = new File(mediaStorageDir + File.separator + "IMG_" + timeStamp + ".jpg");

        // Send intent to take picture
        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mediaFile));
        startActivityForResult(cameraIntent, CAPTURE_IMAGE);



    }

    private void startCameraVideoIntent() {

        // Send intent to take picture
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(videoIntent, CAPTURE_VIDEO);
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE) {
            Uri uri = Uri.parse(mediaFile.getAbsolutePath());
            photoView.setImageURI(uri);
            connectGoogleApiClient();
        }


    }



}
