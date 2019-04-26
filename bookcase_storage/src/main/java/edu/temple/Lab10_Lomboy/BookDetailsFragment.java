package edu.temple.Lab10_Lomboy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.io.InputStream;
import java.util.Objects;

public class BookDetailsFragment extends Fragment {

    ImageView ivCover;
    TextView tvTitle;
    TextView tvAuthor;
    TextView tvPublished;
    Button btnPlay;
    Button btnPause;
    Button btnStop;
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

}
