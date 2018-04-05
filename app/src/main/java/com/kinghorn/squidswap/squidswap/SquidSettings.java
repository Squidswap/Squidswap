package com.kinghorn.squidswap.squidswap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ListView;

import java.util.List;

public class SquidSettings extends AppCompatActivity {
    private ListView settings_list;
    private List<SquidSettingItem> items;
    private SquidSettingsAdapter squid_adapt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squid_settings);

        settings_list = (ListView) findViewById(R.id.SettingsList);
        items = new List<SquidSettingItem>();


    }
}
