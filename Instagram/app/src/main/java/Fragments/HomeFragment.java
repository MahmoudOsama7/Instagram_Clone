package Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram.Adapter.PostAdapter;
import com.example.instagram.Model.Post;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewPost;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private List<String> followingList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        postList=new ArrayList<>();

        recyclerViewPost=view.findViewById(R.id.recycler_view_posts);
        recyclerViewPost.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true); // it means that everything in the list that is inside the recyclerview willhave option that
        // latest entered will be shown first
        linearLayoutManager.setReverseLayout(true);
        recyclerViewPost.setLayoutManager(linearLayoutManager);
        postAdapter=new PostAdapter(getContext(),postList);
        recyclerViewPost.setAdapter(postAdapter);
        followingList=new ArrayList<>();

        checkFollowingUsers();

        return view;
    }

    private void checkFollowingUsers()
    {
        FirebaseDatabase.getInstance().getReference("List").child("Follow").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                child("Following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    followingList.add(snapshot.getKey());
                }
                readPosts();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void readPosts()
    {
        FirebaseDatabase.getInstance().getReference("Post").child("Posting").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    Post post = snapshot.getValue(Post.class);
                    for (String id :followingList) // id to fetch all the users that our user is following
                    {
                        //if id found in firebase = id of one of following lists id so it will fetch all the children ( in that case all the posts found and
                        //compare their publisherdetails if = id of the the users found in the following lists ,
                        //this means that this user that is following our user , posted something , so that our user following it , will see what is the post
                        // so once got for ex 1st id , it will check in posts , if the 1st id has a post their , so we will add this post so that our user can see it
                        if (post.getPublisherDetails().equals(id))
                        {
                            postList.add(post); // then add the the values of post to the list
                        }
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}