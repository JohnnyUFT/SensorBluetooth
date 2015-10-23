package com.br.sensor;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

public class MainActivity extends AppCompatActivity {

    protected Button btnConectar, btnDesconectar, btnLigar1, btnDesligar1; // cada um destes refere-se a um botão na View principal

    TextView sensor1;
    //TextView sensor2;

    private static final int solicitaAtivacao = 1;
    private static final int solicitaConexao = 2;

    // ponte para todas interações bluetooth
    private BluetoothAdapter meuBluetooth = null;

    //responsável por receber os macs de todos os dispositivos pareados
    private static String MAC = null;

    private boolean conexao = false;

    // identificador para a classe StatusAmarino
    private StatusAmarino statusAmarino = new StatusAmarino();

    // tudo que está no onCreate() acontece ao iniciar a Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // FAZENDO A LEITURA DOS BOTÕES:
        btnConectar = (Button) findViewById(R.id.btnConectar);
        btnDesconectar = (Button) findViewById(R.id.btnDesconectar);
        btnLigar1 = (Button) findViewById(R.id.ligarSensor); // estes botões foram removidos da aplicação, pelo menos provisoriamente
        btnDesligar1 = (Button) findViewById(R.id.desligarSensor);

        // os atributos sensor1 e sensor2 são diretamente conectados aos textViews de mesmo nome,
        // isso ocorre através da classe R, por meio do id.
        sensor1 = (TextView) findViewById(R.id.sensor1);
        //sensor2 = (TextView) findViewById(R.id.sensor2);

        // obtém o adaptador bluetoth local
        meuBluetooth = BluetoothAdapter.getDefaultAdapter();

        // se o dispositivo não possui bluetooth
        if (meuBluetooth == null) {
            Toast.makeText(getApplicationContext(), "Ops! seu dispositivo não possui Bluetooth", Toast.LENGTH_LONG).show();
        }
        // se o dispositivo possui bluetooth e o mesmo não estiver ativado
        if (!meuBluetooth.isEnabled()) {
            // solicita ativação utilizando a Intent padrão dos dispositivos android
            Intent ativa = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            // inicia a Activity de ativação e solicita um resultado
            startActivityForResult(ativa, solicitaAtivacao);
        }

        btnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent abreLista = new Intent(MainActivity.this, ListaDispositivos.class);
                startActivityForResult(abreLista, solicitaConexao);
            }
        });

        btnDesconectar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (conexao != false) {
                    Amarino.disconnect(MainActivity.this, MAC);
                    unregisterReceiver(statusAmarino);
                    conexao = false;
                }

            }
        });


        btnLigar1.setOnClickListener((new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (conexao != false) {// quanto a esta flag 'A' ou 'B', representa o primeiro e o segundo sensor (ou LED, mais precisamente) respectivamente
                    Amarino.sendDataToArduino(MainActivity.this, MAC, 'A', 1);// o 'jump of the cat' está aqui neste 1. Significa que está conectado
                    // Amarino.sendDataToArduino(MainActivity.this, MAC, 'B', 1);// quanto a esta flag 'A' ou 'B', representa o primeiro e o segundo sensor respectivamente
                } else {
                    Toast.makeText(getApplicationContext(), "Problemas ao ligar sensores", Toast.LENGTH_LONG).show();//caso não consiga ligá-los corretamente, aparecerá esta mensagem
                    Toast.makeText(getApplicationContext(), "Ou bluetooth desconectado", Toast.LENGTH_LONG).show();
                }
            }
        }));

        btnDesligar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao != false) {
                    Amarino.sendDataToArduino(MainActivity.this, MAC, 'A', 0);// este 0 aqui, indica que não há conexão
                    //Amarino.sendDataToArduino(MainActivity.this, MAC, 'B', 0);// setando 0 desliga-se ambos sensores (leds na verdade), A e B.
                } else {
                    Toast.makeText(getApplicationContext(), "Problemas ao desligar sensores", Toast.LENGTH_LONG).show();// esta mensagem aparecerá caso o sensor não seja desligado corretamente
                }
            }
        });
    }


    // este método foi criado para que o aplicativo não fique consumindo bateria rodando em background
    // este método é chamado pelo sistema android ao ver que não precisa mais manter o app rodando
    @Override
    protected void onStop() {
        super.onStop();
        if (conexao != false) { // verificar a condição correta a ser aplicada aqui
            Amarino.disconnect(MainActivity.this, MAC);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case solicitaAtivacao:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "Conexão bluetooth ativada com sucesso!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Ops! Bluetooth desativado!", Toast.LENGTH_LONG).show();

                    // fecha o aplicativo
                    finish();
                }
                break; // necessário para que o switch não fique rodando intermitentemente

            case solicitaConexao:
                if (resultCode == Activity.RESULT_OK) {
                    MAC = data.getExtras().getString(ListaDispositivos.endMAC);

                    registerReceiver(statusAmarino, new IntentFilter(AmarinoIntent.ACTION_CONNECTED));

                    Amarino.connect(MainActivity.this, MAC);
                } else {
                    Toast.makeText(getApplicationContext(), "Falha ao obter o endereço MAC!", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public class StatusAmarino extends BroadcastReceiver {
        // String inicio = "";
        //String umidadeSensor1 = "";
        //String umidadeSensor2 = "";

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (AmarinoIntent.ACTION_CONNECTED.equals(action)) {

                // registra o serviço para recepção de dados
                registerReceiver(statusAmarino, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));

                conexao = true;

                Toast.makeText(getApplicationContext(), "Bluetooth conectado com sucesso!", Toast.LENGTH_LONG).show();

            } else if (AmarinoIntent.ACTION_CONNECTION_FAILED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Erro de conexão, por favor tente novamente.", Toast.LENGTH_LONG).show();
            }

            if (AmarinoIntent.ACTION_RECEIVED.equals(action)) {

                final int tipoDados = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);

                //esse trecho serve para um sensor
                switch (tipoDados){
                    case AmarinoIntent.STRING_EXTRA:

                        String dados = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
                        sensor1.setText(dados);

                        break;
                }


                //esse trecho serve para dois sensorores

                /*
                switch (tipoDados) {
                    case AmarinoIntent.STRING_EXTRA:
                        String dados = intent.getStringExtra(AmarinoIntent.EXTRA_DATA); // recolhe os dados enviados pelo Arduino, através do Amarino, claro

                        if (dados.equals("s")) {// caso o primeiro dado recebido for o "s" então começa-se a dividir
                            // e alocar os recebimentos devido a ordem pre estabelecida no programa do arduino.
                            inicio = dados;
                        } else if (!inicio.isEmpty() && umidadeSensor1.isEmpty()) {
                            umidadeSensor1 = dados;
                            sensor1.setText(dados);
                        } else if (!umidadeSensor1.isEmpty() && umidadeSensor2.isEmpty()) {
                            umidadeSensor2 = dados;
                            sensor2.setText(dados);
                        } else if (!umidadeSensor2.isEmpty()) {//quando o terceiro dado for recebido limpa-se os dados de todos os campos
                            inicio = "";
                            umidadeSensor1 = "";
                            umidadeSensor2 = "";
                        }

                        break;
                }
                */
            }
        }
    }
}
