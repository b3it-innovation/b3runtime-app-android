package com.b3.development.b3runtime.ui.home;

import com.b3.development.b3runtime.base.BaseViewModel;
import com.b3.development.b3runtime.data.repository.useraccount.UserAccountRepository;

public class HomeViewModel extends BaseViewModel {

    private UserAccountRepository userAccountRepository;

    public HomeViewModel(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public void saveUserAccount(String uid) {
        userAccountRepository.saveUserAccount(uid);
    }
}
