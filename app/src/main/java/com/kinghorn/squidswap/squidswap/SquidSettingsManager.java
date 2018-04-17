package com.kinghorn.squidswap.squidswap;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;

//Class that will handle loading and saving of settings accross the entire application.
public class SquidSettingsManager {
    private Context cont;
    private SharedPreferences prefs;
    private SharedPreferences.Editor edit;

    public SquidSettingsManager(Context c){
        this.cont = c;
    }

    //String preference getters and setters.
    public String LoadStringSetting(String name){
        this.prefs = this.cont.getSharedPreferences(name,Context.MODE_PRIVATE);
        String retrieved = this.prefs.getString(name,null);
        return retrieved;
    }

    public void SaveStringSetting(String name,String value){
        this.edit = this.cont.getSharedPreferences(name, Context.MODE_PRIVATE).edit();
        this.edit.putString(name,value);
        this.edit.apply();
    }

    //Boolean preference getters and setters.
    public boolean LoadBoolSetting(String name){
        this.prefs = this.cont.getSharedPreferences(name,Context.MODE_PRIVATE);
        return this.prefs.getBoolean(name,false);
    }

    public void SaveBoolSetting(String name,boolean value){
        this.edit = this.cont.getSharedPreferences(name,Context.MODE_PRIVATE).edit();
        this.edit.putBoolean(name,value);
        this.edit.apply();
    }
}
