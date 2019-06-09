package e.ev.tlacrm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import e.ev.tlacrm.helpers.Fetch;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText username, password;
    CheckBox autoLogin;
    Button buttonLogin;
    Dialog dialog;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sp = getSharedPreferences("tlacrm", this.MODE_PRIVATE);
        editor = sp.edit();
        if(sp.getBoolean("loggedIn", false)) {
           Intent intent = new Intent(getApplicationContext(), Home.class);
           startActivity(intent);
        } else {
            onRun();
        }

    }

    private void onRun() {
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        autoLogin = findViewById(R.id.autologin);
        buttonLogin = findViewById(R.id.login_button);
        dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.progress_login);


        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                try {
                    Fetch fetch = new Fetch(getApplicationContext());
                    final JSONObject body = new JSONObject();
                    body.put("username", username.getText());
                    body.put("password", password.getText());
                    body.put("autoLogin", autoLogin.isChecked());
                    fetch.Post("/users/login", body, new Fetch.OnResponse() {
                        @Override
                        public void onRespose(JSONObject response) {
                            try {

                                dialog.dismiss();

                                if(response.getBoolean("error")) {
                                    onLoginError(response.getString("message"));
                                } else {

                                    JSONObject user = response.getJSONObject("user");
                                    JSONObject profile = user.getJSONObject("profile");


                                    editor.putBoolean("loggedIn", true);
                                    editor.putString("token", response.getString("token"));
                                    editor.putString("fullname", user.getString("name"));
                                    editor.putString("username", user.getString("username"));
                                    editor.putString("email", user.getString("email"));
                                    editor.putString("password", body.getString("password"));
                                    editor.putString("avatar",  getString(R.string.host) +"/image-profile/" + user.getString("_id"));
                                    editor.putString("modules", profile.getJSONArray("modules").toString());
                                    editor.putBoolean("isAdmin", profile.getBoolean("isAdmin"));
                                    editor.putBoolean("autoLogin", autoLogin.isChecked());
                                    editor.commit();

                                    Log.d("ResponseJSON", "here");

                                    Intent intent = new Intent(LoginActivity.this, Home.class);
                                    startActivity(intent);

                                }

                            } catch(JSONException e) {
                                e.printStackTrace();
                                onLoginError(e.toString());
                            }
                        }

                        @Override
                        public void onResponseError(VolleyError error) {
                            onLoginError(error.toString());
                            dialog.dismiss();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void onLoginError(String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Ocurrio un errror al iniciar sesión");
        builder.setMessage(error);
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(true);
        builder.create();
        builder.show();
    }
}
