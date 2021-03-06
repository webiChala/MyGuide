package com.example.myguide.Utils;

import com.example.myguide.interfaces.CourseInterface;
import com.example.myguide.models.Course;
import com.example.myguide.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

public class CourseUtils {

    public CourseInterface delegate = null;

    public CourseUtils(CourseInterface asyncResponse) {
        delegate = asyncResponse;
    }

    public void getAllCourses(ParseQuery<Course> courseParseQuery) {
        ParseQuery<Course> query = ParseQuery.getQuery(Course.class);
        if (courseParseQuery == null) {
            query.include(Course.KEY_TITLE);
            query.orderByAscending(Course.KEY_TITLE);
        } else {
            query = courseParseQuery;
        }
        query.findInBackground(new FindCallback<Course>() {
            @Override
            public void done(List<Course> courses, ParseException e) {
                if (e != null) {
                    return;
                }
                delegate.processFinish(courses);
            }
        });
    }

    public void getUserCourses(User user) {
        ParseQuery<Course> query = ParseQuery.getQuery(Course.class);
        query.include(Course.KEY_TITLE);
        query.orderByAscending(Course.KEY_TITLE);
        query.whereContainedIn(Course.KEY_OBJECT_ID, user.getCourses());
        query.findInBackground(new FindCallback<Course>() {
            @Override
            public void done(List<Course> courses, ParseException e) {
                if (e != null) {
                    return;
                }
                delegate.processFinish(courses);
            }
        });

    }
}