package com.b3.development.b3runtime.ui.question;

import android.os.Bundle;
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
import com.b3.development.b3runtime.sound.Jukebox;
import com.b3.development.b3runtime.sound.SoundEvent;
import com.b3.development.b3runtime.ui.map.MapsActivity;
import com.b3.development.b3runtime.ui.map.MapsViewModel;
import com.b3.development.b3runtime.ui.map.MapsViewModelFactory;
import com.github.abdularis.civ.CircleImageView;

import static com.b3.development.b3runtime.R.color.b3Purple;
import static com.b3.development.b3runtime.R.color.b3Yellow;
import static org.koin.java.KoinJavaComponent.get;

/**
 * Contains logic for displaying a {@link ResponseDialogFragment} to inform user of their result
 */
public class ResponseDialogFragment extends BaseDialogFragment {

    public static final String TAG = ResponseDialogFragment.class.getSimpleName();
    private static final String EXTRA_IS_CORRECT = "extraIsCorrect";
    private static final int layoutId = R.layout.fragment_result_dialog;

    private MapsViewModel viewModel;
    private TextView response;
    private ImageView colorBase;
    private CircleImageView colorLogo;
    private Button confirm;

    public ResponseDialogFragment() {
    }

    /**
     * Builds the {@link ResponseDialogFragment}
     *
     * @param isCorrect a boolean to decide on which response to show depending whether the
     * @return responseFragment
     */
    public static ResponseDialogFragment newInstance(boolean isCorrect) {
        Bundle arguments = new Bundle();
        arguments.putBoolean(ResponseDialogFragment.EXTRA_IS_CORRECT, isCorrect);
        ResponseDialogFragment responseFragment = new ResponseDialogFragment();
        responseFragment.setArguments(arguments);
        return responseFragment;
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
        response = view.findViewById(R.id.textResult);
        response.setHeight((int) (getScreenHeightPixels() * 0.3));
        colorBase = view.findViewById(R.id.imageBackgroundResult);
        colorLogo = view.findViewById(R.id.imageLogoResult);
        setCancelable(false);
        confirm = view.findViewById(R.id.confirmResult);
        //sets pin to completed and skips to next if correct answer on question
        confirm.setOnClickListener(v -> {
            dismiss();
        });
        if (getArguments() != null) {
            showResponse(getArguments().getBoolean(EXTRA_IS_CORRECT));
        }
        // Play sound effect
        final MapsActivity mapsActivity = (MapsActivity) getActivity();
        final Jukebox jukebox = mapsActivity.getJukebox();
        if (getArguments().getBoolean(EXTRA_IS_CORRECT)) {
            jukebox.playSoundForGameEvent(SoundEvent.AnswerCorrect);
        } else {
            jukebox.playSoundForGameEvent(SoundEvent.AnswerWrong);
        }
    }

    //changes look of responsefragment depending if answered correctly
    private void showResponse(Boolean isCorrect) {
        if (isCorrect) {
            response.setText(R.string.correctAnswer);
            colorBase.setBackgroundColor(ContextCompat.getColor(getActivity(), b3Yellow));
            colorLogo.setImageResource(R.drawable.b3logo_yellow);
            confirm.setBackgroundColor(ContextCompat.getColor(getActivity(), b3Yellow));
        } else {
            response.setText(R.string.wrongAnswer);
            colorBase.setBackgroundColor(ContextCompat.getColor(getActivity(), b3Purple));
            colorLogo.setImageResource(R.drawable.b3logo_purple);
            confirm.setBackgroundColor(ContextCompat.getColor(getActivity(), b3Purple));
        }
    }

}