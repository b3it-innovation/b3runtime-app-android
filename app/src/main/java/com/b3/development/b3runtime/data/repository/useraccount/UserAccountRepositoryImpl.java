package com.b3.development.b3runtime.data.repository.useraccount;

import com.b3.development.b3runtime.data.remote.BackendInteractor;

public class UserAccountRepositoryImpl implements UserAccountRepository {

    public static final String TAG = UserAccountRepository.class.getSimpleName();

    private final BackendInteractor backendInteractor;

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
}
