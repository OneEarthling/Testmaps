package com.example.testmaps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Transformations.map
import androidx.navigation.fragment.findNavController
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import kotlinx.android.synthetic.main.fragment_first.*
import org.json.JSONObject


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        MapKitFactory.setApiKey("625fdc2d-efbc-474d-a572-322c42418e09")
        MapKitFactory.initialize(activity)
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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

            val objects =  obj.getJSONArray("objects")
            for (i in 0 until objects.length()) {
                val c: JSONObject = objects.getJSONObject(i)
                val pointLon = c.getJSONObject("point").get("lon").toString().toDouble()
                val pointLat = c.getJSONObject("point").get("lat").toString().toDouble()
                val color = c.getString("color")
                val text = c.getString("text")
                val link = c.getString("link")

                val pointCollection = mapView.getMap().getMapObjects().addCollection()
                pointCollection.addTapListener(this)
                val placemark = pointCollection.addPlacemark(Point(pointLat, pointLon))
                placemark.setUserData(text)
            }


        }
    }


     fun onMapObjectTap(mapObject: MapObject, point: Point) {
        //WhateverType data = (WhateverType) mapObject.getUserData();

    }
}

private fun MapObjectCollection.addTapListener(firstFragment: FirstFragment) {
    Toast.makeText(firstFragment.activity, "Tapped", Toast.LENGTH_LONG).show()
}
