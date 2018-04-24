package com.kinghorn.squidswap.squidswap;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.Image;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SquidSettings extends AppCompatActivity {
    private ListView settings_list;
    private ArrayList<SquidSettingItem> items;
    private SquidSettingsAdapter squid_adapt;
    private Button SendEmail;

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
        ImageButton layBtn = (ImageButton) lay.findViewById(R.id.LayersToggle);
        ImageButton backBtn = (ImageButton) lay.findViewById(R.id.SettingsBack);
        ImageButton rateBtn = (ImageButton) lay.findViewById(R.id.RateButton);

        rateBtn.setVisibility(View.GONE);
        backBtn.setVisibility(View.VISIBLE);
        layBtn.setVisibility(View.GONE);
        setbtn.setVisibility(View.GONE);
        ac.setDisplayShowCustomEnabled(true);
        ac.setCustomView(lay);
        ac.setDisplayShowTitleEnabled(false);

        settings_list = (ListView) findViewById(R.id.SettingsList);

        items = new ArrayList<SquidSettingItem>();

        //items.add(new SquidSettingItem("Autocrop Paint","check"));
        //items.add(new SquidSettingItem("Crop to Background","check"));
        items.add(new SquidSettingItem("Unlock Features","text"));
        items.add(new SquidSettingItem("Clear Cached Images","text"));
        items.add(new SquidSettingItem("About","text"));

        squid_adapt = new SquidSettingsAdapter(getApplicationContext(),items);
        settings_list.setAdapter(squid_adapt);
        final AlertDialog.Builder buil = new AlertDialog.Builder(SquidSettings.this);
        final LayoutInflater flate = getLayoutInflater();

        settings_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 2:
                        RelativeLayout r = (RelativeLayout) flate.inflate(R.layout.about_dialog,null);
                        SendEmail = (Button) r.findViewById(R.id.EmailButton);

                        SendEmail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("plain/text");
                                intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "dev@squidswap.com" });
                                intent.putExtra(Intent.EXTRA_SUBJECT, "Squidswap Contact");
                                intent.putExtra(Intent.EXTRA_TEXT, "");
                                startActivity(Intent.createChooser(intent, ""));
                            }
                        });

                        buil.setView(r);
                        buil.setTitle("About").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                        break;
                    case 0:
                        Toast.makeText(getApplicationContext(),"New features coming soon...",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
