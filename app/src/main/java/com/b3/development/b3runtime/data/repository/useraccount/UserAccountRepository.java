package com.b3.development.b3runtime.data.repository.useraccount;

import androidx.lifecycle.LiveData;

import com.b3.development.b3runtime.data.local.model.useraccount.UserAccount;
import com.b3.development.b3runtime.utils.failure.Failure;


public interface UserAccountRepository {

    void saveUserAccount(String uid);

    LiveData<UserAccount> getUserAccount(String id);

    LiveData<Failure> getError();

    void updateUserAccount(UserAccount userAccount);

    void fetch(String uid);
}
