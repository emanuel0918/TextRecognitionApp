package com.example.textrecognitionapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public final int REQUEST_OPEN_IMAGE=203;
    public final int REQUEST_CAMERA_PERMISSION = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    public final int REQUEST_IMAGE_CAPTURE = 1;

    private Bitmap bitmapToRecognize;

    private Button captureImageBtn, browseBtn, detectTextBtn, rotateBtn;
    private ImageView imageView;
    private TextView textView;
    String currentPhotoPath;
    String ocr_string;
    int rotationDegree;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rotationDegree = 0;
        captureImageBtn = findViewById(R.id.capture_image);
        browseBtn = findViewById(R.id.read_image);
        detectTextBtn = findViewById(R.id.detect_text_image);
        rotateBtn = findViewById(R.id.rotate_image);
        imageView = findViewById(R.id.image_view);
        textView = findViewById(R.id.text_display);
        captureImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				int permission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
				if (permission == PackageManager.PERMISSION_GRANTED) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
				} else {
				    ActivityCompat.requestPermissions(MainActivity.this,
                            new String []{ Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION
                    );
				}
            }
        });
        browseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permission1 = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                int permission2 = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent();
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/jpg","image/png"});
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent,REQUEST_OPEN_IMAGE);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String []{
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            },
                            REQUEST_OPEN_IMAGE
                    );
                }
            }
        });
        detectTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectTextBtn.setEnabled(false);
                detectTextFromImage();
            }
        });
        rotateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImage();

            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImage();
            }
        });
    }

    private void detectTextFromImage() {
        // [START get_detector_default]
        TextRecognizer recognizer = TextRecognition.getClient();
        // [END get_detector_default]
        ocr_string="";
        // [START run_detector]
        Task<Text> result =
                recognizer.process(InputImage.fromBitmap(bitmapToRecognize, 0))
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // Task completed successfully
                                // [START_EXCLUDE]
                                // [START get_text]
                                for (Text.TextBlock block : visionText.getTextBlocks()) {
                                    Rect boundingBox = block.getBoundingBox();
                                    Point[] cornerPoints = block.getCornerPoints();
                                    String text = block.getText();
                                    ocr_string+= text+"\n";
                                }
                                textView.setText(ocr_string);
                                Log.v("pretty_exact","\nOCR:\n"+ocr_string);
                                // [END get_text]
                                // [END_EXCLUDE]
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });
        // [END run_detector]
    }

//    private void dispatchTakePictureIntent(int origen) {
//
//        bitmapToRecognize = null;
//        detectTextBtn.setEnabled(false);
//        switch (origen) {
//            case REQUEST_IMAGE_CAPTURE:
//                break;
//            case REQUEST_OPEN_IMAGE:
//                Intent intent = new Intent();
//                intent.setType("*/*");
//                intent.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/jpg","image/png"});
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                startActivityForResult(intent,REQUEST_OPEN_IMAGE);
//                break;
//            default:
//                return;
//        }
//    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void rotateImage() {
        if (bitmapToRecognize!= null) {
            switch (rotationDegree) {
                case 0:
                    rotationDegree = 90;
                    break;
                case 90:
                    rotationDegree = 180;
                    break;
                case 180:
                    rotationDegree = 270;
                    break;
                case 270:
                    rotationDegree = 0;
                    break;
            }
            Matrix matrix = new Matrix();

            matrix.postRotate(rotationDegree);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapToRecognize, bitmapToRecognize.getWidth(), bitmapToRecognize.getHeight(), true);

            bitmapToRecognize = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
            imageView.setImageBitmap(bitmapToRecognize);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_OPEN_IMAGE) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    if (resultCode == RESULT_OK) {
                        Bundle extras = data.getExtras();
                        bitmapToRecognize = (Bitmap) extras.get("data");
                        detectTextBtn.setEnabled(true);
                        rotationDegree = 90;
                        Matrix matrix = new Matrix();

                        matrix.postRotate(rotationDegree);
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapToRecognize, bitmapToRecognize.getWidth(), bitmapToRecognize.getHeight(), true);

                        bitmapToRecognize = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                        imageView.setImageBitmap(bitmapToRecognize);
                    } else {
                        detectTextBtn.setEnabled(false);
                    }
                    break;
                case REQUEST_OPEN_IMAGE:
                    if (resultCode == Activity.RESULT_OK) {
                        InputStream stream = null;
                        try {
                            stream = getContentResolver().openInputStream(data.getData());
                            Log.v("pretty_exact", data.getData().getPath());
                            bitmapToRecognize = BitmapFactory.decodeStream(stream);
                            detectTextBtn.setEnabled(true);
                            imageView.setImageBitmap(bitmapToRecognize);
                        } catch (Exception e) {

                        } finally {
                            if (stream != null) {
                                try {
                                    stream.close();
                                } catch (Exception e) {

                                }
                            }
                        }
                    } else {
                        detectTextBtn.setEnabled(false);
                    }
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                } else {
                    //Toast.makeText(MainActivity.this, "Permission denied to use the camera", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_OPEN_IMAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent();
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/jpg","image/png"});
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent,REQUEST_OPEN_IMAGE);
                } else {
                    //Toast.makeText(MainActivity.this, "Permission denied to use the camera", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }


}
