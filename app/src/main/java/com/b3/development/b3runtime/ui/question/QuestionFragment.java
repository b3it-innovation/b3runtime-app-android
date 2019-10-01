package com.b3.development.b3runtime.ui.question;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.b3.development.b3runtime.R;
import com.b3.development.b3runtime.base.BaseQuestionFragment;
import com.b3.development.b3runtime.data.local.model.question.Question;
import com.b3.development.b3runtime.data.repository.question.QuestionRepository;
import com.b3.development.b3runtime.ui.FragmentShowHideCallback;

import static org.koin.java.KoinJavaComponent.get;

/**
 * Contains logic for displaying questions and handling the answer
 */
public class QuestionFragment extends BaseQuestionFragment {

    private QuestionViewModel viewModel;
    private int selectedOption;
    private int layoutId;
    private TextView questionTextView;
    private RadioGroup answers;
    private RadioButton buttonA;
    private RadioButton buttonB;
    private RadioButton buttonC;
    private RadioButton buttonD;
    private Button confirmButton;
    private ProgressBar pb;

    private FragmentShowHideCallback callback;


    public QuestionFragment() {
    }

    public static final QuestionFragment newInstance(int layoutId) {
        QuestionFragment fragment = new QuestionFragment();
        Bundle bundle = new Bundle(1);
        bundle.putInt("layoutId", layoutId);
        fragment.setArguments(bundle);
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //create or connect viewmodel to fragment
        viewModel = ViewModelProviders.of(this,
                new QuestionViewModelFactory(get(QuestionRepository.class)))
                .get(QuestionViewModel.class);
        this.layoutId = getArguments().getInt("layoutId");
        setStyle(DialogFragment.STYLE_NORMAL, R.style.QuestionStyle);
    }

    @Override
    public Integer getLayoutId() {
        return layoutId;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //enables attaching/detaching of fragment from inside fragment
        // needs to be here due to recreation of activity on screen rotation
        callback = (FragmentShowHideCallback) getActivity();
        //sets data as in ViewModel
        questionTextView = view.findViewById(R.id.textQuestion);
        buttonA = view.findViewById(R.id.optionA);
        buttonB = view.findViewById(R.id.optionB);
        buttonC = view.findViewById(R.id.optionC);
        buttonD = view.findViewById(R.id.optionD);
        pb = view.findViewById(R.id.progress_loader);
        pb.setVisibility(View.INVISIBLE);
        confirmButton = view.findViewById(R.id.buttonConfirmAnswer);
        confirmButton.setEnabled(false);
        Fragment fragment = this;
        //gets and forwards answer for validation
        answers = view.findViewById(R.id.radioGroupAnswers);
        answers.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                selectedOption = answers.getCheckedRadioButtonId();
                System.out.println("selected option:" + selectedOption);
                confirmButton.setEnabled(true);
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Calling viewModel to validate answer");
                viewModel.validateAnswer(selectedOption);
                callback.switchFragmentVisible(fragment);
                //uncheck buttons for next time question is shown
                buttonA.setChecked(false);
                buttonB.setChecked(false);
                buttonC.setChecked(false);
                buttonD.setChecked(false);
            }
        });
        setCancelable(false);

        //observe LiveData in ViewModel
        viewModel.showLoading.observe(this, QuestionFragment.this::showLoading);
        viewModel.quest.observe(this, QuestionFragment.this::handleQuestion);
        viewModel.validated.observe(this, QuestionFragment.this::showResponse);
        viewModel.question.observe(this, QuestionFragment.this::updateQuestion);
    }

    private void updateQuestion(Question q) {
        if (q == null) {
            return;
        }
        System.out.println("Update QUESTION called in Q Fragment");
        viewModel.updateQuestion(q);
    }

    private void showLoading(boolean b) {
        if (b) pb.setVisibility(View.VISIBLE);
    }

    private void showResponse(Boolean isCorrect) {
        ResponseFragment.newInstance(isCorrect).show(getFragmentManager(), "response");
        //remove observer and recreate MutableLiveData to prevent showing of response more than once
        viewModel.validated.removeObservers(this);
        viewModel.validated = new MutableLiveData<>();
    }

    //changes text in questionfragment to current question
    private void handleQuestion(Question question) {
        System.out.println("Question in fragment is null: " + (question == null));
        if (question == null) {
            //reset questions if all question are answered todo: (delete this in release version)
            viewModel.resetQuestionsIsAnswered();
            return;
        }
        questionTextView.setText(question.question);
        buttonA.setText(question.optionA);
        buttonB.setText(question.optionB);
        buttonC.setText(question.optionC);
        buttonD.setText(question.optionD);
    }
}