package com.example.readotp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    public SmsBroadcastReceiverListener smsBroadcastReceiverListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Status smsRetrieverStatus = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

                if (smsRetrieverStatus != null) {
                    switch (smsRetrieverStatus.getStatusCode()) {
                        case CommonStatusCodes.SUCCESS:
                            Intent messageIntent = extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
                            if (smsBroadcastReceiverListener != null) {
                                smsBroadcastReceiverListener.onSuccess(messageIntent);
                            }
                            break;
                        case CommonStatusCodes.TIMEOUT:
                            if (smsBroadcastReceiverListener != null) {
                                smsBroadcastReceiverListener.onFailure();
                            }
                            break;
                    }
                }
            }
        }
    }

    public interface SmsBroadcastReceiverListener {
        void onSuccess(Intent intent);
        void onFailure();
    }
}