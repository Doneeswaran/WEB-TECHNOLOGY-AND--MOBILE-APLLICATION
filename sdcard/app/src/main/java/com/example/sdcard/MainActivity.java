package com.example.sdcard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 100;
    private EditText inputText;
    private Button writeButton,clearButton;

    private static final int MANAGE_STORAGE_REQUEST_CODE = 101;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputText = findViewById(R.id.input_text);
        writeButton = findViewById(R.id.button_write);
        clearButton = findViewById(R.id.button_clear);

        // Check and request permissions based on Android version
        checkAndRequestPermissions();

        writeButton.setOnClickListener(v -> {
            String data = inputText.getText().toString();
            if (!data.isEmpty()) {
                if (isStoragePermissionGranted()) {
                    writeToFile(data);
                    inputText.setText(""); // Clear after writing
                } else {
                    Toast.makeText(MainActivity.this, "Storage permission required", Toast.LENGTH_SHORT).show();
                    checkAndRequestPermissions();
                }
            } else {
                Toast.makeText(MainActivity.this, "Please enter some text", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the Clear Button to clear the EditText
        clearButton.setOnClickListener(v -> inputText.setText(""));  // Clears the EditText field
    }
    
    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API 30) and above
            if (!Environment.isExternalStorageManager()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_required_title);
                builder.setMessage(R.string.permission_required_message);
                builder.setPositiveButton(R.string.dialog_button_ok, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, MANAGE_STORAGE_REQUEST_CODE);
                });
                builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> 
                    Toast.makeText(this, R.string.permission_denied_message, Toast.LENGTH_LONG).show());
                builder.show();
            }
        } else {
            // Android 10 (API 29) and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE
                );
            }
        }
    }
    
    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void writeToFile(String text) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File file;
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // For Android 11+, we'll use the app-specific directory
                file = new File(getExternalFilesDir(null), "output.txt");
            } else {
                // For older versions, we can use the public directory
                File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                file = new File(directory, "output.txt");
            }
            
            try (FileOutputStream output = new FileOutputStream(file, true)) {
                output.write((text + "\n").getBytes());
                
                // Show a more user-friendly success message with file path
                showSuccessDialog(file.getAbsolutePath());
            } catch (IOException e) {
                Toast.makeText(this, "Failed to write: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "External storage not available", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MANAGE_STORAGE_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, R.string.permission_granted_message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.permission_denied_message, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    /**
     * Shows a dialog with the file path where data was saved
     * @param filePath The absolute path to the saved file
     */
    private void showSuccessDialog(String filePath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_success_title);
        
        // Create a simplified path for display
        String displayPath = filePath;
        try {
            // Try to make the path more readable
            if (filePath.contains("/Android/data/")) {
                displayPath = "App Storage: " + filePath.substring(filePath.lastIndexOf("/") + 1);
            } else if (filePath.contains("/Documents/")) {
                displayPath = "Documents: " + filePath.substring(filePath.lastIndexOf("/") + 1);
            }
        } catch (Exception e) {
            // If any error in string manipulation, use the full path
            displayPath = filePath;
        }
        
        builder.setMessage(getString(R.string.dialog_success_message) + "\n" + displayPath);
        builder.setPositiveButton(R.string.dialog_button_ok, null);
        
        // Add a button to show the full path
        builder.setNeutralButton(R.string.dialog_button_show_path, (dialog, which) -> {
            AlertDialog.Builder pathBuilder = new AlertDialog.Builder(this);
            pathBuilder.setTitle(R.string.dialog_full_path_title);
            pathBuilder.setMessage(filePath);
            pathBuilder.setPositiveButton(R.string.dialog_button_ok, null);
            pathBuilder.show();
        });
        
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
