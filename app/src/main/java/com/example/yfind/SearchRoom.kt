package com.example.yfind

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Menu
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.FeatureQueryResult
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import java.util.*
import androidx.recyclerview.widget.RecyclerView
import android.widget.*
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters.*
import com.esri.arcgisruntime.geometry.Geometry
import kotlinx.android.parcel.Parcelize

class SearchRoom : AppCompatActivity() {
    companion object {
        private val TAG: String = SearchRoom::class.java.simpleName
    }

    private val roomsFeatureTable: ServiceFeatureTable by lazy {
        ServiceFeatureTable(getString(R.string.rooms_url))
    }

    private var building :String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_room)
        val bundle: Bundle? = intent.extras
        this.building = bundle?.getString("acronym").toString()
        SearchRooms("")
    }

    private fun SearchRooms(searchString: String) {

        // create a query for the state that was entered
        val query = QueryParameters()
//        query.orderByFields.add(OrderBy("Acronym", SortOrder.ASCENDING))
        // make search case insensitive
        Log.d("creating SearchRoom: ", building)

        query.whereClause =
            "upper(RoomNumber) LIKE '%${searchString.toUpperCase(Locale.US)}%' AND upper(BLDG_SHORT)='${building}' "
        query.maxFeatures = 10
        // call select features
        val future: ListenableFuture<FeatureQueryResult> = roomsFeatureTable.queryFeaturesAsync(query)
        // add done loading listener to fire when the selection returns
        future.addDoneListener {

            try {
                Log.d("TAG" , "in doneListener")
                // call get on the future to get the result
                val result = future.get().toList()
                // check there are some results
                val resultIterator = result.iterator()
                if(result.any()){
                    var results: List<String> = result.map { it.attributes["RoomNumber"].toString() }
//                    while (resultIterator.hasNext()){
//                        results.add(resultIterator.next().attributes["Acronym"].toString())
//                    }
                    Log.d("result:     " , results[0])
                    val lv = findViewById<ListView>(R.id.list)
                    var arrayAdapter = ArrayAdapter<String>(this, R.layout.list_row, R.id.label, results)
                    lv.adapter = arrayAdapter

                    lv.setOnItemClickListener{ parent, view, position, id ->
                        val geo = result[position].geometry.toJson() // The item that was clicked
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("geometry", geo)
                        startActivity(intent)
                    }
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
                    "No rooms found with name: $searchString for building: $building".also {
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
    override
    fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        // Inflate menu to add items to action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu)
        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.action_search).getActionView() as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(it: String): Boolean {
                if (it.isNotEmpty()) {
                    SearchRooms(it)
                }
                return false
            }

            override fun onQueryTextSubmit(it: String): Boolean {
                SearchRooms(it)
                return false
            }

        })
        searchView.setIconifiedByDefault(false)
        searchView.setSearchableInfo(
            searchManager.getSearchableInfo(componentName)
        )

        return true
    }
}


//@Parcelize
//class Geometry(val name: Geometry, val age: Int) : Parcelable