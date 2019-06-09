package e.ev.tlacrm.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import e.ev.tlacrm.LoginActivity;
import e.ev.tlacrm.R;

public class Fetch {

    Context ctx;
    RequestQueue request;

    public Fetch(Context ctx){
        this.ctx = ctx;
        request = Volley.newRequestQueue(ctx);
    }

    public interface OnResponse {
        void onRespose(JSONObject response) throws JSONException;
        void onResponseError(VolleyError error);
    }

    public void Post(String url, final JSONObject body,final OnResponse resp) {
        final String uri = ctx.getString(R.string.api) + url;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, uri, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.has("tokenExpired") && response.getBoolean("tokenExpired")) {
                        renewToken();
                        Post(uri, body, resp);
                    }
                    resp.onRespose(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                resp.onResponseError(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences sp = ctx.getSharedPreferences("tlacrm", ctx.MODE_PRIVATE);
                if(sp.contains("token")) {
                    params.put("token", sp.getString("token", ""));
                } else {
                    params.put("token", "");
                }

                return params;
            }
        };
        request.add(req);
    }
    public void Get(String url, final OnResponse resp) {
        final String uri = ctx.getString(R.string.api) + url;
        StringRequest req = new StringRequest(Request.Method.GET, uri, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject r = new JSONObject(response);
                    if(r.has("tokenExpired") && r.getBoolean("tokenExpired")) {
                        renewToken();
                        Get(uri, resp);
                    }
                    resp.onRespose(r);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                resp.onResponseError(error);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences sp = ctx.getSharedPreferences("tlacrm", ctx.MODE_PRIVATE);
                if(sp.contains("token")) {
                    params.put("token", sp.getString("token", ""));
                } else {
                    params.put("token", "");
                }

                return params;
            }
        };

        request.add(req);
    }
    public void Put(String url , final  JSONObject body, final OnResponse resp) {
        final String uri = ctx.getString(R.string.api) + url;
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT,uri, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.has("tokenExpired") && response.getBoolean("tokenExpired")) {
                        renewToken();
                        Get(uri, resp);
                    }
                    resp.onRespose(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                resp.onResponseError(error);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences sp = ctx.getSharedPreferences("tlacrm", ctx.MODE_PRIVATE);
                if(sp.contains("token")) {
                    params.put("token", sp.getString("token", ""));
                } else {
                    params.put("token", "");
                }

                return params;
            }
        };

        request.add(req);
    }
    private void renewToken() {
        try {
            final SharedPreferences sp = ctx.getSharedPreferences("tlacrm", ctx.MODE_PRIVATE);
            final  SharedPreferences.Editor editor = sp.edit();
            JSONObject body = new JSONObject();
            body.put("username", sp.getString("username", ""));
            body.put("password", sp.getString("password", ""));
            body.put("autoLogin", sp.getBoolean("autoLogin", Boolean.parseBoolean("")));

            String url = ctx.getString(R.string.api) + "/users/login";

            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, body, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if(response.getBoolean("error")) {
                            editor.putBoolean("loggedIn", false);
                            editor.commit();
                            Intent intent = new Intent(ctx, LoginActivity.class);
                            ctx.startActivity(intent);
                        } else {
                            editor.putString("token", response.getString("token"));
                            editor.commit();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.getNetworkTimeMs();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    SharedPreferences sp = ctx.getSharedPreferences("tlacrm", ctx.MODE_PRIVATE);
                    if(sp.contains("token")) {
                        params.put("token", sp.getString("token", ""));
                    } else {
                        params.put("token", "");
                    }

                    return super.getParams();
                }
            };
            request.add(req);



        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
