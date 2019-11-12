package com.b3.development.b3runtime.ui.profile;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseFragment;
import com.b3.development.b3runtime.utils.Util;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends BaseFragment {

    public static final String TAG = ProfileFragment.class.getSimpleName();
    private static final int layoutId = R.layout.fragment_profile;
    private static final int RC_PHOTO_PICKER = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3;
    private static final int PERMISSIONS_REQUEST_CAMERA_AND_WRITE_EXTERNAL_STORAGE = 4;

    private String profileImageFileName;

    private FirebaseStorage storage;
    private StorageReference profilePhotoReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private ImageView profileImageView;
    private String currentPhotoPath;

    public ProfileFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ProfileFragment.
     */
    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public Integer getLayoutId() {
        return layoutId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storage = FirebaseStorage.getInstance();
        profilePhotoReference = storage.getReference().child("profile_images");
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        profileImageFileName = getString(R.string.profile_image_file_name);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        profileImageView = view.findViewById(R.id.imageViewProfile);
        profileImageView.setOnClickListener(v -> {
            requestWriteExternalStoragePermissions();
            pickUpImage(v);
        });
        showProfileImage(profileImageView);

        Button btnResetPassword = view.findViewById(R.id.btn_reset_password);
        Button btnChangeName = view.findViewById(R.id.btnEditName);
        Button btnSeeResults = view.findViewById(R.id.btn_results);

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResetPasswordMail(view);
            }
        });
        btnChangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeName(view);
            }
        });

        btnSeeResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View f) {
                showResultsFragment();
            }
        });

        drawProfile(view);

        view.findViewById(R.id.btn_camera).setOnClickListener(v -> {
            requestCameraAndWriteExternalStoragePermissions();
            dispatchTakePictureIntent();
        });
    }

    private void showResultsFragment() {
        ResultsFragment profileFragment = ResultsFragment.newInstance(currentUser.getUid());
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.home_container, profileFragment, ProfileFragment.TAG);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void pickUpImage(View view) {
        if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
    }

    private void dispatchTakePictureIntent() {
        if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri uri = FileProvider.getUriForFile(
                        getContext(), getActivity().getApplicationContext().getPackageName() + ".provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            // Call Crop Activity from fragment (DO NOT use `getActivity()`)
            CropImage.activity(data.getData())
                    .setAspectRatio(1, 1)
                    .start(getContext(), this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                uploadProfileImage(result.getUri());
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            galleryAddPic();
            CropImage.activity(Uri.fromFile(new File(currentPhotoPath)))
                    .setAspectRatio(1, 1)
                    .start(getContext(), this);
        }
    }

    private void uploadProfileImage(Uri imageUri) {
        StorageReference photoRef =
                profilePhotoReference.child(currentUser.getUid() + "/" + profileImageFileName);
        UploadTask uploadTask = photoRef.putFile(imageUri);
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
                            showProfileImage(profileImageView);
                        } else {
                            // todo: Handle failures
                        }
                    }
                });
    }

    private void showProfileImage(ImageView imageView) {
        Glide.with(imageView.getContext())
                .load(currentUser.getPhotoUrl())
                .into(imageView);
    }

    private void drawProfile(View view) {

        String userName = currentUser.getDisplayName();
        String email = currentUser.getEmail();

        TextView name = view.findViewById(R.id.editTextName);
        TextView mail = view.findViewById(R.id.textViewMail);

        name.setText(userName);
        mail.setText(email);
    }

    private void changeName(View view) {
        //get old name
        TextView name = view.findViewById(R.id.editTextName);
        String newName = name.getText().toString();

        //create dialog, insert old name as placeholder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter new name");
        final EditText input = new EditText(getContext());

        //filters for text
        InputFilter[] filterArray = new InputFilter[2];
        filterArray[0] = new InputFilter.LengthFilter(20);
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        filterArray[1] = filter;
        input.setFilters(filterArray);

        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
        input.setText(newName);
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //take the new name entered and set it
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(input.getText().toString())
                        .build();
                currentUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            drawProfile(view);
                        } else {
                            Log.d(TAG, "Couldn't set new username");
                            Toast.makeText(getContext(), "Something went wrong.", Toast.LENGTH_SHORT);
                        }
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void sendResetPasswordMail(View view) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String emailAddress = currentUser.getEmail();
//        reauthenticate();
        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                        }
                    }
                });
    }

    private void reauthenticate() {
        if (currentUser.getProviderData().get(currentUser.getProviderData().size() - 1).getProviderId().equals("google.com")) {
            // Get the account
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity().getApplicationContext());
            if (acct != null) {
                AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Reauthenticated.");
                        }
                    }
                });
            }
        } else {
            // todo: check if registered with mail and reauthenticate
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat(getString(R.string.image_file_date_format)).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + getResources().getString(R.string.external_storage_image_path));

        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = FileProvider.getUriForFile(
                getContext(), getActivity().getApplicationContext().getPackageName() + ".provider", f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    private void requestWriteExternalStoragePermissions() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void requestCameraAndWriteExternalStoragePermissions() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_CAMERA_AND_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickUpImage(profileImageView);
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //showPermissionDeniedDialog();
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog doNotAskAgainClickedDialog = createDoNotAskAgainClickedDialog();
                doNotAskAgainClickedDialog.show();
            }
        }

        if (requestCode == PERMISSIONS_REQUEST_CAMERA_AND_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //showPermissionDeniedDialog();
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog doNotAskAgainClickedDialog = createDoNotAskAgainClickedDialog();
                doNotAskAgainClickedDialog.show();
            }
        }
    }

    private AlertDialog createDoNotAskAgainClickedDialog() {
        AlertDialog doNotAskAgainClickedDialog = Util.createDialog(
                getString(R.string.deniedPermissionsDialogTitle),
                getString(R.string.changePermissionsInSettingsMessage_not_specific),
                getString(R.string.goToSettingsButtonText),
                (dialog, which) -> {
                    dialog.dismiss();
                    //opens settings of the app to manually allow permissions
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                    intent.setData(uri);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.d(TAG, getString(R.string.intent_failed));
                        Toast.makeText(getActivity().getApplicationContext(),
                                getString(R.string.somethingWentWrong),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                    getActivity().finish();
                },
                getString(R.string.no_text),
                (dialog, which) -> {
                    dialog.dismiss();
                },
                false,
                getActivity());
        return doNotAskAgainClickedDialog;
    }

}