package com.b3.development.b3runtime.ui.profile;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.local.model.useraccount.UserAccount;
import com.b3.development.b3runtime.data.repository.useraccount.UserAccountRepository;
import com.b3.development.b3runtime.utils.failure.Failure;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileViewModel extends BaseViewModel {

    public static final String TAG = ProfileViewModel.class.getSimpleName();

    private String uid = FirebaseAuth.getInstance().getUid();
    private LiveData<UserAccount> userAccountLiveData;
    private LiveData<String> firstName;
    private LiveData<String> lastName;
    private LiveData<String> userName;
    private LiveData<String> organization;
    private LiveData<Failure> error;
    private MutableLiveData<Uri> userPhotoUri = new MutableLiveData<>();
    private LiveData<Boolean> userNameExists;
    private UserAccountRepository userAccountRepository;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private StorageReference profilePhotoReference;
    private String currentUserEmail;
    private String profileImageFileName;

    public ProfileViewModel(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
        this.userAccountRepository.fetch(uid);
        this.error = userAccountRepository.getError();
        userAccountLiveData = userAccountRepository.getUserAccount(uid);
        firstName = Transformations.map(userAccountLiveData, userAccount -> {
            if (userAccount != null) {
                return userAccount.firstName;
            } else {
                return "";
            }
        });
        lastName = Transformations.map(userAccountLiveData, userAccount -> {
            if (userAccount != null) {
                return userAccount.lastName;
            } else {
                return "";
            }
        });
        userName = Transformations.map(userAccountLiveData, userAccount -> {
            if (userAccount != null) {
                return userAccount.userName;
            } else {
                return "";
            }
        });
        organization = Transformations.map(userAccountLiveData, userAccount -> {
            if (userAccount != null) {
                return userAccount.organization;
            } else {
                return "";
            }
        });
        storage = FirebaseStorage.getInstance();
        profilePhotoReference = storage.getReference().child("profile_images");
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        currentUserEmail = currentUser.getEmail();
        profileImageFileName = "avatar.jpg"; // todo: read value from string resources
        userPhotoUri.postValue(currentUser.getPhotoUrl());
    }

    public void setUp() {
        firstName = Transformations.map(userAccountLiveData, userAccount -> userAccount.firstName);
        lastName = Transformations.map(userAccountLiveData, userAccount -> userAccount.lastName);
        userName = Transformations.map(userAccountLiveData, userAccount -> userAccount.userName);
        organization = Transformations.map(userAccountLiveData, userAccount -> userAccount.organization);
    }

    public LiveData<UserAccount> getUserAccountLiveData() {
        return userAccountLiveData;
    }

    public void updateUserAccount(UserAccount userAccount, String oldValue) {
        userAccountRepository.updateUserAccount(userAccount, oldValue);
    }

    public void uploadProfileImage(Uri imageUri) {
        StorageReference photoRef =
                profilePhotoReference.child(currentUser.getUid() + "/" + profileImageFileName);
        UploadTask uploadTask = photoRef.putFile(imageUri);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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
                    updateProfileImage(downloadUri);
                } else {
                    // todo: Handle failures
                    Log.d(TAG, task.getException().getMessage());
                }
            }
        });
    }

    private void updateProfileImage(Uri uri) {
        // update user profile image in Firebase Authentication
        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();
        currentUser.updateProfile(profileChangeRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            userPhotoUri.postValue(currentUser.getPhotoUrl());
                        } else {
                            Log.e(TAG, task.getException().getMessage());
                        }
                    }
                });
    }

    public LiveData<Failure> getError() {
        return error;
    }

    public LiveData<String> getFirstName() {
        return firstName;
    }

    public LiveData<String> getLastName() {
        return lastName;
    }

    public LiveData<String> getUserName() {
        return userName;
    }

    public LiveData<String> getOrganization() {
        return organization;
    }

    public MutableLiveData<Uri> getUserPhotoUri() {
        return userPhotoUri;
    }

    public LiveData<Boolean> getUserNameExists() {
        return userNameExists;
    }

    public FirebaseUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(FirebaseUser currentUser) {
        this.currentUser = currentUser;
    }

    public String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public void setCurrentUserEmail(String currentUserEmail) {
        this.currentUserEmail = currentUserEmail;
    }

    public void checkUserNameExists(String userName) {
        userNameExists = Transformations.map(userAccountRepository.getUserAccountByUserName(userName),
                userAccount -> {
                    Log.d(TAG, "");
                    return userAccount != null;
                });
    }

    public void resetUserNameExists() {
        userNameExists = null;
    }
}
