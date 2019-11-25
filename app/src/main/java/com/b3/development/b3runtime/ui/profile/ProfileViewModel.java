package com.b3.development.b3runtime.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.local.model.useraccount.UserAccount;
import com.b3.development.b3runtime.data.repository.useraccount.UserAccountRepository;
import com.b3.development.b3runtime.utils.failure.Failure;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileViewModel extends BaseViewModel {

    public static final String TAG = ProfileViewModel.class.getSimpleName();

    private String uid = FirebaseAuth.getInstance().getUid();
    private LiveData<UserAccount> userAccountLiveData;
    private LiveData<String> firstName;
    private LiveData<String> lastName;
    private LiveData<String> userName;
    private LiveData<String> organization;
    private LiveData<Failure> error;
    private UserAccountRepository userAccountRepository;

    public ProfileViewModel(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
        this.userAccountRepository.fetch(uid);
        this.error = userAccountRepository.getError();
        userAccountLiveData = userAccountRepository.getUserAccount(uid);
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
}
