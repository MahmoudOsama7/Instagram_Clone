package com.example.instagram.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram.Model.User;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/*
 1st the method public class ViewHolder extends RecyclerView.ViewHolder is activated as to initiialize the data
 then the method public ViewHolder(@NonNull View itemView) is used as to make a viewHolder(view) and then use it to attach the data in the layout with the variables
 then the method  public void onBindViewHolder(@NonNull ViewHolder holder, int position) { is used to access the data now
 the constructors are used as to get the data passed from the main
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private boolean isFragment;
    private FirebaseUser mauth ;   // is an object from FirebaseAuth , the same

    public UserAdapter(Context mContext, List<User> mUsers, boolean isFragment) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isFragment = isFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(mContext).inflate(R.layout.users_item,parent,false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        mauth=FirebaseAuth.getInstance().getCurrentUser();
        final User user = mUsers.get(position); // position as to get the position of the item selected in the recycler view
        holder.btnFollow.setVisibility(View.VISIBLE);
        holder.userName.setText(user.getUsername());
        holder.name.setText(user.getName());


        //to download image , we use picasso library
        Picasso.get().load(user.getImageUrl()).placeholder(R.mipmap.ic_launcher).into(holder.imageProfile);

        isFollowed(user.getId(),holder.btnFollow);

        if(user.getId().equals(mauth.getUid()))
        {
            holder.btnFollow.setVisibility(View.GONE);
        }


        holder.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.btnFollow.getText().toString().equals("Follow"))
                {
                    //Toast.makeText(mContext, holder.btnFollow.getText().toString(), Toast.LENGTH_SHORT).show();
                    // here will create a branch of follow and put branch inside includes the id of this user that will follow the user we see in the search
                    // and then make the value of this id to be true as an indicator that it;s following
                    FirebaseDatabase.getInstance().getReference("List").child("Follow").child(mauth.getUid()).child("Following").child(user.getId()).setValue(true);
                    // here will create a branch of follow and put branch inside include user that we followed and will be found in the search so after the follow request
                    // it now has followered so will put them in a branch called followers and put the current user signing in and put it;s id and set it;s value as true
                    FirebaseDatabase.getInstance().getReference("List").child("Follow").child(user.getId()).child("Followers").child(mauth.getUid()).setValue(true);
                }
                else
                {
                    //Toast.makeText(mContext, holder.btnFollow.getText().toString(), Toast.LENGTH_SHORT).show();
                    // not (here will create a branch of follow and put branch inside includes the id of this user that will follow the user we see in the search
                    // and then make the value of this id to be true as an indicator that it;s following)
                    FirebaseDatabase.getInstance().getReference("List").child("Follow").child(mauth.getUid()).child("Following").child(mUsers.get(position).getId()).removeValue();
                    // not (here will create a branch of follow and put branch inside include user that we followed and will be found in the search so after the follow request
                    // it now has followered so will put them in a branch called followers and put the current user signing in and put it;s id and set it;s value as true)
                    FirebaseDatabase.getInstance().getReference("List").child("Follow").child(mUsers.get(position).getId()).child("Followers").child(mauth.getUid()).removeValue();
                }
            }
        });
    }



    private void isFollowed(final String id, final Button button)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("List").child("Follow").child(mauth.getUid()).child("Following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(id).exists())
                {
                    button.setText("Following");
                }
                else
                {
                    button.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public CircleImageView imageProfile ;
        public TextView userName;
        public TextView name;
        public Button btnFollow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.image_profile);
            userName=itemView.findViewById(R.id.username);
            name=itemView.findViewById(R.id.name);
            btnFollow=itemView.findViewById(R.id.btn_follow);
        }
    }
}
