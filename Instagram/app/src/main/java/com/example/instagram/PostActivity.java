package com.example.instagram;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.common.io.Files;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hendraanggrian.appcompat.socialview.Hashtag;
import com.hendraanggrian.appcompat.widget.HashtagArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.theartofdev.edmodo.cropper.CropImage;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;

public class PostActivity extends AppCompatActivity {

    private ImageView close;
    private ImageView imageAdded;
    private TextView post;
    private Uri imageUri;
    private String imageUrl;
    SocialAutoCompleteTextView description;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close=findViewById(R.id.close);
        imageAdded=findViewById(R.id.image_added);
        post=findViewById(R.id.post);
        description=findViewById(R.id.description);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostActivity.this,MainActivity.class));
                finish();
            }
        });

        // this method is used when getting image from gallery , then the image is returned and we will crop it by overriding the method activity restult
        // this activity result method is used as when returning from certain activity we return with result which in this case is the image
        // so will crop this image also in the same method once getting it as result
        CropImage.activity().start(PostActivity.this);


        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
        


    }


    //now when overriding the activity result , we have the image , i want to to get the uri part of the result got and store it in a uri created
    // then set the uri of  imageAdded of image view to be the this uri result i got
    //i have if condition to put all of this in it , but if not applied , will go to else as to give toast message then go back to main activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode== RESULT_OK)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();  //
            imageAdded.setImageURI(imageUri);
        }
        else
        {
            Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PostActivity.this,MainActivity.class));
            finish();
        }
    }

    private void uploadImage()
    {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading..");
        pd.show();

        if(imageUri!=null)
        {
            // creating a storage reference
            final StorageReference filePth = FirebaseStorage.getInstance().getReference("Posts").child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            StorageTask uploadTask = filePth.putFile(imageUri); // uploading the imageUri we have in the storage reference we created
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful())
                    {
                        throw task.getException();
                    }
                    else
                    {
                        return filePth.getDownloadUrl(); // return the download link here to the task object
                    }
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    //from here we start to upload the image url to the firebase database but in a different branch
                    Uri downloadUri = task.getResult();
                    imageUrl=downloadUri.toString();
                    DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Post").child("Posting");
                    String postId = ref.push().getKey(); // push.getkey is used as to get a unique id so in that case it will be stored in postId of type string
                    HashMap<String,Object>map = new HashMap<>();
                    map.put("postId",postId);
                    map.put("imageUrl",imageUrl);
                    map.put("description",description.getText().toString());
                    map.put("publisherDetails", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    ref.push().setValue(map);

                    //from here we start to upload the hashtags found in the description content in the firebase database , in different branch called
                    // hashtags

                    DatabaseReference mHashTagRef = FirebaseDatabase.getInstance().getReference("Post").child("HashTags");
                    List<String> hashTags =description.getHashtags();
                    if (!hashTags.isEmpty())
                    {
                        for(String tag: hashTags)
                        {
                            map.clear();
                            map.put("tag",tag.toLowerCase());
                            map.put("postId",postId);
                            mHashTagRef.child(tag.toLowerCase()).setValue(map);
                        }
                    }
                    pd.dismiss();
                    startActivity(new Intent(PostActivity.this,MainActivity.class));
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
            Toast.makeText(this, "No Image Was Selected ", Toast.LENGTH_SHORT).show();
        }
    }
    private String getFileExtension(Uri uri)
    {

        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri));
    }

    @Override
    protected void onStart() {

        super.onStart();
        //array adapter of type hashtag this datatype comes alongside the social autocomplete library
        final ArrayAdapter <Hashtag> hashtagAdapter = new HashtagArrayAdapter<>(getApplicationContext());
        FirebaseDatabase.getInstance().getReference("Post").child("HashTags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    hashtagAdapter.add(new Hashtag(snapshot.getKey(),(int) snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // here it will list all the hashtags used already and found as to give them as an option for the user to use
    description.setHashtagAdapter(hashtagAdapter);
    }
}