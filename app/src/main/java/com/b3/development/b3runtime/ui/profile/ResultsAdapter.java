package com.b3.development.b3runtime.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.data.remote.model.result.BackendResult;
import com.b3.development.b3runtime.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ResultViewHolder> {

    private List<BackendResult> results = new ArrayList<>();

    public void setResults(List<BackendResult> results) {
        this.results = results;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ResultViewHolder(inflater.inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        holder.setup(results.get(position));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    class ResultViewHolder extends RecyclerView.ViewHolder {

        private TextView result;

        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            result = itemView.findViewById(R.id.row_item);
        }

        public void setup(BackendResult backendResult) {
            if (backendResult.getTotalTime() == null) {
                result.setText(R.string.track_unfinished);
            } else {
                long minutes = Util.getMinutesFromLong(backendResult.getTotalTime());
                long seconds = Util.getSecondsFromLong(backendResult.getTotalTime());
                result.setText(String.format("%d min %d sec", minutes, seconds));
            }
        }
    }
}
