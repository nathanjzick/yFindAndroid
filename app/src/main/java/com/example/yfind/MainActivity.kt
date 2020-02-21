package com.example.yfind

import android.app.SearchManager
import android.app.Service
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.FeatureQueryResult
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import kotlinx.android.synthetic.main.activity_main.mapView
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG: String = MainActivity::class.java.simpleName
    }
    private val buildingsFeatureTable: ServiceFeatureTable by lazy {
        ServiceFeatureTable(getString(R.string.buildings_url))
    }
    private val roomsFeatureTable: ServiceFeatureTable by lazy {
        ServiceFeatureTable(getString(R.string.rooms_url))
    }
    private val buildingLayer: FeatureLayer by lazy {
        FeatureLayer(buildingsFeatureTable)
    }
    private val roomsLayer: FeatureLayer by lazy {
        FeatureLayer(roomsFeatureTable)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val map = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 40.249727, -111.649265, 16)
        mapView.map = map

        // create feature layer with its service feature table
        // create the service feature table
//        val buildingsFeatureTable = ServiceFeatureTable(
//            "https://services.arcgis.com/FvF9MZKp3JWPrSkg/arcgis/rest/services/Campus_Buildings/FeatureServer/0"
//        )
//        val roomsFeatureTable = ServiceFeatureTable(
//            "https://services.arcgis.com/FvF9MZKp3JWPrSkg/arcgis/rest/services/Floorplans_V/FeatureServer/0"
//        )
//
//        // create the feature layer using the service feature table
//        val buildingLayer = FeatureLayer(buildingsFeatureTable)
//        val roomsLayer = FeatureLayer(roomsFeatureTable)
//
//        // add the layer to the map
//        map.operationalLayers.add(buildingLayer)
//        map.operationalLayers.add(roomsLayer)

        // set the map to be displayed in the mapview
        mapView.map = map
    }
    override fun onNewIntent(intent: Intent) {
        this.intent = intent

        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.let {
                if (it.isNotEmpty()) {
                    searchBuildings(it)
                }
            }
        }
    }

    private fun searchBuildings(searchString: String) {
        // clear any previous selections
        buildingLayer.clearSelection()
        // create a query for the state that was entered
        val query = QueryParameters()
        // make search case insensitive
        query.whereClause =
        "(upper(Name) LIKE '% ${searchString.toUpperCase(Locale.US)}%' OR Upper(Acronym) LIKE '%${searchString.toUpperCase(Locale.US)}%') AND Acronym != ''"
        // call select features
        val future: ListenableFuture<FeatureQueryResult> = buildingsFeatureTable.queryFeaturesAsync(query)
        // add done loading listener to fire when the selection returns
        future.addDoneListener {
            try {
                // call get on the future to get the result
                val result = future.get()
                // check there are some results
                val resultIterator = result.iterator()
                if (resultIterator.hasNext()) {
                    resultIterator.next().run {
                        // get the extent of the first feature in the result to zoom to
//                        val envelope = geometry.extent
//                        mapView.setViewpointGeometryAsync(envelope, 10.0)
//                        // select the feature
//                        buildingLayer.selectFeature(this)
                        Log.d(TAG, attributes.toString())
                    }
                } else {
                    "No buildings found with name: $searchString".also {
                        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                        Log.d(TAG, it)
                    }
                }
            } catch (e: Exception) {
                "Feature search failed for: $searchString. Error: ${e.message}".also {
                    Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                    Log.e(TAG, it)
                }

            }
        }
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
