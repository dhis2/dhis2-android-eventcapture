package org.hisp.dhis.android.eventcapture.fragments;


import android.content.Context;
import android.text.Html;
import android.text.SpannableString;
import android.text.util.Linkify;

import org.hisp.dhis.android.sdk.R;

import java.io.InputStream;

public class AboutUsFragment extends org.hisp.dhis.android.sdk.ui.fragments.AboutUsFragment {

    private SpannableString getDescriptionMessage(Context context) {
        InputStream message = context.getResources().openRawResource(R.raw.description);
        String stringMessage = convertFromInputStreamToString(message).toString();
        final SpannableString linkedMessage = new SpannableString(Html.fromHtml(stringMessage));
        Linkify.addLinks(linkedMessage, Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
        return linkedMessage;
    }
}