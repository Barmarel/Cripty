package com.f0x1d.cripty.activity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.f0x1d.cripty.R;
import com.f0x1d.cripty.fragment.SettingsFragment;
import com.f0x1d.cripty.utils.ThemeUtils;
import com.f0x1d.cripty.view.CenteredToolbar;

public class SettingsActivity extends AppCompatActivity {

    private CenteredToolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(ThemeUtils.getCurrentTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setNavigationIcon(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("night", false)
                ? R.drawable.ic_arrow_back_white_24dp : R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, SettingsFragment.newInstance())
                .commit();
    }
}
