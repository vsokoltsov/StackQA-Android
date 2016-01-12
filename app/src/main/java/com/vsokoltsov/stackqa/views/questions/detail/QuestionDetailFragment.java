package com.vsokoltsov.stackqa.views.questions.detail;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.vsokoltsov.stackqa.controllers.AppController;
import com.vsokoltsov.stackqa.models.Question;
import com.vsokoltsov.stackqa.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link QuestionDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link QuestionDetailFragment# newInstance} factory method to
 * create an instance of this fragment.
 */
public class QuestionDetailFragment extends Fragment{
//    public Question DETAIL_QUESTION;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public Question detailQuestion;
    private View fragmentView;


    public QuestionDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle != null){
            detailQuestion = bundle.getParcelable("question");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView =  inflater.inflate(R.layout.fragment_question_detail, container, false);
        if(detailQuestion != null){
            try {
                TextView textView = (TextView) fragmentView.findViewById(R.id.questionText);
                TextView rateView = (TextView) fragmentView.findViewById(R.id.questionRate);
                TextView createdAtView = (TextView) fragmentView.findViewById(R.id.questionCreatedAt);
                TextView titleView = (TextView) fragmentView.findViewById(R.id.questionTitle);
                TextView tagsView = (TextView) fragmentView.findViewById(R.id.questionTags);


                titleView.setText(detailQuestion.getTitle());
                textView.setText(detailQuestion.getText());
                rateView.setText(String.valueOf(detailQuestion.getRate()));
                createdAtView.setText(detailQuestion.getCreatedAt());
                tagsView.setText(detailQuestion.getTags());
                if(detailQuestion.getTags() == ""){
                    tagsView.setVisibility(View.GONE);
                } else {
                    tagsView.setVisibility(View.VISIBLE);
                    tagsView.setText(detailQuestion.getTags());
                }

                setCategoryInfo(fragmentView);
                setUserInfo(fragmentView);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return fragmentView;
    }

    // TODO: Rename method, update argument and hook method into UI event


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public void setCategoryInfo(View fragmentView){
        final ImageView categoryImage = (ImageView) fragmentView.findViewById(R.id.categoryImageView);
        final TextView categoryTitle = (TextView) fragmentView.findViewById(R.id.categoryTitle);

        String url = AppController.APP_HOST + detailQuestion.getCategory().getImageUrl();
        ImageRequest ir = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                categoryImage.setImageBitmap(response);
                categoryTitle.setText(detailQuestion.getCategory().getTitle());
            }
        }, 0, 0, null, null);
        AppController.getInstance().addToRequestQueue(ir);
    }

    public void setUserInfo(View fragmentView){
        final ImageView userImage = (ImageView) fragmentView.findViewById(R.id.userAvatarView);
        final TextView userName = (TextView) fragmentView.findViewById(R.id.userName);

        String url = AppController.APP_HOST + detailQuestion.getUser().getAvatarUrl();
        ImageRequest ir = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                userImage.setImageBitmap(response);
                userName.setText(detailQuestion.getUser().getCorrectNaming());
                userImage.getLayoutParams().height = 60;
                userImage.getLayoutParams().width = 60;
            }
        }, 0, 0, null, null);
        AppController.getInstance().addToRequestQueue(ir);
    }

}