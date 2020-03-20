package com.example.mapas

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    //Permisos
    private val permissionFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permissionCoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION

    //un numero para identificar el permiso asignado
    private val CODIGO_SOLICITUD_PERMISO = 100

    //obtenemos los datos de la ubicacion
    var fusedLocationClient: FusedLocationProviderClient ?= null
    var locationRequest: LocationRequest ?= null
    private var callback: LocationCallback?= null
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //inicializacion
        fusedLocationClient = FusedLocationProviderClient(this)
        InicializarLocationRequest()
        //responde cuando se activa la funcion requestlocationUpdate
        callback = object:LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                if(mMap != null){
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true

                    for(ubicacion in locationResult?.locations!!){
                        //mensaje latitud y longitud
                        Toast.makeText(applicationContext, ubicacion.latitude.toString() + ", " + ubicacion.longitude.toString(), Toast.LENGTH_LONG).show()
                        val miPosision = LatLng(ubicacion.latitude, ubicacion.longitude)
                        //marcador
                        mMap.addMarker(MarkerOptions().position(miPosision).title("aquí estoy"))
                        //Camara
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(miPosision))
                    }
                }
            }
        }
    }
    private fun InicializarLocationRequest(){
        locationRequest = LocationRequest()
        //cada cuanto
        locationRequest?.interval = 10000
        //velocidad mas alta del intervalo
        locationRequest?.fastestInterval = 5000
        //Prioridad
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
    }
    private fun validarPermisosUbicacion():Boolean{
        val hayUbicacionPrecisa = ActivityCompat.checkSelfPermission(this, permissionFineLocation) == PackageManager.PERMISSION_GRANTED
        val hayUbicacionOrdinaria = ActivityCompat.checkSelfPermission(this, permissionCoarseLocation) == PackageManager.PERMISSION_GRANTED
        return hayUbicacionPrecisa && hayUbicacionOrdinaria
    }
    @SuppressLint("MissingPermission")
    private fun obtenerUbicacion(){
        fusedLocationClient?.requestLocationUpdates(locationRequest, callback, null)
    }
    private fun pedirPermisos(){
        val proveerContexto = ActivityCompat.shouldShowRequestPermissionRationale(this, permissionFineLocation)
        if(proveerContexto){
            solicitarPermiso()
        }
        else{
            solicitarPermiso()
        }
    }
    private fun solicitarPermiso(){
        requestPermissions(arrayOf(permissionFineLocation, permissionCoarseLocation), CODIGO_SOLICITUD_PERMISO)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            CODIGO_SOLICITUD_PERMISO -> {
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    obtenerUbicacion()
                }
                else{
                    Toast.makeText(this, "se necesita el permiso para acceder a la ubicación", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    fun inicializarLocationRequest(){}
    private fun detenerActualizacionUbicacion(){
        fusedLocationClient?.removeLocationUpdates(callback)
    }

    override fun onStart() {
        super.onStart()
        if(validarPermisosUbicacion()){
            obtenerUbicacion()
        }
        else{
            pedirPermisos()
        }
    }

    override fun onPause() {
        super.onPause()
        detenerActualizacionUbicacion()
    }
}
