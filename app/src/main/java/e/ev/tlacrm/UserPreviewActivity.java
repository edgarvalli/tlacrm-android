package e.ev.tlacrm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import e.ev.tlacrm.helpers.CircleTransform;
import e.ev.tlacrm.helpers.Fetch;
import e.ev.tlacrm.helpers.Utils;

public class UserPreviewActivity extends AppCompatActivity {

    JSONObject user;
    JSONArray profiles;
    TextView phone, username, id, name, email, profileText;
    ImageView avatar, back, edit, camare, changePassword;
    Spinner spinner;
    int REQUEST_IMAGE_CAPTURE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_preview);
        init();
    }

    private void init() {
        try {

            user = new JSONObject(getIntent().getStringExtra("user"));

            phone = findViewById(R.id.preview_cellphone);
            username = findViewById(R.id.preview_username);
            id = findViewById(R.id.preview_id);
            name = findViewById(R.id.preview_name);
            email = findViewById(R.id.preview_email);
            changePassword = findViewById(R.id.preview_change_password);
            profileText = findViewById(R.id.preview_profile);

            avatar = findViewById(R.id.preview_avatar);
            back = findViewById(R.id.preview_back);
            edit = findViewById(R.id.preview_edit);
            camare = findViewById(R.id.preview_camara);
            REQUEST_IMAGE_CAPTURE = 0;

            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            camare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent camareIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if(camareIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(camareIntent, REQUEST_IMAGE_CAPTURE );
                    }
                }
            });

            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent camareIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if(camareIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(camareIntent, REQUEST_IMAGE_CAPTURE );
                    }
                }
            });

            String uri = getString(R.string.host) +"/image-profile/" + user.getString("_id");
            if(URLUtil.isValidUrl(uri)) {
                Picasso.get().load(uri).transform(new CircleTransform()).into(avatar);
            }

            setUserInfo(user);

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        final Dialog dialog = new Dialog(UserPreviewActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setCancelable(false);
                        dialog.setContentView(R.layout.user_edit);

                        final TextInputEditText fullname, cellphone, email;

                        fullname = dialog.findViewById(R.id.user_edit_fullname);
                        cellphone = dialog.findViewById(R.id.user_edit_cellphone);
                        email = dialog.findViewById(R.id.user_edit_email);
                        setSpiner(dialog);

                        fullname.setText(user.getString("name"));
                        cellphone.setText(user.getString("cellphone"));
                        email.setText(user.getString("email"));

                        Button cancel = dialog.findViewById(R.id.user_edit_cancel);
                        Button save = dialog.findViewById(R.id.user_edit_save);

                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        save.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                JSONObject body = new JSONObject();
                                try {

                                    JSONObject data = new JSONObject();

                                    data.put("name", fullname.getText());
                                    data.put("cellphone", cellphone.getText());
                                    data.put("email", email.getText());

                                    body.put("id", user.getString("_id"));
                                    body.put("data", data);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Fetch fetch = new Fetch(UserPreviewActivity.this);
                                fetch.Put("/users/", body, new Fetch.OnResponse() {
                                    @Override
                                    public void onRespose(JSONObject response) throws JSONException {
                                        if(response.getBoolean("error")) {
                                            alertResponseError(response.getString("message"));
                                        } else {
                                            user.put("name", fullname.getText());
                                            user.put("cellphone", cellphone.getText());
                                            user.put("email", email.getText());
                                            setUserInfo(user);
                                            dialog.dismiss();
                                        }
                                    }

                                    @Override
                                    public void onResponseError(VolleyError error) {
                                        alertResponseError(error.toString());
                                        dialog.dismiss();
                                    }
                                });
                            }
                        });

                        dialog.show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            changePassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(UserPreviewActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(true);
                    dialog.setContentView(R.layout.change_user_password);
                    dialog.show();

                    Button saveButton = dialog.findViewById(R.id.user_edit_password_save);
                    saveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            TextInputEditText pass = dialog.findViewById(R.id.user_edit_password);
                            if(pass.getText().equals("")) {
                               dialog.dismiss();
                            } else {
                                try {
                                    JSONObject params = new JSONObject();
                                    params.put("id", user.getString("_id"));
                                    params.put("password", pass.getText());
                                    Fetch fetch = new Fetch(getApplicationContext());
                                    fetch.Put("/users/update-password", params, new Fetch.OnResponse() {
                                        @Override
                                        public void onRespose(JSONObject response) {
                                            Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
                                            dialog.dismiss();
                                        }

                                        @Override
                                        public void onResponseError(VolleyError error) {
                                            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ocurrio un error al convertir JSON", Toast.LENGTH_LONG).show();
        }
    }

    private void alertResponseError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserPreviewActivity.this);
        builder.setTitle("Ocurrio un error en el servidor");
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void setSpiner(Dialog dialog) {
        spinner = dialog.findViewById(R.id.user_edit_spiner);
        Fetch fetch = new Fetch(getApplicationContext());
        fetch.Get("/users/profiles/", new Fetch.OnResponse() {
            @Override
            public void onRespose(JSONObject response) throws JSONException {
                if(response.getBoolean("error")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(UserPreviewActivity.this);
                    builder.setTitle("Ocurrio un error con el servidor");
                    builder.setMessage(response.getString("message"));
                    builder.show();
                } else {
                    profiles = response.getJSONArray("profiles");
                    ArrayList<String> profilesList = new ArrayList<String>();
                    for(int i = 0; i < profiles.length(); i++) {
                        profilesList.add(profiles.getJSONObject(i).getString("profileName"));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(UserPreviewActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, profilesList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            try {
                                user.put("profileId", profiles.getJSONObject(position).getString("_id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });


                }
            }

            @Override
            public void onResponseError(VolleyError error) {
                Log.d("VolleyResponse", error.toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                avatar.setImageBitmap(Utils.GetBitmapClippedCircle(imageBitmap));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                byte[] imageByte = outputStream.toByteArray();

                String imageStr = Base64.encodeToString(imageByte, Base64.DEFAULT);
                JSONObject params = new JSONObject();
                params.put("filename", user.getString("_id") + ".png");
                params.put("file", imageStr);

                Fetch fetch = new Fetch(getApplicationContext());
                fetch.Post("/users/upload-image", params, new Fetch.OnResponse() {
                    @Override
                    public void onRespose(JSONObject response) {
                        Toast.makeText(getApplicationContext(), "Imagen guardada", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponseError(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                        Log.d("UploadImage", error.toString());
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void setUserInfo(JSONObject userInfo) throws JSONException {

        JSONObject profile = user.getJSONObject("profile");

        name.setText(userInfo.getString("name"));
        id.setText(userInfo.getString("_id"));
        phone.setText(userInfo.getString("cellphone"));
        username.setText(userInfo.getString("username"));
        email.setText(userInfo.getString("email"));
        profileText.setText(profile.getString("profileName"));

    }

}
