package com.b3.development.b3runtime.data.remote;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class FirestoreQueryLiveData extends LiveData<QuerySnapshot> {

    private static final String TAG = FirestoreQueryLiveData.class.getSimpleName();

    private final Query query;
    private final QueryValueEventListener myEventListener = new QueryValueEventListener();
    private ListenerRegistration registration;

    public FirestoreQueryLiveData(Query ref) {
        this.query = ref;
    }

    @Override
    protected void onActive() {
        Log.d(TAG, "onActive");
        registration = query.addSnapshotListener(myEventListener);
    }

    @Override
    protected void onInactive() {
        Log.d(TAG, "onInactive");
        registration.remove();
    }

    private class QueryValueEventListener implements EventListener<QuerySnapshot> {
        @Override
        public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException e) {
            if (e != null) {
                Log.e(TAG, "Failed to read value.", e);
            } else {
                setValue(querySnapshot);
            }
        }
    }

}
