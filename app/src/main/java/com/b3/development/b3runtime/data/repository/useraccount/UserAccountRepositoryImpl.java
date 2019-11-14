package com.b3.development.b3runtime.data.repository.useraccount;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.b3.development.b3runtime.data.local.model.useraccount.UserAccount;
import com.b3.development.b3runtime.data.local.model.useraccount.UserAccountDao;
import com.b3.development.b3runtime.data.remote.BackendInteractor;
import com.b3.development.b3runtime.utils.failure.Failure;

public class UserAccountRepositoryImpl implements UserAccountRepository {

    public static final String TAG = UserAccountRepository.class.getSimpleName();

    private UserAccountDao userAccountDao;
    private BackendInteractor backendInteractor;
    private MutableLiveData<Failure> error = new MutableLiveData<>();

    /**
     * A public constructor for {@link UserAccountRepository} implementation
     *
     * @param bi a reference to {@link BackendInteractor}
     */
    public UserAccountRepositoryImpl(BackendInteractor bi) {
        this.backendInteractor = bi;
    }

    @Override
    public void saveUserAccount(String uid) {
        backendInteractor.saveUserAccount(uid);
    }

    @Override
    public LiveData<UserAccount> getUserAccount(String id) {
        return userAccountDao.getUserAccountById(id);
    }

    /**
     * @return error <code>LiveData</code> of <code>Failure></code>
     */
    @Override
    public LiveData<Failure> getError() {
        return error;
    }

    @Override
    public void updateUserAccount(UserAccount userAccount) {

    }
}
