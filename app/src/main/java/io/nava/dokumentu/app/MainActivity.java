package io.nava.dokumentu.app;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;

import io.nava.dokumentu.app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PICK_CSV_FILE = 2;

    // Used to load the 'app' library on application startup.
    static {
        System.loadLibrary("app");
    }

    private ActivityMainBinding binding;
    private TextView statusText;
    private Button selectFileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeViews();
        setupClickListeners();

        // Initialize with welcome message
        statusText.setText(stringFromJNI());
    }

    private void initializeViews() {
        statusText = binding.sampleText;
        selectFileButton = binding.selectFileButton;
    }

    private void setupClickListeners() {
        selectFileButton.setOnClickListener(v -> openFilePicker());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Add MIME types for CSV files
        String[] mimeTypes = {"text/csv", "text/comma-separated-values", "application/csv", "text/plain"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_csv_file)), PICK_CSV_FILE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CSV_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                processSelectedFile(uri);
            } else {
                Toast.makeText(this, getString(R.string.no_file_selected), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processSelectedFile(Uri uri) {
        statusText.setText(getString(R.string.processing_file));
        selectFileButton.setEnabled(false);

        try {
            // Get file name from URI
            String fileName = getFileNameFromUri(uri);

            // Copy URI content to a temporary file that native code can access
            String tempFilePath = copyUriToTempFile(uri);

            if (tempFilePath != null) {
                launchCSVViewer(tempFilePath, fileName);
            } else {
                showError("Failed to access file");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error processing file", e);
            showError("Error processing file: " + e.getMessage());
        } finally {
            // Re-enable button and reset status
            selectFileButton.setEnabled(true);
            statusText.setText(getString(R.string.csv_viewer));
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = "Unknown";
        try {
            if (uri.getLastPathSegment() != null) {
                fileName = uri.getLastPathSegment();
                // Clean up the filename
                if (fileName.contains(":")) {
                    fileName = fileName.substring(fileName.lastIndexOf(":") + 1);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not get filename from URI", e);
        }
        return fileName;
    }

    private String copyUriToTempFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }

            // Create temp file in app's private directory
            File tempFile = new File(getCacheDir(), "temp_csv_file.csv");
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            return tempFile.getAbsolutePath();

        } catch (IOException e) {
            Log.e(TAG, "Error copying file", e);
            return null;
        }
    }

    private void launchCSVViewer(String filePath, String fileName) {
        // Pre-load the CSV file to check if it's valid
        boolean loaded = CSVDataBridge.loadCSVFile(filePath);

        if (loaded) {
            Intent intent = new Intent(this, CSVViewerActivity.class);
            intent.putExtra(CSVViewerActivity.EXTRA_FILE_PATH, filePath);
            intent.putExtra(CSVViewerActivity.EXTRA_FILE_NAME, fileName);
            startActivity(intent);
        } else {
            showError("Failed to load CSV file. Please check the file format.");
        }
    }

    private void showError(String message) {
        statusText.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Native method declarations (kept for backward compatibility with CSVTableAdapter)
    public native String stringFromJNI();

    // These methods now delegate to CSVDataBridge for consistency
    public boolean loadCSVFile(String filePath) {
        return CSVDataBridge.loadCSVFile(filePath);
    }

    public String[] getCSVHeaders() {
        return CSVDataBridge.getCSVHeaders();
    }

    public String[] getCSVRow(int rowIndex) {
        return CSVDataBridge.getCSVRow(rowIndex);
    }

    public int getRowCount() {
        return CSVDataBridge.getRowCount();
    }

    public int getColumnCount() {
        return CSVDataBridge.getColumnCount();
    }

    public String getCellValue(int rowIndex, int columnIndex) {
        return CSVDataBridge.getCellValue(rowIndex, columnIndex);
    }
}