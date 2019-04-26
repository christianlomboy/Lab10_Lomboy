package edu.temple.Lab10_Lomboy;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity
        implements BookListFragment.OnCallbackReceivedList, BookDetailsFragment.OnCallbackReceivedList {

    static volatile ArrayList<Book> books = new ArrayList<>();

    EditText etSearch;
    Button btnSearch;

    ViewPager pager;
    CustomViewPagerAdapter viewPagerAdapter;

    boolean isTablet;
    boolean connected;

    AudiobookService.MediaControlBinder mcb;
    GetBooksTask task;

    private static String dirName = "bookstorage";
    File storageDir;

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mcb = (AudiobookService.MediaControlBinder) service;
            connected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connected = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(this, AudiobookService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mcb.stop();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main);
            pager = findViewById(R.id.view_pager);
            initList();
            initDetails(books);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_main);
            pager = findViewById(R.id.view_pager);
            initDetails(books);
            if (isTablet) {
                initList();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storageDir = new File(Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS, dirName);

        if (storageDir.exists() && storageDir.isDirectory()) {
            Log.d("Storage directory found: ", storageDir.getAbsolutePath());
        } else {
            storageDir.mkdir();
            Log.d("Storage directory not found, creating...", storageDir.getAbsolutePath());
        }

        pager = findViewById(R.id.view_pager);
        task = new GetBooksTask();
        task.execute("https://kamorris.com/lab/audlib/booksearch.php");

        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_search);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (etSearch.getText().toString().equals("")) {
                    GetBooksTask booksTask = new GetBooksTask();
                    booksTask.execute("https://kamorris.com/lab/audlib/booksearch.php");
                    Toast toast = Toast.makeText(getApplicationContext(), "Please enter a search query", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    String query = etSearch.getText().toString();
                    GetBooksTask booksTask = new GetBooksTask();
                    booksTask.execute("https://kamorris.com/lab/audlib/booksearch.php?search=" + query);
                }
            }
        });
    }


    private void initList() {
        int layout = 0;
        isTablet = getResources().getBoolean(R.bool.isTablet);
        int orientation = getResources().getConfiguration().orientation;

        if (isTablet) {
            layout = R.id.tab_master;
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layout = R.id.land_master;
        }

        if (layout != 0) {
            BookListFragment blf = new BookListFragment();
            FragmentTransaction ft;
            ft = getSupportFragmentManager()
                    .beginTransaction()
                    .replace(layout, blf);
            ft.commit();
        }
    }

    private void initDetails(ArrayList<Book> books) {
        ArrayList<Fragment> fragmentArrayList = new ArrayList<>();
        for (int i = 0; i < books.size(); i++) {
            fragmentArrayList.add(BookDetailsFragment.newInstance(books.get(i)));
        }
        viewPagerAdapter = new CustomViewPagerAdapter(getSupportFragmentManager(), fragmentArrayList);
        pager.setAdapter(viewPagerAdapter);
    }

    @Override
    public void displayBook(String title) {
        int bookId = 0;
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getTitle().equals(title)) {
                bookId = i;
                break;
            }
        }
        pager.setCurrentItem(bookId);
    }

    @Override
    public void playBook(int id) {
        mcb.play(id + 1);
    }

    @Override
    public void pauseBook() {
        mcb.pause();
    }

    @Override
    public void stopBook() {
        mcb.stop();
    }

    @Override
    public void bookProg(Handler progHandler) {
        mcb.setProgressHandler(progHandler);
    }

    @Override
    public void setBookProg(int position) {
        mcb.seekTo(position);
    }


    @SuppressLint("StaticFieldLeak")
    public class GetBooksTask extends AsyncTask<String, Void, ArrayList<Book>> {

        @Override
        protected ArrayList<Book> doInBackground(String... strings) {

            try {
                URL booksUrl = new URL(strings[0]);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(booksUrl.openStream()));
                StringBuilder stringBuilder = new StringBuilder();

                String tempStr;
                while ((tempStr = bufferedReader.readLine()) != null) {
                    stringBuilder.append(tempStr);
                }
                bufferedReader.close();

                try {
                    JSONArray booksJsonArr = new JSONArray(stringBuilder.toString());
                    if (booksJsonArr.length() > 0) {
                        books.clear();

                        for (int i = 0; i < booksJsonArr.length(); i++) {
                            books.add(new Book(i, booksJsonArr.getJSONObject(i)));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return books;
        }

        @Override
        protected void onPostExecute(ArrayList<Book> books) {
            initList();
            initDetails(books);

        }
    }

}
