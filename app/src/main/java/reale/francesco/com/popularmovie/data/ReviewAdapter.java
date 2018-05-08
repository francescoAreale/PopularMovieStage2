package reale.francesco.com.popularmovie.data;


import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import reale.francesco.com.popularmovie.R;
import reale.francesco.com.popularmovie.Review;
import reale.francesco.com.popularmovie.databinding.FragmentReviewDetailBinding;



public class ReviewAdapter extends  RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private ArrayList<Review> ListOfRev;
    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FragmentReviewDetailBinding bind = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.fragment_review_detail,
                        parent,
                        false);
        // we have just one layout so we inflate just that layout on the View
        return new ReviewViewHolder(bind);
    }

    public ReviewAdapter(){

    }
    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review tmp = ListOfRev.get(position); // get the element from the list+
        holder.bindReview(tmp,position);  // ok let's bind the movie contained a the sepcified position
    }

    @Override
    public int getItemCount() {
        if(ListOfRev == null)
            return 0;
        else
            return ListOfRev.size();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder{

        public  FragmentReviewDetailBinding bind;
        int position;

        public ReviewViewHolder( FragmentReviewDetailBinding bind) {

            super(bind.getRoot());
            this.bind = bind;
            bind.getRoot().setFocusable(true);
        }

        void bindReview(Review selectedReview, int position) {

           bind.nameTextView.setText(selectedReview.getAuthor());
           bind.descriptionTextView.setText(selectedReview.getContent());
           this.position = position;
        }

    }

    public void setList(final List<Review> newList) {
        ListOfRev = (ArrayList<Review>) newList;
    }
    public void setList( ArrayList<Review> passedList) {
        this.ListOfRev = passedList;
    }
}
