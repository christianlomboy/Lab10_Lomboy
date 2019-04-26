package edu.temple.Lab10_Lomboy;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static edu.temple.Lab10_Lomboy.MainActivity.books;

public class BookListFragment extends Fragment {

    OnCallbackReceivedList mCallback;

    public interface OnCallbackReceivedList {
        void displayBook(String title);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);

        String[] tempArr = new String[books.size()];
        for (int i = 0; i < books.size(); i++) {
            tempArr[i] = books.get(i).getTitle();
        }
        final String[] bookArray = tempArr;
        ArrayList<String> bookList = new ArrayList<>(Arrays.asList(bookArray));

        ListView listView = view.findViewById(R.id.lv_book_list);
        ArrayAdapter<String> bookAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_list_item_1, bookList);
        listView.setAdapter(bookAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallback.displayBook(bookArray[position]);
            }

        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity;
        if (context instanceof Activity) {
            activity = (Activity) context;
        } else {
            activity = null;
        }

        try {
            mCallback = (OnCallbackReceivedList) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("ClassCastException " + e);
        }

    }


}
