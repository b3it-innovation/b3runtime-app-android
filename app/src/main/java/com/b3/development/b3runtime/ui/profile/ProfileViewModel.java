package com.b3.development.b3runtime.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.local.model.useraccount.UserAccount;
import com.b3.development.b3runtime.data.remote.model.result.BackendResult;
import com.b3.development.b3runtime.data.repository.result.ResultRepository;
import com.b3.development.b3runtime.data.repository.useraccount.UserAccountRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ProfileViewModel extends BaseViewModel {

    public static final String TAG = ProfileViewModel.class.getSimpleName();

    private String uid = FirebaseAuth.getInstance().getUid();
    private LiveData<UserAccount> userAccountLiveData;
    private UserAccountRepository userAccountRepository;

    public ProfileViewModel(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
        this.userAccountRepository.fetch(uid);
        userAccountLiveData = userAccountRepository.getUserAccount(uid);
    }

    public LiveData<UserAccount> getUserAccountLiveData() {
        return userAccountLiveData;
    }
}
