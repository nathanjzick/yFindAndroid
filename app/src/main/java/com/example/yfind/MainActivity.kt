package com.example.yfind


import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.tasks.networkanalysis.Route
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask
import com.esri.arcgisruntime.tasks.networkanalysis.Stop
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.ExecutionException


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

    private var MapView: MapView? = null
    private var mRoute: Route? = null
    private val mRouteTask: RouteTask? = null
    private val mRouteParams: RouteParameters? = null
    private var mSourcePoint: Point? = null
    private var mDestinationPoint: Point? = null
    private var mRouteSymbol: SimpleLineSymbol? = null
    private var mGraphicsOverlay: GraphicsOverlay? = null
    private var roomGeometry : Geometry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        MapView = findViewById(R.id.mapView)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        val bundle: Bundle? = intent.extras
        this.roomGeometry = Geometry.fromJson(bundle?.getString("geometry"))
        val lo = LocationUtil()
        var currentLocation: Location? = lo.currentLocation

        val map = ArcGISMap(Basemap.Type.LIGHT_GRAY_CANVAS_VECTOR, 40.249727, -111.649265, 16)
//        mapView.map = map

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
        map.operationalLayers.add(roomsLayer)

        // set the map to be displayed in the mapview
        mapView.map = map

        setupSymbols()

        progressBar.isInvisible = true


        val routeTaskUrl =
            "https://utility.arcgis.com/usrsvcs/servers/a8ee36150d1b4178b33affcb5d7027cb/rest/services/World/Route/NAServer/Route_World"
        // create route task from San Diego service
        val routeTask = RouteTask(applicationContext, routeTaskUrl)
//        var  listenableFuture: ListenableFuture<RouteParameters> = routeTask.createDefaultParametersAsync()

//        routeTask.loadAsync()
        val routeParameters = routeTask.createDefaultParametersAsync().get()

        routeTask.addDoneLoadingListener {
            if (routeTask.loadError == null && routeTask.loadStatus == LoadStatus.LOADED) { // route task has loaded successfully
                try { // get default route parameters
//                    val ESPG_3857 = SpatialReference.create(102100);
                    val roomCenter = this.roomGeometry?.extent?.center
                    if (lo == null){

                    }
                    val stop1Loc = Stop(Point(lo.currentLocation!!.latitude, lo.currentLocation!!.longitude))
                    val stop2Loc = Stop(roomCenter)
                    // add route stops
                    routeParameters.setStops(listOf<Stop>(stop1Loc, stop2Loc))

                    val result = routeTask.solveRouteAsync(routeParameters).get()
                    val routes = result.routes
                    mRoute = routes[0]
                    val routeGraphic: Graphic = Graphic(mRoute?.routeGeometry, mRouteSymbol)
                    mGraphicsOverlay?.graphics?.add(routeGraphic);


                    val directions = mRoute?.directionManeuvers
//                    val directionsArray = List(directions.size!!)

                    // add mRouteSymbol graphic to the map
                    val selectedRouteSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.GREEN, 5F)
                    val selectedRouteGraphic = Graphic(
                        directions?.get(0)?.getGeometry(),
                        selectedRouteSymbol)
                    mGraphicsOverlay?.graphics?.add(selectedRouteGraphic)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        progressBar.isVisible = false

    }


    private fun setupSymbols() {
        mGraphicsOverlay = GraphicsOverlay()
        //add the overlay to the map view
        mapView?.graphicsOverlays?.add(mGraphicsOverlay)
        //[DocRef: Name=Picture Marker Symbol Drawable-android, Category=Fundamentals, Topic=Symbols and Renderers]
        //Create a picture marker symbol from an app resource
        val startDrawable =
            ContextCompat.getDrawable(this, R.drawable.ic_source) as BitmapDrawable?
        val pinSourceSymbol: PictureMarkerSymbol
        try {
            pinSourceSymbol = PictureMarkerSymbol.createAsync(startDrawable).get()
            pinSourceSymbol.loadAsync()
            pinSourceSymbol.addDoneLoadingListener {
                //add a new graphic as start point
                mSourcePoint =
                    Point(-117.15083257944445, 32.741123367963446, SpatialReferences.getWgs84())
                val pinSourceGraphic = Graphic(mSourcePoint, pinSourceSymbol)
                mGraphicsOverlay!!.getGraphics().add(pinSourceGraphic)
            }
            pinSourceSymbol.offsetY = 20f
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
        //[DocRef: END]
        val endDrawable =
            ContextCompat.getDrawable(this, R.drawable.ic_destination) as BitmapDrawable?
        val pinDestinationSymbol: PictureMarkerSymbol
        try {
            pinDestinationSymbol = PictureMarkerSymbol.createAsync(endDrawable).get()
            pinDestinationSymbol.loadAsync()
            pinDestinationSymbol.addDoneLoadingListener {
                //add a new graphic as end point
                mDestinationPoint =
                    Point(-117.15557279683529, 32.703360305883045, SpatialReferences.getWgs84())
                val destinationGraphic =
                    Graphic(mDestinationPoint, pinDestinationSymbol)
                mGraphicsOverlay!!.getGraphics().add(destinationGraphic)
            }
            pinDestinationSymbol.offsetY = 20f
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
        //[DocRef: END]
        mRouteSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5F)
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

