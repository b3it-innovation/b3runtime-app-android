package com.b3.development.b3runtime.ui.profile;

import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.local.model.useraccount.UserAccount;
import com.b3.development.b3runtime.data.repository.useraccount.UserAccountRepository;
import com.b3.development.b3runtime.utils.failure.Failure;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileViewModel extends BaseViewModel {

    public static final String TAG = ProfileViewModel.class.getSimpleName();

    private String uid = FirebaseAuth.getInstance().getUid();
    private LiveData<UserAccount> userAccountLiveData;
    private LiveData<Failure> error;
    private UserAccountRepository userAccountRepository;

    public ProfileViewModel(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
        this.userAccountRepository.fetch(uid);
        this.error = userAccountRepository.getError();
        userAccountLiveData = userAccountRepository.getUserAccount(uid);
    }

    public LiveData<UserAccount> getUserAccountLiveData() {
        return userAccountLiveData;
    }

    public void updateUserAccount(UserAccount userAccount, String oldValue) {
        userAccountRepository.updateUserAccount(userAccount, oldValue);
    }

    public LiveData<Failure> getError() {
        return error;
    }
}
