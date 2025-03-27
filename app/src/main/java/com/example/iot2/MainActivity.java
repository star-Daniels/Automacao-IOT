package com.example.iot2;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private TextView temperatura, umidade, hci, estadoLuz;
    private Button btnAlterarLuz;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = getWindow();

        window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));

        temperatura = findViewById(R.id.tempc);
        umidade = findViewById(R.id.umidade);
        hci = findViewById(R.id.hci);
        estadoLuz = findViewById(R.id.btn);



        // Inicializa Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Verifica se o usuário já está logado
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            buscarDados();
        } else {
            temperatura.setText("Usuário não autenticado");
            loginUsuario();
        }

        // Adiciona o OnClickListener ao botão
        estadoLuz.setOnClickListener(v -> alterarEstadoLuz());


    }

    // Método para buscar os dados do Firebase
    private void buscarDados() {
        databaseReference = FirebaseDatabase.getInstance().getReference("dados");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int lig = getResources().getColor(R.color.ld);
                    int des = getResources().getColor(R.color.led);

                    // Obtém os valores do Firebase
                    Double temp = snapshot.child("temper").getValue(Double.class);
                    Integer umid = snapshot.child("umidade").getValue(Integer.class);
                    Double indice = snapshot.child("index").getValue(Double.class);
                    Boolean luz = snapshot.child("estadoLuz").getValue(Boolean.class);

                    // Atualiza os TextViews com os dados recebidos
                    if (temp != null) temperatura.setText(String.format(" %.1f°", temp));
                    if (umid != null) umidade.setText(umid + "%");
                    if (indice != null) hci.setText(String.format("%.2f", indice));
                    if (luz != null){
                        boolean sts = true;
                        if(luz){
                            estadoLuz.setText("LIGADA");
                            estadoLuz.setBackgroundColor(lig);
                            databaseReference.child("estadoLuz").setValue(sts);
                        } else {
                            estadoLuz.setText("DESLIGADA");
                            estadoLuz.setBackgroundColor(des);
                        }
                    }

                } else {
                    temperatura.setText("Sem dados");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Erro ao obter dados", error.toException());
            }
        });
    }





    // Método para alterar o estado da luz
    private void alterarEstadoLuz() {
        // Verifica o estado atual da luz e inverte
        databaseReference.child("estadoLuz").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean estadoAtual = snapshot.getValue(Boolean.class);
                if (estadoAtual != null) {
                    boolean novoEstado = !estadoAtual;  // Inverte o estado atual
                    databaseReference.child("estadoLuz").setValue(novoEstado)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String status = novoEstado ? "Ligada" : "Desligada";
                                    estadoLuz.setText(status);
                                    int cor = novoEstado ? getResources().getColor(R.color.ld) : getResources().getColor(R.color.led);
                                    estadoLuz.setBackgroundColor(cor);
                                    Toast.makeText(MainActivity.this, "Estado da luz atualizado!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Erro ao alterar o estado da luz.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Erro ao verificar estado da luz", error.toException());
            }
        });
    }

    // Método para fazer login do usuário
    private void loginUsuario() {
        String email = "email";
        String senha = "senha";

        auth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            buscarDados();
                            Toast.makeText(MainActivity.this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Erro ao autenticar", Toast.LENGTH_SHORT).show();
                        Log.e("Auth", "Falha ao autenticar", task.getException());
                    }
                });
    }
}
