package com.example.myguide.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myguide.R;
import com.example.myguide.Utils.SnackBarUtil;
import com.example.myguide.databinding.ActivityAddEducationBinding;
import com.example.myguide.databinding.ActivityChooseroleBinding;
import com.example.myguide.models.Degree;
import com.example.myguide.models.Education;
import com.example.myguide.models.FieldOfStudy;
import com.example.myguide.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class AddEducationActivity extends AppCompatActivity {

    ActivityAddEducationBinding addEducationBinding;
    ArrayList<String> degreeTitleArrayList;
    ArrayList<String> fieldOfStudyTitleArrayList;
    ArrayList<Degree> degrees;
    ArrayList<FieldOfStudy> fieldOfStudy;
    Dialog dialog;
    public static final String TAG = "AddEducationActivity";
    Education toBeEditedEducation;
    Integer adapterPosition = 1;
    SnackBarUtil snackBarUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addEducationBinding = ActivityAddEducationBinding.inflate(getLayoutInflater());
        View view = addEducationBinding.getRoot();
        setContentView(view);

        degreeTitleArrayList =new ArrayList<>();
        fieldOfStudyTitleArrayList = new ArrayList<>();

        degrees =new ArrayList<>();
        fieldOfStudy = new ArrayList<>();

        if (getIntent().hasExtra("editEducation")) {
            toBeEditedEducation = getIntent().getParcelableExtra("editEducation");
            adapterPosition = getIntent().getIntExtra("adapterPosition", 0);
            addEducationBinding.etFieldOfStudy.setText(toBeEditedEducation.getFieldofStudy());
            addEducationBinding.etDegree.setText(toBeEditedEducation.getDegree());
            addEducationBinding.etSchool.setText(toBeEditedEducation.getSchool());
        }
        snackBarUtil = new SnackBarUtil(this, addEducationBinding.AddEducationActivity);


        addEducationBinding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addEducationBinding.etSchool.getText().toString().length() == 0) {
                    snackBarUtil.setSnackBar("Please insert a school");
                    return;
                }
                if (addEducationBinding.etDegree.getText().toString().length() == 0) {
                    snackBarUtil.setSnackBar("Please insert a degree");
                    return;

                }
                if (addEducationBinding.etFieldOfStudy.getText().length() == 0) {
                    snackBarUtil.setSnackBar("Please insert field of study");
                    return;

                }
                String school = addEducationBinding.etSchool.getText().toString();
                String degree = addEducationBinding.etDegree.getText().toString();
                String field = addEducationBinding.etFieldOfStudy.getText().toString();

                if (getIntent().hasExtra("editEducation")) {
                    toBeEditedEducation.setSchool(school);
                    toBeEditedEducation.setDegree(degree);
                    toBeEditedEducation.setFieldofstudy(field);
                    toBeEditedEducation.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                snackBarUtil.setSnackBar("Edit saved successfully");
                                Intent data = new Intent(AddEducationActivity.this, TutorSetupActivity.class);
                                data.putExtra("toBeEditedEducation", toBeEditedEducation);
                                data.putExtra("adapterPosition", adapterPosition);
                                startActivity(data);
                                finish();
                            }
                        }
                    });
                } else {
                    saveEducation(school, degree, field);

                }
            }
        });

        addEducationBinding.tvBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getAllFieldOfStudies();
        getAllDegrees();

    }

    private void saveEducation(String school, String degree, String field) {
        Education education = new Education();
        User currentUser = (User) ParseUser.getCurrentUser();

        education.setSchool(school);
        education.setDegree(degree);
        education.setOwner(currentUser);
        education.setFieldofstudy(field);
        education.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                } else {
                    snackBarUtil.setSnackBar("Successfully saved education!");
                    Intent data = new Intent();
                    data.putExtra("newEducation", education);
                    setResult(RESULT_OK, data); // set result code and bundle data for response
                    finish();
                }
            }
        });
    }

    private void getAllFieldOfStudies() {

        ParseQuery<FieldOfStudy> query = ParseQuery.getQuery(FieldOfStudy.class);
        query.include(FieldOfStudy.KEY_TITLE);
        query.orderByAscending("title");
        query.findInBackground(new FindCallback<FieldOfStudy>() {
            @Override
            public void done(List<FieldOfStudy> field, ParseException e) {
                if (e != null) {
                    return;
                }
                fieldOfStudy.addAll(field);
                for (FieldOfStudy f: fieldOfStudy) {
                    fieldOfStudyTitleArrayList.add(f.getTitle().toString());
                }
                dropdownAction(addEducationBinding.ivSelectedField, false);
            }
        });

    }

    private void dropdownAction(View view, boolean isDegree) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog=new Dialog(AddEducationActivity.this);
                dialog.setContentView(R.layout.dialog_searchable_spinner);
                dialog.getWindow().setLayout(950,1200);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                EditText editText=dialog.findViewById(R.id.edit_text);
                ListView listView=dialog.findViewById(R.id.list_view);

                ArrayAdapter<String> adapter;
                if (isDegree) {
                    adapter =new ArrayAdapter<>(AddEducationActivity.this, android.R.layout.simple_list_item_1,degreeTitleArrayList);

                } else {
                    adapter=new ArrayAdapter<>(AddEducationActivity.this, android.R.layout.simple_list_item_1,fieldOfStudyTitleArrayList);

                }
                listView.setAdapter(adapter);
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        adapter.getFilter().filter(s);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
//
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        if (isDegree) {
                            addEducationBinding.etDegree.setText(adapter.getItem(position));

                        } else {
                            addEducationBinding.etFieldOfStudy.setText(adapter.getItem(position));

                        }
                        dialog.dismiss();
                    }
                });

            }
        });
    }

    private void getAllDegrees() {
        ParseQuery<Degree> query = ParseQuery.getQuery(Degree.class);
        query.include(Degree.KEY_TITLE);
        query.orderByAscending("Title");
        query.findInBackground(new FindCallback<Degree>() {
            @Override
            public void done(List<Degree> degree, ParseException e) {
                if (e != null) {
                    return;
                }
                degrees = (ArrayList<Degree>) degree;
                for (Degree d: degrees) {
                    degreeTitleArrayList.add(d.getTitle().toString());

                }
                dropdownAction(addEducationBinding.ivSelectedDegree, true);
            }
        });
    }
}





