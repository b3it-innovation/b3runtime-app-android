package com.b3.development.b3runtime.base;

import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Gives all app Activities a standardised implementation of methods and handling errors.
 * All specific Activities extend from {@link BaseActivity}
 */
public abstract class BaseActivity extends AppCompatActivity {

    /**
     * Handles back button press in a standardised way
     *
     * @param item MenuItem
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item != null && item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}