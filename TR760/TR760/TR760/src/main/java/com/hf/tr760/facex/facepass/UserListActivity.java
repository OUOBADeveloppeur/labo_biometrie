package com.hf.tr760.facex.facepass;

import static com.hf.tr760.facex.facepass.InitFacePassHandler.group_name;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hf.tr760.R;
import com.hf.tr760.databinding.ActivityUserListBinding;
import com.hf.tr760.facex.App;
import com.hf.tr760.facex.BaseActivity;
import com.hf.tr760.facex.facepass.db.User;
import com.hf.ui.base.EasyEvent;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnConfirmListener;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import mcv.facepass.FacePassException;
import mcv.facepass.FacePassHandler;

public class UserListActivity extends BaseActivity {

    ActivityUserListBinding userListBinding;

    List<User> users = new ArrayList<>();

    FacePassHandler facePassHandler;

    @Override
    public void onCreateBase(Bundle bundle) {
        userListBinding = ActivityUserListBinding.inflate(getLayoutInflater());
        setContentView(userListBinding.getRoot());

        users.clear();
        users.addAll(((App)getApplication()).getUserDao().loadAll());

        userListBinding.actionbar.backTitleStyle(getString(R.string.user_list));

        userListBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userListBinding.recyclerView.setAdapter(new RecyclerView.Adapter<UserHolder>() {
            @NonNull
            @Override
            public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new UserHolder(getLayoutInflater().inflate(R.layout.holder_user,parent,false));
            }

            @Override
            public void onBindViewHolder(@NonNull UserHolder holder, int position) {
                if(facePassHandler!=null){
                    try {
                        holder.head.setImageBitmap(facePassHandler.getFaceImage(users.get(position).faceToken.getBytes(StandardCharsets.ISO_8859_1)));
                    } catch (FacePassException e) {
                    }
                }
                holder.name.setText(users.get(position).name);
                holder.id.setText(users.get(position).id+"");
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(facePassHandler==null){
                            return;
                        }
                        new XPopup.Builder(UserListActivity.this).asConfirm(getString(R.string.face), getString(R.string.user_delete), new OnConfirmListener() {
                            @Override
                            public void onConfirm() {
                                try {
                                    if(!users.get(position).faceToken.isEmpty()) {
                                        facePassHandler.unBindGroup(group_name, users.get(position).faceToken.getBytes(StandardCharsets.ISO_8859_1));
                                        facePassHandler.deleteFace(users.get(position).faceToken.getBytes(StandardCharsets.ISO_8859_1));
                                    }
                                    ((App)getApplication()).getUserDao().delete(users.get(position));
                                    users.remove(position);
                                } catch (FacePassException e) {
                                }
                                userListBinding.recyclerView.getAdapter().notifyDataSetChanged();
                            }
                        }).show();
                    }
                });
            }

            @Override
            public int getItemCount() {
                return users.size();
            }
        });

        initFacePass();
    }

    private void initFacePass(){
        InitFacePassHandler.init(UserListActivity.this, new InitFacePassHandler.IFacePassInit() {
            @Override
            public void result(FacePassHandler facePassHandler) {
                if(facePassHandler==null){
                    return;
                }
                UserListActivity.this.facePassHandler = facePassHandler;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        userListBinding.recyclerView.getAdapter().notifyDataSetChanged();
                    }
                });
            }
        });
    }

    @Override
    public void onMessageRecieve(String name, EasyEvent msg) {

    }

    @Override
    public void restart() {

    }

    @Override
    public void onstart() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }
}
