package com.kinghorn.squidswap.squidswap;

import android.graphics.Typeface;
import android.media.Image;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SquidSettings extends AppCompatActivity {
    private ListView settings_list;
    private ArrayList<SquidSettingItem> items;
    private SquidSettingsAdapter squid_adapt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squid_settings);

        ActionBar ac = getSupportActionBar();
        LayoutInflater infl = getLayoutInflater();
        RelativeLayout lay = (RelativeLayout) infl.inflate(R.layout.actionbar_layout,null);
        TextView settingTitle = (TextView) lay.findViewById(R.id.SquidswapTitle);
        Typeface fac = Typeface.createFromAsset(getAssets(),"fonts/AdmiralCAT.ttf");
        settingTitle.setText("Settings");
        settingTitle.setTypeface(fac);
        ImageButton setbtn = (ImageButton) lay.findViewById(R.id.SquidSwapSettings);
        setbtn.setVisibility(View.GONE);
        ac.setDisplayShowCustomEnabled(true);
        ac.setCustomView(lay);
        ac.setDisplayShowTitleEnabled(false);

        settings_list = (ListView) findViewById(R.id.SettingsList);

        items = new ArrayList<SquidSettingItem>();

        items.add(new SquidSettingItem("Autocrop Paint","check"));
        items.add(new SquidSettingItem("Unlock Features","text"));
        items.add(new SquidSettingItem("About","text"));

        squid_adapt = new SquidSettingsAdapter(getApplicationContext(),items);
        settings_list.setAdapter(squid_adapt);

    }
}
