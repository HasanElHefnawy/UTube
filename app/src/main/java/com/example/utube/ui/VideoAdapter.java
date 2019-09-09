package com.example.utube.ui;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.utube.GlideApp;
import com.example.utube.R;
import com.example.utube.databinding.VideoItemBinding;
import com.example.utube.model.Videos;
import com.example.utube.util;

public class VideoAdapter extends PagedListAdapter<Videos.Item, VideoAdapter.ViewHolder> {
    private static final String TAG = "zzzzz VideoAdapter";
    private Context context;

    VideoAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        VideoItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.video_item, viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Videos.Item videoItem = getItem(position);
        if (videoItem != null) {
            viewHolder.binding.title.setText(videoItem.getSnippet().getTitle());
            GlideApp.with(context)
                    .load(videoItem.getSnippet().getThumbnails().getDefault().getUrl())
                    .into(viewHolder.binding.image);
            viewHolder.binding.publishedAt.setText(util.parseDateTime(videoItem.getSnippet().getPublishedAt()));
            viewHolder.binding.duration.setText(util.parseDuration(context, videoItem.getDuration()));
            viewHolder.itemView.setTag(videoItem.getIdPrimaryKey());
        } else {
            Log.e(TAG, String.format("Item at position %d is null", position));
        }
    }

    private static DiffUtil.ItemCallback<Videos.Item> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Videos.Item>() {
                @Override
                public boolean areItemsTheSame(Videos.Item oldItem, Videos.Item newItem) {
                    Log.d(TAG, "areItemsTheSame: oldItem.getIdPrimaryKey() " + oldItem.getIdPrimaryKey() + " " + oldItem.getSnippet().getTitle());
                    Log.d(TAG, "areItemsTheSame: newItem.getIdPrimaryKey() " + newItem.getIdPrimaryKey() + " " + newItem.getSnippet().getTitle());
                    return oldItem.getIdPrimaryKey() == newItem.getIdPrimaryKey();
                }

                @Override
                public boolean areContentsTheSame(Videos.Item oldItem, Videos.Item newItem) {
                    Log.d(TAG, "areContentsTheSame: oldItem.getIdPrimaryKey() " + oldItem.getIdPrimaryKey() + " " + oldItem.getSnippet().getTitle());
                    Log.d(TAG, "areContentsTheSame: newItem.getIdPrimaryKey() " + newItem.getIdPrimaryKey() + " " + newItem.getSnippet().getTitle());
                    return oldItem.equals(newItem);
                }
            };

    class ViewHolder extends RecyclerView.ViewHolder {
        private VideoItemBinding binding;

        ViewHolder(@NonNull VideoItemBinding itemBinding) {
            super(itemBinding.getRoot());
            this.binding = itemBinding;
        }
    }
}
