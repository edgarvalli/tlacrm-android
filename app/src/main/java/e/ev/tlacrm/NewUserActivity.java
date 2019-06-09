package e.ev.tlacrm;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import e.ev.tlacrm.helpers.Fetch;

public class NewUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        handleSubmit();

    }

    private void handleSubmit() {

        Button save = findViewById(R.id.new_user_save);
        final TextView username, fullname, cellphone, email, password;
        username = findViewById(R.id.new_user_username);
        fullname = findViewById(R.id.new_user_fullname);
        cellphone = findViewById(R.id.new_user_cellphone);
        email = findViewById(R.id.new_user_email);
        password = findViewById(R.id.new_user_password);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject body = new JSONObject();
                    JSONObject user = new JSONObject();

                    user.put("username", username.getText());
                    user.put("fullname", fullname.getText());
                    user.put("cellphone", cellphone.getText());
                    user.put("email", email.getText());
                    user.put("password", password.getText());

                    body.put("user", user);


                    Fetch fetch = new Fetch(NewUserActivity.this);

                    fetch.Post("/users/add", body, new Fetch.OnResponse() {
                        @Override
                        public void onRespose(JSONObject response) throws JSONException {
                            finish();
                        }

                        @Override
                        public void onResponseError(VolleyError error) {
                            finish();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}

