package com.palm.demo;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import com.api.stream.PalmSdk;

import java.util.Collections;
import java.util.List;

public class PalmInitializer implements Initializer<Void> {
    @NonNull
    @Override
    public Void create(@NonNull Context context) {
        PalmSdk.initialize();
        return null;
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.emptyList();
    }
}
