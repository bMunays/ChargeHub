package org.edupage.brighperc.a48175.chargehub;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewSlots;
    private SlotAdapter slotAdapter;
    private List<Slot> slotList;
    private DatabaseHelper dbHelper;

    private String currentPhotoPath;
    private ImageView previewImageView;
    private Button btnStartCharge;
    private int selectedSlot = -1;
    private Dialog checkInDialog;

    private TextView textActivePhones;
    private TextView textDailyRevenue;

    // Modern way to handle Camera Results
    private final ActivityResultLauncher<Intent> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (previewImageView != null) {
                        previewImageView.setVisibility(ViewGroup.VISIBLE);
                        previewImageView.setImageURI(Uri.parse(currentPhotoPath));
                    }
                    if (btnStartCharge != null) {
                        btnStartCharge.setEnabled(true);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind the TextViews
        textActivePhones = findViewById(R.id.textActivePhones);
        textDailyRevenue = findViewById(R.id.textDailyRevenue);

        dbHelper = new DatabaseHelper(this);
        recyclerViewSlots = findViewById(R.id.recyclerViewSlots);
        recyclerViewSlots.setLayoutManager(new GridLayoutManager(this, 3));

        loadGridData();

        // call update on startup
        updateStatistics();
    }

    private void loadGridData() {
        slotList = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            slotList.add(new Slot(i, false));
        }

        // Check SQLite for active sessions to turn slots green
        Cursor cursor = dbHelper.getActiveSessions();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int slotNum = cursor.getInt(cursor.getColumnIndexOrThrow("slot_number"));
                slotList.get(slotNum - 1).setOccupied(true);
            } while (cursor.moveToNext());
            cursor.close();
        }

        slotAdapter = new SlotAdapter(slotList, slot -> {
            if (slot.isOccupied()) {
                showCheckOutDialog(slot.getSlotNumber());
            } else {
                showCheckInDialog(slot.getSlotNumber());
            }
        });
        recyclerViewSlots.setAdapter(slotAdapter);
    }

    private void showCheckOutDialog(int slotNumber) {
        Dialog checkOutDialog = new Dialog(this);
        checkOutDialog.setContentView(R.layout.dialog_check_out);
        checkOutDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView textTitle = checkOutDialog.findViewById(R.id.textCheckoutTitle);
        TextView textTime = checkOutDialog.findViewById(R.id.textCheckInTime);
        TextView textFee = checkOutDialog.findViewById(R.id.textFeeCalculated);
        ImageView imagePhoto = checkOutDialog.findViewById(R.id.imageConditionCheckout);
        Button btnConfirm = checkOutDialog.findViewById(R.id.btnConfirmCheckout);

        textTitle.setText("Check-Out: Slot " + slotNumber);

        // Fetch data from database
        Cursor cursor = dbHelper.getActiveSessionForSlot(slotNumber);
        double calculatedFee = 1.00; // Flat base fee for testing (can make dynamic later)

        if (cursor != null && cursor.moveToFirst()) {
            String checkInStr = cursor.getString(cursor.getColumnIndexOrThrow("check_in_time"));
            String photoUriStr = cursor.getString(cursor.getColumnIndexOrThrow("photo_uri"));
            String phoneNum = cursor.getString(cursor.getColumnIndexOrThrow("primary_phone"));

            textTime.setText("Phone: " + phoneNum + "\nIn: " + checkInStr);
            textFee.setText(String.format(Locale.US, "Amount Due: $%.2f", calculatedFee));

            if (photoUriStr != null) {
                imagePhoto.setImageURI(Uri.parse(photoUriStr));
            }
            cursor.close();
        }

        btnConfirm.setOnClickListener(v -> {
            boolean success = dbHelper.checkOutPhone(slotNumber, calculatedFee);
            if (success) {
                Toast.makeText(this, "Checkout Complete", Toast.LENGTH_SHORT).show();
                slotList.get(slotNumber - 1).setOccupied(false);
                slotAdapter.notifyItemChanged(slotNumber - 1);
                updateStatistics();
                checkOutDialog.dismiss();
            } else {
                Toast.makeText(this, "Error during checkout", Toast.LENGTH_SHORT).show();
            }
        });

        checkOutDialog.show();
    }

    private void showCheckInDialog(int slotNumber) {
        selectedSlot = slotNumber;
        currentPhotoPath = null; // Reset photo

        checkInDialog = new Dialog(this);
        checkInDialog.setContentView(R.layout.dialog_check_in);
        checkInDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView textTitle = checkInDialog.findViewById(R.id.textDialogTitle);
        EditText editPrimary = checkInDialog.findViewById(R.id.editPrimaryPhone);
        EditText editAlt = checkInDialog.findViewById(R.id.editAltPhone);
        EditText editId = checkInDialog.findViewById(R.id.editNationalId);

        Button btnPhoto = checkInDialog.findViewById(R.id.btnTakePhoto);
        btnStartCharge = checkInDialog.findViewById(R.id.btnStartCharge);
        previewImageView = checkInDialog.findViewById(R.id.imagePreview);

        textTitle.setText("Check-In: Slot " + slotNumber);

        btnPhoto.setOnClickListener(v -> dispatchTakePictureIntent());

        btnStartCharge.setOnClickListener(v -> {
            String phone = editPrimary.getText().toString().trim();
            if (phone.isEmpty()) {
                editPrimary.setError("Required");
                return;
            }

            // Save to SQLite
            long sessionId = dbHelper.checkInPhone(
                    slotNumber, phone,
                    editAlt.getText().toString().trim(),
                    editId.getText().toString().trim(),
                    currentPhotoPath
            );

            if (sessionId != -1) {
                Toast.makeText(this, "Checked in successfully!", Toast.LENGTH_SHORT).show();
                // Update UI temporarily (we will link UI to DB query in the next branch)
                slotList.get(slotNumber - 1).setOccupied(true);
                slotAdapter.notifyItemChanged(slotNumber - 1);
                updateStatistics();
                checkInDialog.dismiss();
            } else {
                Toast.makeText(this, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });

        checkInDialog.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "org.edupage.brighperc.a48175.chargehub.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureLauncher.launch(takePictureIntent);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void updateStatistics() {
        int activeCount = 0;
        Cursor cursor = dbHelper.getActiveSessions();
        if (cursor != null) {
            activeCount = cursor.getCount(); // Count the rows currently charging
            cursor.close();
        }

        double todayRevenue = dbHelper.getTodayRevenue();

        textActivePhones.setText(String.format(Locale.US, "Active: %d/30", activeCount));
        textDailyRevenue.setText(String.format(Locale.US, "Revenue: $%.2f", todayRevenue));
    }
}