package io.nava.dokumentu.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SynchronizedCSVAdapter extends RecyclerView.Adapter<SynchronizedCSVAdapter.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ROW = 1;

    private final Context context;
    private final String[] headers;
    private final int dataRowCount;
    private final CSVViewerActivity csvViewerActivity;

    // Synchronized scrolling management
    private final List<HorizontalScrollView> scrollViews = new ArrayList<>();
    private boolean isScrolling = false;

    public SynchronizedCSVAdapter(CSVViewerActivity csvViewerActivity, String[] headers, int dataRowCount) {
        this.context = csvViewerActivity;
        this.csvViewerActivity = csvViewerActivity;
        this.headers = headers;
        this.dataRowCount = dataRowCount;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_ROW;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == VIEW_TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_csv_header_row, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_csv_data_row, parent, false);
            return new DataViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(headers);
        } else if (holder instanceof DataViewHolder) {
            int dataRowIndex = position - 1; // Account for header row
            ((DataViewHolder) holder).bind(dataRowIndex);
        }
    }

    @Override
    public int getItemCount() {
        return dataRowCount + 1; // +1 for header row
    }

    // Synchronized scroll implementation
    private void syncScroll(HorizontalScrollView source, int scrollX) {
        if (isScrolling) return; // Prevent infinite loop

        isScrolling = true;
        for (HorizontalScrollView scrollView : scrollViews) {
            if (scrollView != source) {
                scrollView.scrollTo(scrollX, 0);
            }
        }
        isScrolling = false;
    }

    private void registerScrollView(HorizontalScrollView scrollView) {
        if (!scrollViews.contains(scrollView)) {
            scrollViews.add(scrollView);
            scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                syncScroll((HorizontalScrollView) v, scrollX);
            });
        }
    }

    // Helper method to get smart truncated header text
    private String getSmartTruncatedHeader(String header) {
        if (header == null || header.length() <= 12) {
            return header;
        }

        // If contains underscore or space, show first word + beginning of second
        if (header.contains("_")) {
            String[] parts = header.split("_", 2);
            if (parts.length > 1 && parts[1].length() > 0) {
                int firstPartLen = Math.min(parts[0].length(), 8);
                int secondPartLen = Math.min(parts[1].length(), 12 - firstPartLen - 1);
                return parts[0].substring(0, firstPartLen) + "_" +
                        parts[1].substring(0, secondPartLen);
            }
        }

        if (header.contains(" ")) {
            String[] parts = header.split(" ", 2);
            if (parts.length > 1 && parts[1].length() > 0) {
                int firstPartLen = Math.min(parts[0].length(), 8);
                int secondPartLen = Math.min(parts[1].length(), 12 - firstPartLen - 1);
                return parts[0].substring(0, firstPartLen) + " " +
                        parts[1].substring(0, secondPartLen);
            }
        }

        // Otherwise just truncate smartly
        return header.substring(0, Math.min(12, header.length()));
    }

    // Base ViewHolder class
    public abstract static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    // Header ViewHolder with improved layout
    public class HeaderViewHolder extends ViewHolder {
        private HorizontalScrollView scrollView;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            scrollView = (HorizontalScrollView) itemView;
        }

        public void bind(String[] headers) {
            // Register this scroll view for synchronization
            registerScrollView(scrollView);

            // Get the LinearLayout container
            LinearLayout container = (LinearLayout) scrollView.getChildAt(0);
            container.removeAllViews();

            LayoutInflater inflater = LayoutInflater.from(context);

            for (String header : headers) {
                TextView textView = (TextView) inflater.inflate(R.layout.item_csv_cell_header, container, false);
                textView.setText(getSmartTruncatedHeader(header));

                // Set tooltip for full header text
                textView.setOnLongClickListener(v -> {
                    // Could show a toast with full header text
                    return true;
                });

                container.addView(textView);
            }
        }
    }

    // Data ViewHolder with improved layout
    public class DataViewHolder extends ViewHolder {
        private HorizontalScrollView scrollView;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            scrollView = (HorizontalScrollView) itemView;
        }

        public void bind(int rowIndex) {
            // Register this scroll view for synchronization
            registerScrollView(scrollView);

            // Get the LinearLayout container
            LinearLayout container = (LinearLayout) scrollView.getChildAt(0);
            container.removeAllViews();

            // Get row data from native layer
            String[] rowData = csvViewerActivity.getCSVRow(rowIndex);

            if (rowData != null) {
                LayoutInflater inflater = LayoutInflater.from(context);

                for (String cellValue : rowData) {
                    TextView textView = (TextView) inflater.inflate(R.layout.item_csv_cell_data, container, false);
                    textView.setText(cellValue != null ? cellValue : "");
                    container.addView(textView);
                }
            }
        }
    }

    // Cleanup method to prevent memory leaks
    public void cleanup() {
        scrollViews.clear();
    }
}