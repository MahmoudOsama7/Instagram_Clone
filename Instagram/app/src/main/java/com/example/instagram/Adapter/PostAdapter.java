package com.example.instagram.Adapter;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram.CommentActivity;
import com.example.instagram.Model.Post;
import com.example.instagram.Model.User;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.Viewholder> {


    private Context mcContext;
    private List<Post> mPosts;
    private FirebaseUser firebaseUser;

    public PostAdapter(Context mcContext, List<Post> mPosts) {
        this.mcContext = mcContext;
        this.mPosts = mPosts;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcContext).inflate(R.layout.post_item,parent,false);
        return new PostAdapter.Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Viewholder holder, int position) {
        final Post post = mPosts.get(position);
        //load the image got from the arraylist to be loaded in the recyclerview holder post image
        Picasso.get().load(post.getImageUrl()).into(holder.postImage); // the image that the user posted so that our user is following will see it
        //load the description got from the arraylist to be loaded in the recyclerview holder description
        holder.description.setText(post.getDescription());


        //will go to the reference and then child of userid that got from arraylist and passed to the recyclerview holder
        FirebaseDatabase.getInstance().getReference("Writing").child(post.getPublisherDetails()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user.getImageUrl().equals("default"))
                {
                    holder.imageProfile.setImageResource(R.mipmap.ic_launcher); // the image profile od the user posting the image
                }
                else
                {
                    // load image from imageurl of children attribute imageUrl to be put in holder image profile attribute
                    Picasso.get().load(user.getImageUrl()).into(holder.imageProfile);
                }
                // load image from imageurl of children attribute imageUrl to be put in holder image profile attribute
                holder.username.setText(user.getUsername());
                holder.author.setText(user.getName());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        isLiked(post.getPostId(),holder.like);
        noOfLikes(post.getPostId(),holder.noOfLikes);
        getComments(post.getPostId(),holder.noOfComments);
        isSaved(post.getPostId(),holder.save);

        // to like the post or not like it
        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.like.getTag().equals("like"))
                {
                    // it means this post will be set to be liked by our firebase user signing in
                    FirebaseDatabase.getInstance().getReference("Likes").child(post.getPostId()).child(firebaseUser.getUid()).setValue(true);
                }
                else
                {
                    // it means this post will be set to be not liked by our firebase user signing in
                    FirebaseDatabase.getInstance().getReference("Likes").child(post.getPostId()).child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mcContext, CommentActivity.class); // it will go to the activity comment
                intent.putExtra("postId",post.getPostId()); // it will pass the user id  posting this comment
                intent.putExtra("authorId",post.getPublisherDetails()); // it will pass the post itself that i want to comment on it
                mcContext.startActivity(intent);
            }
        });

        holder.noOfComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mcContext, CommentActivity.class); // it will go to the activity comment
                intent.putExtra("postId",post.getPostId()); // it will pass the user id  posting this comment
                intent.putExtra("authorId",post.getPublisherDetails()); // it will pass the post itself that i want to comment on it
                mcContext.startActivity(intent);
            }
        });


        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.save.getTag().equals("save"))
                {
                    FirebaseDatabase.getInstance().getReference("Saved").child(firebaseUser.getUid()).child(post.getPostId()).setValue(true);
                }
                else
                {
                    FirebaseDatabase.getInstance().getReference("Saved").child(firebaseUser.getUid()).child(post.getPostId()).removeValue() ;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder
    {
        public ImageView imageProfile;
        public ImageView postImage;
        public ImageView like;
        public ImageView comment;
        public ImageView save;
        public ImageView more;

        public TextView username;
        public TextView noOfLikes;
        public TextView author;
        public TextView noOfComments;

        SocialTextView description;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            imageProfile = itemView.findViewById(R.id.image_profile);
            postImage = itemView.findViewById(R.id.post_image);
            like = itemView.findViewById(R.id.like);
            comment=itemView.findViewById(R.id.comment);
            save= itemView.findViewById(R.id.save);
            more =itemView.findViewById(R.id.more);

            username =itemView.findViewById(R.id.username);
            noOfLikes=itemView.findViewById(R.id.no_of_likes);
            author =itemView.findViewById(R.id.author);
            noOfComments =itemView.findViewById(R.id.no_of_comments);

            description=itemView.findViewById(R.id.description);
        }
    }
    private void isLiked(String postId, final ImageView imageView) // to check post already liked or not
    {
        FirebaseDatabase.getInstance().getReference("Likes").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                // it means inside this root of likes under postId if our signing user exists or not
                if(dataSnapshot.child(firebaseUser.getUid()).exists())
                {
                    imageView.setImageResource(R.drawable.ic_liked);  // it will show the heart as red so that the post is liked
                    imageView.setTag("liked");
                }
                else
                {
                    imageView.setImageResource(R.drawable.ic_like); // it will show the heart as normal so that post is not liked
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void noOfLikes(String postId, final TextView text)
    {
        FirebaseDatabase.getInstance().getReference("Likes").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                text.setText(snapshot.getChildrenCount() + " Likes");  // we cant use toString as the values returned is long type so we use this format
                // to state that string values
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getComments(String postId, final TextView text)
    {
        FirebaseDatabase.getInstance().getReference("Comments").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                text.setText("View All " + snapshot.getChildrenCount() + " Comments ");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void isSaved(final String postId, final ImageView save)
    {
        FirebaseDatabase.getInstance().getReference("Saved").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postId).exists())
                {
                    save.setImageResource(R.drawable.ic_save_black);
                    save.setTag("saved");
                }
                else
                {
                    save.setImageResource(R.drawable.ic_save);
                    save.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
