package com.f0x1d.cripty.utils;

import android.preference.PreferenceManager;

import com.f0x1d.cripty.App;
import com.f0x1d.cripty.R;

public class ThemeUtils {

    public static int getCurrentTheme() {
        if (PreferenceManager.getDefaultSharedPreferences(App.getInstance().getApplicationContext()).getBoolean("night", false)) {
            return R.style.AppTheme_Dark;
        } else {
            return R.style.AppTheme;
        }
    }

    public static int getCurrentPickerTheme() {
        if (PreferenceManager.getDefaultSharedPreferences(App.getInstance().getApplicationContext()).getBoolean("night", false)) {
            return R.style.AppTheme_Picker_Dark;
        } else {
            return R.style.AppTheme_Picker;
        }
    }
}
