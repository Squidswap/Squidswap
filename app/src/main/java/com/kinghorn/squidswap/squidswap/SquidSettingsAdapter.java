package com.kinghorn.squidswap.squidswap;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SquidSettingsAdapter extends ArrayAdapter<SquidSettingItem> {
    private ArrayList<SquidSettingItem> items;
    private Context ctx;
    private SquidSettingsManager setManage;

    public SquidSettingsAdapter(@NonNull Context context, ArrayList<SquidSettingItem> list) {
        super(context,0,list);

        this.items = list;
        this.ctx = context;
        this.setManage = new SquidSettingsManager(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context c = this.ctx;
        if(convertView == null){
            switch(this.items.get(position).type){
                case "check":
                    convertView = LayoutInflater.from(this.ctx).inflate(R.layout.squid_setting_check,parent,false);
                    TextView switch_label = (TextView) convertView.findViewById(R.id.switch_text);
                    switch_label.setText(this.items.get(position).label);
                    final CheckBox settings_switch = (CheckBox) convertView.findViewById(R.id.settings_check);
                    settings_switch.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            setManage.SaveBoolSetting("SquidswapAutocropPaint",settings_switch.isChecked());
                            Toast.makeText(c,String.valueOf(setManage.LoadBoolSetting("SquidswapAutocropPaint")),Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case "text":
                    convertView = LayoutInflater.from(this.ctx).inflate(R.layout.squid_setting_text,parent,false);
                    TextView text_label = (TextView) convertView.findViewById(R.id.settings_text);
                    text_label.setText(this.items.get(position).label);
                    break;
            }
        }
        return convertView;
    }


}
