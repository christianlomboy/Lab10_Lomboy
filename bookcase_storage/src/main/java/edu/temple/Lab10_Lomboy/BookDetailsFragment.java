package edu.temple.Lab10_Lomboy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

import static edu.temple.Lab10_Lomboy.MainActivity.dirName;

public class BookDetailsFragment extends Fragment {

    ImageView ivCover;
    TextView tvTitle;
    TextView tvAuthor;
    TextView tvPublished;
    Button btnPlay;
    Button btnPause;
    Button btnStop;
    Button btnStorage;
    SeekBar sbProgress;
    View view;

    Book book;

    OnCallbackReceivedList mCallback;


    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos = msg.what;
            sbProgress.setProgress(pos);
        }
    };

    public interface OnCallbackReceivedList {
        void playBook(int id);

        void pauseBook();

        void stopBook();

        void bookProg(Handler progHandler);

        void setBookProg(int position);
    }

    public static BookDetailsFragment newInstance(Book book) {
        Bundle args = new Bundle();
        args.putParcelable("book", book);

        BookDetailsFragment bdf = new BookDetailsFragment();
        bdf.setArguments(args);
        return bdf;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_book_details, container, false);


        Bundle bundle = this.getArguments();
        if (bundle != null) {
            book = bundle.getParcelable("book");
        }

        setBookDetails();

        btnPlay = view.findViewById(R.id.btn_play);
        btnPause = view.findViewById(R.id.btn_pause);
        btnStop = view.findViewById(R.id.btn_stop);
        btnStorage = view.findViewById(R.id.btn_storage);
        // TODO set text depending on if book exists in storage
        if (!book.isInStorage()) {
            btnStorage.setText("Download");
        } else {
            btnStorage.setText("Delete");
        }
        btnStorage.setVisibility(View.VISIBLE);

        sbProgress = view.findViewById(R.id.sb_progress);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.playBook(book.getId());
                sbProgress.post(new Runnable() {
                    @Override
                    public void run() {
                        mCallback.bookProg(mHandler);
                    }
                });
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.pauseBook();
                sbProgress.post(new Runnable() {
                    @Override
                    public void run() {
                        mCallback.bookProg(mHandler);
                    }
                });
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.stopBook();
                sbProgress.post(new Runnable() {
                    @Override
                    public void run() {
                        mCallback.bookProg(mHandler);
                    }
                });
            }
        });

        btnStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnStorage.getText() == "Download") {
                    // download book
                    new DownloadBookTask().execute(book.getDlUrlString());
                } else {
                    // delete book
                    File delFile = new File(Environment.getExternalStorageDirectory()
                            + File.separator
                            + Environment.DIRECTORY_DOWNLOADS, dirName + '/' + (book.getId() + 1) + ".mp3");
                    delFile.delete();
                    Toast.makeText(getContext(), "Book deleted.", Toast.LENGTH_SHORT).show();
                    btnStorage.setText("Download");
                }

            }
        });

        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCallback.setBookProg(progress);
                sbProgress.post(new Runnable() {
                    @Override
                    public void run() {
                        mCallback.bookProg(mHandler);
                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity;
        if (context instanceof Activity) {
            activity = (Activity) context;
        } else {
            activity = null;
        }

        try {
            mCallback = (OnCallbackReceivedList) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("ClassCastException " + e);
        }

    }

    public void setBookDetails() {
        ivCover = view.findViewById(R.id.iv_cover);
        new DownloadImageTask((ImageView) view.findViewById(R.id.iv_cover))
                .execute(book.getCoverURL());
        tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(book.getTitle());
        tvAuthor = view.findViewById(R.id.tv_author);
        tvAuthor.setText(book.getAuthor());
        tvPublished = view.findViewById(R.id.tv_published);
        tvPublished.setText(book.getPublished());
    }


    public void downloadBook() {
        File bookFile = new File(Environment.getExternalStorageDirectory()
                + File.separator
                + Environment.DIRECTORY_DOWNLOADS, dirName + '/' + book.getId());

    }

    public void deleteBook() {
        File bookFile = new File(Environment.getExternalStorageDirectory()
                + File.separator
                + Environment.DIRECTORY_DOWNLOADS, dirName + '/' + book.getId());
        bookFile.delete();
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }

            float aspectRatio = Objects.requireNonNull(mIcon11).getWidth() /
                    (float) mIcon11.getHeight();
            int width = 500;
            int height = Math.round(width / aspectRatio);

            mIcon11 = Bitmap.createScaledBitmap(
                    mIcon11, width, height, false);

            return Bitmap.createScaledBitmap(mIcon11, 160, 280, false);
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadBookTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... urlParams) {
            int count;
            try {
                URL url = new URL(urlParams[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                int fileLength = connection.getContentLength();

                // downlod the book file
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory()
                        + File.separator
                        + Environment.DIRECTORY_DOWNLOADS + '/' + dirName + '/' + (book.getId() + 1) + ".mp3");

                byte[] data = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(getContext(), "Book downloaded.", Toast.LENGTH_SHORT).show();
            btnStorage.setText("Delete");
        }
    }
}
