package com.universl.hp.hithatawadinawadan.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.universl.hp.hithatawadinawadan.Adapter.QuotesAdapter;
import com.universl.hp.hithatawadinawadan.R;
import com.universl.hp.hithatawadinawadan.Response.QuotesResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SuccessFragment extends Fragment implements SearchView.OnQueryTextListener {

    QuotesAdapter quotesAdapter;
    ListView listView;
    SearchView searchView;
    List<QuotesResponse> quotesResponseArrayList,quotesResponses;
    private DatabaseReference databaseQuotes;
    ArrayList<String> image_path;

    public SuccessFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        databaseQuotes = FirebaseDatabase.getInstance().getReference("favorite_quotes");
        searchView = view.findViewById(R.id.search_title);
        listView = view.findViewById(R.id.quotes_list);

        quotesResponses = new ArrayList<>();
        image_path = new ArrayList<>();
        //final ArrayList<String> image_path;
        quotesResponseArrayList = (ArrayList<QuotesResponse>) getArguments().getSerializable("quotes");

        searchView.setOnQueryTextListener(this);
        return view;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        quotesAdapter.filter(newText);
        return false;
    }
    @Override
    public void onStart() {
        super.onStart();
        databaseQuotes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                quotesResponses.clear();
                image_path.clear();
                for (DataSnapshot quotesSnapshot : dataSnapshot.getChildren()){
                    QuotesResponse quotes = quotesSnapshot.getValue(QuotesResponse.class);

                    quotesResponses.add(quotes);
                }
                for (int i = 0; i < quotesResponses.size(); i++){
                    image_path.add(quotesResponses.get(i).photo);
                }
                if (getActivity()!=null){
                    quotesAdapter = new QuotesAdapter(getActivity(),quotesResponseArrayList,quotesResponses,image_path);
                }

                //quotesAdapter = new QuotesAdapter(getContext(),quotesResponseArrayList);
                listView.setAdapter(quotesAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        assert quotesResponseArrayList != null;
                        searchView.setQuery(quotesResponseArrayList.get(position).title,true);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
