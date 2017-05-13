package com.gophillygo.explorer.fragments;

import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gophillygo.explorer.R;
import com.gophillygo.explorer.models.Destination;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DestinationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DestinationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DestinationFragment extends Fragment {
    public interface DestinationManager {
        // return a destination given its ID
        Destination getDestination(int id);
    }

    private static final String DESTINATION_ID = "destinationId";
    private static final String LOG_LABEL = "DestinationFragment";

    private int destinationId;
    private Destination destination;
    private DestinationManager destinationManager;
    private View destinationView;

    private OnFragmentInteractionListener mListener;

    public DestinationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param destinationId Identifier for the Destination to present
     * @return A new instance of fragment DestinationFragment.
     */
    public static DestinationFragment newInstance(int destinationId) {
        DestinationFragment fragment = new DestinationFragment();
        Bundle args = new Bundle();
        args.putInt(DESTINATION_ID, destinationId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            destinationId = getArguments().getInt(DESTINATION_ID);
        }

        // activity will tell us which destination to present
        destinationManager = (DestinationManager)getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_LABEL, "onResume");
        destination = destinationManager.getDestination(destinationId);
        Log.d(LOG_LABEL, "Got destination: " + destination.getName());

        TextView nameView = (TextView)destinationView.findViewById(R.id.destination_detail_name);
        nameView.setText(destination.getName());

        TextView detailView = (TextView)destinationView.findViewById(R.id.destination_detail_description);
        detailView.setText(fromHtml(destination.getDescription()));
        detailView.setMovementMethod(LinkMovementMethod.getInstance());

        ImageView imageView = (ImageView)destinationView.findViewById(R.id.destination_detail_image);
        Glide.with(this).load(destination.getImage()).into(imageView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        destinationView = inflater.inflate(R.layout.fragment_destination, container, false);
        return destinationView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }
}
