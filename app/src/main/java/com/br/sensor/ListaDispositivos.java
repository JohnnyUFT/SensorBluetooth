/*
 ESTA CLASSE É APENAS PARA MOSTRAR A LISTA DE DISPOSITIVOS QUE ESTÃO DISPONIVEIS PARA CONEXÃO BLUETOOTH
 EM SUMA, É UMA IMPLEMENTAÇÃO SIMPLES E EFICAZ
 */

package com.br.sensor;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

/**
 * Created by Johnny on 23/09/2015.
 */
public class ListaDispositivos extends ListActivity {

    private BluetoothAdapter meuBluetooth;

    static String endMAC = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayAdapter<String> ArrayBluetooth = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        //recebe o adaptador bluetooth local
        meuBluetooth = BluetoothAdapter.getDefaultAdapter();

        // obtem a lista de dispositivos
        Set<BluetoothDevice> dispositivosPareados = meuBluetooth.getBondedDevices();

        if(dispositivosPareados.size() > 0){
            for(BluetoothDevice dispositivo : dispositivosPareados){
                String nome = dispositivo.getName();
                String mac = dispositivo.getAddress();
                ArrayBluetooth.add(nome + "\n" + mac);
            }
        }
        setListAdapter(ArrayBluetooth);
    }

    // exemplo: 00:11:22:33:44:55 são 17 caracteres
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // obtém todos os dados do item que foi clicado
        String infoGeral = ((TextView) v).getText().toString();

        // retira o endereço MAC que sãoos últimos 17 caracteres da informação
        String endereco = infoGeral.substring(infoGeral.length() - 17);

        // retorna o MAC para a atividade anterior
        Intent retormaMac = new Intent();
        retormaMac.putExtra(endMAC, endereco);

        //atribui o resultado como ok
        setResult(RESULT_OK, retormaMac);

        // fecha a lista
        finish();
    }
}
