package vaa.pingocean.phonecallstimecounter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.CallLog;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import core.Helper;
import core.StatEntry;
import core.utils.OnPhoneChangedListener;
import core.utils.PhoneNumberTextWatcher;
import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;


@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    PhoneNumberUtil mPhoneNumberUtil = null;
    String mLastEnteredPhone = "";

    @NeedsPermission({Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG})
    void readyForMonitoring() {
        StatEntry resultNeeded = Helper.OutgoingTotalTime(this);
        String result = Helper.getLastCall(this);
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }

    @OnShowRationale({Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG})
    void showRationaleForState(final PermissionRequest request) {

        new AlertDialog.Builder(this)
                .setMessage(R.string.message)
                .setPositiveButton(R.string.rationale_ask_ok, (dialog, button) -> request.proceed())
                .setNegativeButton(R.string.rationale_ask_cancel, (dialog, button) -> request.cancel())
                .show();


    }

    @OnPermissionDenied({Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG})
    void showDeniedForState() {
        Toast.makeText(this, R.string.denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain({Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG})
    void showNeverAskForState() {
        Toast.makeText(this, R.string.never_ask, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);

    }

    protected OnPhoneChangedListener mOnPhoneChangedListener = new OnPhoneChangedListener() {
        @Override
        public void onPhoneChanged(String phone) {
            try {
                mLastEnteredPhone = phone;
                Phonenumber.PhoneNumber p = mPhoneNumberUtil.parse(phone, null);
            } catch (NumberParseException ignore) {
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPhoneNumberUtil = PhoneNumberUtil.createInstance(this);
        MainActivityPermissionsDispatcher.readyForMonitoringWithPermissionCheck(this);
        EditText mEditTextValue = findViewById(R.id.editTextPhone);

        PhoneNumberTextWatcher phoneNumberTextWatcher = new PhoneNumberTextWatcher(mOnPhoneChangedListener, getBaseContext());
        mEditTextValue.addTextChangedListener(phoneNumberTextWatcher);
        InputFilter filter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (dstart > 0 && !Character.isDigit(c)) {
                    return "";
                }
            }
            return null;
        };
        mEditTextValue.setFilters(new InputFilter[]{filter});
        mEditTextValue.setImeOptions(EditorInfo.IME_ACTION_SEND);
        mEditTextValue.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return actionId == EditorInfo.IME_ACTION_SEND;
            }
        });
    }


    public void closeApplication(View view) {
        final Boolean[] cancelExit = {false};
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle("Leaving app in 3 secs")
                .setMessage("Application close shortly");
        dialog.setPositiveButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        cancelExit[0] = true;
                    }
                });
        final AlertDialog alert = dialog.create();
        alert.show();

        new CountDownTimer(2000, 1000) {
            int cntDown = 2;

            @Override
            public void onTick(long millisUntilFinished) {
                cntDown = cntDown - 1;
                alert.setTitle("Leaving app in " + cntDown + " secs");
            }

            @Override
            public void onFinish() {
                if (!cancelExit[0]) System.exit(0);
            }
        }.start();

    }

    public void checkPhoneState(View view) {
        Toast.makeText(this, mLastEnteredPhone, Toast.LENGTH_SHORT).show();
    }
}