package com.hf.passport.text;

import android.graphics.Rect;

import com.google.mlkit.vision.text.Text;

import java.util.List;
import java.util.stream.Collectors;

public class BlockWrapper {
    private Text.TextBlock gmsTextBLock;

    public BlockWrapper(Text.TextBlock gmsTextBLock) {
        this.gmsTextBLock = gmsTextBLock;
    }

    public BlockWrapper() {
        this(null);
    }

    public Rect getBoundingBox() {
        return gmsTextBLock.getBoundingBox();
    }

    public String getText() {
        return gmsTextBLock.getText();
    }

    public List<String> getLines() {
        return gmsTextBLock.getLines().stream().map(Text.Line::getText).collect(Collectors.toList());
    }
}

