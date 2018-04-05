package com.kinghorn.squidswap.squidswap;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class SquidSettingsAdapter extends ArrayAdapter {
    public SquidSettingsAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        System.out.println("testing");
        return super.getView(position, convertView, parent);
    }
}
