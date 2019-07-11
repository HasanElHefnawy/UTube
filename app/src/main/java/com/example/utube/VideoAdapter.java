package com.example.utube;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.utube.database.VideoEntry;
import com.example.utube.databinding.VideoItemBinding;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {
    private List<VideoEntry> videoEntries;
    private Context context;

    VideoAdapter(List<VideoEntry> videoEntries, Context context) {
        this.videoEntries = videoEntries;
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
        VideoEntry videoEntry = videoEntries.get(position);
        viewHolder.binding.title.setText(videoEntry.getTitle());
        GlideApp.with(context)
                .load(videoEntry.getThumbnailsUrl())
                .into(viewHolder.binding.image);
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("MMM, d yyyy");
        String publishDate = dateTimeFormatter.print(videoEntry.getPublishedAt());
        viewHolder.binding.publishedAt.setText(publishDate);
        viewHolder.binding.duration.setText(videoEntry.getDuration().equals("0:00") ? "Live" : videoEntry.getDuration());
    }

    @Override
    public int getItemCount() {
        if (videoEntries == null) {
            return 0;
        }
        return videoEntries.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private VideoItemBinding binding;

        ViewHolder(@NonNull VideoItemBinding itemBinding) {
            super(itemBinding.getRoot());
            this.binding = itemBinding;
        }
    }
}
