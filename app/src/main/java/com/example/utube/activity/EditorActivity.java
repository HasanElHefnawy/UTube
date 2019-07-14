package com.example.utube.activity;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.utube.R;
import com.example.utube.database.AppDatabase;
import com.example.utube.database.VideoEntry;
import com.example.utube.databinding.ActivityEditorBinding;
import com.example.utube.viewmodel.EditorViewModelDatabase;
import com.example.utube.viewmodel.EditorViewModelDatabaseFactory;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class EditorActivity extends AppCompatActivity {
    private static final String TAG = "zzzzz EditorActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityEditorBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_editor);
        AppDatabase mDb = AppDatabase.getInstance(this);
        Intent intent = getIntent();
        int id = intent.getIntExtra("id", -1);
        EditorViewModelDatabaseFactory editorViewModelDatabaseFactory = new EditorViewModelDatabaseFactory(mDb, id);
        EditorViewModelDatabase editorViewModelDatabase = ViewModelProviders.of(this, editorViewModelDatabaseFactory).get(EditorViewModelDatabase.class);
        editorViewModelDatabase.getVideoEntry().observe(this, new Observer<VideoEntry>() {
            @Override
            public void onChanged(@Nullable VideoEntry videoEntry) {
                if (videoEntry != null) {
                    Log.e(TAG, "Updating current video entry from LiveData in ViewModel");
                    binding.titleEditText.setText(videoEntry.getTitle());
                    DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("MMM, d yyyy");
                    String publishDate = dateTimeFormatter.print(videoEntry.getPublishedAt());
                    binding.publishedAtEditText.setText(publishDate);
                    binding.durationEditText.setText(videoEntry.getDuration());
                }
            }
        });
    }
}
