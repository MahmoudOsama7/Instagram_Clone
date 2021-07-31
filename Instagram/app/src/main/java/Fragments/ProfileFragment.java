package Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.instagram.Adapter.PhotoAdapter;
import com.example.instagram.Adapter.PostAdapter;
import com.example.instagram.Model.Post;
import com.example.instagram.Model.User;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.type.PostalAddress;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {

    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private List<Post>myPhotoList;

    private RecyclerView recyclerViewSaves;
    private PhotoAdapter postAdapterSaves;
    private List<Post> mySavedPosts;


    private CircleImageView imageProfile;
    private ImageView options;
    private TextView posts;
    private TextView followers;
    private TextView followings;
    private TextView fullName;
    private TextView bio;
    private TextView userName;
    private Button editProfile;
    private ImageView myPictures;
    private ImageView savedPictures;
    String profileId;

    private FirebaseUser fUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imageProfile=view.findViewById(R.id.image_profile);
        options=view.findViewById(R.id.options);
        posts=view.findViewById(R.id.posts);
        followers=view.findViewById(R.id.followers);
        followings=view.findViewById(R.id.followings);
        fullName=view.findViewById(R.id.fullname);
        bio=view.findViewById(R.id.bio);
        userName=view.findViewById(R.id.username);
        fUser= FirebaseAuth.getInstance().getCurrentUser();
        editProfile=view.findViewById(R.id.edit_profile);
        myPictures=view.findViewById(R.id.my_pictures);
        savedPictures=view.findViewById(R.id.saved_pictures);
        recyclerView=view.findViewById(R.id.recycler_view_pictures);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),3));
        myPhotoList=new ArrayList<>();
        photoAdapter=new PhotoAdapter(getContext(),myPhotoList);
        recyclerView.setAdapter(photoAdapter);

        recyclerViewSaves=view.findViewById(R.id.recycler_view_saved);
        recyclerViewSaves.setHasFixedSize(true);
        recyclerViewSaves.setLayoutManager(new GridLayoutManager(getContext() , 3));
        mySavedPosts=new ArrayList<>();
        postAdapterSaves=new PhotoAdapter(getContext(),mySavedPosts);
        recyclerViewSaves.setAdapter(postAdapterSaves);

        String data = getContext().getSharedPreferences("PROFILE", Context.MODE_PRIVATE).getString("profileId","none");
        if(data.equals("none"))
        {
            profileId=fUser.getUid();
        }
        else
        {
            profileId=data;

        }


        userInfo();
        getFollowersAndFollowingCount();
        getPostCount();
        myPhotos();
        getSavedPosts();


        if(profileId.equals(fUser.getUid()))
        {
            editProfile.setText("Edit Profile");
        }
        else
        {
            checkFollowingStatus();
        }
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editProfile.getText().toString();
                if (text.equals("Edit Profile"))
                {
                    // GOTO EDIT OUR OWN PROFILE
                }
                else
                {
                    if(text.equals("Follow"))
                    {
                        FirebaseDatabase.getInstance().getReference("List").child("Follow").
                                child(fUser.getUid()).child("Following").child(profileId).setValue(true);

                        FirebaseDatabase.getInstance().getReference("List").child("Follow").
                                child(profileId).child("Followers").child(fUser.getUid()).setValue(true);
                    }
                    else
                    {
                        FirebaseDatabase.getInstance().getReference("List").child("Follow").
                                child(fUser.getUid()).child("Following").child(profileId).removeValue();

                        FirebaseDatabase.getInstance().getReference("List").child("Follow").
                                child(profileId).child("Followers").child(fUser.getUid()).removeValue();
                    }
                }
            }
        });


        recyclerViewSaves.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        myPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerViewSaves.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
        savedPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.GONE);
                recyclerViewSaves.setVisibility(View.VISIBLE);
            }
        });
        

        return view;


    }

    private void getSavedPosts()
    {
        final List<String>savedIds=new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Saved").child(fUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot:snapshot.getChildren())
                {
                    savedIds.add(dataSnapshot.getKey());

                }
                FirebaseDatabase.getInstance().getReference("Post").child("Posting").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mySavedPosts.clear();
                        for (DataSnapshot dataSnapshot: snapshot.getChildren())
                        {
                            Post post = dataSnapshot.getValue(Post.class);
                            for(String id:savedIds)
                            {
                                if(post.getPostId().equals(id));
                                {
                                    mySavedPosts.add(post);
                                }
                            }
                        }
                        postAdapterSaves.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void myPhotos()
    {
        FirebaseDatabase.getInstance().getReference("Post").child("Posting").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myPhotoList.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren())
                {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post.getPublisherDetails().equals(profileId))
                    {
                        myPhotoList.add(post);
                    }
                }
                Collections.reverse(myPhotoList);
                photoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkFollowingStatus()
    {
        FirebaseDatabase.getInstance().getReference("List").child("Follow").child(fUser.getUid()).child("Following").
                addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(profileId).exists())
                {
                    editProfile.setText("Following");
                }
                else
                {
                    editProfile.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPostCount()
    {
        FirebaseDatabase.getInstance().getReference("Post").child("Posting").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int counter = 0;
                for(DataSnapshot dataSnapshot:snapshot.getChildren())
                {
                    Post post = dataSnapshot.getValue(Post.class);
                    if(post.getPublisherDetails().equals(profileId))
                    {
                        counter++;
                    }
                    posts.setText(String.valueOf(counter));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowersAndFollowingCount()
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("List").child("Follow").child(profileId);
        ref.child("Followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers.setText(snapshot.getChildrenCount()+ "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ref.child("Following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followings.setText(snapshot.getChildrenCount()+ "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void userInfo()
    {
        FirebaseDatabase.getInstance().getReference("Writing").child(profileId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Picasso.get().load(user.getImageUrl()).into(imageProfile);
                userName.setText(user.getUsername());
                fullName.setText(user.getName());
                bio.setText(user.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}