package com.example.readotp;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

public class number extends AppCompatActivity implements SmsBroadcastReceiver.SmsBroadcastReceiverListener {

    AppCompatButton generateOTPButton, verifyOTPButton;
    EditText editMobile, editOtp1, editOtp2, editOtp3, editOtp4, editOtp5, editOtp6;
    TextView textView5;
    ProgressBar progressBar;
    String verificationCode;
    PhoneAuthProvider.ForceResendingToken forceResendingToken;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    SmsBroadcastReceiver smsBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number);
        findViewById(R.id.cd3).setVisibility(View.GONE);

        generateOTPButton = findViewById(R.id.genOTP);
        verifyOTPButton = findViewById(R.id.verifyOTP1);
        textView5 = findViewById(R.id.textView5);
        editMobile = findViewById(R.id.editMobile);
        editOtp1 = findViewById(R.id.editText1);
        editOtp2 = findViewById(R.id.editText2);
        editOtp3 = findViewById(R.id.editText3);
        editOtp4 = findViewById(R.id.editText4);
        editOtp5 = findViewById(R.id.editText5);
        editOtp6 = findViewById(R.id.editText6);
        progressBar = findViewById(R.id.progressBar);
        TextView textView7 = findViewById(R.id.textView8);
        String text = "Didn’t receive the code? Request Again";

        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), text.indexOf("Request Again"), text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView7.setText(spannableString);
        editOtp1.requestFocus();
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(
                getApplicationContext(), R.array.numbers, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);


        generateOTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear previous OTP values and show the OTP view
                clearOtpFields();
                sendOtp(editMobile.getText().toString(), false);
                textView5.setText("Code is sent to " + editMobile.getText().toString());
            }
        });

        verifyOTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyOtp(
                        editOtp1.getText().toString() +
                                editOtp2.getText().toString() +
                                editOtp3.getText().toString() +
                                editOtp4.getText().toString() +
                                editOtp5.getText().toString() +
                                editOtp6.getText().toString()
                );
            }
        });

        // Initialize SMS Broadcast Receiver
        smsBroadcastReceiver = new SmsBroadcastReceiver();
        smsBroadcastReceiver.smsBroadcastReceiverListener = this;

        // Register SMS Broadcast Receiver
        registerReceiver(smsBroadcastReceiver, new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION));

        // Start listening for SMS retrieval
        startSmsListener();

        // Set TextWatcher for moving focus to the next EditText
        addTextWatcher(editOtp1, editOtp2);
        addTextWatcher(editOtp2, editOtp3);
        addTextWatcher(editOtp3, editOtp4);
        addTextWatcher(editOtp4, editOtp5);
        addTextWatcher(editOtp5, editOtp6);
    }

    private void addTextWatcher(final EditText currentEditText, final EditText nextEditText) {
        currentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (count > 0) {
                    // Move focus to the next EditText
                    nextEditText.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not used
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Clear OTP fields and hide views when the user comes back to this screen
        clearOtpFields();
        findViewById(R.id.cardViewOtp).setVisibility(View.GONE);
        findViewById(R.id.cd3).setVisibility(View.GONE);
        findViewById(R.id.genOTP).setVisibility(View.VISIBLE);
        findViewById(R.id.editMobile).setVisibility(View.VISIBLE);
        textView5.setText("Code is sent to " + editMobile.getText().toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister SMS Broadcast Receiver
        unregisterReceiver(smsBroadcastReceiver);
    }

    void startSmsListener() {
        SmsRetrieverClient client = SmsRetriever.getClient(this);

        // Start SMS retrieval
        client.startSmsRetriever();
    }

    void sendOtp(String phoneNumber, boolean isResend) {
        setInProgress(true);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber("+91" + phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        setInProgress(false);
                        // Handle the completed verification (auto-retrieval of OTP)
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(number.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        setInProgress(false);
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        super.onCodeSent(s, token);
                        verificationCode = s;
                        forceResendingToken = token;
                        Toast.makeText(number.this, "OTP Sent", Toast.LENGTH_SHORT).show();
                        setInProgress(false);
                        showOtpView();
                    }
                })
                .build();

        // Initiate the phone number verification with the options
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    void verifyOtp(String otp) {
        setInProgress(true);

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, otp);
        signInWithPhoneAuthCredential(credential);
    }

    void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        setInProgress(false);
                        Toast.makeText(number.this, "Verification Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, Verifivation.class);
                        startActivity(intent);
                        finish();

                    } else {
                        setInProgress(false);
                        Toast.makeText(number.this, "Verification Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearOtpFields() {
        editOtp1.setText("");
        editOtp2.setText("");
        editOtp3.setText("");
        editOtp4.setText("");
        editOtp5.setText("");
        editOtp6.setText("");
    }

    void setInProgress(boolean inProgress) {
        runOnUiThread(() -> {
            if (inProgress) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    void showOtpView() {
        runOnUiThread(() -> {

            findViewById(R.id.cardView).setVisibility(View.GONE);
            findViewById(R.id.con).setVisibility(View.GONE);
            findViewById(R.id.spinner).setVisibility(View.GONE);
            findViewById(R.id.editMobile).setVisibility(View.GONE);
            findViewById(R.id.cardViewOtp).setVisibility(View.VISIBLE);
            findViewById(R.id.cd3).setVisibility(View.VISIBLE);

            findViewById(R.id.genOTP).setVisibility(View.GONE);
            String num = editMobile.getText().toString();
            textView5.setText("Code is sent to"+" "+num );
        });
    }

    @Override
    public void onSuccess(Intent intent) {
        // Handle SMS retrieval success
        String message = intent.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE);
        // Extract OTP from the message and populate the OTP field
        if (message != null && message.length() > 0) {
            String otp = extractOtpFromMessage(message);
            // Assuming you want to set the OTP to the first EditText
            editOtp1.setText(otp);
        }
    }

    @Override
    public void onFailure() {
        // Handle SMS retrieval failure
        Toast.makeText(this, "SMS Retrieval Failed", Toast.LENGTH_SHORT).show();
    }

    private String extractOtpFromMessage(String message) {
        // Assuming the OTP format is a 6-digit number
        Pattern pattern = Pattern.compile("(\\b\\d{6}\\b)");
        Matcher matcher = pattern.matcher(message);

        // Check if a match is found
        if (matcher.find()) {
            return matcher.group(1); // Extract the matched 6-digit number
        } else {
            // If no match is found, handle the error or return an empty string
            return "";
        }
    }
}
