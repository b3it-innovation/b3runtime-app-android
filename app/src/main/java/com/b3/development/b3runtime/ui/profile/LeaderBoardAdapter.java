package com.b3.development.b3runtime.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.data.remote.model.result.BackendResult;
import com.b3.development.b3runtime.databinding.ListItemLeaderboardBinding;
import com.b3.development.b3runtime.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class LeaderBoardAdapter extends RecyclerView.Adapter<LeaderBoardAdapter.ResultViewHolder> {

    private List<BackendResult> results = new ArrayList<>();

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

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ResultViewHolder extends RecyclerView.ViewHolder {

        private ListItemLeaderboardBinding binding;

        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public void setup(BackendResult backendResult, int position) {
            binding.setBackendResult(backendResult);
            binding.executePendingBindings();
            binding.rankingNumber.setText(String.valueOf(position + 1));
            if (backendResult.getTotalTime() == null) {
                binding.totalTime.setText(R.string.track_unfinished);
            } else {
                long minutes = Util.getMinutesFromLong(backendResult.getTotalTime());
                long seconds = Util.getSecondsFromLong(backendResult.getTotalTime());
                binding.totalTime.setText(String.format("%d min %d sec", minutes, seconds));
            }
        }
    }

}
