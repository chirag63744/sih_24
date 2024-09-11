package com.example.busyatra_user;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    AutoCompleteTextView autoCompleteTextView;
    RecyclerView recyclerView;
    SearchResultAdapter adapter;
    List<SearchResultItem> searchResults;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        autoCompleteTextView = root.findViewById(R.id.autoCompleteTextView);
        recyclerView = root.findViewById(R.id.searchResultsRecyclerView);

        // Initialize the search results list and RecyclerView adapter
        searchResults = new ArrayList<>();
        adapter = new SearchResultAdapter(searchResults);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call a method to fetch search results based on the user's input
                // and update the searchResults list accordingly.
                // For example, you could use a network call or a local search function.

                // Dummy data for demonstration:
                List<SearchResultItem> newSearchResults = new ArrayList<>();
                newSearchResults.add(new SearchResultItem(R.drawable.education, "Result 1"));
                newSearchResults.add(new SearchResultItem(R.drawable.education, "Result 2"));
                newSearchResults.add(new SearchResultItem(R.drawable.education, "Result 3"));
                updateSearchResults(newSearchResults);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return root;
    }

    // Method to update the searchResults list and refresh the adapter
    private void updateSearchResults(List<SearchResultItem> newResults) {
        searchResults.clear();
        searchResults.addAll(newResults);
        adapter.notifyDataSetChanged();
    }

    // Inner RecyclerView adapter class for displaying search results
    private class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder> {

        List<SearchResultItem> data;

        SearchResultAdapter(List<SearchResultItem> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_result, parent, false);
            return new SearchResultViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
            SearchResultItem item = data.get(position);
            holder.iconImageView.setImageResource(item.iconResId);
            holder.textView.setText(item.text);

            // Set click listener for each item
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "ok", Toast.LENGTH_SHORT).show();
                     // Handle item click here
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        // ViewHolder class for the search result item layout
        class SearchResultViewHolder extends RecyclerView.ViewHolder {
            ImageView iconImageView;
            TextView textView;

            SearchResultViewHolder(View itemView) {
                super(itemView);
                iconImageView = itemView.findViewById(R.id.resultIcon);
                textView = itemView.findViewById(R.id.resultText);
            }
        }
    }

    // Inner class representing the search result item data
    private static class SearchResultItem {
        int iconResId;
        String text;

        SearchResultItem(int iconResId, String text) {
            this.iconResId = iconResId;
            this.text = text;
        }
    }
}
