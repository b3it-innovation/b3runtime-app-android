package com.b3.development.b3runtime.data.repository.useraccount;

import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.b3.development.b3runtime.data.local.model.useraccount.UserAccount;
import com.b3.development.b3runtime.data.local.model.useraccount.UserAccountDao;
import com.b3.development.b3runtime.data.remote.BackendInteractor;
import com.b3.development.b3runtime.data.remote.model.useraccount.BackendUserAccount;
import com.b3.development.b3runtime.utils.failure.Failure;
import com.b3.development.b3runtime.utils.failure.FailureType;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class UserAccountRepositoryImpl implements UserAccountRepository {

    public static final String TAG = UserAccountRepository.class.getSimpleName();

    private UserAccountDao userAccountDao;
    private BackendInteractor backendInteractor;
    private MutableLiveData<Failure> error = new MutableLiveData<>();

    /**
     * A public constructor for {@link UserAccountRepository} implementation
     *
     * @param bi             a reference to {@link BackendInteractor}
     * @param userAccountDao a reference to {@link UserAccountDao}
     */
    public UserAccountRepositoryImpl(BackendInteractor bi, UserAccountDao userAccountDao) {
        this.backendInteractor = bi;
        this.userAccountDao = userAccountDao;
    }

    @Override
    public void saveUserAccount(String uid) {
        backendInteractor.saveUserAccount(uid);
    }

    @Override
    public LiveData<UserAccount> getUserAccount(String id) {
        return userAccountDao.getUserAccountById(id);
    }

    @Override
    public LiveData<BackendUserAccount> getUserAccountByUserName(String userName) {
        return Transformations.map(backendInteractor.getUserAccountLiveDataByUserName(userName),
                snapshot -> convertDataSnapshotToBackendUserAccount(snapshot));
    }

    /**
     * @return error <code>LiveData</code> of <code>Failure></code>
     */
    @Override
    public LiveData<Failure> getError() {
        return error;
    }

    @Override
    public void updateUserAccount(UserAccount userAccount, String oldValue) {
        backendInteractor.updateUserAccount(new BackendInteractor.ErrorCallback() {
            @Override
            public void onErrorReceived(FailureType failureType) {
                if (failureType == FailureType.PERMISSION) {
                    error.setValue(new Failure(FailureType.PERMISSION, "Username must be unique"));
                } else if (failureType == FailureType.GENERIC) {
                    error.setValue(new Failure(FailureType.GENERIC, "Incorrect username"));
                }
            }
        }, userAccount, oldValue);
    }

    /**
     * Contains logic for fetching data from backend
     */
    @Override
    public void fetch(String uid) {
        //implements BackendInteractor.CheckpointsCallback
        backendInteractor.getUserAccountById(new BackendInteractor.UserAccountCallback() {
            //handles response
            @Override
            public void onUserAccountReceived(BackendUserAccount backendUseraccount) {
                //early return in case of server error
                if (backendUseraccount == null) {
                    error.postValue(new Failure(FailureType.SERVER));
                    return;
                }
                Log.d(TAG, "USERACCOUNT RECEIVED FROM BACKEND");
                UserAccount userAccount = new UserAccount();
                userAccount.id = backendUseraccount.getKey();
                if (backendUseraccount.getUserName() == null) {
                    userAccount.userName = "";
                } else {
                    userAccount.userName = backendUseraccount.getUserName();
                }
                if (backendUseraccount.getFirstName() == null) {
                    userAccount.firstName = "";
                } else {
                    userAccount.firstName = backendUseraccount.getFirstName();
                }
                if (backendUseraccount.getLastName() == null) {
                    userAccount.lastName = "";
                } else {
                    userAccount.lastName = backendUseraccount.getLastName();
                }
                if (backendUseraccount.getOrganization() == null) {
                    userAccount.organization = "";
                } else {
                    userAccount.organization = backendUseraccount.getOrganization();
                }
                //writes in local database asynchronously
                AsyncTask.execute(() -> userAccountDao.insertUserAccount(userAccount));
                Log.d(TAG, "USERACCOUNT CONVERTED... WRITING IN DATABASE ASYNC STARTS");
            }

            @Override
            public void onError() {
                error.postValue(new Failure(FailureType.NETWORK));
            }
        }, uid);
    }

    private BackendUserAccount convertDataSnapshotToBackendUserAccount(QuerySnapshot querySnapshot) {
        BackendUserAccount userAccount = null;
        for (QueryDocumentSnapshot document : querySnapshot) {
            userAccount = document.toObject(BackendUserAccount.class);
            userAccount.setKey(document.getId());
        }
        return userAccount;
    }

}
