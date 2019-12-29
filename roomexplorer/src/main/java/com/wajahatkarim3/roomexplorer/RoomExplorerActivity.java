package com.wajahatkarim3.roomexplorer;

import java.util.ArrayList;
import java.util.LinkedList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class RoomExplorerActivity extends Activity implements OnItemClickListener {

    protected Class<? extends RoomDatabase> myClass;
    protected String databaseName;

    public static final String DATABASE_CLASS_KEY = "dbClassName";
    public static final String DATABASE_NAME_KEY = "dbName";

    static class IndexInfo
    {
        public static int index = 10;
        public static int numberofpages = 0;
        public static int currentpage = 0;
        public static String table_name="";
        public static Cursor maincursor;
        public static int cursorpostion=0;
        public static ArrayList<String> value_string;
        public static ArrayList<String> tableheadernames;
        public static ArrayList<String> emptytablecolumnnames;
        public static boolean isEmpty;
        public static boolean isCustomQuery;
    }

    TableLayout tableLayout;
    TableRow.LayoutParams tableRowParams;
    HorizontalScrollView hsv;
    ScrollView mainscrollview;
    LinearLayout mainLayout;
    TextView tvmessage;
    Button previous;
    Button next;
    Spinner select_table;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(DATABASE_CLASS_KEY))
        {
            myClass = (Class<? extends RoomDatabase>) getIntent().getExtras().get(DATABASE_CLASS_KEY);
            databaseName = (String) getIntent().getExtras().get(DATABASE_NAME_KEY);
        }
        else
        {
            throw new RuntimeException("No database class name passed in the launch Intent.");
        }

        mainscrollview = new ScrollView(RoomExplorerActivity.this);

        //the main linear layout to which all tables spinners etc will be added.In this activity every element is created dynamically  to avoid using xml file
        mainLayout = new LinearLayout(RoomExplorerActivity.this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.WHITE);
        mainLayout.setScrollContainer(true);
        mainscrollview.addView(mainLayout);

        //all required layouts are created dynamically and added to the main scrollview
        setContentView(mainscrollview);

        //the first row of layout which has a text view and spinner
        final LinearLayout firstrow = new LinearLayout(RoomExplorerActivity.this);
        firstrow.setPadding(0,10,0,20);
        LinearLayout.LayoutParams firstrowlp = new LinearLayout.LayoutParams(0, 150);
        firstrowlp.weight = 1;

        TextView maintext = new TextView(RoomExplorerActivity.this);
        maintext.setText("Select Table");
        maintext.setTextSize(22);
        maintext.setLayoutParams(firstrowlp);
        select_table=new Spinner(RoomExplorerActivity.this);
        select_table.setLayoutParams(firstrowlp);

        firstrow.addView(maintext);
        firstrow.addView(select_table);
        mainLayout.addView(firstrow);

        ArrayList<Cursor> alc ;

        //the horizontal scroll view for table if the table content doesnot fit into screen
        hsv = new HorizontalScrollView(RoomExplorerActivity.this);

        //the main table layout where the content of the sql tables will be displayed when user selects a table
        tableLayout = new TableLayout(RoomExplorerActivity.this);
        tableLayout.setHorizontalScrollBarEnabled(true);
        hsv.addView(tableLayout);

        //the second row of the layout which shows number of records in the table selected by user
        final LinearLayout secondrow = new LinearLayout(RoomExplorerActivity.this);
        secondrow.setPadding(0,20,0,10);
        LinearLayout.LayoutParams secondrowlp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        secondrowlp.weight = 1;
        TextView secondrowtext = new TextView(RoomExplorerActivity.this);
        secondrowtext.setText("No. Of Records : ");
        secondrowtext.setTextSize(20);
        secondrowtext.setLayoutParams(secondrowlp);
        tv =new TextView(RoomExplorerActivity.this);
        tv.setTextSize(20);
        tv.setLayoutParams(secondrowlp);
        secondrow.addView(secondrowtext);
        secondrow.addView(tv);
        mainLayout.addView(secondrow);
        //A button which generates a text view from which user can write custome queries
        final EditText customquerytext = new EditText(this);
        customquerytext.setVisibility(View.GONE);
        customquerytext.setHint("Enter Your Query here and Click on Submit Query Button .Results will be displayed below");
        mainLayout.addView(customquerytext);

        final Button submitQuery = new Button(RoomExplorerActivity.this);
        submitQuery.setVisibility(View.GONE);
        submitQuery.setText("Submit Query");

        submitQuery.setBackgroundColor(Color.parseColor("#BAE7F6"));
        mainLayout.addView(submitQuery);

        final TextView help = new TextView(RoomExplorerActivity.this);
        help.setText("Click on the row below to update values or delete the tuple");
        help.setPadding(0,5,0,5);

        // the spinner which gives user a option to add new row , drop or delete table
        final Spinner spinnertable =new Spinner(RoomExplorerActivity.this);
        mainLayout.addView(spinnertable);
        mainLayout.addView(help);
        hsv.setPadding(0,10,0,10);
        hsv.setScrollbarFadingEnabled(false);
        hsv.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
        mainLayout.addView(hsv);
        //the third layout which has buttons for the pagination of content from database
        final LinearLayout thirdrow = new LinearLayout(RoomExplorerActivity.this);
        previous = new Button(RoomExplorerActivity.this);
        previous.setText("Previous");

        previous.setBackgroundColor(Color.parseColor("#BAE7F6"));
        previous.setLayoutParams(secondrowlp);
        next = new Button(RoomExplorerActivity.this);
        next.setText("Next");
        next.setBackgroundColor(Color.parseColor("#BAE7F6"));
        next.setLayoutParams(secondrowlp);
        TextView tvblank = new TextView(this);
        tvblank.setLayoutParams(secondrowlp);
        thirdrow.setPadding(0,10,0,10);
        thirdrow.addView(previous);
        thirdrow.addView(tvblank);
        thirdrow.addView(next);
        mainLayout.addView(thirdrow);

        //the text view at the bottom of the screen which displays error or success messages after a query is executed
        tvmessage =new TextView(RoomExplorerActivity.this);

        tvmessage.setText("Error Messages will be displayed here");
        String Query = "SELECT name _id FROM sqlite_master WHERE type ='table'";
        tvmessage.setTextSize(18);
        mainLayout.addView(tvmessage);

        final Button customQuery = new Button(RoomExplorerActivity.this);
        customQuery.setText("Custom Query");
        customQuery.setBackgroundColor(Color.parseColor("#BAE7F6"));
        mainLayout.addView(customQuery);
        customQuery.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //set drop down to custom Query
                IndexInfo.isCustomQuery=true;
                secondrow.setVisibility(View.GONE);
                spinnertable.setVisibility(View.GONE);
                help.setVisibility(View.GONE);
                customquerytext.setVisibility(View.VISIBLE);
                submitQuery.setVisibility(View.VISIBLE);
                select_table.setSelection(0);
                customQuery.setVisibility(View.GONE);
            }
        });


        //when user enter a custom query in text view and clicks on submit query button
        //display results in tablelayout
        submitQuery.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                tableLayout.removeAllViews();
                customQuery.setVisibility(View.GONE);

                ArrayList<Cursor> alc2;
                String Query10=customquerytext.getText().toString();
                //pass the query to getdata method and get results
                alc2 = getData(Query10);
                final Cursor c4=alc2.get(0);
                Cursor Message2 =alc2.get(1);
                Message2.moveToLast();

                //if the query returns results display the results in table layout
                if(Message2.getString(0).equalsIgnoreCase("Success"))
                {

                    tvmessage.setBackgroundColor(Color.parseColor("#2ecc71"));
                    if(c4!=null){
                        tvmessage.setText("Queru Executed successfully.Number of rows returned :"+c4.getCount());
                        if(c4.getCount()>0)
                        {
                            IndexInfo.maincursor=c4;
                            refreshTable(1);
                        }
                    }else{
                        tvmessage.setText("Queru Executed successfully");
                        refreshTable(1);
                    }

                }
                else
                {
                    //if there is any error we displayed the error message at the bottom of the screen
                    tvmessage.setBackgroundColor(Color.parseColor("#e74c3c"));
                    tvmessage.setText("Error:"+Message2.getString(0));

                }
            }
        });
        //layout parameters for each row in the table
        tableRowParams = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tableRowParams.setMargins(0, 0, 2, 0);

        // a query which returns a cursor with the list of tables in the database.We use this cursor to populate spinner in the first row
        alc = getData(Query);

        //the first cursor has reults of the query
        final Cursor c=alc.get(0);

        //the second cursor has error messages
        Cursor Message =alc.get(1);

        Message.moveToLast();
        String msg = Message.getString(0);

        ArrayList<String> tablenames = new ArrayList<String>();

        if(c!=null)
        {

            c.moveToFirst();
            tablenames.add("click here");
            do{
                //add names of the table to tablenames array list
                tablenames.add(c.getString(0));
            }while(c.moveToNext());
        }
        //an array adapter with above created arraylist
        ArrayAdapter<String> tablenamesadapter = new ArrayAdapter<String>(RoomExplorerActivity.this,
                android.R.layout.simple_spinner_item, tablenames) {

            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                v.setBackgroundColor(Color.WHITE);
                TextView adap =(TextView)v;
                adap.setTextSize(20);

                return adap;
            }


            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v =super.getDropDownView(position, convertView, parent);

                v.setBackgroundColor(Color.WHITE);

                return v;
            }
        };

        tablenamesadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if(tablenamesadapter!=null)
        {
            //set the adpater to select_table spinner
            select_table.setAdapter(tablenamesadapter);
        }

        // when a table names is selecte display the table contents
        select_table.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int pos, long id) {
                if(pos==0&&!IndexInfo.isCustomQuery)
                {
                    secondrow.setVisibility(View.GONE);
                    hsv.setVisibility(View.GONE);
                    thirdrow.setVisibility(View.GONE);
                    spinnertable.setVisibility(View.GONE);
                    help.setVisibility(View.GONE);
                    tvmessage.setVisibility(View.GONE);
                    customquerytext.setVisibility(View.GONE);
                    submitQuery.setVisibility(View.GONE);
                    customQuery.setVisibility(View.GONE);
                }
                if(pos!=0){
                    secondrow.setVisibility(View.VISIBLE);
                    spinnertable.setVisibility(View.VISIBLE);
                    help.setVisibility(View.VISIBLE);
                    customquerytext.setVisibility(View.GONE);
                    submitQuery.setVisibility(View.GONE);
                    customQuery.setVisibility(View.VISIBLE);
                    hsv.setVisibility(View.VISIBLE);

                    tvmessage.setVisibility(View.VISIBLE);

                    thirdrow.setVisibility(View.VISIBLE);
                    c.moveToPosition(pos-1);
                    IndexInfo.cursorpostion=pos-1;
                    //displaying the content of the table which is selected in the select_table spinner
                    IndexInfo.table_name=c.getString(0);
                    tvmessage.setText("Error Messages will be displayed here");
                    tvmessage.setBackgroundColor(Color.WHITE);

                    //removes any data if present in the table layout
                    tableLayout.removeAllViews();
                    ArrayList<String> spinnertablevalues = new ArrayList<String>();
                    spinnertablevalues.add("Click here to change this table");
                    spinnertablevalues.add("Add row to this table");
                    spinnertablevalues.add("Delete this table");
                    spinnertablevalues.add("Drop this table");
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, spinnertablevalues);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

                    // a array adapter which add values to the spinner which helps in user making changes to the table
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(RoomExplorerActivity.this,
                            android.R.layout.simple_spinner_item, spinnertablevalues) {

                        public View getView(int position, View convertView, ViewGroup parent) {
                            View v = super.getView(position, convertView, parent);

                            v.setBackgroundColor(Color.WHITE);
                            TextView adap =(TextView)v;
                            adap.setTextSize(20);

                            return adap;
                        }

                        public View getDropDownView(int position, View convertView, ViewGroup parent) {
                            View v =super.getDropDownView(position, convertView, parent);

                            v.setBackgroundColor(Color.WHITE);

                            return v;
                        }
                    };

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnertable.setAdapter(adapter);
                    String Query2 ="select * from "+c.getString(0);

                    //getting contents of the table which user selected from the select_table spinner
                    ArrayList<Cursor> alc2=getData(Query2);
                    final Cursor c2=alc2.get(0);
                    //saving cursor to the static IndexInfo class which can be resued by the other functions
                    IndexInfo.maincursor=c2;

                    // if the cursor returned form the database is not null we display the data in table layout
                    if(c2!=null)
                    {
                        int counts = c2.getCount();
                        IndexInfo.isEmpty=false;
                        tv.setText(""+counts);


                        //the spinnertable has the 3 items to drop , delete , add row to the table selected by the user
                        //here we handle the 3 operations.
                        spinnertable.setOnItemSelectedListener((new AdapterView.OnItemSelectedListener() {
                            @SuppressLint("ResourceType")
                            @Override
                            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {


                                ((TextView)parentView.getChildAt(0)).setTextColor(Color.rgb(0,0,0));
                                //when user selects to drop the table the below code in if block will be executed
                                if(spinnertable.getSelectedItem().toString().equals("Drop this table"))
                                {
                                    // an alert dialog to confirm user selection
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(!isFinishing()){

                                                new AlertDialog.Builder(RoomExplorerActivity.this)
                                                        .setTitle("Are you sure ?")
                                                        .setMessage("Pressing yes will remove "+ IndexInfo.table_name+" table from database")
                                                        .setPositiveButton("yes",
                                                                new DialogInterface.OnClickListener() {
                                                                    // when user confirms by clicking on yes we drop the table by executing drop table query
                                                                    public void onClick(DialogInterface dialog, int which) {

                                                                        String Query6 = "Drop table "+ IndexInfo.table_name;
                                                                        ArrayList<Cursor> aldropt=getData(Query6);
                                                                        Cursor tempc=aldropt.get(1);
                                                                        tempc.moveToLast();
                                                                        if(tempc.getString(0).equalsIgnoreCase("Success"))
                                                                        {
                                                                            tvmessage.setBackgroundColor(Color.parseColor("#2ecc71"));
                                                                            tvmessage.setText(IndexInfo.table_name+"Dropped successfully");
                                                                            refreshactivity();
                                                                        }
                                                                        else
                                                                        {
                                                                            //if there is any error we displayd the error message at the bottom of the screen
                                                                            tvmessage.setBackgroundColor(Color.parseColor("#e74c3c"));
                                                                            tvmessage.setText("Error:"+tempc.getString(0));
                                                                            spinnertable.setSelection(0);
                                                                        }
                                                                    }})
                                                        .setNegativeButton("No",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        spinnertable.setSelection(0);
                                                                    }
                                                                })
                                                        .create().show();
                                            }
                                        }
                                    });

                                }
                                //when user selects to drop the table the below code in if block will be executed
                                if(spinnertable.getSelectedItem().toString().equals("Delete this table"))
                                {	// an alert dialog to confirm user selection
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(!isFinishing()){

                                                new AlertDialog.Builder(RoomExplorerActivity.this)
                                                        .setTitle("Are you sure?")
                                                        .setMessage("Clicking on yes will delete all the contents of "+ IndexInfo.table_name+" table from database")
                                                        .setPositiveButton("yes",
                                                                new DialogInterface.OnClickListener() {

                                                                    // when user confirms by clicking on yes we drop the table by executing delete table query
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        String Query7 = "Delete  from "+ IndexInfo.table_name;
                                                                        ArrayList<Cursor> aldeletet=getData(Query7);
                                                                        Cursor tempc=aldeletet.get(1);
                                                                        tempc.moveToLast();
                                                                        if(tempc.getString(0).equalsIgnoreCase("Success"))
                                                                        {
                                                                            tvmessage.setBackgroundColor(Color.parseColor("#2ecc71"));
                                                                            tvmessage.setText(IndexInfo.table_name+" table content deleted successfully");
                                                                            IndexInfo.isEmpty=true;
                                                                            refreshTable(0);
                                                                        }
                                                                        else
                                                                        {
                                                                            tvmessage.setBackgroundColor(Color.parseColor("#e74c3c"));
                                                                            tvmessage.setText("Error:"+tempc.getString(0));
                                                                            spinnertable.setSelection(0);
                                                                        }
                                                                    }})
                                                        .setNegativeButton("No",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        spinnertable.setSelection(0);
                                                                    }
                                                                })
                                                        .create().show();
                                            }
                                        }
                                    });

                                }

                                //when user selects to add row to the table the below code in if block will be executed
                                if(spinnertable.getSelectedItem().toString().equals("Add row to this table"))
                                {
                                    //we create a layout which has textviews with column names of the table and edittexts where
                                    //user can enter value which will be inserted into the datbase.
                                    final LinkedList<TextView> addnewrownames = new LinkedList<TextView>();
                                    final LinkedList<EditText> addnewrowvalues = new LinkedList<EditText>();
                                    final ScrollView addrowsv =new ScrollView(RoomExplorerActivity.this);
                                    Cursor c4 = IndexInfo.maincursor;
                                    if(IndexInfo.isEmpty)
                                    {
                                        getcolumnnames();
                                        for(int i = 0; i< IndexInfo.emptytablecolumnnames.size(); i++)
                                        {
                                            String cname = IndexInfo.emptytablecolumnnames.get(i);
                                            TextView tv = new TextView(getApplicationContext());
                                            tv.setText(cname);
                                            addnewrownames.add(tv);

                                        }
                                        for(int i=0;i<addnewrownames.size();i++)
                                        {
                                            EditText et = new EditText(getApplicationContext());

                                            addnewrowvalues.add(et);
                                        }

                                    }
                                    else{
                                        for(int i=0;i<c4.getColumnCount();i++)
                                        {
                                            String cname = c4.getColumnName(i);
                                            TextView tv = new TextView(getApplicationContext());
                                            tv.setText(cname);
                                            addnewrownames.add(tv);

                                        }
                                        for(int i=0;i<addnewrownames.size();i++)
                                        {
                                            EditText et = new EditText(getApplicationContext());

                                            addnewrowvalues.add(et);
                                        }
                                    }
                                    final RelativeLayout addnewlayout = new RelativeLayout(RoomExplorerActivity.this);
                                    RelativeLayout.LayoutParams addnewparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                    addnewparams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                                    for(int i=0;i<addnewrownames.size();i++)
                                    {
                                        TextView tv =addnewrownames.get(i);
                                        EditText et=addnewrowvalues.get(i);
                                        int t = i+400;
                                        int k = i+500;
                                        int lid = i+600;

                                        tv.setId(t);
                                        tv.setTextColor(Color.parseColor("#000000"));
                                        et.setBackgroundColor(Color.parseColor("#F2F2F2"));
                                        et.setTextColor(Color.parseColor("#000000"));
                                        et.setId(k);
                                        final LinearLayout ll = new LinearLayout(RoomExplorerActivity.this);
                                        LinearLayout.LayoutParams tvl = new LinearLayout.LayoutParams(0, 100);
                                        tvl.weight = 1;
                                        ll.addView(tv,tvl);
                                        ll.addView(et,tvl);
                                        ll.setId(lid);

                                        RelativeLayout.LayoutParams rll = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                        rll.addRule(RelativeLayout.BELOW,ll.getId()-1 );
                                        rll.setMargins(0, 20, 0, 0);
                                        addnewlayout.addView(ll, rll);

                                    }
                                    addnewlayout.setBackgroundColor(Color.WHITE);
                                    addrowsv.addView(addnewlayout);
                                    //the above form layout which we have created above will be displayed in an alert dialog
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(!isFinishing()){
                                                new AlertDialog.Builder(RoomExplorerActivity.this)
                                                        .setTitle("values")
                                                        .setCancelable(false)
                                                        .setView(addrowsv)
                                                        .setPositiveButton("Add",
                                                                new DialogInterface.OnClickListener() {
                                                                    // after entering values if user clicks on add we take the values and run a insert query
                                                                    public void onClick(DialogInterface dialog, int which) {

                                                                        IndexInfo.index = 10;
                                                                        //tableLayout.removeAllViews();
                                                                        //trigger select table listener to be triggerd
                                                                        String Query4 ="Insert into "+ IndexInfo.table_name+" (";
                                                                        for(int i=0 ; i<addnewrownames.size();i++)
                                                                        {

                                                                            TextView tv = addnewrownames.get(i);
                                                                            tv.getText().toString();
                                                                            if(i==addnewrownames.size()-1)
                                                                            {

                                                                                Query4=Query4+tv.getText().toString();

                                                                            }
                                                                            else
                                                                            {
                                                                                Query4=Query4+tv.getText().toString()+", ";
                                                                            }
                                                                        }
                                                                        Query4=Query4+" ) VALUES ( ";
                                                                        for(int i=0 ; i<addnewrownames.size();i++)
                                                                        {
                                                                            EditText et = addnewrowvalues.get(i);
                                                                            et.getText().toString();

                                                                            if(i==addnewrownames.size()-1)
                                                                            {

                                                                                Query4=Query4+"'"+et.getText().toString()+"' ) ";
                                                                            }
                                                                            else
                                                                            {
                                                                                Query4=Query4+"'"+et.getText().toString()+"' , ";
                                                                            }


                                                                        }
                                                                        //this is the insert query which has been generated
                                                                        ArrayList<Cursor> altc=getData(Query4);
                                                                        Cursor tempc=altc.get(1);
                                                                        tempc.moveToLast();
                                                                        if(tempc.getString(0).equalsIgnoreCase("Success"))
                                                                        {
                                                                            tvmessage.setBackgroundColor(Color.parseColor("#2ecc71"));
                                                                            tvmessage.setText("New Row added succesfully to "+ IndexInfo.table_name);
                                                                            refreshTable(0);
                                                                        }
                                                                        else
                                                                        {
                                                                            tvmessage.setBackgroundColor(Color.parseColor("#e74c3c"));
                                                                            tvmessage.setText("Error:"+tempc.getString(0));
                                                                            spinnertable.setSelection(0);
                                                                        }

                                                                    }
                                                                })
                                                        .setNegativeButton("close",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        spinnertable.setSelection(0);
                                                                    }
                                                                })
                                                        .create().show();
                                            }
                                        }
                                    });
                                }
                            }
                            public void onNothingSelected(AdapterView<?> arg0) { }
                        }));

                        //display the first row of the table with column names of the table selected by the user
                        TableRow tableheader = new TableRow(getApplicationContext());

                        tableheader.setBackgroundColor(Color.BLACK);
                        tableheader.setPadding(0, 2, 0, 2);
                        for(int k=0;k<c2.getColumnCount();k++)
                        {
                            LinearLayout cell = new LinearLayout(RoomExplorerActivity.this);
                            cell.setBackgroundColor(Color.WHITE);
                            cell.setLayoutParams(tableRowParams);
                            final TextView tableheadercolums = new TextView(getApplicationContext());
                            // tableheadercolums.setBackgroundDrawable(gd);
                            tableheadercolums.setPadding(0, 0, 4, 3);
                            tableheadercolums.setText(""+c2.getColumnName(k));
                            tableheadercolums.setTextColor(Color.parseColor("#000000"));

                            //columsView.setLayoutParams(tableRowParams);
                            cell.addView(tableheadercolums);
                            tableheader.addView(cell);

                        }
                        tableLayout.addView(tableheader);
                        c2.moveToFirst();

                        //after displaying columnnames in the first row  we display data in the remaining columns
                        //the below paginatetbale function will display the first 10 tuples of the tables
                        //the remaining tuples can be viewed by clicking on the next button
                        paginatetable(c2.getCount());

                    }
                    else{
                        //if the cursor returned from the database is empty we show that table is empty
                        help.setVisibility(View.GONE);
                        tableLayout.removeAllViews();
                        getcolumnnames();
                        TableRow tableheader2 = new TableRow(getApplicationContext());
                        tableheader2.setBackgroundColor(Color.BLACK);
                        tableheader2.setPadding(0, 2, 0, 2);

                        LinearLayout cell = new LinearLayout(RoomExplorerActivity.this);
                        cell.setBackgroundColor(Color.WHITE);
                        cell.setLayoutParams(tableRowParams);
                        final TextView tableheadercolums = new TextView(getApplicationContext());

                        tableheadercolums.setPadding(0, 0, 4, 3);
                        tableheadercolums.setText("   Table   Is   Empty   ");
                        tableheadercolums.setTextSize(30);
                        tableheadercolums.setTextColor(Color.RED);

                        cell.addView(tableheadercolums);
                        tableheader2.addView(cell);


                        tableLayout.addView(tableheader2);

                        tv.setText(""+0);
                    }
                }}
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    //get columnnames of the empty tables and save them in a array list
    public void getcolumnnames()
    {
        ArrayList<Cursor> alc3=getData("PRAGMA table_info("+ IndexInfo.table_name+")");
        Cursor c5=alc3.get(0);
        IndexInfo.isEmpty=true;
        if(c5!=null)
        {
            IndexInfo.isEmpty=true;

            ArrayList<String> emptytablecolumnnames= new ArrayList<String>();
            c5.moveToFirst();
            do
            {
                emptytablecolumnnames.add(c5.getString(1));
            }while(c5.moveToNext());
            IndexInfo.emptytablecolumnnames=emptytablecolumnnames;
        }



    }
    //displays alert dialog from which use can update or delete a row
    @SuppressLint("ResourceType")
    public void updateDeletePopup(int row)
    {
        Cursor c2= IndexInfo.maincursor;
        // a spinner which gives options to update or delete the row which user has selected
        ArrayList<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add("Click Here to Change this row");
        spinnerArray.add("Update this row");
        spinnerArray.add("Delete this row");

        //create a layout with text values which has the column names and
        //edit texts which has the values of the row which user has selected
        final ArrayList<String> value_string = IndexInfo.value_string;
        final LinkedList<TextView> columnames = new LinkedList<TextView>();
        final LinkedList<EditText> columvalues = new LinkedList<EditText>();

        for(int i=0;i<c2.getColumnCount();i++)
        {
            String cname = c2.getColumnName(i);
            TextView tv = new TextView(getApplicationContext());
            tv.setText(cname);
            columnames.add(tv);

        }
        for(int i=0;i<columnames.size();i++)
        {
            String cv =value_string.get(i);
            EditText et = new EditText(getApplicationContext());
            value_string.add(cv);
            et.setText(cv);
            columvalues.add(et);
        }

        int lastrid = 0;
        // all text views , edit texts are added to this relative layout lp
        final RelativeLayout lp = new RelativeLayout(RoomExplorerActivity.this);
        lp.setBackgroundColor(Color.WHITE);
        RelativeLayout.LayoutParams lay = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lay.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        final ScrollView updaterowsv =new ScrollView(RoomExplorerActivity.this);
        LinearLayout lcrud = new LinearLayout(RoomExplorerActivity.this);

        LinearLayout.LayoutParams paramcrudtext = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        paramcrudtext.setMargins(0, 20, 0, 0);

        //spinner which displays update , delete options
        final Spinner crud_dropdown = new Spinner(getApplicationContext());

        ArrayAdapter<String> crudadapter = new ArrayAdapter<String>(RoomExplorerActivity.this,
                android.R.layout.simple_spinner_item, spinnerArray) {

            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                v.setBackgroundColor(Color.WHITE);
                TextView adap =(TextView)v;
                adap.setTextSize(20);

                return adap;
            }


            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v =super.getDropDownView(position, convertView, parent);

                v.setBackgroundColor(Color.WHITE);

                return v;
            }
        };


        crudadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        crud_dropdown.setAdapter(crudadapter);
        lcrud.setId(299);
        lcrud.addView(crud_dropdown,paramcrudtext);

        RelativeLayout.LayoutParams rlcrudparam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlcrudparam.addRule(RelativeLayout.BELOW,lastrid);

        lp.addView(lcrud, rlcrudparam);
        for(int i=0;i<columnames.size();i++)
        {
            TextView tv =columnames.get(i);
            EditText et=columvalues.get(i);
            int t = i+100;
            int k = i+200;
            int lid = i+300;

            tv.setId(t);
            tv.setTextColor(Color.parseColor("#000000"));
            et.setBackgroundColor(Color.parseColor("#F2F2F2"));

            et.setTextColor(Color.parseColor("#000000"));
            et.setId(k);
            final LinearLayout ll = new LinearLayout(RoomExplorerActivity.this);
            ll.setBackgroundColor(Color.parseColor("#FFFFFF"));
            ll.setId(lid);
            LinearLayout.LayoutParams lpp = new LinearLayout.LayoutParams(0, 100);
            lpp.weight = 1;
            tv.setLayoutParams(lpp);
            et.setLayoutParams(lpp);
            ll.addView(tv);
            ll.addView(et);

            RelativeLayout.LayoutParams rll = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            rll.addRule(RelativeLayout.BELOW,ll.getId()-1 );
            rll.setMargins(0, 20, 0, 0);
            lastrid=ll.getId();
            lp.addView(ll, rll);

        }

        updaterowsv.addView(lp);
        //after the layout has been created display it in a alert dialog
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!isFinishing()){
                    new AlertDialog.Builder(RoomExplorerActivity.this)
                            .setTitle("values")
                            .setView(updaterowsv)
                            .setCancelable(false)
                            .setPositiveButton("Ok",
                                    new DialogInterface.OnClickListener() {

                                        //this code will be executed when user changes values of edit text or spinner and clicks on ok button
                                        public void onClick(DialogInterface dialog, int which) {

                                            //get spinner value
                                            String spinner_value = crud_dropdown.getSelectedItem().toString();

                                            //it he spinner value is update this row get the values from
                                            //edit text fields generate a update query and execute it
                                            if(spinner_value.equalsIgnoreCase("Update this row"))
                                            {
                                                IndexInfo.index = 10;
                                                String Query3="UPDATE "+ IndexInfo.table_name+" SET ";

                                                for(int i=0;i<columnames.size();i++)
                                                {
                                                    TextView tvc = columnames.get(i);
                                                    EditText etc = columvalues.get(i);

                                                    if(!etc.getText().toString().equals("null"))
                                                    {

                                                        Query3=Query3+tvc.getText().toString()+" = ";

                                                        if(i==columnames.size()-1)
                                                        {

                                                            Query3=Query3+"'"+etc.getText().toString()+"'";

                                                        }
                                                        else{

                                                            Query3=Query3+"'"+etc.getText().toString()+"' , ";

                                                        }
                                                    }

                                                }
                                                Query3=Query3+" where ";
                                                for(int i=0;i<columnames.size();i++)
                                                {
                                                    TextView tvc = columnames.get(i);
                                                    if(!value_string.get(i).equals("null"))
                                                    {

                                                        Query3=Query3+tvc.getText().toString()+" = ";

                                                        if(i==columnames.size()-1)
                                                        {

                                                            Query3=Query3+"'"+value_string.get(i)+"' ";

                                                        }
                                                        else
                                                        {
                                                            Query3=Query3+"'"+value_string.get(i)+"' and ";
                                                        }

                                                    }
                                                }
                                                //dbm.getData(Query3);
                                                ArrayList<Cursor> aluc=getData(Query3);
                                                Cursor tempc=aluc.get(1);
                                                tempc.moveToLast();

                                                if(tempc.getString(0).equalsIgnoreCase("Success"))
                                                {
                                                    tvmessage.setBackgroundColor(Color.parseColor("#2ecc71"));
                                                    tvmessage.setText(IndexInfo.table_name+" table Updated Successfully");
                                                    refreshTable(0);
                                                }
                                                else
                                                {
                                                    tvmessage.setBackgroundColor(Color.parseColor("#e74c3c"));
                                                    tvmessage.setText("Error:"+tempc.getString(0));
                                                }
                                            }
                                            //it he spinner value is delete this row get the values from
                                            //edit text fields generate a delete query and execute it

                                            if(spinner_value.equalsIgnoreCase("Delete this row"))
                                            {

                                                IndexInfo.index = 10;
                                                String Query5="DELETE FROM "+ IndexInfo.table_name+" WHERE ";

                                                for(int i=0;i<columnames.size();i++)
                                                {
                                                    TextView tvc = columnames.get(i);
                                                    if(!value_string.get(i).equals("null"))
                                                    {

                                                        Query5=Query5+tvc.getText().toString()+" = ";

                                                        if(i==columnames.size()-1)
                                                        {

                                                            Query5=Query5+"'"+value_string.get(i)+"' ";

                                                        }
                                                        else
                                                        {
                                                            Query5=Query5+"'"+value_string.get(i)+"' and ";
                                                        }

                                                    }
                                                }

                                                getData(Query5);

                                                ArrayList<Cursor> aldc=getData(Query5);
                                                Cursor tempc=aldc.get(1);
                                                tempc.moveToLast();

                                                if(tempc.getString(0).equalsIgnoreCase("Success"))
                                                {
                                                    tvmessage.setBackgroundColor(Color.parseColor("#2ecc71"));
                                                    tvmessage.setText("Row deleted from "+ IndexInfo.table_name+" table");
                                                    refreshTable(0);
                                                }
                                                else
                                                {
                                                    tvmessage.setBackgroundColor(Color.parseColor("#e74c3c"));
                                                    tvmessage.setText("Error:"+tempc.getString(0));
                                                }
                                            }
                                        }

                                    })
                            .setNegativeButton("close",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                            .create().show();
                }
            }
        });
    }

    public void refreshactivity()
    {

        finish();
        startActivity(getIntent());
    }

    public void refreshTable(int d )
    {
        Cursor c3=null;
        tableLayout.removeAllViews();
        if(d==0)
        {
            String Query8 = "select * from "+ IndexInfo.table_name;
            ArrayList<Cursor> alc3=getData(Query8);
            c3=alc3.get(0);
            //saving cursor to the static IndexInfo class which can be resued by the other functions
            IndexInfo.maincursor=c3;
        }
        if(d==1)
        {
            c3= IndexInfo.maincursor;
        }
        // if the cursor returened form tha database is not null we display the data in table layout
        if(c3!=null)
        {
            int counts = c3.getCount();

            tv.setText(""+counts);
            TableRow tableheader = new TableRow(getApplicationContext());

            tableheader.setBackgroundColor(Color.BLACK);
            tableheader.setPadding(0, 2, 0, 2);
            for(int k=0;k<c3.getColumnCount();k++)
            {
                LinearLayout cell = new LinearLayout(RoomExplorerActivity.this);
                cell.setBackgroundColor(Color.WHITE);
                cell.setLayoutParams(tableRowParams);
                final TextView tableheadercolums = new TextView(getApplicationContext());
                tableheadercolums.setPadding(0, 0, 4, 3);
                tableheadercolums.setText(""+c3.getColumnName(k));
                tableheadercolums.setTextColor(Color.parseColor("#000000"));
                cell.addView(tableheadercolums);
                tableheader.addView(cell);

            }
            tableLayout.addView(tableheader);
            c3.moveToFirst();

            //after displaying column names in the first row  we display data in the remaining columns
            //the below paginate table function will display the first 10 tuples of the tables
            //the remaining tuples can be viewed by clicking on the next button
            paginatetable(c3.getCount());
        }
        else{

            TableRow tableheader2 = new TableRow(getApplicationContext());
            tableheader2.setBackgroundColor(Color.BLACK);
            tableheader2.setPadding(0, 2, 0, 2);

            LinearLayout cell = new LinearLayout(RoomExplorerActivity.this);
            cell.setBackgroundColor(Color.WHITE);
            cell.setLayoutParams(tableRowParams);

            final TextView tableheadercolums = new TextView(getApplicationContext());
            tableheadercolums.setPadding(0, 0, 4, 3);
            tableheadercolums.setText("   Table   Is   Empty   ");
            tableheadercolums.setTextSize(30);
            tableheadercolums.setTextColor(Color.RED);

            cell.addView(tableheadercolums);
            tableheader2.addView(cell);


            tableLayout.addView(tableheader2);

            tv.setText(""+0);
        }

    }

    //the function which displays tuples from database in a table layout
    public void paginatetable(final int number)
    {


        final Cursor c3 = IndexInfo.maincursor;
        IndexInfo.numberofpages=(c3.getCount()/10)+1;
        IndexInfo.currentpage=1;
        c3.moveToFirst();
        int currentrow=0;

        //display the first 10 tuples of the table selected by user
        do
        {

            final TableRow tableRow = new TableRow(getApplicationContext());
            tableRow.setBackgroundColor(Color.BLACK);
            tableRow.setPadding(0, 2, 0, 2);

            for(int j=0 ;j<c3.getColumnCount();j++)
            {
                LinearLayout cell = new LinearLayout(this);
                cell.setBackgroundColor(Color.WHITE);
                cell.setLayoutParams(tableRowParams);
                final TextView columsView = new TextView(getApplicationContext());
                String column_data = "";
                try{
                    column_data = c3.getString(j);
                }catch(Exception e){
                    // Column data is not a string , do not display it
                }
                columsView.setText(column_data);
                columsView.setTextColor(Color.parseColor("#000000"));
                columsView.setPadding(0, 0, 4, 3);
                cell.addView(columsView);
                tableRow.addView(cell);

            }

            tableRow.setVisibility(View.VISIBLE);
            currentrow=currentrow+1;
            //we create listener for each table row when clicked a alert dialog will be displayed
            //from where user can update or delete the row
            tableRow.setOnClickListener(new OnClickListener(){
                public void onClick(View v) {

                    final ArrayList<String> value_string = new ArrayList<String>();
                    for(int i=0;i<c3.getColumnCount();i++)
                    {
                        LinearLayout llcolumn = (LinearLayout) tableRow.getChildAt(i);
                        TextView tc =(TextView)llcolumn.getChildAt(0);

                        String cv =tc.getText().toString();
                        value_string.add(cv);

                    }
                    IndexInfo.value_string=value_string;
                    //the below function will display the alert dialog
                    updateDeletePopup(0);
                }
            });
            tableLayout.addView(tableRow);


        }while(c3.moveToNext()&&currentrow<10);

        IndexInfo.index=currentrow;


        // when user clicks on the previous button update the table with the previous 10 tuples from the database
        previous.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int tobestartindex=(IndexInfo.currentpage-2)*10;

                //if the tbale layout has the first 10 tuples then toast that this is the first page
                if(IndexInfo.currentpage==1)
                {
                    Toast.makeText(getApplicationContext(), "This is the first page", Toast.LENGTH_LONG).show();
                }
                else
                {
                    IndexInfo.currentpage= IndexInfo.currentpage-1;
                    c3.moveToPosition(tobestartindex);

                    boolean decider=true;
                    for(int i=1;i<tableLayout.getChildCount();i++)
                    {
                        TableRow tableRow = (TableRow) tableLayout.getChildAt(i);


                        if(decider)
                        {
                            tableRow.setVisibility(View.VISIBLE);
                            for(int j=0;j<tableRow.getChildCount();j++)
                            {
                                LinearLayout llcolumn = (LinearLayout) tableRow.getChildAt(j);
                                TextView columsView = (TextView) llcolumn.getChildAt(0);

                                columsView.setText(""+c3.getString(j));

                            }
                            decider=!c3.isLast();
                            if(!c3.isLast()){c3.moveToNext();}
                        }
                        else
                        {
                            tableRow.setVisibility(View.GONE);
                        }

                    }

                    IndexInfo.index=tobestartindex;
                }
            }
        });

        // when user clicks on the next button update the table with the next 10 tuples from the database
        next.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                //if there are no tuples to be shown toast that this the last page
                if(IndexInfo.currentpage>= IndexInfo.numberofpages)
                {
                    Toast.makeText(getApplicationContext(), "This is the last page", Toast.LENGTH_LONG).show();
                }
                else
                {
                    IndexInfo.currentpage= IndexInfo.currentpage+1;
                    boolean decider=true;


                    for(int i=1;i<tableLayout.getChildCount();i++)
                    {
                        TableRow tableRow = (TableRow) tableLayout.getChildAt(i);


                        if(decider)
                        {
                            tableRow.setVisibility(View.VISIBLE);
                            for(int j=0;j<tableRow.getChildCount();j++)
                            {
                                LinearLayout llcolumn = (LinearLayout) tableRow.getChildAt(j);
                                TextView columsView =(TextView)llcolumn.getChildAt(0);

                                columsView.setText(""+c3.getString(j));

                            }
                            decider=!c3.isLast();
                            if(!c3.isLast()){c3.moveToNext();}
                        }
                        else
                        {
                            tableRow.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });

    }
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub

    }


    public ArrayList<Cursor> getData(String Query){
        if (myClass == null)
        {
            throw new RuntimeException("myClass is not initialized yet!");
        }

        RoomDatabase roomDatabase = Room.databaseBuilder(this, myClass, databaseName).build();

        SupportSQLiteDatabase sqlDB = roomDatabase.getOpenHelper().getWritableDatabase();

        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.query(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){
            ex.printStackTrace();
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }
}