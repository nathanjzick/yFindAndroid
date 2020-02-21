package com.example.yfind

import android.app.SearchManager
import android.app.Service
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.FeatureQueryResult
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer

import java.util.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.*
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters.*


class SearchBuilding : AppCompatActivity() {
    companion object {
        private val TAG: String = SearchBuilding::class.java.simpleName
    }

    private val buildingsFeatureTable: ServiceFeatureTable by lazy {
        ServiceFeatureTable(getString(R.string.buildings_url))
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var results: MutableList<Feature>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_building)

//        lv.adapter = ListExampleAdapter(this)
        searchBuildings("")


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

        // create a query for the state that was entered
        val query = QueryParameters()
        query.orderByFields.add(OrderBy("Acronym", SortOrder.ASCENDING))
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
                print(result.toString())
                if(result.count() > 0){
                    var results: List<String> = result.map { it.attributes["Acronym"].toString() }
//                    while (resultIterator.hasNext()){
//                        results.add(resultIterator.next().attributes["Acronym"].toString())
//                    }
                    Log.d("result:     " , results[0])
                    val lv = findViewById<ListView>(R.id.list)
                    var arrayAdapter = ArrayAdapter<String>(this, R.layout.list_row, R.id.label, results)
                    lv.adapter = arrayAdapter
                }
//                if (resultIterator.hasNext()) {
//                    resultIterator.next().run {
//                        // get the extent of the first feature in the result to zoom to
////                        val envelope = geometry.extent
////                        mapView.setViewpointGeometryAsync(envelope, 10.0)
////                        // select the feature
//                        Log.d(TAG, attributes.toString())
//                    }
//                }
                else {
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
//    override
//    fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.menu_main, menu)
//
//        val searchItem = menu.findItem(R.id.action_search)
//        val searchView = searchItem.getActionView() as SearchView
//        searchView.setOnQueryTextListener(this)
//
//        return true
//    }
//
//    fun onQueryTextChange(query: String): Boolean {
//        // Here is where we are going to implement the filter logic
//        return false
//    }
//
//    fun onQueryTextSubmit(query: String): Boolean {
//        return false
//    }


}

