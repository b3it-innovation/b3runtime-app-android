package com.b3.development.b3runtime.ui.competition;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.databinding.ListItemBinding;

import java.util.List;

public class ItemArrayAdapter extends RecyclerView.Adapter<ItemArrayAdapter.ListItemViewHolder> {

    private List<ListItem> itemList;
    private View.OnClickListener listener;

    public void setListItems(List<ListItem> listItems) {
        this.itemList = listItems;
        notifyDataSetChanged();
    }

    // get the size of the list
    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    // specify the row layout file and click for each row
    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ListItemViewHolder(inflater.inflate(R.layout.list_item, parent, false));
    }

    // load data in each row element
    @Override
    public void onBindViewHolder(final ListItemViewHolder holder, final int listPosition) {
        holder.bind(itemList.get(listPosition), listener);
    }

    public void setOnItemClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    // static inner class to initialize the views of rows
    static class ListItemViewHolder extends RecyclerView.ViewHolder {

        private ListItemBinding binding;

        public ListItemViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public void bind(ListItem listItem, View.OnClickListener listener) {
            binding.setListItem(listItem);
            binding.executePendingBindings();
            itemView.setOnClickListener(listener);
        }
    }

}