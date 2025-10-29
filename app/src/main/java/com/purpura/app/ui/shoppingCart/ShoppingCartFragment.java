package com.purpura.app.ui.shoppingCart;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.purpura.app.R;
import com.purpura.app.configuration.Methods;
import com.purpura.app.model.postgres.Order;
import com.purpura.app.model.postgres.OrderItem;
import com.purpura.app.remote.service.PostgresService;

import java.io.Serializable;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShoppingCartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShoppingCartFragment extends Fragment {
    PostgresService service = new PostgresService();
    Methods methods = new Methods();
    Bundle env = new Bundle();
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ShoppingCartFragment() {
        // Required empty public constructor
    }

    public static ShoppingCartFragment newInstance(String param1, String param2) {
        ShoppingCartFragment fragment = new ShoppingCartFragment();
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

        FirebaseFirestore.getInstance()
                .collection("empresa")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(document -> {
                            if (!isAdded()) return;
                            if (document.exists()) {
                                String cnpj = document.getString("cnpj");
                                env.putString("id", cnpj);
                            }
                });

        Bundle env = getArguments();
        Serializable serItem = env.getSerializable("item");
        OrderItem item = (OrderItem) serItem;
        String sellerId = env.getString("sellerId");
        String cnpj = env.getString("id");

        Order order = new Order(
                sellerId,
                cnpj,
                null
        );

        verifyOrder(env.getString("id"), order, item);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shopping_cart, container, false);
    }

    public void verifyOrder(String cnpj, Order order, OrderItem item){
        try{
            service.getOrdersByClient(cnpj).enqueue(new Callback<List<Order>>() {
                @Override
                public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                    List<Order> orders = response.body();
                    orders.forEach( order -> {
                        if(order.getStatus().equals("aberto")){

                        } else {
                            createOrder(order);
                        }
                    });
                }

                @Override
                public void onFailure(Call<List<Order>> call, Throwable t) {

                }
            });

        }catch(Exception e){

        }
    }

    public void createOrder(Order order){
        try{
            service.createOrder(order).enqueue(new Callback<Order>() {
                @Override
                public void onResponse(Call<Order> call, Response<Order> response) {
                    System.out.println("sjdvnsjnvosnvjs");
                    System.out.println("sjdvnsjnvosnvjs");
                    System.out.println("sjdvnsjnvosnvjs");
                    System.out.println("sjdvnsjnvosnvjs");
                    System.out.println("sjdvnsjnvosnvjs");
                    System.out.println("sjdvnsjnvosnvjs");
                    System.out.println("sjdvnsjnvosnvjs");
                    System.out.println("sjdvnsjnvosnvjs");
                    System.out.println("sjdvnsjnvosnvjs");
                    System.out.println("sjdvnsjnvosnvjs");
                    System.out.println("sjdvnsjnvosnvjs");
                    System.out.println("sjdvnsjnvosnvjs");
                    System.out.println("sjdvnsjnvosnvjs");
                    System.out.println("sjdvnsjnvosnvjs");


                }

                @Override
                public void onFailure(Call<Order> call, Throwable t) {

                }
            });
        }catch(Exception e){

        }
    }

}