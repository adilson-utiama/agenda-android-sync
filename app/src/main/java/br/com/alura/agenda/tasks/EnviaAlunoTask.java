package br.com.alura.agenda.tasks;

import android.os.AsyncTask;

import br.com.alura.agenda.WebClient;
import br.com.alura.agenda.converter.AlunoConverter;
import br.com.alura.agenda.modelo.Aluno;

/**
 * Created by Adilson on 17/06/2017.
 */

public class EnviaAlunoTask extends AsyncTask{
    private final Aluno aluno;

    public EnviaAlunoTask(Aluno aluno) {
        this.aluno = aluno;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        String json = new AlunoConverter().converteParaJSONCompleto(aluno);
        new WebClient().insere(json);
        return null;
    }
}
