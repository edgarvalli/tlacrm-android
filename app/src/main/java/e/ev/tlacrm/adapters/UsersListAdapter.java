package e.ev.tlacrm.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import e.ev.tlacrm.R;
import e.ev.tlacrm.UserPreviewActivity;
import e.ev.tlacrm.helpers.CircleTransform;

public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.ViewHolder> {

    Context ctx;
    JSONArray users;
    public UsersListAdapter(Context ctx, JSONArray users){
        this.ctx = ctx;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(ctx).inflate(R.layout.list_item_user, viewGroup, false);
        ViewHolder holder = new ViewHolder(layout);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        try {

            final JSONObject user = users.getJSONObject(i);
            viewHolder.email.setText(user.getString("email"));
            viewHolder.name.setText(user.getString("name"));
            String avatar = ctx.getString(R.string.host) + "/image-profile/" + user.getString("_id");
            if(URLUtil.isValidUrl(avatar)) {
                Picasso.get().load(avatar).transform(new CircleTransform()).into(viewHolder.image);
            }

            viewHolder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ctx, UserPreviewActivity.class);
                    intent.putExtra("user", user.toString());
                    ctx.startActivity(intent);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return users.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView email, name;
        ImageView image;
        LinearLayout layout;
        public ViewHolder(LinearLayout v) {
            super(v);
            email = v.findViewById(R.id.email);
            name = v.findViewById(R.id.name);
            image = v.findViewById(R.id.user_avatar);
            layout = v.findViewById(R.id.user_item_layout);
        }

    }

}
