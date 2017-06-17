package br.com.alura.agenda;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import br.com.alura.agenda.dao.AlunoDAO;
import br.com.alura.agenda.modelo.Aluno;

/**
 * Created by Adilson on 05/02/2017.
 */
public class MapaFragment extends SupportMapFragment implements OnMapReadyCallback {

    private GoogleMap mapa;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mapa = googleMap;
        LatLng posicaoEscola = pegaCoordenadaDoEndereco("Rua Vergueiro 3185, Vila Mariana, Sao Paulo");
        if(posicaoEscola != null) {
            centralizaEm(posicaoEscola);
        }

        AlunoDAO alunoDAO = new AlunoDAO(getContext());
        preencherMarcadore(alunoDAO, googleMap);

        new Localizador(getContext(), this);

    }

    private LatLng pegaCoordenadaDoEndereco(String endereco) {
        try {
            Geocoder geocoder = new Geocoder(getContext());
            List<Address> resultados =
                    geocoder.getFromLocationName(endereco, 1);
            if (!resultados.isEmpty()) {
                LatLng posicao = new LatLng(resultados.get(0).getLatitude(), resultados.get(0).getLongitude());
                return posicao;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void preencherMarcadore(AlunoDAO alunoDAO, GoogleMap googleMap){
        for(Aluno aluno : alunoDAO.buscaAlunos()){
            LatLng coordenadas = pegaCoordenadaDoEndereco(aluno.getEndereco());
            if(coordenadas != null){
                MarkerOptions marcador = new MarkerOptions();
                marcador.position(coordenadas);
                marcador.title(aluno.getNome());
                marcador.snippet(String.valueOf(aluno.getNota()));
                googleMap.addMarker(marcador);
            }
            alunoDAO.close();
        }
    }

    public void centralizaEm(LatLng coordenada) {
        if (mapa != null) {
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(coordenada, 17);
            mapa.moveCamera(update);
        }
    }

}
