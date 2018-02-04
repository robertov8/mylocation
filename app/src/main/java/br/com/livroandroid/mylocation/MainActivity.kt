package br.com.livroandroid.mylocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private val TAG = "livroandroid"
    var map: GoogleMap? = null
    var mapFragment: SupportMapFragment? = null
    var mGoogleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Configura o objeto GoogleApiClient
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        // Solicita as permissões
        PermissionUtils.validate(this, 0,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (result in grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                // Alguma permissão foi negada, agora é com você :-)
                alertAndFinish()
                return
            }
        }
        // Se chegou aqui está OK :-)
    }

    private fun alertAndFinish() {
        run {
            val builder = AlertDialog.Builder(this)
            builder
                .setTitle(R.string.app_name)
                .setMessage(R.string.alertAndFinish)
                .setPositiveButton("OK") { _, _ -> finish() } // Add the buttons
                .create()
                .show()
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        Log.d(TAG, "onMapReady: $map")
        this.map = map
        // Configura o tipo de mapa
        map?.mapType = GoogleMap.MAP_TYPE_NORMAL
    }

    override fun onStart() {
        super.onStart()
        // Conecta no Google Play Services
        mGoogleApiClient?.connect()
    }

    override fun onStop() {
        super.onStop()
        // Desconecta
        mGoogleApiClient?.disconnect()
    }

    override fun onConnected(bundle: Bundle?) {
        toast("Conectado no Google Play Services!")
    }

    override fun onConnectionSuspended(p0: Int) {
        toast("Conexão interrompida.")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        toast("Erro ao conectar: $connectionResult")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater?.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("MissingPermission")
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_my_location -> {
                // Última localização
                val l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
                Log.d(TAG, "lastLocation: $l")
                setMapLocation(l)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Atualiza a coordenada do mapa
    private fun setMapLocation(l: Location?) {
        if (map != null && l != null) {
            val latLng = LatLng(l.latitude, l.longitude)
            val update = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
            map?.animateCamera(update)
            Log.d(TAG, "setMapLocaltion: $l")
            tvInfo.text = String.format("Lat/Lnt> ${l.latitude}/${l.longitude}, provider: ${l.provider}")
            // Desenha uma bolinha vermelha
            val circle = CircleOptions()
                    .center(latLng)
                    .fillColor(Color.RED)
                    .radius(25.0) // Em metros
            map?.clear()
            map?.addCircle(circle)
        }
    }

    private fun toast(s: String) {
        Toast.makeText(baseContext, s, Toast.LENGTH_LONG).show()
    }

}
