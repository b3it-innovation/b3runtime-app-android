package com.b3.development.b3runtime.ui.question;

import android.os.Bundle;
import android.util.DisplayMetrics;
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
import com.b3.development.b3runtime.data.repository.pin.PinRepository;
import com.b3.development.b3runtime.geofence.GeofenceManager;
import com.b3.development.b3runtime.ui.map.MapsViewModel;
import com.b3.development.b3runtime.ui.map.MapsViewModelFactory;
import com.github.abdularis.civ.CircleImageView;

import static com.b3.development.b3runtime.R.color.b3Yellow;
import static org.koin.java.KoinJavaComponent.get;

/**
 * Contains logic for displaying a {@link CheckinFragment} to inform user if they want start the game
 */
public class CheckinFragment extends BaseQuestionFragment {

    private MapsViewModel viewModel;

    private static final int layoutId = R.layout.fragment_result_dialog;
    private TextView response;
    private ImageView colorBase;
    private CircleImageView colorLogo;
    private Button confirm;

    public CheckinFragment(){}

    /**
     * Builds the {@link CheckinFragment}
     *
     * @return responseFragment
     */
    public static CheckinFragment newInstance() {
        Bundle arguments = new Bundle();
        CheckinFragment resultFragment = new CheckinFragment();
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
                new MapsViewModelFactory(get(PinRepository.class), get(GeofenceManager.class)))
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

        response.setText("Are you ready to start?");
        colorBase.setBackgroundColor(ContextCompat.getColor(getActivity(), b3Yellow));
        colorLogo.setImageResource(R.drawable.b3logo_yellow);
        confirm.setText("Start");
        confirm.setBackgroundColor(ContextCompat.getColor(getActivity(), b3Yellow));

        confirm.setOnClickListener(v -> {
            viewModel.updatePinCompleted();
            dismiss();
        });
    }

    private int getScreenHeightPixels() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int y = displayMetrics.heightPixels;
        return y;
    }

}
