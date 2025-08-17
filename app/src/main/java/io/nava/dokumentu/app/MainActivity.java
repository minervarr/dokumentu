package io.nava.dokumentu.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private RecyclerView csvRecyclerView;
    private CSVTableAdapter csvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeViews();
        setupClickListeners();

        // Initialize with native library test
        statusText.setText(stringFromJNI());
    }

    private void initializeViews() {
        statusText = binding.sampleText;
        selectFileButton = binding.selectFileButton;
        csvRecyclerView = binding.csvRecyclerView;

        // Setup RecyclerView
        csvRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        csvRecyclerView.setHasFixedSize(true);

        // Initially hide the RecyclerView
        csvRecyclerView.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        selectFileButton.setOnClickListener(v -> openFilePicker());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Add MIME types for CSV files
        String[] mimeTypes = {"text/csv", "text/comma-separated-values", "application/csv"};
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
        statusText.setText(getString(R.string.loading));

        try {
            // Copy URI content to a temporary file that native code can access
            String tempFilePath = copyUriToTempFile(uri);

            if (tempFilePath != null) {
                processCSVFile(tempFilePath);
            } else {
                showError("Failed to access file");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error processing file", e);
            showError("Error processing file: " + e.getMessage());
        }
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

    private void processCSVFile(String filePath) {
        Log.d(TAG, "Loading CSV file: " + filePath);

        boolean success = loadCSVFile(filePath);

        if (success) {
            displayCSVData();
        } else {
            showError(getString(R.string.error_loading_file));
        }
    }

    private void displayCSVData() {
        String[] headers = getCSVHeaders();
        int rowCount = getRowCount();
        int columnCount = getColumnCount();

        Log.d(TAG, "CSV loaded - Rows: " + rowCount + ", Columns: " + columnCount);

        if (headers != null && headers.length > 0) {
            // Update status text
            String status = getString(R.string.file_loaded_successfully) + "\n" +
                    getString(R.string.rows_count, rowCount) + "\n" +
                    getString(R.string.columns_count, columnCount);
            statusText.setText(status);

            // Setup and show the RecyclerView
            csvAdapter = new CSVTableAdapter(this, headers, rowCount);
            csvRecyclerView.setAdapter(csvAdapter);
            csvRecyclerView.setVisibility(View.VISIBLE);

            Toast.makeText(this, getString(R.string.file_loaded_successfully), Toast.LENGTH_SHORT).show();
        } else {
            showError("No data found in CSV file");
        }
    }

    private void showError(String message) {
        statusText.setText(message);
        csvRecyclerView.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Native method declarations
    public native String stringFromJNI();
    public native boolean loadCSVFile(String filePath);
    public native String[] getCSVHeaders();
    public native String[] getCSVRow(int rowIndex);
    public native int getRowCount();
    public native int getColumnCount();
    public native String getCellValue(int rowIndex, int columnIndex);
}