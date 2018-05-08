package reale.francesco.com.popularmovie.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import reale.francesco.com.popularmovie.Video;

public class ListOfVideos implements Parcelable{

    private List<Video> results;    // list of Movies for the actual page

    protected ListOfVideos(Parcel in) {
        results = in.createTypedArrayList(Video.CREATOR);
    }

    public ListOfVideos ( List<Video> results){
        this.results = new ArrayList<Video>();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(results);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ListOfVideos> CREATOR = new Creator<ListOfVideos>() {
        @Override
        public ListOfVideos createFromParcel(Parcel in) {
            return new ListOfVideos(in);
        }

        @Override
        public ListOfVideos[] newArray(int size) {
            return new ListOfVideos[size];
        }
    };

    public List<Video> getResults() {
        return results;
    }

    public void setResults(List<Video> results) {
        this.results = results;
    }



}
