package com.b3.development.b3runtime.ui.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.BindingAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseFragment;
import com.b3.development.b3runtime.data.local.model.useraccount.UserAccount;
import com.b3.development.b3runtime.data.repository.useraccount.UserAccountRepository;
import com.b3.development.b3runtime.databinding.FragmentProfileBinding;
import com.b3.development.b3runtime.utils.AlertDialogUtil;
import com.b3.development.b3runtime.utils.failure.FailureType;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static org.koin.java.KoinJavaComponent.get;

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
    public static final int USERNAME_VIEW = 1;
    public static final int FIRSTNAME_VIEW = 2;
    public static final int LASTNAME_VIEW = 3;
    public static final int ORGANIZATION_VIEW = 4;

    private String currentPhotoPath;
    private ProfileViewModel viewModel;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this,
                new ProfileViewModelFactory(get(UserAccountRepository.class)))
                .get(ProfileViewModel.class);

        FragmentProfileBinding binding = DataBindingUtil.inflate(inflater, layoutId, container, false);
        View view = binding.getRoot();
        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setFragment(this);
        binding.setViewmodel(viewModel);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button btnResetPassword = view.findViewById(R.id.btn_reset_password);
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // todo sendResetPasswordMail(view);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && error.getType() == FailureType.PERMISSION) {
                AlertDialogUtil.createCustomInfoDialog(getContext(), "Invalid username",
                        "That username is already taken").show();
            } else if (error != null && error.getType() == FailureType.GENERIC) {
                AlertDialogUtil.createCustomInfoDialog(getContext(), "Invalid username",
                        "That username is invalid. Username must be between 1-20 " +
                                "characters and can only contain A-ö and numbers.");
            }
        });
    }

    public void onProfileImageClick() {
        requestWriteExternalStoragePermissions();
        pickUpImage();
    }

    public void onTakePhotoClick() {
        requestCameraAndWriteExternalStoragePermissions();
        dispatchTakePictureIntent();
    }

    public void showResultsFragment() {
        ResultsFragment resultsFragment = ResultsFragment.newInstance(viewModel.getCurrentUser().getUid());
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.home_container, resultsFragment, ResultsFragment.TAG);
        ft.addToBackStack(ResultsFragment.TAG);
        ft.commit();
    }

    private void pickUpImage() {
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
                Log.e(TAG, ex.getMessage());
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
                viewModel.uploadProfileImage(result.getUri());
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.d(TAG, result.getError().getMessage());
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            galleryAddPic();
            CropImage.activity(Uri.fromFile(new File(currentPhotoPath)))
                    .setAspectRatio(1, 1)
                    .start(getContext(), this);
        }
    }

    public void changeUserValue(View view) {
        TextView textView = (TextView) view;
        int viewType = -1;
        switch (textView.getId()) {
            case R.id.editUserName:
                viewType = USERNAME_VIEW;
                break;
            case R.id.editFirstName:
                viewType = FIRSTNAME_VIEW;
                break;
            case R.id.editLastName:
                viewType = LASTNAME_VIEW;
                break;
            case R.id.editOrganization:
                viewType = ORGANIZATION_VIEW;
                break;
            default:
                Log.e(TAG, "incompatible viewType sent to changeUserValue");
                textView = null;
                break;
        }

        if (textView != null) {
            String oldValue = textView.getText().toString();
            //create dialog, insert old name as placeholder
            AlertDialogUtil.createTextInputDialogForProfile(this, oldValue, viewType).show();
        }
    }

    public void updateUserValue(String newValue, int viewType, String oldValue) {

        UserAccount userAccount = viewModel.getUserAccountLiveData().getValue();

        switch (viewType) {
            case USERNAME_VIEW:
                userAccount.userName = newValue;
                break;
            case FIRSTNAME_VIEW:
                userAccount.firstName = newValue;
                break;
            case LASTNAME_VIEW:
                userAccount.lastName = newValue;
                break;
            case ORGANIZATION_VIEW:
                userAccount.organization = newValue;
                break;
            default:
                Log.e(TAG, "incompatible viewType sent to updateUserValue");
                break;
        }
        //Alert on empty username
        if ((newValue == null || newValue.equals("")) && (viewType == USERNAME_VIEW
                || viewType == FIRSTNAME_VIEW || viewType == LASTNAME_VIEW)) {
            AlertDialogUtil.createEmptyValueDialog(getActivity()).show();
        } else {
            viewModel.updateUserAccount(userAccount, oldValue);
        }
    }

    // todo: complete this method with check if user is newly logged in
    public void sendResetPasswordMail(View view) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String emailAddress = viewModel.getCurrentUser().getEmail();
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

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat(getString(R.string.image_file_date_format), Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + getResources().getString(R.string.external_storage_image_path));

        if (!storageDir.exists()) {
            boolean resultOK = storageDir.mkdir();
            if (!resultOK) {
                throw new IOException();
            }
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
                pickUpImage();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //showPermissionDeniedDialog();
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialogUtil.createDoNotAskAgainClickedDialog(getActivity()).show();
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
                AlertDialogUtil.createDoNotAskAgainClickedDialog(getActivity()).show();
            }
        }
    }

    @BindingAdapter("android:profileImage")
    public static void showProfileImage(ImageView imageView, Uri uri) {
        Glide.with(imageView.getContext())
                .load(uri)
                .into(imageView);
    }

}