package Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.instagram.Adapter.TagAdapter;
import com.example.instagram.Adapter.UserAdapter;
import com.example.instagram.MainActivity;
import com.example.instagram.Model.User;
import com.example.instagram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private RecyclerView recyclerView;
    private SocialAutoCompleteTextView search_bar;
    private DatabaseReference database;
    private List<User> mUsers;
    private RecyclerView recyclerViewTags;
    private List<String>mHashTags;
    private List<String>mHashTagsCount;
    private TagAdapter tagAdapter;
    private UserAdapter userAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_search, container, false);
        search_bar=view.findViewById(R.id.search_bar);

        mUsers=new ArrayList<>();

        recyclerView=view.findViewById(R.id.recycler_view_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter=new UserAdapter(getContext(),mUsers,true);
        recyclerView.setAdapter(userAdapter);

        mHashTags=new ArrayList<>();
        mHashTagsCount=new ArrayList<>();

        recyclerViewTags=view.findViewById(R.id.recycler_view_tags);
        recyclerViewTags.setHasFixedSize(true);
        recyclerViewTags.setLayoutManager(new LinearLayoutManager(getContext()));
        tagAdapter=new TagAdapter(getContext(),mHashTags,mHashTagsCount);
        recyclerViewTags.setAdapter(tagAdapter);



        readUsers();
        readTags();

        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUser(s.toString());
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    private void readTags()
    {
        FirebaseDatabase.getInstance().getReference("Post").child("HashTags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                mHashTags.clear();
                mHashTagsCount.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    mHashTags.add(snapshot.getKey()); // get the key of the hashtag and add it to the list
                    mHashTagsCount.add(snapshot.getChildrenCount() +""); // as getchildrencount is returned as long so will convert to string
                }
                tagAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }


    private void readUsers()
    {
            database=FirebaseDatabase.getInstance().getReference("Writing");
            database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (TextUtils.isEmpty(search_bar.getText().toString()))
                    {
                        mUsers.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            User user = dataSnapshot.getValue(User.class);
                            mUsers.add(user);
                        }
                        userAdapter.notifyDataSetChanged();
                    }

                }
            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }
    private void searchUser(String s)
    {
        Query query = FirebaseDatabase.getInstance().getReference().child("Writing").orderByChild("username").startAt(s).endAt(s+"\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    User user = snapshot.getValue(User.class);
                    mUsers.add(user);
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void filter (String text)
    {
        List<String>mSearchTags = new ArrayList<>();
        List<String>mSearchTagsCount = new ArrayList<>();
        for (String s : mHashTags)
        {
            if (s.toLowerCase().contains(text.toLowerCase()))
            {
                mSearchTags.add(s);
                mSearchTagsCount.add(mHashTagsCount.get(mHashTags.indexOf(s)));
            }
        }
        tagAdapter.filter(mSearchTags,mHashTagsCount);
    }
}