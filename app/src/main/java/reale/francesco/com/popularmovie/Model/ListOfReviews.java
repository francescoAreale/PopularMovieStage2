package reale.francesco.com.popularmovie.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import reale.francesco.com.popularmovie.Review;

public class ListOfReviews implements Parcelable{
    private List<Review> results;    // list of Movies for the actual page
    private int pageNum;   //  the page of the result

    protected ListOfReviews(Parcel in) {
        results = in.createTypedArrayList(Review.CREATOR);
        pageNum = in.readInt();
        totalResultsNum = in.readInt();
        totalPagesNum = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(results);
        dest.writeInt(pageNum);
        dest.writeInt(totalResultsNum);
        dest.writeInt(totalPagesNum);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ListOfReviews> CREATOR = new Creator<ListOfReviews>() {
        @Override
        public ListOfReviews createFromParcel(Parcel in) {
            return new ListOfReviews(in);
        }

        @Override
        public ListOfReviews[] newArray(int size) {
            return new ListOfReviews[size];
        }
    };

    public List<Review> getResults() {
        return results;
    }

    public void setResults(List<Review> results) {
        this.results = results;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getTotalResultsNum() {
        return totalResultsNum;
    }

    public void setTotalResultsNum(int totalResultsNum) {
        this.totalResultsNum = totalResultsNum;
    }

    public int getTotalPagesNum() {
        return totalPagesNum;
    }

    public void setTotalPagesNum(int totalPagesNum) {
        this.totalPagesNum = totalPagesNum;
    }

    @SerializedName("total_results")
    private int totalResultsNum;   // total number of results
    @SerializedName("total_pages")
    private int totalPagesNum;     // total number of pages retrived from the app


}
