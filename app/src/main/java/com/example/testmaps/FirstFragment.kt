package com.example.testmaps

import android.app.Activity
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Context.WINDOW_SERVICE
import android.graphics.*
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.runtime.Runtime.getApplicationContext
import com.yandex.runtime.image.ImageProvider
import kotlinx.android.synthetic.main.fragment_first.*
import org.json.JSONObject
import java.util.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    companion object {

        private const val PLACEMARKS_NUMBER = 2000
        private val CLUSTER_CENTERS: List<Point> = Arrays.asList<Point>(
            Point(55.756, 37.618),
            Point(59.956, 30.313),
            Point(56.838, 60.597),
            Point(43.117, 131.900),
            Point(56.852, 53.204)
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)

    }

    fun getAct(): Activity{
        return getActivity() as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey("625fdc2d-efbc-474d-a572-322c42418e09")
        MapKitFactory.initialize(activity)
        super.onCreate(savedInstanceState)

    }

    override fun onStop() {
        super.onStop()
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart();
        MapKitFactory.getInstance().onStart();
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            val json_string = it.assets.open("map.html").bufferedReader().use {
                it.readText()
            }
            val obj = JSONObject(json_string)
            val zoom = obj.get("zoom").toString().toFloat()
            val centerLon = obj.getJSONObject("center").get("lon").toString().toDouble()
            val centerLat = obj.getJSONObject("center").get("lat").toString().toDouble()

            mapView.map.move(
                CameraPosition(Point(centerLat, centerLon), zoom, 0.0f, 0.0f),
                Animation(Animation.Type.SMOOTH, 0F),
                null
            )

            val imageProvider: ImageProvider = ImageProvider.fromResource(
                activity, R.drawable.ico_red)

            val clusterizedCollection = mapView.map.mapObjects.addClusterizedPlacemarkCollection(YandexClusterListener())
//            val pointsCollection = mapView.map.mapObjects.addCollection()

            val points: ArrayList<Point> = ArrayList<Point>()
            val objects =  obj.getJSONArray("objects")
            for (i in 0 until objects.length()) {
                val c: JSONObject = objects.getJSONObject(i)
                val pointLon = c.getJSONObject("point").get("lon").toString().toDouble()
                val pointLat = c.getJSONObject("point").get("lat").toString().toDouble()
                val color = c.getString("color").toString()
                val text = c.getString("text")
                val link = c.getString("link")

                var icon = when (color){
                    "1"-> R.drawable.ico_red
                    "2"-> R.drawable.ico_yellow
                    "3"-> R.drawable.ico_green
                    else -> R.drawable.ico_blue
                }
                points.add(Point(pointLat, pointLon))
//                pointsCollection.addTapListener(YandexMapObjectTapListener())
//                pointCollection.addEmptyPlacemark(Point(pointLat, pointLon))

                val placemark = clusterizedCollection.addPlacemark(Point(pointLat, pointLon), ImageProvider.fromResource(context, icon))
                placemark.setUserData(text)
                placemark.addTapListener(YandexMapObjectTapListener())
            }
            clusterizedCollection.clusterPlacemarks(30.0, 19)
        }
    }

    class YandexClusterListener: ClusterListener {
        override fun onClusterAdded(cluster: Cluster) {
            Log.e("TAG", "cluster added")
            cluster.appearance.setIcon(
                TextImageProvider(Integer.toString(cluster.getSize()))
            );
            cluster.addClusterTapListener(YandexClusterTapListener())
        }
    }

    class YandexClusterTapListener: ClusterTapListener{
        override fun onClusterTap(cluster: Cluster): Boolean {
            Log.e("TAG", "cluster tapped")
            Toast.makeText(
                getApplicationContext(),
                "cluster tapped"+ cluster.getSize(),
                Toast.LENGTH_SHORT).show();
            return true;
        }

    }

    inner class YandexMapObjectTapListener() : MapObjectTapListener {
        override fun onMapObjectTap(mapObject: MapObject, point: Point): Boolean {
//            Toast.makeText(
//                getApplicationContext(),
//                "mapObject tapped" + mapObject.userData.toString(),
//                Toast.LENGTH_SHORT
//            ).show()
            // inflate the layout of the popup window
            val inflater: LayoutInflater = activity?.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.popup_window, null)
            popupView.findViewById<TextView>(R.id.textMarker).setText(
                Html.fromHtml((mapObject.userData.toString())))
            // create the popup window
            val width = LinearLayout.LayoutParams.WRAP_CONTENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            val focusable = true; // lets taps outside the popup also dismiss it
            val popupWindow = PopupWindow(popupView, width, height, focusable)
            // show the popup window
            // which view you pass in doesn't matter, it is only used for the window tolken
            popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

            popupView.findViewById<ImageButton>(R.id.closeBtn).setOnClickListener {
                popupWindow.dismiss()
            }

            popupView.findViewById<Button>(R.id.detailBtn).setOnClickListener {
                Toast.makeText(getApplicationContext(),"Подробнее",
                Toast.LENGTH_SHORT).show()
            }
            // dismiss the popup window when touched
//            popupView.setOnTouchListener(object : View.OnTouchListener {
//                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                    popupWindow.dismiss()
//                    return v?.onTouchEvent(event) ?: true
//                }
//            })
            return true
        }
    }
}



class TextImageProvider(private val text: String) : ImageProvider() {
    companion object {
        private const val FONT_SIZE = 26f
        private const val MAX_SIZE_BITMAP = 120
    }

    override fun getId(): String {
        return "text_$text"
    }

    override fun getImage(): Bitmap {
        val bm: Bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ico_cluster).copy(Bitmap.Config.ARGB_8888, true)
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.textSize = FONT_SIZE
        paint.textAlign = Paint.Align.CENTER
        // reducing too big bitmap
        var width = bm.width
        var height = bm.height
        val bitmapRatio = bm.width.toFloat() / bm.height.toFloat()
        if (bitmapRatio > 1.0) {
            width = MAX_SIZE_BITMAP
            height = (width / bitmapRatio).toInt()
        } else {
            height = MAX_SIZE_BITMAP
            width = (height * bitmapRatio).toInt()
        }
        val scaled = Bitmap.createScaledBitmap(bm, width, height,true)
        val canvas = Canvas(scaled)
        canvas.drawText(text, (scaled.width.toFloat() / 2.0F), (scaled.height.toFloat() / 2.0F + 5.0F), paint)
        return scaled
    }
}