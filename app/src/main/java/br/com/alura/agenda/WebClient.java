package br.com.alura.agenda;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by adilson on 23/04/2016.
 */
public class WebClient {

    public String post(String json){
        String endereco = "https://www.caelum.com.br/mobile";
        return realizaConexao(json, endereco);
    }

    public void insere(String json) {
        String endereco = "http://192.168.1.100:8080/api/aluno";
        realizaConexao(json, endereco);
    }

    @Nullable
    private String realizaConexao(String json, String endereco) {
        try {
            URL url = new URL(endereco);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("content-type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            PrintStream output = new PrintStream(connection.getOutputStream());
            output.println(json);

            connection.connect();

            Scanner scanner = new Scanner(connection.getInputStream());
            String resposta = scanner.nextLine();
            return resposta;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
