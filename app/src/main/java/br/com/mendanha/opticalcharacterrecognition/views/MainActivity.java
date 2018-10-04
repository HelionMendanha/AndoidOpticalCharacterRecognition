package br.com.mendanha.opticalcharacterrecognition.views;


import android.Manifest;
import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Layout;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import br.com.mendanha.opticalcharacterrecognition.R;

public class MainActivity extends AppCompatActivity  implements  View.OnClickListener{

    //private Button selectButton,roteteL,roteteR;
    //private FloatingActionButton fotoFAB;
    private Bitmap currentlBitmap;
    private TextView resultadoTXT;
    private ImageView image,camStart,galeryStart,rotateLeft,rotateRight;
    private int typeRequest;
    private ConstraintLayout constraintLayoutBar;

    private CardView cardViewImage;
    private ViewGroup.LayoutParams defaultLayoutCardViewImage;
    private int defaultHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        camStart = findViewById(R.id.camStart);
        camStart.setOnClickListener(this);

        galeryStart = findViewById(R.id.galeryStart);
        galeryStart.setOnClickListener(this);

        rotateLeft = findViewById(R.id.rotateLeft);
        rotateLeft.setOnClickListener(this);

        rotateRight = findViewById(R.id.rotateRight);
        rotateRight.setOnClickListener(this);

        rotateRight.setVisibility(View.GONE);
        rotateLeft.setVisibility(View.GONE);

        resultadoTXT = findViewById(R.id.resultadoTXT);
        resultadoTXT.setOnClickListener(this);

        image = findViewById(R.id.image);
        image.setOnClickListener(this);

        cardViewImage = findViewById(R.id.cardViewImage);
        defaultLayoutCardViewImage = cardViewImage.getLayoutParams();
        defaultHeight = defaultLayoutCardViewImage.height;

        constraintLayoutBar = findViewById(R.id.constraintLayoutBar);
    }

    void openCamera(){

        typeRequest = 1;
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        } else {

            PackageManager pm = getApplicationContext().getPackageManager();

            if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // Caso eu consiga abrir a camera
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, 1);
                }

            } else {
                Toast.makeText(getApplicationContext(), "Você não tem uma câmera disponível", Toast.LENGTH_LONG).show();
            }
        }
    }

    void openGallery(){

        typeRequest = 2;
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else{

            Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(gallery, 1);
        }
    }

    @Override
    public void onClick(View view) {

        AlphaAnimation fadeAnimation = new AlphaAnimation(0,1);
        fadeAnimation.setDuration(1000);
        fadeAnimation.setStartOffset(100);

        if(view.getId() == R.id.rotateLeft){

            rotete(90);
        }else if(view.getId() == R.id.rotateRight){

            rotete(-90);
        }else if(view.getId() == R.id.galeryStart){

            openGallery();
        }else if(view.getId() == R.id.camStart ){

            openCamera();
        }else if(view.getId() == R.id.image){

            constraintLayoutBar.setVisibility(View.VISIBLE);
            constraintLayoutBar.setAnimation(fadeAnimation);
            
            defaultLayoutCardViewImage.height = defaultHeight;
            cardViewImage.setLayoutParams(defaultLayoutCardViewImage);
        }else if(view.getId() == R.id.resultadoTXT){

            constraintLayoutBar.setVisibility(View.GONE);
            constraintLayoutBar.clearAnimation();

            defaultLayoutCardViewImage.height = 300;
            cardViewImage.setLayoutParams(defaultLayoutCardViewImage);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){

            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                if(typeRequest == 2) {

                    openGallery();
                }else{

                    openCamera();
                }
            }else{
                Toast.makeText(getApplicationContext(),"Infelizmente não foi possivel acessar a camera para tirar um foto, pois você não deu permissão :(", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(typeRequest == 1 && requestCode == 1 && resultCode == RESULT_OK){

            Toast.makeText(
                    getApplicationContext(),getResources().getString(R.string.receive_photo_message),
                    Toast.LENGTH_LONG)
                    .show();

            currentlBitmap = data.getParcelableExtra("data");

            image.setImageBitmap(currentlBitmap);

            setButtons();
            detectOCR();

        }else if(typeRequest == 2 && requestCode == 1){

            Uri imagePath = data.getData();
            try{
                currentlBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
                image.setImageBitmap(currentlBitmap);
                setButtons();
                detectOCR();
            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }

    private void setButtons(){

        //getSupportActionBar().hide();

        AlphaAnimation fadeAnimation = new AlphaAnimation(0,1);
        fadeAnimation.setDuration(1000);
        fadeAnimation.setStartOffset(100);

        rotateRight.setVisibility(View.VISIBLE);
        rotateRight.setAnimation(fadeAnimation);
        rotateLeft.setVisibility(View.VISIBLE);
        rotateLeft.setAnimation(fadeAnimation);
    }

    private void detectOCR(){

        TextRecognizer txtRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!txtRecognizer.isOperational()){

            Toast.makeText(
                    getApplicationContext(),
                    getResources().getString(R.string.receive_photo_error_ocr_detector),
                    Toast.LENGTH_LONG)
                    .show();
            return;

        }else {

            // Set the bitmap taken to the frame to perform OCR Operations.
            Frame frame = new Frame.Builder().setBitmap(currentlBitmap).build();
            SparseArray items = txtRecognizer.detect(frame);
            StringBuilder strBuilder = new StringBuilder();

            for (int j = 0; j < items.size(); j++)
            {
                TextBlock item = (TextBlock)items.valueAt(j);
                strBuilder.append(item.getValue());
                strBuilder.append("/");
                // The following Process is used to show how to use lines & elements as well
                for (int i = 0; i < items.size(); i++) {

                    TextBlock item3 = (TextBlock) items.valueAt(i);
                    strBuilder.append(item3.getValue());
                    strBuilder.append("/");

                    for (Text line : item.getComponents()) {
                        //extract scanned text lines here
                        Log.v("lines", line.getValue());
                        for (Text element : line.getComponents()) {
                            //extract scanned text words here
                            Log.v("element", element.getValue());
                        }
                    }
                }
            }

            String retorno = strBuilder.toString();

            if(retorno.isEmpty()){

                retorno = getResources().getString(R.string.no_orc_detect);
            }
            resultadoTXT.setText(retorno);

        }
    }

    private void rotete(int degress){

        Matrix matrix = new Matrix();

        matrix.postRotate(degress);

        int width = currentlBitmap.getWidth();
        int height = currentlBitmap.getHeight();
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(currentlBitmap, width, height, true);

        currentlBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

        image.setImageBitmap(currentlBitmap);
        detectOCR();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putSerializable("foto", image );
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //nome = savedInstanceState.getSerializable("nome");
    }
}