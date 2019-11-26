package com.example.test;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.test.model.Document;
import com.example.test.model.Result;
import com.google.gson.Gson;

import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyOnClickListener implements View.OnClickListener, Runnable {
    //검색 키워드
    MapView mapView;
    private EditText editText;
    private EditText editText2;
    private MapPoint.GeoCoordinate center;
    Gson gson = new Gson();

    public MyOnClickListener(MapView mapView, EditText editText, EditText editText2, MapPoint.GeoCoordinate center){
        this.mapView = mapView;
        this.editText = editText;
        this.editText2 = editText2;
        this.center = center;
    }

    @Override
    public void onClick(View v) {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        String resultJson = null;
        try {
            // Open the connection
            URL url = new URL("https://dapi.kakao.com/v2/local/search/keyword.json?y="+center.latitude+"&x="+center.longitude+"&radius="+editText2.getText().toString()+"&query="+editText.getText().toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "KakaoAK restapi키");
            InputStream is = conn.getInputStream();

            // Get the stream
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            // Set the result
            resultJson = builder.toString();
        }
        catch (Exception e) {
            // Error calling the rest api
            Log.e("REST_API", "GET method failed: " + e.getMessage());
            e.printStackTrace();
        }

        Log.d("rest 결과", resultJson);

        Result result = gson.fromJson(resultJson, Result.class);

        mapView.removeAllPOIItems();
        mapView.removeAllCircles();
        for(Document d : result.getDocuments()){
            double longitude = Double.parseDouble(d.getX());
            double latitude = Double.parseDouble(d.getY());

            MapPOIItem poiItem = new MapPOIItem();
            poiItem.setItemName(d.getPlaceName());
            poiItem.setMapPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude));
            poiItem.setMarkerType(MapPOIItem.MarkerType.BluePin);
            mapView.addPOIItem(poiItem);
        }
        MapCircle circle = new MapCircle(MapPoint.mapPointWithGeoCoord(center.latitude, center.longitude), Integer.parseInt(editText2.getText().toString()), Color.RED, Color.argb(60,0,255,0));
        mapView.addCircle(circle);

    }
}
