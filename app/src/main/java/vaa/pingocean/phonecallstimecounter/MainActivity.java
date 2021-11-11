package vaa.pingocean.phonecallstimecounter;

import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.CallLog;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import core.Helper;
import core.StatEntry;
import core.utils.OnPhoneChangedListener;
import core.utils.PhoneNumberTextWatcher;
import core.utils.Utils;
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
    boolean ready = false;

    EditText mEditTextValue;
    EditText meditTextLastThreeDays;
    EditText meditTextTotalCount;

    private Rect mRect = new Rect();

    @NeedsPermission({Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG})
    void readyForMonitoring() {
        if (!ready) Helper.getLastCall(this);
        else {

            StatEntry day3Times = Helper.OutgoingCallsTime(this, mLastEnteredPhone.replace(" ",""), 1);
            StatEntry allTimes = Helper.OutgoingCallsTime(this, mLastEnteredPhone.replace(" ",""), 0);
            meditTextTotalCount.setText( allTimes.getDuration() + " secs");
            meditTextLastThreeDays.setText( day3Times.getDuration() + " secs");
        }
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

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            final int action = MotionEventCompat.getActionMasked(event);

            int[] location = new int[2];
            mEditTextValue.getLocationOnScreen(location);
            mRect.left = location[0];
            mRect.top = location[1];
            mRect.right = location[0] + mEditTextValue.getWidth();
            mRect.bottom = location[1] + mEditTextValue.getHeight();

            int x = (int) event.getX();
            int y = (int) event.getY();

            if (action == MotionEvent.ACTION_DOWN && !mRect.contains(x, y)) {
                Utils.hideKeyboardFromFragment(MainActivity.this.getBaseContext(), mEditTextValue);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View contentView = (View) findViewById(R.id.mainView);
        contentView.setOnTouchListener(onTouchListener);

        mPhoneNumberUtil = PhoneNumberUtil.createInstance(this);
        MainActivityPermissionsDispatcher.readyForMonitoringWithPermissionCheck(this);
        mEditTextValue = findViewById(R.id.editTextPhone);
        meditTextLastThreeDays = findViewById(R.id.editTextLastThreeDays);
        meditTextTotalCount = findViewById(R.id.editTextTotalCount);

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
        Utils.hideKeyboardFromFragment(MainActivity.this.getBaseContext(), mEditTextValue);
        final Boolean[] cancelExit = {false};
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle("Leaving app in 2 secs")
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

        new CountDownTimer(1500, 500) {
            int cntDown = 3;

            @Override
            public void onTick(long millisUntilFinished) {
                cntDown = cntDown - 1;
                alert.setTitle("Leaving app in " + cntDown + " half secs");
            }

            @Override
            public void onFinish() {
                if (!cancelExit[0]) System.exit(0);
            }
        }.start();

    }

    public void checkPhoneState(View view) {
        Utils.hideKeyboardFromFragment(MainActivity.this.getBaseContext(), mEditTextValue);
        if (mLastEnteredPhone.length() > 0) {
            ready = true;
            readyForMonitoring();
        }
    }
}