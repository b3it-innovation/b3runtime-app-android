package com.b3.development.b3runtime.data.remote;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class QueryLiveData extends LiveData<DataSnapshot> {

    private static final String TAG = BackendInteractor.class.getSimpleName();

    private final Query query;
    private final QueryValueEventListener valueEventListener = new QueryValueEventListener();

    public QueryLiveData(Query query) {
        this.query = query;
    }

    public QueryLiveData(DatabaseReference databaseRef) {
        this.query = databaseRef;
    }

    @Override
    protected void onActive() {
        Log.d(TAG, "onActive");
        query.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onInactive() {
        Log.d(TAG, "onInactive");
        query.removeEventListener(valueEventListener);
    }

    private class QueryValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            setValue(dataSnapshot);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, "Can't listen to query " + query, databaseError.toException());
        }
    }


}
