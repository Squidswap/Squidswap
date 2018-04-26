package com.kinghorn.squidswap.squidswap;

import android.content.Context;

public class SquidSettingItem {

    public String label,type,prefName;

    public SquidSettingItem(String label,String type,String name){
        this.label = label;
        this.type = type;

        if(this.type == "switch"){
            this.prefName = name;
        }
    }
}
