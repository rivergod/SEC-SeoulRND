package net.rivergod.sec.seoulrnd.android.menu;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LicenseDialog extends Dialog {

    private final static String TAG = LicenseDialog.class.getName();

    public LicenseDialog(Context mainClass) {
        super(mainClass, android.R.style.Theme_Translucent_NoTitleBar);
        setContentView(R.layout.license_dialog);

        ((Button)findViewById(R.id.license_go_page_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/rivergod/SEC-SeoulRND")));
            }
        });

        findViewById(R.id.custom_popup_msg_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        String licenseString = "";

        try {
            InputStream inputStream = mainClass.getResources().openRawResource(R.raw.txt_license);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int i;
            i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();

            licenseString = byteArrayOutputStream.toString();
        } catch (IOException e) {
            Log.d(TAG, "Cannot open license resource.");
        }

        TextView msgTextView = ((TextView)findViewById(R.id.custom_popup_msg));
        msgTextView.setMovementMethod(new ScrollingMovementMethod());
        msgTextView.setText(licenseString);
    }
}
