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
import com.b3.development.b3runtime.data.repository.pin.PinRepository;
import com.b3.development.b3runtime.geofence.GeofenceManager;
import com.b3.development.b3runtime.ui.map.MapsViewModel;
import com.b3.development.b3runtime.ui.map.MapsViewModelFactory;
import com.github.abdularis.civ.CircleImageView;

import static com.b3.development.b3runtime.R.color.b3Purple;
import static com.b3.development.b3runtime.R.color.b3Yellow;
import static org.koin.java.KoinJavaComponent.get;

/**
 * Contains logic for displaying a {@link ResultFragment} to inform user of their result
 */
public class ResultFragment extends BaseQuestionFragment {

    private int layoutId;
    private TextView response;
    private ImageView colorBase;
    private CircleImageView colorLogo;
    private Button confirm;

    public ResultFragment(int layoutId) {
        this.layoutId = layoutId;
    }

    /**
     * Builds the {@link ResultFragment}
     *
     * @return responseFragment
     */
    public static ResultFragment newInstance() {
        Bundle arguments = new Bundle();
        ResultFragment resultFragment = new ResultFragment(R.layout.fragment_result_dialog);
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
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        response = view.findViewById(R.id.textResult);
        colorBase = view.findViewById(R.id.imageBackgroundResult);
        colorLogo = view.findViewById(R.id.imageLogoResult);
        setCancelable(false);
        confirm = view.findViewById(R.id.confirmResult);

        response.setText("Thank you for testing this app!");
        colorBase.setBackgroundColor(ContextCompat.getColor(getActivity(), b3Yellow));
        colorLogo.setImageResource(R.drawable.b3logo_yellow);
        confirm.setText("Close App");
        confirm.setBackgroundColor(ContextCompat.getColor(getActivity(), b3Yellow));

        //sets pin to completed and skips to next if correct answer on question
        confirm.setOnClickListener(v -> {
            //todo close app
            dismiss();
        });
    }

}