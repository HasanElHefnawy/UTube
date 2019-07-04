package com.example.utube;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.utube.databinding.VideoItemBinding;
import com.example.utube.model.Video;
import com.example.utube.model.Videos;
import com.example.utube.network.RetrofitApiClient;
import com.example.utube.network.RetrofitApiService;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {
    private static final String TAG = "zzzzz VideoAdapter";
    private List<Videos.Item> items;
    private Context context;
    private RetrofitApiService retrofitApiService;
    private CompositeDisposable disposable = new CompositeDisposable();

    VideoAdapter(List<Videos.Item> items, Context context) {
        this.items = items;
        this.context = context;
        retrofitApiService = RetrofitApiClient.getClient().create(RetrofitApiService.class);
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
        final Videos.Item item = items.get(position);
        viewHolder.binding.title.setText(item.getSnippet().getTitle());
        GlideApp.with(context)
                .load(item.getSnippet().getThumbnails().getDefault().getUrl())
                .into(viewHolder.binding.image);
        disposable.add(retrofitApiService.getVideoDuration(
                "videos",
                item.getId().getVideoId(),
                "contentDetails")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Video>() {
                    @Override
                    public void onNext(Video video) {
                        Log.e(TAG, "onNext: video " + video);
                        Log.e(TAG, "onNext: video.getItems().size() " + video.getItems().size());
                        Log.e(TAG, "duration " + video.getItems().get(0).getContentDetails().getDuration());
                        viewHolder.binding.duration.setText(video.getItems().get(0).getContentDetails().getDuration());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, "onError: " + throwable);
                        Toast.makeText(context, "Error!!!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete: ");
                    }
                })
        );
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
