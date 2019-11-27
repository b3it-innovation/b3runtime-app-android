package com.b3.development.b3runtime.ui.question;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
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
import com.b3.development.b3runtime.base.BaseDialogFragment;
import com.b3.development.b3runtime.data.local.model.question.Question;
import com.b3.development.b3runtime.data.repository.attendee.AttendeeRepository;
import com.b3.development.b3runtime.data.repository.checkpoint.CheckpointRepository;
import com.b3.development.b3runtime.data.repository.question.QuestionRepository;
import com.b3.development.b3runtime.data.repository.result.ResultRepository;
import com.b3.development.b3runtime.geofence.GeofenceManager;
import com.b3.development.b3runtime.ui.FragmentShowHideCallback;
import com.b3.development.b3runtime.ui.map.MapsViewModel;
import com.b3.development.b3runtime.ui.map.MapsViewModelFactory;

import static org.koin.java.KoinJavaComponent.get;

/**
 * Contains logic for displaying questions and handling the answer
 */
public class QuestionFragment extends BaseDialogFragment {

    public static final String TAG = QuestionFragment.class.getSimpleName();
    private static final int layoutId = R.layout.fragment_question_dialog;

    private QuestionViewModel questionViewModel;
    private MapsViewModel mapsViewModel;
    private int selectedOption;
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

    public static final QuestionFragment newInstance() {
        QuestionFragment fragment = new QuestionFragment();
        Bundle arguments = new Bundle();
        fragment.setArguments(arguments);
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.QuestionStyle);
        //create or connect viewmodel to fragment
        questionViewModel = ViewModelProviders.of(this,
                new QuestionViewModelFactory(get(QuestionRepository.class)))
                .get(QuestionViewModel.class);
        mapsViewModel = ViewModelProviders.of(getActivity(),
                new MapsViewModelFactory(get(CheckpointRepository.class), get(QuestionRepository.class), get(ResultRepository.class),
                        get(AttendeeRepository.class), get(GeofenceManager.class), getActivity().getApplicationContext()))
                .get(MapsViewModel.class);
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
        questionTextView.setHeight((int) (getScreenHeightPixels() * 0.3));
        questionTextView.setMovementMethod(new ScrollingMovementMethod());
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
                Log.d(TAG, "selected option:" + selectedOption);
                confirmButton.setEnabled(true);
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Calling viewModel to validate answer");
                questionViewModel.validateAnswer(selectedOption);
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
        questionViewModel.getShowLoading().observe(getViewLifecycleOwner(), QuestionFragment.this::showLoading);
        questionViewModel.getQuest().observe(getViewLifecycleOwner(), QuestionFragment.this::handleQuestion);
        //questionViewModel.getValidated().observe(getViewLifecycleOwner(), QuestionFragment.this::showResponse);
        questionViewModel.getValidated().observe(getViewLifecycleOwner(), isCorrect -> {
            showResponse(isCorrect);
            if (isCorrect) {
                Log.d(TAG, "SKIP PIN CALLED IN RESPONSE FRAGMENT");
                mapsViewModel.updateCheckpointCorrectAnswer();
            } else {
                Log.d(TAG, "UPDATE PIN CALLED IN RESPONSE FRAGMENT");
                mapsViewModel.updateCheckpointCompleted();
            }
        });
        questionViewModel.getQuestion().observe(getViewLifecycleOwner(), QuestionFragment.this::updateQuestion);
    }

    private void updateQuestion(Question q) {
        if (q == null) {
            return;
        }
        Log.d(TAG, "Update QUESTION called in Q Fragment");
        questionViewModel.updateQuestion(q);
    }

    private void showLoading(boolean b) {
        if (b) {
            pb.setVisibility(View.VISIBLE);
        }
    }

    private void showResponse(Boolean isCorrect) {
        ResponseFragment.newInstance(isCorrect).show(getActivity().getSupportFragmentManager(), ResponseFragment.TAG);
        //remove observer and recreate MutableLiveData to prevent showing of response more than once
        questionViewModel.getValidated().removeObservers(this);
        questionViewModel.setValidated(new MutableLiveData<>());
    }

    //changes text in questionfragment to current question
    private void handleQuestion(Question question) {
        Log.d(TAG, "Question in fragment is null: " + (question == null));
        if (question == null) {
            //reset questions if all question are answered todo: (delete this in release version)
            questionViewModel.resetQuestionsIsAnswered();
            return;
        }
        questionTextView.setText(question.question);
        buttonA.setText(question.optionA);
        buttonB.setText(question.optionB);
        buttonC.setText(question.optionC);
        buttonD.setText(question.optionD);
    }

}