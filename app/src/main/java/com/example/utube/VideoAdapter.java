package com.example.utube;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.utube.databinding.VideoItemBinding;
import com.example.utube.model.Videos;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {
    private List<Videos.Item> items;
    private Context context;

    VideoAdapter(List<Videos.Item> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        VideoItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.video_item, viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Videos.Item item = items.get(position);
        viewHolder.binding.title.setText(item.getSnippet().getTitle());
        GlideApp.with(context)
                .load(item.getSnippet().getThumbnails().getDefault().getUrl())
                .into(viewHolder.binding.image);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private VideoItemBinding binding;

        ViewHolder(@NonNull VideoItemBinding itemBinding) {
            super(itemBinding.getRoot());
            this.binding = itemBinding;
        }
    }
}
