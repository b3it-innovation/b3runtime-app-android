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
import com.b3.development.b3runtime.ui.map.MapsViewModel;
import com.b3.development.b3runtime.ui.map.MapsViewModelFactory;
import com.github.abdularis.civ.CircleImageView;

import static com.b3.development.b3runtime.R.color.b3Yellow;
import static org.koin.java.KoinJavaComponent.get;

/**
 * Contains logic for displaying a {@link CheckInFragment} to inform user if they want start the game
 */
public class CheckInFragment extends BaseDialogFragment {

    public static final String TAG = CheckInFragment.class.getSimpleName();
    private static final int layoutId = R.layout.fragment_result_dialog;

    private MapsViewModel viewModel;

    public CheckInFragment() {
    }

    /**
     * Builds the {@link CheckInFragment}
     *
     * @return responseFragment
     */
    public static CheckInFragment newInstance() {
        Bundle arguments = new Bundle();
        CheckInFragment fragmnet = new CheckInFragment();
        fragmnet.setArguments(arguments);
        return fragmnet;
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

        TextView response;
        ImageView colorBase;
        CircleImageView colorLogo;
        Button confirm;

        response = view.findViewById(R.id.textResult);
        response.setHeight((int) (getScreenHeightPixels() * 0.3));
        colorBase = view.findViewById(R.id.imageBackgroundResult);
        colorLogo = view.findViewById(R.id.imageLogoResult);
        setCancelable(false);
        confirm = view.findViewById(R.id.confirmResult);

        response.setText(getString(R.string.start_confirm_text));
        colorBase.setBackgroundColor(ContextCompat.getColor(getActivity(), b3Yellow));
        colorLogo.setImageResource(R.drawable.b3logo_yellow);
        confirm.setText(getString(R.string.start_text));
        confirm.setBackgroundColor(ContextCompat.getColor(getActivity(), b3Yellow));

        confirm.setOnClickListener(v -> {
            viewModel.updateCheckpointCompleted();
            dismiss();
        });
    }

}
