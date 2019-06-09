package e.ev.tlacrm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import e.ev.tlacrm.adapters.UsersListAdapter;
import e.ev.tlacrm.helpers.CircleTransform;
import e.ev.tlacrm.helpers.Fetch;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView rv;
    Fetch fetch;
    UsersListAdapter usersListAdapter;
    SharedPreferences.Editor editor;
    SharedPreferences sp;
    SwipeRefreshLayout swipeRefreshLayout;
    Dialog dialog;
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabClickAction();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        run();
    }

    private void fabClickAction() {

        String view = sp.getString("view", "home");
        
        switch (view) {

            case "users":
                Intent intent = new Intent(getApplicationContext(),NewUserActivity.class);
                startActivity(intent);
                break;
        }

    }

    private void run(){
        sp = getSharedPreferences("tlacrm", MODE_PRIVATE);
        editor = sp.edit();
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.progress_login);

        drawerLayout = findViewById(R.id.drawer_layout);

        fetch = new Fetch(this);
        rv = findViewById(R.id.list_view);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(manager);
        changeMainView();

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                changeMainView();
            }
        });

        View header = navigationView.getHeaderView(0);
        TextView username = header.findViewById(R.id.nav_header_username);
        TextView email = header.findViewById(R.id.nav_header_email);
        final ImageView avatar = header.findViewById(R.id.nav_header_avatar);

        username.setText(sp.getString("fullname", "User"));
        email.setText(sp.getString("email", ""));
        if(URLUtil.isValidUrl(sp.getString("avatar", ""))){
            Picasso.get().load(sp.getString("avatar", "")).transform(new CircleTransform()).into(avatar);
        }

        if(sp.getBoolean("isAdmin", true)) {
            Menu menu = navigationView.getMenu();
            menu.findItem(R.id.nav_setting).setVisible(true);
        }

    }

    private void changeMainView() {
        String view = sp.getString("view", "home");
        TextView tv = dialog.findViewById(R.id.progress_label);

        if(rv.getVisibility() == View.INVISIBLE) {
            rv.setVisibility(View.VISIBLE);
        }

       switch (view) {

           case "home":
               rv.setVisibility(View.INVISIBLE);
               tv.setText("Inicio...");
               dialog.show();
               break;

           case "users":
               tv.setText("Obteniendo usuarios");
               dialog.show();
               getUsers();
               break;

           case "clients":
               tv.setText("Obteniendo clientes");
               dialog.show();
               getClients();
               break;

       }

        dialog.dismiss();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void getUsers() {
        fetch.Get("/users/", new Fetch.OnResponse() {
            @Override
            public void onRespose(JSONObject response) {
                try {

                    JSONArray users = response.getJSONArray("users");
                    usersListAdapter = new UsersListAdapter(getApplicationContext(), users);
                    rv.setAdapter(usersListAdapter);
                    editor.putString("users", response.toString());
                    editor.commit();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG);
                }
            }

            @Override
            public void onResponseError(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Estas offline", Toast.LENGTH_LONG).show();
                usersListAdapter = new UsersListAdapter(getApplicationContext(), getOfflineData("users"));
                rv.setAdapter(usersListAdapter);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    private void getClients() {
        fetch.Get("/clients/", new Fetch.OnResponse() {
            @Override
            public void onRespose(JSONObject response) throws JSONException {
                if(response.getBoolean("error")) {

                }
            }

            @Override
            public void onResponseError(VolleyError error) {

            }
        });
    }

    private JSONArray getOfflineData(String module) {
        try {
            JSONObject object = new JSONObject(sp.getString(module, "{}"));
            JSONArray data = object.getJSONArray(module);
            return data;
        } catch (JSONException e) {
            e.printStackTrace();
            JSONArray data = new JSONArray();
            return data;
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint("Realiza tu busqueda");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchItems(s);
                return false;
            }
        });
        return true;
    }

    private void searchItems(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch(id){

            case R.id.nav_home:
                editor.putString("view", "home");
                break;

            case R.id.nav_users:
                editor.putString("view", "users");
                break;

            case R.id.nav_clients:
                editor.putString("view", "home");
                break;

            case R.id.nav_logout:

                final AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                builder.create();
                builder.setTitle("¿Deseas cerrar sesión?");
                builder.setMessage("Esta apunto de cerrar sesión");
                builder.setIcon(R.drawable.ic_add_alert_black_24dp);
                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editor.clear();
                        editor.commit();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        getApplicationContext().startActivity(intent);
                    }
                });

                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

                break;

        }

        editor.commit();
        changeMainView();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
