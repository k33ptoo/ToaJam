package com.keeptoo.toajam.chat;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.keeptoo.toajam.R;
import com.keeptoo.toajam.chat.adapter.ChatAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by keeptoo on 11/04/2017.
 */

public class ChatFragment extends Fragment {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swiperefresh_nearby)
    SwipeRefreshLayout swipeLayout;

    ChatAdapter adapter;
    View view;
    ArrayList n_items;

    public ChatFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_nearby, container, false);
        ButterKnife.bind(this, view);
        initViews();
        swipeLayout.setColorSchemeColors(
                Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initViews();
                        swipeLayout.setRefreshing(false);
                    }
                }, 2000);

            }
        });

        return view;
    }


    private void initViews() {
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        n_items = new ArrayList<>();
        n_items.add("TMTest 1");
        n_items.add("TMTest 2");
        n_items.add("TMTest 3");
        n_items.add("TMTest 4");
        n_items.add("TMTest 1");
        n_items.add("TMTest 2");
        n_items.add("TMTest 3");
        n_items.add("TMTest 4");
        n_items.add("TMTest 1");
        n_items.add("TMTest 2");
        n_items.add("TMTest 3");
        n_items.add("TMTest 4");
        n_items.add("TMTest 1");
        n_items.add("TMTest 2");
        n_items.add("TMTest 3");
        n_items.add("TMTest 4");

        RecyclerView.Adapter adapter = new ChatAdapter(n_items);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);


        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(getActivity().getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

            });

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null && gestureDetector.onTouchEvent(e)) {
                    int position = rv.getChildAdapterPosition(child);

                }


                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

                rv.setNestedScrollingEnabled(false);
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
    }
}
