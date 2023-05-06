package com.example.fitnessisus;

public class removed {

    //    private void requestNewLocationData() {
//
//        Log.d("LocationMainActivity", "Here1");
//
//        // Initializing LocationRequest object with appropriate methods
//        LocationRequest locationRequest = new LocationRequest();
//        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
//        locationRequest.setInterval(5);
//        locationRequest.setFastestInterval(0);
//        locationRequest.setNumUpdates(1);
//
//        // setting LocationRequest on FusedLocationClient
//        locationProvider = LocationServices.getFusedLocationProviderClient(this);
//
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//            return;
//
//        Log.d("LocationMainActivity", "Here10");
//        locationProvider.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
//        Log.d("LocationMainActivity", "Here11");
//    }
//
//    // method to check for permissions
//    private boolean checkPermissions() {
//        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
//            return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
//
//        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
//    }

    // method to check if location is enabled

    // If everything is alright then


//    public class CityTask extends AsyncTask<Void, Void, Void> implements LocationListener {
//
//        LocationManager locationManager;
//        boolean isGpsSignalAvailable = true;
//
//        public CityTask(){
//            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        }
//
//        @Override
//        public void onLocationChanged(@NonNull Location location) {
//
//        }
//
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//            LocationListener.super.onStatusChanged(provider, status, extras);
//
//            isGpsSignalAvailable = status == LocationProvider.AVAILABLE;
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            if(isInternetConnectionAvailable)
//                getUserCityName();
//
//            return null;
//        }
//
//        private void getUserCityName() {
//            // Get the user's current location
//            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, locationPermissionId);
//                return;
//            }
//
//            if(!isLocationEnabled()) {
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                startActivity(intent);
//            }
//
//            else{
//                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
//                fusedLocationClient.getLastLocation()
//                        .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
//                            @Override
//                            public void onSuccess(Location location) {
//                                if (location != null) {
//                                    // Get the user's city name from the location
//                                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.ENGLISH);
//                                    List<Address> addresses;
//                                    try {
//                                        Log.d("CityName", location.getLatitude() + " , " + location.getLongitude());
//                                        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
//                                        Log.d("CityName", " " + addresses.get(0));
//
//                                        if (!addresses.isEmpty()) {
//                                            String cityName = addresses.get(0).getLocality();
//                                            Log.d("CityName", "User's city name: " + cityName);
//
//                                            if(cityName != null){
//                                                city = cityName;
//
//                                                WeatherTask weatherTask = new WeatherTask();
//                                                weatherTask.execute();
//                                            }
//                                        }
//                                    }
//                                    catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            }
//                        });
//            }
//        }
//
//        private boolean isLocationEnabled() {
//            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//        }
//    }
}
