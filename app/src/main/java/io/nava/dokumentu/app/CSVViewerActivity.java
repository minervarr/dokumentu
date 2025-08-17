package io.nava.dokumentu.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CSVViewerActivity extends AppCompatActivity {

    private static final String TAG = "CSVViewerActivity";
    public static final String EXTRA_FILE_PATH = "file_path";
    public static final String EXTRA_FILE_NAME = "file_name";

    private TextView fileInfoText;
    private Button openAnotherButton;
    private RecyclerView csvRecyclerView;
    private SynchronizedCSVAdapter csvAdapter;
    private String currentFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csv_viewer);

        // Handle system window insets properly
        getWindow().setStatusBarColor(getResources().getColor(R.color.purple_700, null));

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.csv_data_viewer);
        }

        initializeViews();
        setupClickListeners();

        // Get file path from intent
        Intent intent = getIntent();
        currentFilePath = intent.getStringExtra(EXTRA_FILE_PATH);
        String fileName = intent.getStringExtra(EXTRA_FILE_NAME);

        if (currentFilePath != null) {
            loadAndDisplayCSV(currentFilePath, fileName);
        } else {
            showError("No file path provided");
            finish();
        }
    }

    private void initializeViews() {
        fileInfoText = findViewById(R.id.file_info_text);
        openAnotherButton = findViewById(R.id.open_another_button);
        csvRecyclerView = findViewById(R.id.csv_recycler_view);

        // Setup RecyclerView with optimizations
        csvRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        csvRecyclerView.setHasFixedSize(true);
        csvRecyclerView.setItemViewCacheSize(10); // Cache more views
        csvRecyclerView.setDrawingCacheEnabled(true);
        csvRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        csvRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER); // Disable overscroll
    }

    private void setupClickListeners() {
        openAnotherButton.setOnClickListener(v -> {
            // Return to MainActivity to select another file
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void loadAndDisplayCSV(String filePath, String fileName) {
        Log.d(TAG, "Loading CSV file: " + filePath);

        // Show loading state
        fileInfoText.setText(R.string.processing_file);
        openAnotherButton.setEnabled(false);

        // Load CSV file
        boolean success = loadCSVFile(filePath);

        if (success) {
            displayCSVData(fileName);
        } else {
            showError(getString(R.string.error_loading_file));
        }
    }

    private void displayCSVData(String fileName) {
        String[] headers = getCSVHeaders();
        int rowCount = getRowCount();
        int columnCount = getColumnCount();

        Log.d(TAG, "CSV loaded - Rows: " + rowCount + ", Columns: " + columnCount);

        if (headers != null && headers.length > 0) {
            // Update file info with ultra-compact display
            String fileInfo;
            if (fileName != null && !fileName.equals("Unknown")) {
                // Clean filename if it's too long
                String cleanFileName = fileName.length() > 15 ?
                        fileName.substring(0, 12) + "..." : fileName;
                fileInfo = getString(R.string.file_info_with_name, cleanFileName, rowCount, columnCount);
            } else {
                fileInfo = getString(R.string.file_info_compact, rowCount, columnCount);
            }
            fileInfoText.setText(fileInfo);

            // Setup synchronized CSV adapter
            csvAdapter = new SynchronizedCSVAdapter(this, headers, rowCount);
            csvRecyclerView.setAdapter(csvAdapter);

            openAnotherButton.setEnabled(true);

            Toast.makeText(this, R.string.file_loaded_successfully, Toast.LENGTH_SHORT).show();
        } else {
            showError("No data found in CSV file");
        }
    }

    private void showError(String message) {
        fileInfoText.setText(message);
        openAnotherButton.setEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Bridge methods to access native functionality through MainActivity
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