package br.com.alura.agenda;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import br.com.alura.agenda.adapter.AlunosAdapter;
import br.com.alura.agenda.dao.AlunoDAO;
import br.com.alura.agenda.event.AtualizaListaAlunoEvent;
import br.com.alura.agenda.modelo.Aluno;
import br.com.alura.agenda.retrofit.RetrofitInicializador;
import br.com.alura.agenda.sinc.AlunoSincronizador;
import br.com.alura.agenda.tasks.EnviaAlunosTask;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaAlunosActivity extends AppCompatActivity {

    private final AlunoSincronizador sincronizador = new AlunoSincronizador(this);
    private ListView listaAlunos;
    private SwipeRefreshLayout swipe;
    private EventBus eventBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_alunos);

        eventBus = EventBus.getDefault();

        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] { Manifest.permission.RECEIVE_SMS } , 123456);
        }

        listaAlunos = (ListView) findViewById(R.id.lista_alunos);

        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe_lista_alunos);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sincronizador.buscaTodos();
                sincronizador.atualizaAlunosInternos();
            }
        });

        listaAlunos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listat, View item, int position, long id) {
                Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(position);
                Intent intentFormulario = new Intent(ListaAlunosActivity.this, FormularioActivity.class);
                intentFormulario.putExtra("aluno", aluno);
                startActivity(intentFormulario);
            }
        });

        Button novoAluno = (Button)findViewById(R.id.novo_aluno);
        novoAluno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListaAlunosActivity.this, FormularioActivity.class);
                startActivity(intent);
            }
        });

        registerForContextMenu(listaAlunos);

        sincronizador.buscaTodos();
        sincronizador.atualizaAlunosInternos();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void atualizaListaAlunosEvent(AtualizaListaAlunoEvent event){
        if(swipe.isRefreshing()) {
             swipe.setRefreshing(false);
        }
        carregaLista();

    }

    private void carregaLista() {

        AlunoDAO alunoDAO = new AlunoDAO(this);
        List<Aluno> alunos =  alunoDAO.buscaAlunos();

        for (Aluno aluno : alunos) {
            Log.i("ID do aluno: ", String.valueOf(aluno.getId()));
            Log.i("Aluno sincronizado: ", String.valueOf(aluno.getSincronizado()));
        }

        alunoDAO.close();

        AlunosAdapter adapter = new AlunosAdapter(this, alunos);
        listaAlunos.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus.register(this);
        carregaLista();
    }

    @Override
    protected void onPause() {
        super.onPause();
        eventBus.unregister(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lista_alunos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_enviar_notas:
                new EnviaAlunosTask(this).execute();
                break;
            case R.id.menu_baixar_provas:
                Intent baixarProvas = new Intent(this, ProvasActivity.class);
                startActivity(baixarProvas);
                break;
            case R.id.menu_mapa:
                Intent irParaMapa = new Intent(this, MapaActivity.class);
                startActivity(irParaMapa);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, final ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        final Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(info.position);

        MenuItem itemSite = menu.add("Visitar Site");
        Intent intentSite = new Intent(Intent.ACTION_VIEW);
        String site = aluno.getSite();
        if(!site.startsWith("http://")){
            site = "http://" + site;
        }
        intentSite.setData(Uri.parse(site));
        itemSite.setIntent(intentSite);

        MenuItem itemSMS = menu.add("Mandar SMS");
        Intent intentSMS = new Intent(Intent.ACTION_VIEW);
        intentSMS.setData(Uri.parse("sms:" + aluno.getTelefone()));
        itemSMS.setIntent(intentSMS);

        MenuItem itemMapa = menu.add("Visualizar no Mapa");
        Intent intentMapa = new Intent(Intent.ACTION_VIEW);
        intentMapa.setData(Uri.parse("geo:0,0?q=" + aluno.getEndereco()));
        itemMapa.setIntent(intentMapa);

        MenuItem itemLigar = menu.add("Ligar para Aluno");
        itemLigar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(ActivityCompat.checkSelfPermission(ListaAlunosActivity.this, Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ListaAlunosActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE}, 123);
                }else{
                    Intent intentLigar = new Intent(Intent.ACTION_CALL);
                    intentLigar.setData(Uri.parse("tel: " + aluno.getTelefone()));
                    startActivity(intentLigar);
                }
                return false;
            }
        });


        MenuItem deletar = menu.add("Deletar");
        deletar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                Call<Void> call = new RetrofitInicializador().getAlunoService().deleta(aluno.getId());
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Log.i("onResponse", "Aluno removido com sucesso");
                        AlunoDAO alunoDAO = new AlunoDAO(ListaAlunosActivity.this);
                        alunoDAO.deleta(aluno);
                        alunoDAO.close();

                        Toast.makeText(ListaAlunosActivity.this, "Aluno removido com sucesso", Toast.LENGTH_SHORT).show();

                        carregaLista();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("onFailure: ", "Nao foi possivel remover aluno");
                        Toast.makeText(ListaAlunosActivity.this, "Não foi possivel remover aluno", Toast.LENGTH_SHORT).show();
                    }
                });


                return false;
            }
        });
    }
}
