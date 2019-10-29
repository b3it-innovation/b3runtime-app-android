package com.b3.development.b3runtime.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseFragment;
import com.bumptech.glide.Glide;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends BaseFragment {

    public static final String TAG = ProfileFragment.class.getSimpleName();
    private static final int layoutId = R.layout.fragment_profile;
    private static final int RC_PHOTO_PICKER = 2;

    private FirebaseStorage storage;
    private StorageReference profilePhotoReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private ImageView imageView;

    public ProfileFragment() {
    }

    public static final ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle arguments = new Bundle();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storage = FirebaseStorage.getInstance();
        profilePhotoReference = storage.getReference().child("profile_photos");
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser= firebaseAuth.getCurrentUser();
    }

    @Override
    public Integer getLayoutId() {
        return layoutId;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.upload_button).setOnClickListener(v -> pickUpImage(v));
        imageView = view.findViewById(R.id.profile_image);
        Glide.with(imageView.getContext())
                .load(currentUser.getPhotoUrl())
                .into(imageView);
    }

    public void pickUpImage(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build();
            Uri selectedImageUri = data.getData();
            StorageReference photoRef = profilePhotoReference.child(selectedImageUri.getLastPathSegment());
            UploadTask uploadTask = photoRef.putFile(selectedImageUri, metadata);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return photoRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(downloadUri)
                                .build();
                        currentUser.updateProfile(profileChangeRequest)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Glide.with(imageView.getContext())
                                                    .load(currentUser.getPhotoUrl())
                                                    .into(imageView);
                                        } else {
                                            // todo: Handle failures
                                        }
                                    }
                                });
                    } else {
                        // todo: Handle failures
                        // ...
                    }
                }
            });
        }
    }
}
