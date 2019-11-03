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
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.f0x1d.cripty.R;
import com.f0x1d.cripty.activity.MainActivity;

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

        Toolbar toolbar = v.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.about_app);
        toolbar.setNavigationIcon(((MainActivity) getActivity()).getDefaultPreferences().getBoolean("night", false)
                ? R.drawable.ic_arrow_back_white_24dp : R.drawable.ic_arrow_back_black_24dp);

        toolbar.setNavigationOnClickListener(v13 -> getActivity().getSupportFragmentManager().popBackStack());
        v.findViewById(R.id.source_code).setOnClickListener(v1 -> startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/F0x1d/Cripty"))));
        v.findViewById(R.id.support_me).setOnClickListener(v12 -> startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://yasobe.ru/na/f0x1d"))));

        TextView createdBy = v.findViewById(R.id.created);
        createdBy.setSingleLine();
        createdBy.setText(Html.fromHtml(getString(R.string.created_by) + " <a href=\"https://t.me/f0x3d\">F0x1d</a>" + getString(R.string.apostrofe)));
        createdBy.setClickable(true);
        createdBy.setMovementMethod(LinkMovementMethod.getInstance());
        return v;
    }
}
