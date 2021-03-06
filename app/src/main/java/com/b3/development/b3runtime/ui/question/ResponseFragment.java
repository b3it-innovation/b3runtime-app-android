package com.b3.development.b3runtime.ui.question;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseQuestionFragment;
import com.b3.development.b3runtime.ui.map.MapsViewModel;
import com.github.abdularis.civ.CircleImageView;

import static com.b3.development.b3runtime.R.color.b3Purple;
import static com.b3.development.b3runtime.R.color.b3Yellow;
import static org.koin.java.KoinJavaComponent.get;

/**
 * Contains logic for displaying a {@link ResponseFragment} to inform user of their result
 */
public class ResponseFragment extends BaseQuestionFragment {

    private static final String EXTRA_IS_CORRECT = "extraIsCorrect";

    private MapsViewModel viewModel;

    private int layoutId;
    private TextView response;
    private ImageView colorBase;
    private CircleImageView colorLogo;
    private Button confirm;

    public ResponseFragment(int layoutId) {
        this.layoutId = layoutId;
    }

    /**
     * Builds the {@link ResponseFragment}
     *
     * @param isCorrect a boolean to decide on which response to show depending whether the
     * @return responseFragment
     */
    public static ResponseFragment build(boolean isCorrect) {
        Bundle arguments = new Bundle();
        arguments.putBoolean(ResponseFragment.EXTRA_IS_CORRECT, isCorrect);
        ResponseFragment responseFragment = new ResponseFragment(R.layout.fragment_result_dialog);
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
        viewModel = get(MapsViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        response = view.findViewById(R.id.textResult);
        colorBase = view.findViewById(R.id.imageBackgroundResult);
        colorLogo = view.findViewById(R.id.imageLogoResult);
        setCancelable(false);
        confirm = view.findViewById(R.id.confirmResult);
        confirm.setOnClickListener(v -> {
            if (getArguments().getBoolean(EXTRA_IS_CORRECT)) {
                //todo update pin here
                System.out.println("SKIP PIN CALLED IN RESPONSE FRAGMENT");
                viewModel.skipPin();
            } else {
                //todo implement extra route
                System.out.println("UPDATE PIN CALLED IN RESPONSE FRAGMENT");
                viewModel.updatePin();
            }
            dismiss();
        });
        if (getArguments() != null) {
            showResponse(getArguments().getBoolean(EXTRA_IS_CORRECT));
        }
    }

    private void showResponse(Boolean isCorrect) {
        if (isCorrect) {
            response.setText(R.string.correctAnswer);
            colorBase.setBackgroundColor(getResources().getColor(b3Yellow));
            colorLogo.setImageResource(R.drawable.b3logo_yellow);
            confirm.setBackgroundColor(getResources().getColor(b3Yellow));
        } else {
            response.setText(R.string.wrongAnswer);
            colorBase.setBackgroundColor(getResources().getColor(b3Purple));
            colorLogo.setImageResource(R.drawable.b3logo_purple);
            confirm.setBackgroundColor(getResources().getColor(b3Purple));
        }
    }
}