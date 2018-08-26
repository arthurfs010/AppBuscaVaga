package bv.buscavaga;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import me.drakeet.materialdialog.MaterialDialog;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private LocationManager lm;
    private Location location;
    private double longitude = 0.0;
    private double latitude = 0.0;
    private MaterialDialog mMaterialDialog;

    public static final String TAG = "LOG";
    public static final int REQUEST_PERMISSIONS_CODE = 128;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    //Inicia o mapa
    @Override
    public void onMapReady(GoogleMap map) {
        LatLng atual;
        atual = new LatLng(latitude, longitude);
        if (lm != null) {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
            atual = new LatLng(latitude, longitude);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                callDialog("É preciso que a permissão ACESS_FINE_LOCATION esteja liberada para acesso.", new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_CODE);
            }
        } else {
            readMyCurrentCoordinates();
        }


        // Add um marcador ao mapa
        //LatLng atual = new LatLng(latitude, longitude);
        //map.addMarker(new MarkerOptions().position(atual).title("Localiza"));

        //habilita o botão para mover ir para localização do dispositivo
        map.setMyLocationEnabled(true);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(atual, 14));

        //move o mapa para a localização atual
        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(cascavel, 14));
        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(atual, 14));

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Lat: " + location.getLatitude() + " | Long: " + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //valida se a permissão foi concedida
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissoes, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CODE: {
                for (int i = 0; i < permissoes.length; i++) {
                    if (permissoes[i].equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        readMyCurrentCoordinates();
                    }
                }
            }
            super.onRequestPermissionsResult(requestCode, permissoes, grantResults);
        }
    }

    //Pega as coordenadas do GPS e da Internet do aparelho e vai atualizando conforme o tempo
    private void readMyCurrentCoordinates() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGPSHabilitado = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isInternetHabilitada = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Location location = null;
        double latitude = 0.0;
        double longitude = 0.0;

        if (!isGPSHabilitado && !isInternetHabilitada) {
            Log.i(TAG, "Nenhum serviço de localização está habilitado para uso.");
        } else {
            if (isInternetHabilitada) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        callDialog("É preciso que a permissão ACESS_FINE_LOCATION esteja liberada para acesso.", new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_CODE);
                    }
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, this);
                Log.d(TAG, "Internet");
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }

            if (isGPSHabilitado) {
                if (location == null) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);
                    Log.d(TAG, "GPS Habilitado");
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
        }
        Log.i(TAG, "Latitude: "+latitude+" | Longitude: "+longitude);
    }


    //Alert solicitando permissão
    private void callDialog(String mensagem, final String[] permissoes){
        mMaterialDialog = new MaterialDialog(this).setTitle("Permissão").setMessage(mensagem).setPositiveButton("Ok", new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(MapsActivity.this, permissoes, REQUEST_PERMISSIONS_CODE);
                mMaterialDialog.dismiss();
            }
        }).setNegativeButton("Cancelar", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMaterialDialog.dismiss();
            }
        });
        mMaterialDialog.show();
    }
}
