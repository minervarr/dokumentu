package io.nava.dokumentu.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CSVTableAdapter extends RecyclerView.Adapter<CSVTableAdapter.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ROW = 1;

    private final Context context;
    private final String[] headers;
    private final int dataRowCount;
    private final MainActivity mainActivity;

    public CSVTableAdapter(MainActivity mainActivity, String[] headers, int dataRowCount) {
        this.context = mainActivity;
        this.mainActivity = mainActivity;
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

    // Base ViewHolder class
    public abstract static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    // Header ViewHolder
    public class HeaderViewHolder extends ViewHolder {

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(String[] headers) {
            // Get the LinearLayout inside the HorizontalScrollView
            ViewGroup scrollView = (ViewGroup) itemView;
            ViewGroup container = null;

            // Safely get the LinearLayout container
            if (scrollView.getChildCount() > 0) {
                View child = scrollView.getChildAt(0);
                if (child instanceof ViewGroup) {
                    container = (ViewGroup) child;
                }
            }

            // If container is still null, create it
            if (container == null) {
                container = new LinearLayout(context);
                ((LinearLayout) container).setOrientation(LinearLayout.HORIZONTAL);
                container.setPadding(4, 4, 4, 4);
                scrollView.addView(container);
            }

            container.removeAllViews();

            LayoutInflater inflater = LayoutInflater.from(context);

            for (String header : headers) {
                TextView textView = (TextView) inflater.inflate(R.layout.item_csv_cell_header, container, false);
                textView.setText(header);
                container.addView(textView);
            }
        }
    }

    // Data ViewHolder
    public class DataViewHolder extends ViewHolder {

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(int rowIndex) {
            // Get the LinearLayout inside the HorizontalScrollView
            ViewGroup scrollView = (ViewGroup) itemView;
            ViewGroup container = null;

            // Safely get the LinearLayout container
            if (scrollView.getChildCount() > 0) {
                View child = scrollView.getChildAt(0);
                if (child instanceof ViewGroup) {
                    container = (ViewGroup) child;
                }
            }

            // If container is still null, create it
            if (container == null) {
                container = new LinearLayout(context);
                ((LinearLayout) container).setOrientation(LinearLayout.HORIZONTAL);
                container.setPadding(4, 4, 4, 4);
                scrollView.addView(container);
            }

            container.removeAllViews();

            // Get row data from native layer
            String[] rowData = mainActivity.getCSVRow(rowIndex);

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
}