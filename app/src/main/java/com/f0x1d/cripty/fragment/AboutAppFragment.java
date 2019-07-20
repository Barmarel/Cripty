package com.f0x1d.cripty.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.f0x1d.cripty.R;

public class AboutAppFragment extends Fragment {

    public static AboutAppFragment newInstance() {
        Bundle args = new Bundle();

        AboutAppFragment fragment = new AboutAppFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.about_app_fragment, container, false);

        v.findViewById(R.id.source_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/F0x1d/Cripty")));
            }
        });
        v.findViewById(R.id.support_me).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://yasobe.ru/na/f0x1d")));
            }
        });

        TextView createdBy = v.findViewById(R.id.created);
        createdBy.setSingleLine();
        createdBy.setText(Html.fromHtml(getString(R.string.created_by) + " <a href=\"https://t.me/f0x3d\">F0x1d</a>" + getString(R.string.apostrofe)));
        createdBy.setClickable(true);
        createdBy.setMovementMethod(LinkMovementMethod.getInstance());
        return v;
    }
}
