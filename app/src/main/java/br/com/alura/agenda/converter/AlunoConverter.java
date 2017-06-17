package br.com.alura.agenda.converter;

import org.json.JSONException;
import org.json.JSONStringer;

import java.util.List;

import br.com.alura.agenda.modelo.Aluno;

/**
 * Created by adilson on 23/04/2016.
 */
public class AlunoConverter {

    public String converteParaJSON(List<Aluno> alunos) {
        try {
            JSONStringer jsonStringer = new JSONStringer();
            jsonStringer.object().key("list").array()
                    .object().key("aluno").array();

            for (Aluno aluno : alunos) {
                jsonStringer.object()
                        .key("id").value(aluno.getId())
                        .key("nome").value(aluno.getNome())
                        .key("telefone").value(aluno.getTelefone())
                        .key("endereco").value(aluno.getEndereco())
                        .key("site").value(aluno.getSite())
                        .key("nota").value(aluno.getNota())
                        .endObject();
            }
            return jsonStringer.endArray().endObject()
                    .endArray().endObject().toString();


        } catch (JSONException e) {
            e.printStackTrace();
        }


        return "";
    }

    public String converteParaJSONCompleto(Aluno aluno) {
        JSONStringer js = new JSONStringer();

        try {
            js.object()
                    .key("nome").value(aluno.getNome())
                    .key("telefone").value(aluno.getTelefone())
                    .key("endereco").value(aluno.getEndereco())
                    .key("site").value(aluno.getSite())
                    .key("nota").value(aluno.getNota())
                    .endObject();

            return js.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
