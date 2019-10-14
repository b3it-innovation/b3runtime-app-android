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
import com.b3.development.b3runtime.base.BaseQuestionFragment;
import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepository;
import com.b3.development.b3runtime.geofence.GeofenceManager;
import com.b3.development.b3runtime.ui.map.MapsViewModel;
import com.b3.development.b3runtime.ui.map.MapsViewModelFactory;
import com.github.abdularis.civ.CircleImageView;

import static com.b3.development.b3runtime.R.color.b3Yellow;
import static org.koin.java.KoinJavaComponent.get;

/**
 * Contains logic for displaying a {@link ResultFragment} to inform user of their result
 */
public class ResultFragment extends BaseQuestionFragment {

    public static final String TAG = ResultFragment.class.getSimpleName();

    private MapsViewModel viewModel;

    private static final int layoutId = R.layout.fragment_result_dialog;
    private TextView response;
    private ImageView colorBase;
    private CircleImageView colorLogo;
    private Button confirm;

    public ResultFragment() {
    }

    /**
     * Builds the {@link ResultFragment}
     *
     * @return responseFragment
     */
    public static ResultFragment newInstance() {
        Bundle arguments = new Bundle();
        ResultFragment resultFragment = new ResultFragment();
        resultFragment.setArguments(arguments);
        return resultFragment;
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
                new MapsViewModelFactory(get(CheckpointRepository.class), get(GeofenceManager.class), getActivity().getApplicationContext(), ""))
                .get(MapsViewModel.class);
        //observe allCheckpoints and set response with the result
        viewModel.allCheckpoints.observe(this, pins -> response.setText(viewModel.getResult()));
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

        colorBase.setBackgroundColor(ContextCompat.getColor(getActivity(), b3Yellow));
        colorLogo.setImageResource(R.drawable.b3logo_yellow);
        confirm.setText("Close App");
        confirm.setBackgroundColor(ContextCompat.getColor(getActivity(), b3Yellow));

        confirm.setOnClickListener(v -> {
            viewModel.updateCheckpointCompleted();
            dismiss();
        });
    }
}