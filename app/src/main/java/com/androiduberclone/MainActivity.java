package com.androiduberclone;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.androiduberclone.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn, btnRegister;
    RelativeLayout rootLayout;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Arkhip_font.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        setContentView(R.layout.activity_main);

        //  Init Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");


        //  Init View
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);

        //  Events
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegisterDialog();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoginDialog();
            }
        });
    }

    private void showLoginDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("SIGN IN");
        dialog.setMessage("Please use email to sig in");

        LayoutInflater inflater = LayoutInflater.from(this);

        View login_Layout = inflater.inflate(R.layout.layout_register,null);

        final MaterialEditText editEmail = login_Layout.findViewById(R.id.editEmail);
        final MaterialEditText editPassword = login_Layout.findViewById(R.id.editPassword);

        dialog.setView(login_Layout);
        dialog.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

                //  Check Validation
                if (TextUtils.isEmpty(editEmail.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter email address", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(editPassword.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter password", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (editPassword.getText().toString().length() < 6) {
                    Snackbar.make(rootLayout, "Password too strong !!!", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                auth.signInWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                startActivity(new Intent(MainActivity.this, Welcome.class));
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(rootLayout,"Failed : "+e.getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
    }

    private void showRegisterDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("REGISTER");
        dialog.setMessage("Please use email to register");

        LayoutInflater inflater = LayoutInflater.from(this);

        View registerLayout = inflater.inflate(R.layout.layout_register,null);

        final MaterialEditText editEmail = registerLayout.findViewById(R.id.editEmail);
        final MaterialEditText editPassword = registerLayout.findViewById(R.id.editPassword);
        final MaterialEditText editName = registerLayout.findViewById(R.id.editName);
        final MaterialEditText editPhone = registerLayout.findViewById(R.id.editPhone);

        dialog.setView(registerLayout);
        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

                //  Check Validation
                if(TextUtils.isEmpty(editEmail.getText().toString()))
                {
                    Snackbar.make(rootLayout, "Please enter email address", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(editPassword.getText().toString()))
                {
                    Snackbar.make(rootLayout, "Please enter password", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(editPassword.getText().toString().length() < 6)
                {
                    Snackbar.make(rootLayout, "Password too strong !!!", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(editName.getText().toString()))
                {
                    Snackbar.make(rootLayout, "Please enter your name", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(editPhone.getText().toString()))
                {
                    Snackbar.make(rootLayout, "Please enter phone number", Snackbar.LENGTH_SHORT).show();
                    return;
                }


                auth.createUserWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString())
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            //  Save user to db;

                            User user = new User();
                            user.setEmail(editEmail.getText().toString());
                            user.setPassword(editPassword.getText().toString());
                            user.setName(editName.getText().toString());
                            user.setPhone(editPhone.getText().toString());

                            users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Snackbar.make(rootLayout,"Registered Successful !!!", Snackbar.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Snackbar.make(rootLayout, "Failed : "+e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(rootLayout, "Failed : "+e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                    });
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }
}