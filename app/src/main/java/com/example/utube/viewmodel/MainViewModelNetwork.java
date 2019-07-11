package com.example.utube.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.util.Log;
import android.widget.EditText;

import com.jakewharton.rxbinding2.InitialValueObservable;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

public class MainViewModelNetwork extends ViewModel {

    private static final String TAG = "zzzzz " + MainViewModelNetwork.class.getSimpleName();
    private InitialValueObservable<TextViewTextChangeEvent> textViewTextChangeEvent;

    public MainViewModelNetwork(EditText editText) {
        textViewTextChangeEvent = RxTextView.textChangeEvents(editText);
        Log.e(TAG, "Actively fetching videos over Internet");
    }

    public InitialValueObservable<TextViewTextChangeEvent> getTextViewTextChangeEvent(EditText editText) {
        return textViewTextChangeEvent;
    }

    public void setTextViewTextChangeEvent(InitialValueObservable<TextViewTextChangeEvent> textViewTextChangeEvent) {
        this.textViewTextChangeEvent = textViewTextChangeEvent;
    }
}
