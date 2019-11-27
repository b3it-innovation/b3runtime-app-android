package com.b3.development.b3runtime.ui.question;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseDialogFragment;
import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepository;
import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepository;
import com.b3.development.b3runtime.data.repository.question.QuestionRepository;
import com.b3.development.b3runtime.data.repository.result.ResultRepository;
import com.b3.development.b3runtime.geofence.GeofenceManager;
import com.b3.development.b3runtime.sound.SoundEvent;
import com.b3.development.b3runtime.ui.map.MapsActivity;
import com.b3.development.b3runtime.ui.map.MapsViewModel;
import com.b3.development.b3runtime.ui.map.MapsViewModelFactory;
import com.github.abdularis.civ.CircleImageView;

import static com.b3.development.b3runtime.R.color.b3Blue;
import static org.koin.java.KoinJavaComponent.get;

/**
 * Contains logic for displaying a {@link PenaltyDialogFragment} to inform user of their result
 */
public class PenaltyDialogFragment extends BaseDialogFragment {

    public static final String TAG = PenaltyDialogFragment.class.getSimpleName();
    private static final int layoutId = R.layout.fragment_result_dialog;

    private MapsViewModel viewModel;
    private TextView response;
    private ImageView colorBase;
    private CircleImageView colorLogo;
    private Button confirm;

    public PenaltyDialogFragment() {
    }

    /**
     * Builds the {@link PenaltyDialogFragment}
     */
    public static PenaltyDialogFragment newInstance() {
        return new PenaltyDialogFragment();
    }

    @Override
    public Integer getLayoutId() {
        return layoutId;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.QuestionStyle);
        //create or connect viewmodel to fragment
        viewModel = ViewModelProviders.of(getActivity(),
                new MapsViewModelFactory(get(CheckpointRepository.class), get(QuestionRepository.class), get(ResultRepository.class),
                        get(AttendeeRepository.class), get(GeofenceManager.class), getActivity().getApplicationContext()))
                .get(MapsViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.setPenaltyOnScreen(true);
        response = view.findViewById(R.id.textResult);
        response.setHeight((int) (getScreenHeightPixels() * 0.3));
        colorBase = view.findViewById(R.id.imageBackgroundResult);
        colorLogo = view.findViewById(R.id.imageLogoResult);
        setCancelable(false);
        confirm = view.findViewById(R.id.confirmResult);

        confirm.setOnClickListener(v -> {
            Log.d(TAG, "UPDATE PIN CALLED IN RESPONSE FRAGMENT");
            viewModel.updateCheckpointCompleted();
            viewModel.setPenaltyOnScreen(false);
            dismiss();
        });

        showResponse();

        // Play sound effect
        final MapsActivity mapsActivity = (MapsActivity) getActivity();
        mapsActivity.getJukebox().playSoundForGameEvent(SoundEvent.AnswerCorrect);
    }

    //changes look of responsefragment depending if answered correctly
    private void showResponse() {
        response.setText(R.string.penaltyText);
        colorBase.setBackgroundColor(ContextCompat.getColor(getActivity(), b3Blue));
        colorLogo.setImageResource(R.drawable.b3logo_purple);
        confirm.setBackgroundColor(ContextCompat.getColor(getActivity(), b3Blue));
    }

}