package com.example.scandal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.FileProvider;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import java.util.Objects;
/** Activity for managing the creation of a new event */

public class NewEventActivity extends AppCompatActivity {


    // Assuming these ImageViews are for displaying the QR codes or posters if needed
    ImageView checkinQRCode, promoQRCode;
    AppCompatButton saveEventButton;
    FirebaseFirestore db;


    /**
     * QRCode object for generating and handling QR codes.
     */
    QRCode QR;
    /**
     * token to be encoded in the default QR code for checkins
     */
    String token;
    /**
     *  the token to be encoded for the default QR code for event promo
     */
    String token2;
    /**
     * string of the event poster to make the passed intents smaller
     */
    static String imageString;
    /**
     *
     * Called when the activity is starting. This is where most initialization should go:
     * calling setContentView(int) to inflate the activity's UI, initializing objects, etc.
     * param savedInstanceState If the activity is being re-initialized after previously being shut down
     *                           then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise, it is null.
     */
  //  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_created_page); // Ensure you have this layout

        db = FirebaseFirestore.getInstance();

        initializeUI();

        // Retrieve event details passed from EventActivity
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String description = intent.getStringExtra("description");
        String eventTime = intent.getStringExtra("Time");
        String eventLocation = intent.getStringExtra("Location");
        String checkinToken = intent.getStringExtra("CheckinToken");
        String promoToken = intent.getStringExtra("PromoToken");
        String posterImage = intent.getStringExtra("posterImage"); // Base64 image string
        // Fetch device ID to use as organizer
        final String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        // Save button action
        saveEventButton.setOnClickListener(v -> {
            Map<String, Object> event = new HashMap<>();
            event.put("name", name);
            event.put("description", description);
            event.put("time", eventTime);
            event.put("location", eventLocation);
            event.put("checkinToken", checkinToken);
            event.put("promoToken", promoToken);
            event.put("posterImage", posterImage);
            event.put("organizer", deviceId); // Add device ID as organizer

            // Save the event to Firestore
            saveEventToFirestore(event);


        setContentView(R.layout.events_created_page);
        db = FirebaseFirestore.getInstance();
        // Initialize your components here
        initializeUI();
        Button share = findViewById(R.id.sharebtn123); // Remove line after testing
        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");
        //String imageString = getIntent().getStringExtra("posterImage");
        String eventLocation = getIntent().getStringExtra("Location");
        String eventTime = getIntent().getStringExtra("Time");

        generateQRs();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("hpeebles", "Going to event page");
                finish();
            }
        });
        saveProj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> event = new HashMap<>();
                event.put("name", name);
                event.put("time", eventTime);
                event.put("location", eventLocation);
                event.put("description", description);
                event.put("checkinToken", token);
                event.put("promoToken", token2);
                event.put("posterImage", imageString); // Add the image string to the event map
                final String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                event.put("organizer", deviceId); // Add device ID as organizer
                // Save event to Firestore
                Log.e("hpeebles", "before storing in db");
                db.collection("events")
                        .add(event)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.e("hpeebles", "Added to DB");
                                Toast.makeText(NewEventActivity.this, "Event saved successfully", Toast.LENGTH_SHORT).show();
                                Intent homePage = new Intent(NewEventActivity.this, HomeActivity.class);
                                startActivity(homePage);
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed to create event", Toast.LENGTH_SHORT).show());
//                Intent homePage = new Intent(NewEventActivity.this, HomeActivity.class);
//                startActivity(homePage);
            }

        });
    }


        /**
         * activates the QR scanner to get the custom qr code for checkins
         */
        saveCheckinCode.setOnClickListener(v -> {
            QRCodeScanner.imageString = imageString;
            Intent scanner = new Intent(NewEventActivity.this, QRCodeScanner.class);
            scanner.putExtra("Activity", 2);
            scanner.putExtra("name", name);
            scanner.putExtra("Time", eventTime);
            scanner.putExtra("Location", eventLocation);
            scanner.putExtra("description", description);
            scanner.putExtra("PromoQRCode", token2);
            startActivity(scanner);

        });
        /**
         * activates the QR scanner to get the custom qr code for event promotion
         */
        savePromoCode.setOnClickListener(v -> {
            QRCodeScanner.imageString = imageString;
            Intent scanner = new Intent(NewEventActivity.this, QRCodeScanner.class);
            scanner.putExtra("Activity", 2);
            scanner.putExtra("name", name);
            scanner.putExtra("Time", eventTime);
            scanner.putExtra("Location", eventLocation);
            scanner.putExtra("description", description);
            scanner.putExtra("QRCode", token);
            startActivity(scanner);        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImage(QR.getQRPic(), "Promo QR shared from Scandal");
            }
        });
    }
    private void initializeUI(){
        backButton = findViewById(R.id.buttonBack_EventsCreatedPage);
        checkinQRCode = findViewById(R.id.checkinQRCode);
        promoQRCode = findViewById(R.id.promoQRCode);
        saveCheckinCode = findViewById(R.id.buttonSaveCheckinCode);
        savePromoCode = findViewById(R.id.buttonSavePromoCode);
        saveProj = findViewById(R.id.buttonSaveProject);
    }
    private void generateQRs(){
        QR = new QRCode(); // Assuming you have a default constructor

        token = getIntent().getStringExtra("CheckinToken");

        if (QR.generateQR(checkinQRCode, token)) {
            checkinQRCode.setImageBitmap(QR.getQRPic());
        } else {
            Log.e("NewEventActivity", "Checkin QR generation failed");
        }
        token2 = getIntent().getStringExtra("PromoToken");
        if (QR.generateQR(promoQRCode, token2)) {
            promoQRCode.setImageBitmap(QR.getQRPic());
        } else {
            Log.e("NewEventActivity", "Promo QR generation failed");
        }
    }

    /**
     * Shares in image pic with other apps and send the textAccompany with it
     * @param pic img being shared
     * @param textAccompany text to be sent with img
     */
    protected void shareImage(Bitmap pic, String textAccompany){
        Intent share = new Intent(Intent.ACTION_SENDTO);
        share.setType("image/jpeg");
        Uri picUri;
        picUri = saveImage(pic, getApplicationContext());
        share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        share.putExtra(Intent.EXTRA_STREAM, picUri);
        share.putExtra(Intent.EXTRA_SUBJECT, "Share To Apps");
        share.putExtra(Intent.EXTRA_TEXT, textAccompany);
        startActivity(Intent.createChooser(share, "Share Data"));
    }

    /**
     * This method takes a bit map and the context and converts and returns the uri of the image
     * @param pic bitmap to be converted to uri format
     * @param instance the context in which the method is called
     * @return a uri version of the bitmap passed
     */
    private Uri saveImage(Bitmap pic, Context instance){
        File imageFolder = new File(instance.getCacheDir(), "images");
        Uri picUri = null;
        try{
            imageFolder.mkdir();
            File file = new File(imageFolder, "share_codes.jpg");
            FileOutputStream stream = new FileOutputStream(file);
            pic.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            stream.flush();
            stream.close();
            picUri = FileProvider.getUriForFile(Objects.requireNonNull(instance.getApplicationContext()),
                    "com.example.scandal"+".provider", file);

        } catch (IOException error){
            Log.d("saveImage", "Exception"+error.getMessage());
        }
        return picUri;
    }
}
