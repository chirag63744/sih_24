package com.example.busyatra_user;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;

import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

public class BlankFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    EditText editText;

    public BlankFragment() {
        // Required empty public constructor
    }

    public static BlankFragment newInstance(String param1, String param2) {
        BlankFragment fragment = new BlankFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_blank, container, false);
         editText = root.findViewById(R.id.search);

        String apiKey = "AIzaSyCYd9DNtP8fAnic_H5XwgCef7dmqj_7vB0"; // Replace with your Places API key
        Places.initialize(requireActivity().getApplicationContext(), apiKey);

        editText.setFocusable(false);

        editText.setOnClickListener(view -> {
            // Set the fields to specify which types of place data to return after the user has made a selection.
            List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);

            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(requireContext());
            // Use startActivityForResult to start the activity and get the result.
            startActivityForResult(intent, 100);
        });

        // Return the inflated layout view.
        return root;
    }

    // Override onActivityResult to handle the result from Autocomplete activity.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == AutocompleteActivity.RESULT_OK) {
                // Handle the result data here
                Place place = Autocomplete.getPlaceFromIntent(data);
                String address = place.getAddress();
                LatLng latLng = place.getLatLng();
                String name = place.getName();
                editText.setText(address);
                Toast.makeText(getActivity(), ""+latLng+name, Toast.LENGTH_SHORT).show();
                // Do whatever you need with the place data
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(getActivity(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                // Handle the error status if needed
            } else if (resultCode == AutocompleteActivity.RESULT_CANCELED) {
                // Handle when the user cancels the autocomplete activity
            }
        }
    }
}
