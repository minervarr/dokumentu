package io.nava.dokumentu.app;

/**
 * Bridge class to access CSV native functionality from any activity.
 * This provides static access to the native CSV processing methods.
 */
public class CSVDataBridge {

    // Static initialization to load the native library
    static {
        System.loadLibrary("app");
    }

    // Static native method declarations
    public static native boolean loadCSVFile(String filePath);
    public static native String[] getCSVHeaders();
    public static native String[] getCSVRow(int rowIndex);
    public static native int getRowCount();
    public static native int getColumnCount();
    public static native String getCellValue(int rowIndex, int columnIndex);

    // Optional: Add a method to check if a file is currently loaded
    public static boolean isFileLoaded() {
        try {
            return getRowCount() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // Optional: Add a method to get file information
    public static String getFileInfo() {
        try {
            int rows = getRowCount();
            int cols = getColumnCount();
            return rows + " rows • " + cols + " columns";
        } catch (Exception e) {
            return "No file loaded";
        }
    }
}