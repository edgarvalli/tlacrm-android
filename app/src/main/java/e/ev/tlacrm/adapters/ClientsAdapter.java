package e.ev.tlacrm.adapters;

import android.content.Context;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import e.ev.tlacrm.R;

public class ClientsAdapter extends RecyclerView.Adapter<ClientsAdapter.ViewHolder> {

    JSONArray clients;
    Context ctx;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(ctx).inflate(R.layout.list_item_user, viewGroup, false);
        ViewHolder holder = new ViewHolder(layout);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        try {
            viewHolder.username.setText(clients.getJSONObject(i).getString("name"));
            viewHolder.email.setText(clients.getJSONObject(i).getString("cellphone"));
            viewHolder.imageView.setVisibility(View.INVISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return clients.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView username, email;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            email = itemView.findViewById(R.id.email);
            imageView = itemView.findViewById(R.id.user_avatar);
        }
    }
}
