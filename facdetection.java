
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.camerakit.CameraKit;
import com.camerakit.CameraKitView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.util.List;

public class facedetection_activity extends AppCompatActivity {
    CameraKitView cameraKitView;
    FloatingActionButton capbtn;
    Bitmap bitmap;
    TextView showoutput;
    private static final int PICK_IMAGE_REQUEST = 121;
    FirebaseVisionFaceDetector detector;
    FirebaseVisionImage image;
    GraphicOverlay graphicOverlay;
    ProgressBar progressBar;
    ImageButton imageButton;
    boolean step2verification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facedetection_activity);
        cameraKitView = findViewById(R.id.camera);
        capbtn = findViewById(R.id.btn);
        showoutput = findViewById(R.id.output);
        progressBar = findViewById(R.id.progress_horizontal);
        graphicOverlay = findViewById(R.id.graphic_overlay);
        capbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decode();
            }
        });
        imageButton = findViewById(R.id.imgbtn);
        if (getIntent() != null) {
            String val = getIntent().getStringExtra("turnright");
            if (val != null) {
                step2verification = true;
            }
            showoutput.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cameraKitView.onResume();
                }
            });
        }

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraKitView.getFacing() == CameraKit.FACING_FRONT)
                    cameraKitView.setFacing(CameraKit.FACING_BACK);
                else
                    cameraKitView.setFacing(CameraKit.FACING_FRONT);
            }
        });

        if (step2verification) {
            fragmentreplace("key", "Step 2\n<---- turn right and take selfi");
        } else
            fragmentreplace("key", "To verify ,Take a selfi by smiling");

    }


    private void decode() {
        cameraKitView.captureImage(new CameraKitView.ImageCallback() {
            @Override
            public void onImage(CameraKitView cameraKitView, byte[] bytes) {
                cameraKitView.onStop();
                Bitmap itmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                bitmap = Bitmap.createScaledBitmap(itmap, cameraKitView.getWidth(), cameraKitView.getHeight(), false);
                image = FirebaseVisionImage.fromBitmap(bitmap);
                excutefacedetection();
//                MyAsyncTask task = new MyAsyncTask();
//                task.execute();

            }
        });
//        graphicOverlay.clear();
    }

    private void excutefacedetection() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseVisionFaceDetectorOptions highAccuracyOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
//                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)

                        .build();

        detector = FirebaseVision.getInstance().getVisionFaceDetector(highAccuracyOpts);

        Task<List<FirebaseVisionFace>> result = detector.detectInImage(image)

                .addOnSuccessListener(
                        new OnSuccessListener<List<FirebaseVisionFace>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionFace> faces) {
                                progressBar.setVisibility(View.GONE);

                                if (faces.size() == 0) {
                                    Toast.makeText(facedetection_activity.this, "Face not Found", Toast.LENGTH_SHORT).show();
                                    fragmentreplace("Key", "Face not found");
                                }

                                for (FirebaseVisionFace face : faces) {
                                    Toast.makeText(facedetection_activity.this, "reached", Toast.LENGTH_SHORT).show();

                                    // Landmarks

                                    StringBuilder stringBuilder = new StringBuilder();
                                    RectOverlay rect = new RectOverlay(graphicOverlay, face.getBoundingBox());
                                    graphicOverlay.add(rect);

                                    FirebaseVisionFaceLandmark leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);

                                    FirebaseVisionFaceLandmark rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
                                    FirebaseVisionFaceLandmark nose = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);
                                    FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
                                    FirebaseVisionFaceLandmark rightEar = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EAR);
                                    FirebaseVisionFaceLandmark leftMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT);
                                    FirebaseVisionFaceLandmark bottomMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM);
                                    FirebaseVisionFaceLandmark rightMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_RIGHT);
                                    // Classification
                                    float smilingProbability = face.getSmilingProbability();
                                    String textsmile = "Happiness:" + smilingProbability;
                                    if (!step2verification) {
                                        if (smilingProbability > 0.5)
                                            fragmentreplace("verified", "Verified with" + textsmile + "%");
                                        else
                                            fragmentreplace("key", "Smile PLease to verifiy");
                                    }

                                    stringBuilder.append(textsmile + "\n");

                                    float leftEyeOpenProbability = face.getLeftEyeOpenProbability();
                                    String lefteye = "lefteye open:" + leftEyeOpenProbability;
                                    stringBuilder.append(lefteye + "\n");

                                    float rightEyeOpenProbability = face.getRightEyeOpenProbability();
                                    String righteye = "righteye open:" + rightEyeOpenProbability;
                                    stringBuilder.append(righteye + "\n");
                                    //turning right
                                    Float right = face.getHeadEulerAngleY();
                                    if (right > 0 & step2verification) {
                                        Toast.makeText(facedetection_activity.this, "turned right", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }


                                    // Contours
                                    List<FirebaseVisionPoint> faceContours = face.getContour(FirebaseVisionFaceContour.FACE).getPoints();
                                    List<FirebaseVisionPoint> leftEyebrowTopContours = face.getContour(FirebaseVisionFaceContour.LEFT_EYEBROW_TOP).getPoints();
                                    List<FirebaseVisionPoint> leftEyebrowBottomContours = face.getContour(FirebaseVisionFaceContour.LEFT_EYEBROW_BOTTOM).getPoints();
                                    List<FirebaseVisionPoint> rightEyebrowTopContours = face.getContour(FirebaseVisionFaceContour.RIGHT_EYEBROW_TOP).getPoints();
                                    List<FirebaseVisionPoint> rightEyebrowBottomContours = face.getContour(FirebaseVisionFaceContour.RIGHT_EYEBROW_BOTTOM).getPoints();
                                    List<FirebaseVisionPoint> leftEyeContours = face.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints();
                                    List<FirebaseVisionPoint> rightEyeContours = face.getContour(FirebaseVisionFaceContour.RIGHT_EYE).getPoints();
                                    List<FirebaseVisionPoint> upperLipTopContours = face.getContour(FirebaseVisionFaceContour.UPPER_LIP_TOP).getPoints();
                                    List<FirebaseVisionPoint> upperLipBottomContours = face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).getPoints();
                                    List<FirebaseVisionPoint> lowerLipTopContours = face.getContour(FirebaseVisionFaceContour.LOWER_LIP_TOP).getPoints();
                                    List<FirebaseVisionPoint> lowerLipBottomContours = face.getContour(FirebaseVisionFaceContour.LOWER_LIP_BOTTOM).getPoints();
                                    List<FirebaseVisionPoint> noseBridgeContours = face.getContour(FirebaseVisionFaceContour.NOSE_BRIDGE).getPoints();
                                    List<FirebaseVisionPoint> noseBottomContours = face.getContour(FirebaseVisionFaceContour.NOSE_BOTTOM).getPoints();

//                                    bottomsheetfragment bottomsheet = new bottomsheetfragment(stringBuilder.toString());
//                                    bottomsheet.show(getSupportFragmentManager(), "output");
//                                    showoutput.setText(stringBuilder.toString());
                                }

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(facedetection_activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    private void fragmentreplace(String key, String text) {
        graphicOverlay.clear();
        Bundle bundle = new Bundle();
        bundle.putString(key, text);
        Fragment frame = new task_face_fragment();
        frame.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, frame);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();

        }

    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraKitView.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {
            // Your background method
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }
    }
}
