package com.example.qrgenerator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.CAMERA;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText name, email, dob, address;
    DatePickerDialog fromDatePickerDialog;
    SimpleDateFormat dateFormatter;
    Button selectImage,generateQR, shareQR;
    CircleImageView imageView;
    ImageView qrImage;
    String nameHolder, emailHolder, dobHolder, addressHolder;

    //keep track of camera capture intent
    final int CAMERA_CAPTURE = 1;
    //keep track of gallery intent
    final int PICK_IMAGE_REQUEST = 2;
    //keep track of cropping intent
    final int PIC_CROP = 3;

    //captured picture uri
    File storeDirectory,f;
    String ConvertImage;
    JSONObject json_data;

    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final String IMAGE_DIRECTORY = "/qrgenerator/users";
    private static final String QR_DIRECTORY = "/qrgenerator";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        storeDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!storeDirectory.exists()) {
            storeDirectory.mkdirs();
        }

        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        findViewsById();
        setDateTimeField();
    }

    private void findViewsById() {
        name = findViewById(R.id.name1);
        email= findViewById(R.id.email1);

        dob = findViewById(R.id.dob1);
        //dob.setInputType(InputType.TYPE_NULL);
        dob.setOnClickListener(this);
        address = findViewById(R.id.address1);
        selectImage = findViewById(R.id.selectimage1);
        selectImage.setOnClickListener(this);

        imageView = findViewById(R.id.imageview);

        generateQR = findViewById(R.id.generateQR);
        generateQR.setOnClickListener(this);

        shareQR = findViewById(R.id.shareQR);
        shareQR.setOnClickListener(this);

        qrImage = findViewById(R.id.qrimage);
    }

    private void setDateTimeField() {

        Calendar newCalendar = Calendar.getInstance();
        fromDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                dob.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    public boolean isEmpty() {
        nameHolder = name.getText().toString();
        emailHolder = email.getText().toString();
        dobHolder = dob.getText().toString();
        addressHolder = address.getText().toString();

        int flag = 0;
        if (nameHolder.matches("")) {
            // name.setError("please enter first name");
            flag=1;
        }

        if (emailHolder.matches("")) {
            //email.setError("please enter last name");
            flag=1;
        }

//        if (dobHolder.matches("")) {
//            dob.setError("please select dob");
//        }

        if (addressHolder.matches("")) {
            // address.setError("please enter address");
            flag=1;
        }

        if(ConvertImage==null)
            flag=1;

        if(flag==1)
            return  true;
        else
            return false;
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, CAMERA}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean readaccess = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeaccess = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (readaccess && writeaccess && cameraAccepted)
                    {

                    }
                    else {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
                                showMessageOKCancel("You need to allow access to all the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, CAMERA},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_CAPTURE) {

                //create instance of File with same name we created before to get image from storage
                File file = new File(Environment.getExternalStorageDirectory()+ IMAGE_DIRECTORY +File.separator +  "temp_img.jpg");

                Uri cameraPicUri = FileProvider.getUriForFile(getApplicationContext(),
                        "com.example.mosip.fileprovider", file);

                Bitmap cameraPic = null;
                try {
                    cameraPic = MediaStore.Images.Media.getBitmap(this.getContentResolver(), cameraPicUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(cameraPic);
                //cropImage(cameraPicUri);
                //Toast.makeText(MainActivity.this, "Image Saved!", Toast.LENGTH_LONG).show();
            }

        } if(requestCode == PICK_IMAGE_REQUEST) {
            if(data!=null) {
                Uri gallerypicUri = data.getData();
                Bitmap galleryPic = null;
                try {
                    galleryPic = MediaStore.Images.Media.getBitmap(this.getContentResolver(), gallerypicUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(galleryPic);
                saveImage(galleryPic);
            }
        }
    }

    public String saveImage(Bitmap myBitmap) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 64, bytes);
        imageView.setImageBitmap(myBitmap);
        byte[] byteArrayVar = bytes.toByteArray();

        ConvertImage = Base64.encodeToString(byteArrayVar, Base64.DEFAULT);
        Log.d("Base 64", ConvertImage);
        System.out.println("Base 64 image length : " + ConvertImage.length());

        try {
            String file_name = nameHolder + ".jpg";
            f = new File(storeDirectory,file_name);
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "Image Saved to : " + f.getAbsolutePath());
            Toast.makeText(this, "Image Saved to : "+ f.getAbsolutePath() , Toast.LENGTH_LONG).show();
            return f.getAbsolutePath();
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    public String saveQR(Bitmap myBitmap) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        byte[] byteArrayVar = bytes.toByteArray();

        String  ConvertQR = Base64.encodeToString(byteArrayVar, Base64.DEFAULT);

        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + QR_DIRECTORY);
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        String qr_name = null;
        try {
            qr_name = json_data.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            File f = new File(wallpaperDirectory, qr_name + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();

            Toast.makeText(this, "QR Saved to : "+ f.getAbsolutePath() , Toast.LENGTH_LONG).show();
            return f.getAbsolutePath();
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    //Method to share any image.
    private void shareImage() {
        Intent share = new Intent(Intent.ACTION_SEND);

        // If you want to share a png image only, you can do:
        // setType("image/png"); OR for jpeg: setType("image/jpeg");
        share.setType("image/*");
        // Make sure you put example png image named myImage.png in your
        // directory
        String imagePath = null;
        try {
            imagePath = Environment.getExternalStorageDirectory() +QR_DIRECTORY+"/"+
                    json_data.getString("name")  +".jpg";
        } catch (JSONException e) {
            e.printStackTrace();
        }

        File imageFileToShare = new File(imagePath);

        Uri uri = Uri.fromFile(imageFileToShare);
        share.putExtra(Intent.EXTRA_STREAM, uri);

        startActivity(Intent.createChooser(share, "Share Image!"));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dob1:
                fromDatePickerDialog.show();
                break;
            case R.id.selectimage1:
                if (checkPermission()) {
                    AlertDialog.Builder pictureDialog = new AlertDialog.Builder(MainActivity.this);
                    pictureDialog.setTitle("Select Photo From");
                    String[] pictureDialogItems = {
                            "Camera",
                            "Gallery",
                            "Remove Photo"};
                    pictureDialog.setItems(pictureDialogItems,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            try {
                                                File file = new File(Environment.getExternalStorageDirectory()+ IMAGE_DIRECTORY +File.separator  +  "temp_img.jpg");

                                                Uri outputFileUri = FileProvider.getUriForFile(getApplicationContext(),
                                                        "com.example.mosip.fileprovider", file);

                                                //use standard intent to capture an image
                                                Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");

                                                /*create instance of File with name temp_img.jpg*/
                                                /*put uri as extra in intent object*/
                                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                                                takePictureIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                                                /*start activity for result pass intent as argument and request code */
                                                startActivityForResult(takePictureIntent, CAMERA_CAPTURE);
                                                Log.d("outputFileUri",outputFileUri.toString());
                                            }
                                            catch(ActivityNotFoundException anfe){
                                                //display an error message
                                                String errorMessage = "Your device doesn't support capturing images!";
                                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                            }
                                            break;
                                        case 1:
                                            Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                            // Start the Intent
                                            galleryIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                                            /*start activity for result pass intent as argument and request code */
                                            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
                                            break;
                                        case 2:
                                            imageView.setImageBitmap(null);
                                            ConvertImage=null;
                                            qrImage.setImageBitmap(null);
                                            break;
                                    }
                                }
                            });
                    pictureDialog.show();
                }
                else {
                    requestPermission();
                }
                break;
            case R.id.generateQR:
                if(!isEmpty()) {
                    json_data = new JSONObject();
                    try {
                        json_data.put("name", nameHolder);
                        json_data.put("email", emailHolder);
                        json_data.put("dob", dobHolder);
                        json_data.put("address", addressHolder);
                        Log.d("", ConvertImage);
                        json_data.put("image", ConvertImage);

                        Log.d("json_data", json_data.toString());
                        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                        try {
                            Log.d("json data", String.valueOf(json_data));
                            BitMatrix bitMatrix = multiFormatWriter.encode(String.valueOf(json_data), BarcodeFormat.QR_CODE, 400, 400);
                            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                            saveQR(bitmap);
                            qrImage.setImageBitmap(bitmap);
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                else
                    Toast.makeText(MainActivity.this, "Please enter all details", Toast.LENGTH_LONG).show();
                break;
            case R.id.shareQR:
                if(qrImage.getDrawable()!=null && !isEmpty())
                    shareImage();
                else
                    Toast.makeText(MainActivity.this, "Please Generate QR first", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }
}