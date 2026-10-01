package com.hf.tr760.fingerprint;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hf.tr760.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SyncListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SyncListAdapter adapter;
    private SyncDbHelper dbHelper;
    private Button btnSyncAll;
    private ProgressDialog progressDialog;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_list);

        com.hf.ui.widget.EasyActionbar actionbar = findViewById(R.id.actionbar);
        actionbar.imgTitleStyle("Enregistrements Locaux", 0, v -> finish());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new SyncDbHelper(this);
        
        adapter = new SyncListAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        btnSyncAll = findViewById(R.id.btnSyncAll);
        btnSyncAll.setOnClickListener(v -> syncAllPending());

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        loadData();
    }

    private void loadData() {
        List<PendingEnrollment> all = dbHelper.getAllEnrollments();
        adapter.updateData(all);
    }

    private void syncAllPending() {
        List<PendingEnrollment> all = dbHelper.getAllEnrollments();
        List<PendingEnrollment> pending = new ArrayList<>();
        for (PendingEnrollment e : all) {
            if (!e.isSynchronized()) {
                pending.add(e);
            }
        }

        if (pending.isEmpty()) {
            Toast.makeText(this, "Tout est déjà synchronisé !", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Synchronisation de " + pending.size() + " enregistrement(s)...");
        progressDialog.show();

        syncNext(pending, 0);
    }

    private void syncNext(List<PendingEnrollment> pending, int index) {
        if (index >= pending.size()) {
            runOnUiThread(() -> {
                progressDialog.dismiss();
                Toast.makeText(SyncListActivity.this, "Synchronisation terminée.", Toast.LENGTH_LONG).show();
                loadData();
            });
            return;
        }

        PendingEnrollment current = pending.get(index);
        
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        RequestBody body = RequestBody.create(current.getJsonPayload(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("http://192.168.11.221:8080/api/enroll")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Continue to next, but this one failed
                syncNext(pending, index + 1);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() || response.code() == 409) {
                    // Success or Duplicate (already exists on server) -> mark as synced
                    dbHelper.updateSyncStatus(current.getId(), true);
                }
                syncNext(pending, index + 1);
            }
        });
    }
}
