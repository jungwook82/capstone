package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<User> arrayList;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>();

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("User");

        // ChildEventListener를 사용하여 데이터 변경 시 화면 갱신
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                User user = dataSnapshot.getValue(User.class);
                arrayList.add(user);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                // 데이터가 변경된 경우
                User updatedUser = dataSnapshot.getValue(User.class);
                int index = getItemIndex(updatedUser);
                if (index != -1) {
                    // 리스트에서 해당 아이템을 찾아 업데이트
                    arrayList.set(index, updatedUser);
                    adapter.notifyItemChanged(index);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // 데이터가 삭제된 경우
                User removedUser = dataSnapshot.getValue(User.class);
                int index = getItemIndex(removedUser);
                if (index != -1) {
                    // 리스트에서 해당 아이템을 찾아 삭제
                    arrayList.remove(index);
                    adapter.notifyItemRemoved(index);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                // 데이터 위치가 변경된 경우
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MainActivity", String.valueOf(databaseError.toException()));
            }
        });

        adapter = new CustomAdapter(arrayList, this);
        recyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), new LinearLayoutManager(this).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        ((CustomAdapter) adapter).setOnItemClickListener(new CustomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 클릭 이벤트 처리
                Intent detailIntent = new Intent(MainActivity.this, DetailActivity.class);
                // 클릭된 아이템의 ID를 가져오기
                String clickedItemId = arrayList.get(position).getId();
                // DetailActivity로 전달하기 위해 Intent에 추가
                detailIntent.putExtra("ID", clickedItemId);
                startActivity(detailIntent);
                Toast.makeText(MainActivity.this, "클릭된 아이템: " + arrayList.get(position).getId(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_write, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int curId = item.getItemId();
        switch (curId) {
            case R.id.menu_write:
                Intent intent = new Intent(getApplicationContext(), WriteActivity.class);
                startActivity(intent);
                break;

            case R.id.menu_logout:
                // 로그아웃 시 자동 로그인 설정 초기화
                clearAutoLoginSetting();

                mFirebaseAuth.signOut();
                Toast.makeText(MainActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();

                // 로그인 화면으로 이동
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish(); // 현재 액티비티 종료
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearAutoLoginSetting() {
        // SharedPreferences를 사용하여 자동 로그인 설정 초기화
        getSharedPreferences("AutoLoginPrefs", MODE_PRIVATE)
                .edit()
                .remove("autoLogin")
                .apply();
    }

    private int getItemIndex(User user) {
        for (int i = 0; i < arrayList.size(); i++) {
            if (arrayList.get(i).getId().equals(user.getId())) {
                return i;
            }
        }
        return -1;
    }
}