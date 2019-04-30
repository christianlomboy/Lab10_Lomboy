package edu.temple.Lab10_Lomboy;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static edu.temple.Lab10_Lomboy.MainActivity.dirName;

public class Book implements Parcelable {

    private int id;
    private String title;
    private String author;
    private int published;
    private String coverURL;
    private int duration;

    public Book(int id, String title, String author, int published, String coverURL, int duration) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.published = published;
        this.coverURL = coverURL;
        this.duration = duration;
    }

    Book(int id, JSONObject object) {
        try {
            this.id = id;
            this.title = object.getString("title");
            this.author = object.getString("author");
            this.published = object.getInt("published");
            this.coverURL = object.getString("cover_url");
            // add duration
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Book(Parcel in) {
        id = in.readInt();
        title = in.readString();
        author = in.readString();
        published = in.readInt();
        coverURL = in.readString();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public int getId() {
        return id + 1;
    }

    String getTitle() {
        return title;
    }

    String getAuthor() {
        return author;
    }

    String getPublished() {
        return Integer.toString(published);
    }

    String getCoverURL() {
        return coverURL;
    }

    public int getDuration() {
        return duration;
    }

    Boolean isInStorage() {
        File bookFile = new File(Environment.getExternalStorageDirectory()
                + File.separator
                + Environment.DIRECTORY_DOWNLOADS, dirName + '/' + (this.id + 1) + ".mp3");
        return bookFile.exists();
    }

    public String getPath() {
        String path;
        if (this.isInStorage()) {
            File bookFile = new File(Environment.getExternalStorageDirectory()
                    + File.separator
                    + Environment.DIRECTORY_DOWNLOADS, dirName + '/' + (this.id + 1) + ".mp3");
            path = bookFile.getPath();
        } else {
            path = null;
        }
        return path;
    }

    public String getDlUrlString() {
        return "https://kamorris.com/lab/audlib/download.php?id=" + (this.getId() + 1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeInt(published);
        dest.writeString(coverURL);
    }
}

