package de.js_labs.gaminggroups;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;

import java.util.Arrays;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private Button signOutBtn;
    private Button resetPasswordBtn;
    private Button deleteAccountBtn;
    private ImageView AccountTypeBg;
    private ImageView AccountType;
    private TextView email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle(getResources().getString(R.string.menu_account));

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        setupUI();
    }

    private void setupUI(){
        signOutBtn = (Button) findViewById(R.id.buttonSignOut);
        resetPasswordBtn = (Button) findViewById(R.id.buttonResetPassword);
        deleteAccountBtn = (Button) findViewById(R.id.buttonDeleteAccount);

        signOutBtn.setOnClickListener(this);
        resetPasswordBtn.setOnClickListener(this);
        deleteAccountBtn.setOnClickListener(this);

        AccountTypeBg = (ImageView) findViewById(R.id.imageViewAccountTypeBg);
        AccountType = (ImageView) findViewById(R.id.imageViewAccountType);

        email = (TextView) findViewById(R.id.textViewEmail);

        switch (currentUser.getProviderData().get(1).getProviderId()){
            case "password":
                email.setText(currentUser.getEmail());
                break;
            case "phone":
                resetPasswordBtn.setEnabled(false);
                AccountTypeBg.setImageResource(R.drawable.fui_idp_button_background_phone);
                AccountType.setImageResource(R.drawable.fui_ic_settings_phone_white_24dp);
                email.setText(currentUser.getPhoneNumber());
                break;
            case "google.com":
                resetPasswordBtn.setEnabled(false);
                AccountTypeBg.setImageResource(R.drawable.fui_idp_button_background_google);
                AccountType.setImageResource(R.drawable.fui_ic_googleg_color_24dp);
                email.setText(currentUser.getEmail());
                break;
            case "twitter.com":
                resetPasswordBtn.setEnabled(false);
                AccountTypeBg.setImageResource(R.drawable.fui_idp_button_background_twitter);
                AccountType.setImageResource(R.drawable.fui_ic_twitter_bird_white_24dp);
                email.setText(currentUser.getDisplayName());
                break;
            default:
                resetPasswordBtn.setEnabled(false);
                FirebaseCrash.report(new Exception("Strange Provider: " + currentUser.getProviderData().get(1).getProviderId()));
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAlertDialog(String message, String postivBtn, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton(postivBtn, onClickListener)
                .setCancelable(false);
        builder.create().show();
    }

    @Override
    public void onClick(View view) {
        if(view == signOutBtn){
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            FirebaseCrash.report(e);
                            showAlertDialog(getResources().getString(R.string.dialog_unknown_error), getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            });
                        }
                    });
        }else if(view == resetPasswordBtn){
            firebaseAuth.sendPasswordResetEmail(currentUser.getEmail())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            showAlertDialog(getResources().getString(R.string.fui_confirm_recovery_body).replace("%1$s", currentUser.getEmail()), getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {@Override public void onClick(DialogInterface dialogInterface, int i) {}});
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            FirebaseCrash.report(e);
                            showAlertDialog(getResources().getString(R.string.dialog_unknown_error), getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            });
                        }
                    });
        }else if(view == deleteAccountBtn){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.account_delete_account));
            builder.setMessage(getResources().getString(R.string.account_delete_account_confirm))
                    .setPositiveButton(getResources().getString(R.string.account_delete_account_confirm_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            AuthUI.getInstance()
                                    .delete(AccountActivity.this)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        public void onComplete(@NonNull Task<Void> task) {
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            FirebaseCrash.report(e);
                                            showAlertDialog(getResources().getString(R.string.dialog_unknown_error), getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    finish();
                                                }
                                            });
                                        }
                                    });
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.account_delete_account_confirm_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
            builder.create().show();
        }
    }
}
