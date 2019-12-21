package com.example.supporter.Fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.supporter.Adapter.UserAdapter;
import com.example.supporter.Other.ChatList;
import com.example.supporter.Other.User;
import com.example.supporter.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {


    private RecyclerView recyclerView;
    private View rootView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<User> listUser;
    private List<ChatList> listID;

    //Firebase
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        //init view
        initView();

        //init RecyclerView
        recyclerView = rootView.findViewById(R.id.recycler_view_friends);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // app data to ListFirends
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listID.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatList chatList = snapshot.getValue(ChatList.class);
                    listID.add(chatList);
                }

                addUserToList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return rootView;
    }

    private void addUserToList() {
        DatabaseReference drf = FirebaseDatabase.getInstance().getReference("Users");
        drf.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listUser.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    for (ChatList chatList : listID) {
                        if (chatList.getId().equals(user.getId())) {
                            listUser.add(user);
                            break;
                        }
                    }
                }

                mAdapter = new UserAdapter(listUser, getContext(), true);
                recyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initView() {
        //list user
        listUser = new ArrayList<>();
        listID = new ArrayList<>();

        //FireBase
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid());
    }

   }
