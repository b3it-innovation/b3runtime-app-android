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

public class LeaderBoardAdapter extends RecyclerView.Adapter<LeaderBoardAdapter.ResultViewHolder> {

    private List<BackendResult> results = new ArrayList<>();
    private View.OnClickListener listener;

    public void setResults(List<BackendResult> results) {
        this.results = results;
        notifyDataSetChanged();
    }

    public List<BackendResult> getResults() {
        return results;
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ResultViewHolder(inflater.inflate(R.layout.list_item_leaderboard, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        holder.setup(results.get(position), position);
    }

    public void setOnItemClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ResultViewHolder extends RecyclerView.ViewHolder {

        private TextView totalTime;
        private TextView userName;
        private TextView rankingNumber;

        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            totalTime = itemView.findViewById(R.id.total_time);
            userName = itemView.findViewById(R.id.user_name);
            rankingNumber = itemView.findViewById(R.id.ranking_number);
        }

        public void setup(BackendResult backendResult, int position) {
            userName.setText(backendResult.getAttendee().name);
            rankingNumber.setText(String.valueOf(position + 1));
            if (backendResult.getTotalTime() == null) {
                totalTime.setText(R.string.track_unfinished);
            } else {
                long minutes = Util.getMinutesFromLong(backendResult.getTotalTime());
                long seconds = Util.getSecondsFromLong(backendResult.getTotalTime());
                totalTime.setText(String.format("%d min %d sec", minutes, seconds));
            }
        }
    }
}
