package com.purpura.app.ui.screens;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.purpura.app.R;
import com.purpura.app.adapters.ProductPageAdapter;
import com.purpura.app.configuration.Methods;

import com.purpura.app.model.mongo.Residue;

import java.io.Serializable;

public class ProductPage extends AppCompatActivity {

    private final Methods methods = new Methods();

    private RecyclerView recycler;
    @Nullable private Residue residue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_page_host);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        recycler = findViewById(R.id.recyclerViewProductPage);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        Bundle env = getIntent().getExtras();
        String cnpjFromIntent = env != null ? env.getString("cnpj", "") : "";
        Serializable ser = env != null ? env.getSerializable("residue") : null;
        if (ser instanceof Residue) residue = (Residue) ser;
        if (residue == null) {
            methods.openScreenActivity(this, com.purpura.app.ui.screens.errors.InternetError.class);
            return;
        }

        String cnpjEffective = residue.getCnpj();
        if (cnpjEffective == null || cnpjEffective.trim().isEmpty()) cnpjEffective = cnpjFromIntent;

        recycler.setAdapter(new ProductPageAdapter(residue, cnpjEffective));
    }
}
