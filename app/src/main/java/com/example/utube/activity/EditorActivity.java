package com.example.utube.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.example.utube.AppExecutor;
import com.example.utube.R;
import com.example.utube.database.AppDatabase;
import com.example.utube.database.VideoEntry;
import com.example.utube.databinding.ActivityEditorBinding;
import com.example.utube.util;
import com.example.utube.viewmodel.EditorViewModelDatabase;
import com.example.utube.viewmodel.EditorViewModelDatabaseFactory;

import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.concurrent.Executor;

public class EditorActivity extends AppCompatActivity {
    private static final String TAG = "zzzzz EditorActivity";
    private ActivityEditorBinding binding;
    private Executor dataBaseExecutor;
    private AppDatabase mDb;
    private int id;
    private String videoId;
    private String title;
    private String thumbnailsUrl;
    private DateTime publishedAt;
    private String duration;
    private boolean videoHasChanged = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_editor);
        dataBaseExecutor = AppExecutor.getInstance().dataBaseExecutor();
        mDb = AppDatabase.getInstance(this);
        Intent intent = getIntent();
        id = intent.getIntExtra("id", -1);
        EditorViewModelDatabaseFactory editorViewModelDatabaseFactory = new EditorViewModelDatabaseFactory(mDb, id);
        EditorViewModelDatabase editorViewModelDatabase = ViewModelProviders.of(this, editorViewModelDatabaseFactory).get(EditorViewModelDatabase.class);
        editorViewModelDatabase.getVideoEntry().observe(this, new Observer<VideoEntry>() {
            @Override
            public void onChanged(@Nullable final VideoEntry videoEntry) {
                if (videoEntry != null) {
                    Log.e(TAG, "Updating current video entry from LiveData in ViewModel");
                    videoId = videoEntry.getVideoId();
                    title = videoEntry.getTitle();
                    thumbnailsUrl = videoEntry.getThumbnailsUrl();
                    publishedAt = videoEntry.getPublishedAt();
                    duration = videoEntry.getDuration();
                    binding.titleEditText.setText(title);
                    binding.publishedAtTextViewDialog.setText(util.getStringFromDateTime(publishedAt));
                    Period period = Period.parse(duration);
                    final int[] hours = {period.getHours()};
                    final int[] mins = {period.getMinutes()};
                    final int[] secs = {period.getSeconds()};
                    binding.durationTextViewDialog.setText(util.parseDuration(EditorActivity.this, duration));

                    binding.publishedAtTextViewDialog.setOnClickListener(new View.OnClickListener() {
                        DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                publishedAt = DateTime.parse(year + "-" + (month + 1) + "-" + day + "T" + publishedAt.toLocalTime() + "Z");
                                videoEntry.setPublishedAt(publishedAt);
                                binding.publishedAtTextViewDialog.setText(util.getStringFromDateTime(publishedAt));
                            }
                        };

                        @Override
                        public void onClick(View view) {
                            int year = publishedAt.getYear();
                            int month = publishedAt.getMonthOfYear();
                            int day = publishedAt.getDayOfMonth();
                            DatePickerDialog dialog = new DatePickerDialog(
                                    EditorActivity.this,
                                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                                    mDateSetListener,
                                    year, month - 1, day);
                            if (dialog.getWindow() != null)
                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            dialog.show();
                        }
                    });

                    binding.durationTextViewDialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            View view = View.inflate(EditorActivity.this, R.layout.duration_dialog, null);
                            final NumberPicker numberPickerHour = view.findViewById(R.id.numpicker_hours);
                            numberPickerHour.setMaxValue(999);
                            numberPickerHour.setValue(hours[0]);
                            final NumberPicker numberPickerMinutes = view.findViewById(R.id.numpicker_minutes);
                            numberPickerMinutes.setMaxValue(59);
                            numberPickerMinutes.setValue(mins[0]);
                            final NumberPicker numberPickerSeconds = view.findViewById(R.id.numpicker_seconds);
                            numberPickerSeconds.setMaxValue(59);
                            numberPickerSeconds.setValue(secs[0]);
                            Button cancel = view.findViewById(R.id.cancel);
                            Button ok = view.findViewById(R.id.ok);
                            AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
                            builder.setView(view);
                            final AlertDialog alertDialog = builder.create();
                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alertDialog.dismiss();
                                }
                            });
                            ok.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    hours[0] = numberPickerHour.getValue();
                                    mins[0] = numberPickerMinutes.getValue();
                                    secs[0] = numberPickerSeconds.getValue();
                                    duration = "PT" + hours[0] + "H" + mins[0] + "M" + secs[0] + "S";
                                    videoEntry.setDuration(duration);
                                    binding.durationTextViewDialog.setText(util.parseDuration(EditorActivity.this, duration));
                                    alertDialog.dismiss();
                                }
                            });
                            alertDialog.show();
                        }
                    });
                }
            }
        });

        View.OnTouchListener mTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                videoHasChanged = true;
                return false;
            }
        };
        binding.titleEditText.setOnTouchListener(mTouchListener);
        binding.publishedAtTextViewDialog.setOnTouchListener(mTouchListener);
        binding.durationTextViewDialog.setOnTouchListener(mTouchListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editor_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update:
                title = binding.titleEditText.getText().toString();
                if (TextUtils.isEmpty(title)) {
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.constraintLayout), R.string.snackbar_message, Snackbar.LENGTH_LONG);
                    View view = snackbar.getView();
                    TextView textView = view.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.YELLOW);
                    snackbar.show();
                    InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(binding.titleEditText.getWindowToken(), 0);
                    return true;
                }
                final VideoEntry videoEntry = new VideoEntry(videoId, title, thumbnailsUrl, publishedAt, duration);
                dataBaseExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        videoEntry.setId(id);
                        mDb.videoDao().updateVideo(videoEntry);
                        finish();
                    }
                });
                return true;
            case android.R.id.home:
                if (!videoHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (!videoHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
