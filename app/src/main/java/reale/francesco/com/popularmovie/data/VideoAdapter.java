package reale.francesco.com.popularmovie.data;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

import reale.francesco.com.popularmovie.Movie;
import reale.francesco.com.popularmovie.R;
import reale.francesco.com.popularmovie.Video;
import reale.francesco.com.popularmovie.databinding.FragmentVideoDetailBinding;

public class VideoAdapter extends  RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {



    final private VideoAdapter.VideoClickListner clickListner;

    public interface VideoClickListner {
        void onVideoClick(Video clickedFilm,int position);
    }

    public VideoAdapter(VideoAdapter.VideoClickListner clickListner){
        this.clickListner = clickListner;
    }


    private ArrayList<Video> listOfVideo;
    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FragmentVideoDetailBinding bind = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.fragment_video_detail,
                        parent,
                        false);
        // we have just one layout so we inflate just that layout on the View
        return new VideoViewHolder(bind,parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Video tmp = listOfVideo.get(position); // get the element from the list+
        holder.bindReview(tmp,position);  // ok let's bind the movie contained a the sepcified position
    }

    @Override
    public int getItemCount() {
        if(listOfVideo == null)
            return 0;
        else
            return listOfVideo.size();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public FragmentVideoDetailBinding bind;
        int position;
        Context cont;

        public VideoViewHolder( FragmentVideoDetailBinding bind,Context cont) {

            super(bind.getRoot());
            this.bind = bind;
            bind.getRoot().setFocusable(true);
            bind.getRoot().setOnClickListener(this);
            this.cont = cont;
        }

        void bindReview(final Video selectedVideo, int position) {
            bind.imageButton1.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Ciao, sto guardando  su Popular Movie il Trailer di  " + selectedVideo.getName() + "\n" + "http://www.youtube.com/watch?v="+selectedVideo.getKey());
                    sendIntent.setType("text/plain");
                    cont.startActivity(sendIntent);

                }

            });
            bind.setVideo(selectedVideo);
            Picasso.with(bind.getRoot().getContext())
                    .load(selectedVideo.getPrevieImage())
                    .error(R.mipmap.ic_movie)
                    .placeholder(R.mipmap.ic_movie)
                    .into(bind.imageviewMovie);

            bind.nameTextView.setText(selectedVideo.getName());
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Video clicked = bind.getVideo();

            if (clickListner != null)
                clickListner.onVideoClick(clicked,position);
        }
    }

    public void setList(final List<Video> newList) {

        listOfVideo = (ArrayList<Video>) newList;
    }

    public void setList( ArrayList<Video> passedList) {

        this.listOfVideo = passedList;
    }
}
