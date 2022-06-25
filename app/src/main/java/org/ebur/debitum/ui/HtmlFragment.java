package org.ebur.debitum.ui;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import org.ebur.debitum.R;

public class HtmlFragment extends DialogFragment {

    private final String ARG_HTML_FILE_URI = "html_asset_uri";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Debitum_FullScreenDialog);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Debitum_FloatingDialog);
        //setStyle(DialogFragment.STYLE_NORMAL, 0);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_html, container, false);
        setupHtmlView(root);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Toolbar toolbar = view.findViewById(R.id.dialog_toolbar);
        // use label from navigation graph's destination as toolbar title
        NavController nav = NavHostFragment.findNavController(this);
        NavDestination dest = nav.getCurrentDestination();
        assert dest != null;
        CharSequence title = dest.getLabel();
        toolbar.setTitle(title);
        toolbar.setNavigationOnClickListener(v -> dismiss());
    }

    private void setupHtmlView(View root) {
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_HTML_FILE_URI)) {
            Resources res = getResources();
            WebView htmlView = root.findViewById(R.id.html_view);
            // set light or dark mode depending on current system setting
            // https://developer.android.com/guide/webapps/dark-theme#java
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        WebSettingsCompat.setForceDark(htmlView.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                    case Configuration.UI_MODE_NIGHT_UNDEFINED:
                        WebSettingsCompat.setForceDark(htmlView.getSettings(), WebSettingsCompat.FORCE_DARK_OFF);
                        break;
                }
            }

            htmlView.getSettings().setJavaScriptEnabled(false);
            htmlView.loadUrl(res.getString(args.getInt(ARG_HTML_FILE_URI)));
        }
    }
}
