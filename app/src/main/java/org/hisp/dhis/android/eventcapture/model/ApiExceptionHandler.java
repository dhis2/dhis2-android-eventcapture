/*
 *  Copyright (c) 2016, University of Oslo
 *
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *  Neither the name of the HISP project nor the names of its contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.eventcapture.model;

import android.content.Context;

import org.hisp.dhis.android.eventcapture.R;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.core.common.network.Response;

import java.net.HttpURLConnection;

public class ApiExceptionHandler {
    Context context;

    public ApiExceptionHandler(Context context) {
        this.context = context;
    }

    public AppError handleException(final Throwable apiException) {
        String title = context.getText(R.string.error).toString();
        String message;
        ApiException.Kind kind;

        if (apiException instanceof ApiException) {
            int status = -1;
            kind = ((ApiException) apiException).getKind();
            Response response = ((ApiException) apiException).getResponse();

            if (response != null) {
                status = response.getStatus();
            }

            switch (status) {
                case (HttpURLConnection.HTTP_BAD_REQUEST):
                    message = context.getText(R.string.error_unauthorized).toString();
                    break;
                case (HttpURLConnection.HTTP_UNAUTHORIZED):
                    message = context.getText(R.string.error_unauthorized).toString();
                    break;
                case (HttpURLConnection.HTTP_FORBIDDEN):
                    message = context.getText(R.string.error_unauthorized).toString();
                    break;
                case (HttpURLConnection.HTTP_NOT_FOUND):
                    message = context.getText(R.string.error_not_found).toString();
                    break;
                case (HttpURLConnection.HTTP_INTERNAL_ERROR):
                    message = context.getText(R.string.error_internal).toString();
                    break;
                case (HttpURLConnection.HTTP_UNAVAILABLE):
                    message = context.getText(R.string.error_unavailable).toString();
                    break;
                default:
                    message = context.getText(R.string.error_unexpected).toString()
                            + "\n" + apiException.getMessage();
                    apiException.printStackTrace();
            }
        } else {
            kind = ApiException.Kind.UNEXPECTED;
            message = context.getText(R.string.error_unexpected).toString()
                    + apiException.getMessage();
            apiException.printStackTrace();
        }
        return new AppError(title, message, kind, apiException);
    }
}
