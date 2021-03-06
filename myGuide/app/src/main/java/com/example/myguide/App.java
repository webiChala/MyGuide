package com.example.myguide;

import android.app.Application;

import com.example.myguide.models.Availability;
import com.example.myguide.models.Course;
import com.example.myguide.models.Degree;
import com.example.myguide.models.Education;
import com.example.myguide.models.Event;
import com.example.myguide.models.FieldOfStudy;
import com.example.myguide.models.Message;
import com.example.myguide.models.User;
import com.example.myguide.models.UserTutorConnection;
import com.parse.Parse;
import com.parse.ParseObject;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(User.class);
        ParseObject.registerSubclass(Degree.class);
        ParseObject.registerSubclass(FieldOfStudy.class);
        ParseObject.registerSubclass(Education.class);
        ParseObject.registerSubclass(Course.class);
        ParseObject.registerSubclass(UserTutorConnection.class);
        ParseObject.registerSubclass(Message.class);
        ParseObject.registerSubclass(Event.class);
        ParseObject.registerSubclass(Availability.class);



        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build());
    }
}
