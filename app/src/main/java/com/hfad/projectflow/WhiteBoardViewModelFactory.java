package com.hfad.projectflow;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hfad.projectflow.database.DrawingDao;

public class WhiteBoardViewModelFactory implements ViewModelProvider.Factory {
    private final DrawingDao drawingDao;
    private final int projectId;
    private final WhiteBoardFragment fragment;

    public WhiteBoardViewModelFactory(DrawingDao drawingDao, int projectId, WhiteBoardFragment fragment) {
        this.drawingDao = drawingDao;
        this.projectId = projectId;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(WhiteBoardViewModel.class)) {
            return (T) new WhiteBoardViewModel(drawingDao, projectId, fragment);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
