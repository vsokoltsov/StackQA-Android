package com.vsokoltsov.stackqa.views.questions.detail;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.vsokoltsov.stackqa.R;
import com.vsokoltsov.stackqa.controllers.AppController;
import com.vsokoltsov.stackqa.messages.AnswerMessage;
import com.vsokoltsov.stackqa.messages.QuestionMessage;
import com.vsokoltsov.stackqa.models.Answer;
import com.vsokoltsov.stackqa.models.AnswerFactory;
import com.vsokoltsov.stackqa.models.AuthManager;
import com.vsokoltsov.stackqa.models.Question;
import com.vsokoltsov.stackqa.models.QuestionFactory;
import com.vsokoltsov.stackqa.views.answers.AnswerForm;
import com.vsokoltsov.stackqa.views.answers.AnswerListFragment;
import com.vsokoltsov.stackqa.views.questions.form.QuestionsFormActivity;
import com.vsokoltsov.stackqa.views.questions.list.QuestionsListActivity;
import com.vsokoltsov.stackqa.views.questions.list.QuestionsListFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class QuestionDetail extends ActionBarActivity implements QuestionsListFragment.Callbacks {
    public static Question selectedQuestion;
    private ScrollView layout;
    private List<Question> questionsList = new ArrayList<Question>();
    private boolean replaceFragment = false;
    private AuthManager authManager = AuthManager.getInstance();
    private Menu mainMenu;
    private ImageButton sendAnswer;
    private EditText answerText;
    private AnswerListFragment answersListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);
        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mActionBarToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sendAnswer = (ImageButton) findViewById(R.id.sendAnswer);
        answerText = (EditText) findViewById(R.id.answerUserText);
        if (sendAnswer != null && answerText != null) {
            sendAnswer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        sendAnswer(view);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            selectedQuestion = (Question) extras.getParcelable("question");
            questionsList = extras.getParcelableArrayList("questions");
        }
        if (isTablet) {
            baseConfigurationForTablet();
        }
        else {
            baseConfigForPhone(savedInstanceState);
        }
    }

    private void sendAnswer(View view) throws JSONException {
        if (answerText.getText().toString().trim().equals("")) {
            answerText.setError("Answer can't be blank");
        }
        else {
            String answerTextString = answerText.getText().toString();
            JSONObject answerParams = new JSONObject();
            answerParams.put("text", answerTextString);
            answerParams.put("user_id", authManager.getCurrentUser().getId());
            answerParams.put("question_id", selectedQuestion.getID());
            JSONObject params = new JSONObject();
            params.put("answer", answerParams);
            AnswerFactory.getInstance().create(selectedQuestion.getID(), params);
        }
    }

    private void baseConfigForPhone(Bundle savedInstanceState) {
        try {
            setViewLayout((ScrollView) findViewById(R.id.questionViewMainLayout));
            EditText answerText = (EditText) findViewById(R.id.answerUserText);
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            setSuccesButtonHandler(answerText);
        } catch(Exception e){
            e.printStackTrace();
        }

        if (savedInstanceState == null) {
            if(selectedQuestion != null) {
                loadQuestionData();
            }
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

        }
    }

    private void baseConfigurationForTablet() {
        QuestionFactory.getInstance().get(selectedQuestion.getID());
        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList("questions", (ArrayList<? extends Parcelable>) questionsList);
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        QuestionsListFragment fragment = new QuestionsListFragment();
        fragment.setArguments(arguments);
        fragmentTransaction.add(R.id.question_list, fragment);
        fragmentTransaction.commit();
    }

    public void setLayoutHeight(int height){
        try {
            ViewGroup.LayoutParams params = this.layout.getLayoutParams();
            params.height += height;
            this.layout.setLayoutParams(params);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public int getLayoutHeight(){
        return this.layout.getLayoutParams().height;
    }

    public void setViewLayout(ScrollView layout){
        this.layout = layout;
    }

    public void loadQuestionData(){
        String url = AppController.APP_HOST+"/api/v2/questions/"+selectedQuestion.getID();
        JsonObjectRequest questionRequest = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            successQuestionLoadCallback(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("TAG", "Error: " + error.getMessage());
            }
        });
        AppController.getInstance().addToRequestQueue(questionRequest);
    }

    public void successQuestionLoadCallback(JSONObject response) throws JSONException {
        selectedQuestion = new Question(response);
        JSONArray answersList = response.getJSONArray("answers");
        JSONArray commentsList = response.getJSONArray("comments");
        Bundle arguments = new Bundle();
        arguments.putParcelable("question", selectedQuestion);
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        loadMainQuestionFragment(arguments, fragmentTransaction);
        try {
            loadQuestionAnswersFragment(arguments, fragmentTransaction, response.getJSONArray("answers"));
        } catch(Exception e){
            e.printStackTrace();
        }
        fragmentTransaction.commit();
        setActivityButtons();
    }

    public void loadMainQuestionFragment(Bundle arguments, FragmentTransaction fragmentTransaction){
        QuestionDetailFragment fragment = new QuestionDetailFragment();
        fragment.setArguments(arguments);
        if (replaceFragment) {
            fragmentTransaction.replace(R.id.detail_fragment, fragment);
        }
        else {
            fragmentTransaction.add(R.id.detail_fragment, fragment);
        }

    }

    private void loadQuestionDetailInfo(Bundle arguments, FragmentTransaction fragmentTransaction,
                                        JSONArray answersList, JSONArray commentsList) {
        arguments.putString("answers", answersList.toString());
        arguments.putString("comments", commentsList.toString());
        QuestionDetailInfoFragment fragment = new QuestionDetailInfoFragment();
        fragment.setArguments(arguments);
        if (replaceFragment) {
            fragmentTransaction.replace(R.id.question_info, fragment);
        }
        else {
            fragmentTransaction.add(R.id.question_info, fragment);
        }
    }

    public void loadQuestionAnswersFragment(Bundle arguments, FragmentTransaction fragmentTransaction, JSONArray answers){
        answersListFragment = AnswerListFragment.newInstance(answers);
        if (replaceFragment) {
            fragmentTransaction.replace(R.id.question_info, answersListFragment);
        }
        else {
            fragmentTransaction.add(R.id.question_info, answersListFragment);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        mainMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpTo(this, new Intent(this, QuestionsListActivity.class));
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                return true;
            case R.id.editQuestion:
                Intent detailIntent = new Intent(this, QuestionsFormActivity.class);
                detailIntent.putExtra("question", selectedQuestion);
                startActivity(detailIntent);
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAnswerForm(){
        try {
            Intent detailIntent = new Intent(this, AnswerForm.class);
//            detailIntent.putExtra("question", selectedQuestion);
            startActivity(detailIntent);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void showCommentForm(){

    }

    private void setSuccesButtonHandler(EditText answerText){
        answerText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onItemSelected(Question question) {
        replaceFragment = true;
        QuestionFactory.getInstance().get(question.getID());
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    // This method will be called when a MessageEvent is posted
    public void onEvent(QuestionMessage event) throws JSONException {
        if (event.response instanceof JSONObject) {
            switch (event.operationName){
                case "detail":
                    successQuestionLoadCallback(event.response);
                    break;
            }
        } else {
            switch (event.operationName){
                case "list":
                    break;
            }
        }

    }

    public void onEvent(AnswerMessage event) throws JSONException {
        if (event.response instanceof JSONObject) {
            switch (event.operationName){
                case "create":
                    parseSuccessAnswerCreation(event.response);
                    break;
            }
        } else {
            switch (event.operationName){
                case "create":
                    break;
            }
        }

    }

    private void parseSuccessAnswerCreation(JSONObject object) {
        answerText.setText("");
        Answer answer = new Answer(object);
        answersListFragment.setNewAnswerItem(answer);

    }

    private void parseFailureAnswerCreation(JSONObject object) {

    }

    private void setActivityButtons() {
        if (authManager.getCurrentUser() != null &&
                authManager.getCurrentUser().getId() == selectedQuestion.getUser().getId()) {
            getMenuInflater().inflate(R.menu.menu_question_detail, mainMenu);
            MenuItem editItem = (MenuItem) mainMenu.findItem(R.id.editQuestion);
            MenuItem deleteItem = (MenuItem) mainMenu.findItem(R.id.deleteQuestion);

            editItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            deleteItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }
    }

}