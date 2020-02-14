package com.example.yfind

import android.app.Service
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import kotlinx.android.synthetic.main.activity_main.mapView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val map = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 40.249727, -111.649265, 16)
        mapView.map = map

        // create feature layer with its service feature table
        // create the service feature table
        val buildingsFeatureTable = ServiceFeatureTable(
            "https://services.arcgis.com/FvF9MZKp3JWPrSkg/arcgis/rest/services/Campus_Buildings/FeatureServer/0"
        )
        val roomsFeatureTable = ServiceFeatureTable(
            "https://services.arcgis.com/FvF9MZKp3JWPrSkg/arcgis/rest/services/Floorplans_V/FeatureServer/0"
        )

        // create the feature layer using the service feature table
        val buildingLayer = FeatureLayer(buildingsFeatureTable)
        val roomsLayer = FeatureLayer(roomsFeatureTable)

        // add the layer to the map
        map.operationalLayers.add(buildingLayer)
        map.operationalLayers.add(roomsLayer)

        // set the map to be displayed in the mapview
        mapView.map = map
    }

    override fun onPause() {
        super.onPause()
        mapView.pause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.dispose()
    }
}
