package com.example.utube.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.example.utube.R;
import com.example.utube.database.AppDatabase;
import com.example.utube.databinding.ActivityEditorBinding;
import com.example.utube.model.Videos;
import com.example.utube.util;
import com.example.utube.viewmodel.EditorViewModelDatabase;
import com.example.utube.viewmodel.EditorViewModelDatabaseFactory;

import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class EditorActivity extends AppCompatActivity {
    private static final String TAG = "zzzzz EditorActivity";
    private ActivityEditorBinding binding;
    @Inject Executor dataBaseExecutor;
    @Inject AppDatabase mDb;
    private Videos.Item videoItem;
    private String title;
    private String publishedAt;
    private String duration;
    private boolean videoHasChanged = false;
    private EditorViewModelDatabase editorViewModelDatabase;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_editor);
        AndroidInjection.inject(this);
        Intent intent = getIntent();
        int idPrimaryKey = intent.getIntExtra("idPrimaryKey", -1);
        EditorViewModelDatabaseFactory editorViewModelDatabaseFactory = new EditorViewModelDatabaseFactory(mDb, idPrimaryKey);
        editorViewModelDatabase = ViewModelProviders.of(this, editorViewModelDatabaseFactory).get(EditorViewModelDatabase.class);
        editorViewModelDatabase.getVideoItemLiveData().observe(this, videoItem -> {
            if (videoItem != null) {
                Log.e(TAG, "Updating current video item from LiveData in ViewModel");
                title = videoItem.getSnippet().getTitle();
                publishedAt = videoItem.getSnippet().getPublishedAt();
                duration = videoItem.getDuration();
                binding.titleEditText.setText(title);
                binding.publishedAtTextViewDialog.setText(util.parseDateTime(publishedAt));
                Period period = Period.parse(duration);
                final int[] hours = {period.getHours()};
                final int[] mins = {period.getMinutes()};
                final int[] secs = {period.getSeconds()};
                binding.durationTextViewDialog.setText(util.parseDuration(EditorActivity.this, duration));

                binding.publishedAtTextViewDialog.setOnClickListener(new View.OnClickListener() {
                    DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                            publishedAt = DateTime.parse(year + "-" + (month + 1) + "-" + day + "T" + DateTime.parse(publishedAt).toLocalTime() + "Z").toString();
                            videoItem.getSnippet().setPublishedAt(publishedAt);
                            binding.publishedAtTextViewDialog.setText(util.parseDateTime(publishedAt));
                        }
                    };

                    @Override
                    public void onClick(View view) {
                        int year = DateTime.parse(publishedAt).getYear();
                        int month = DateTime.parse(publishedAt).getMonthOfYear();
                        int day = DateTime.parse(publishedAt).getDayOfMonth();
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

                binding.durationTextViewDialog.setOnClickListener(v -> {
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
                    cancel.setOnClickListener(v1 -> alertDialog.dismiss());
                    ok.setOnClickListener(v12 -> {
                        hours[0] = numberPickerHour.getValue();
                        mins[0] = numberPickerMinutes.getValue();
                        secs[0] = numberPickerSeconds.getValue();
                        duration = "PT" + hours[0] + "H" + mins[0] + "M" + secs[0] + "S";
                        videoItem.setDuration(duration);
                        binding.durationTextViewDialog.setText(util.parseDuration(EditorActivity.this, duration));
                        alertDialog.dismiss();
                    });
                    alertDialog.show();
                });
            }
        });

        View.OnTouchListener mTouchListener = (view, motionEvent) -> {
            videoHasChanged = true;
            return false;
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
                videoItem = editorViewModelDatabase.getVideoItemLiveData().getValue();
                dataBaseExecutor.execute(() -> {
                    videoItem.getSnippet().setTitle(title);
                    videoItem.getSnippet().setPublishedAt(publishedAt);
                    videoItem.setDuration(duration);
                    mDb.videoDao().updateVideo(videoItem);
                    finish();
                });
                return true;
            case R.id.delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!videoHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        (dialogInterface, i) -> NavUtils.navigateUpFromSameTask(EditorActivity.this);
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_message);
        builder.setPositiveButton(R.string.delete, (dialog, id) -> dataBaseExecutor.execute(() -> {
            mDb.videoDao().deleteVideo(editorViewModelDatabase.getVideoItemLiveData().getValue());
            finish();
        }));
        builder.setNegativeButton(android.R.string.cancel, (dialog, id) -> {
            if (dialog != null) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!videoHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                (dialogInterface, i) -> finish();
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, (dialog, id) -> {
            if (dialog != null) {
                dialog.dismiss();
            }
        });
        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
