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
    private List<Videos.Item> videoItems;
    private Context context;

    public VideoAdapter(List<Videos.Item> videoItems, Context context) {
        this.videoItems = videoItems;
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
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        Videos.Item videoItem = videoItems.get(position);
        viewHolder.binding.title.setText(videoItem.getSnippet().getTitle());
        GlideApp.with(context)
                .load(videoItem.getSnippet().getThumbnails().getDefault().getUrl())
                .into(viewHolder.binding.image);
        viewHolder.binding.publishedAt.setText(util.parseDateTime(videoItem.getSnippet().getPublishedAt()));
        viewHolder.binding.duration.setText(util.parseDuration(context, videoItem.getDuration()));
        viewHolder.itemView.setTag(videoItem.getIdPrimaryKey());
    }

    @Override
    public int getItemCount() {
        if (videoItems == null) {
            return 0;
        }
        return videoItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private VideoItemBinding binding;

        ViewHolder(@NonNull VideoItemBinding itemBinding) {
            super(itemBinding.getRoot());
            this.binding = itemBinding;
        }
    }
}
