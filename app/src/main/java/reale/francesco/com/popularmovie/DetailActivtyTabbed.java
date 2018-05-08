package reale.francesco.com.popularmovie;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;

import reale.francesco.com.popularmovie.Model.ListOfReviews;
import reale.francesco.com.popularmovie.Model.ListOfVideos;
import reale.francesco.com.popularmovie.Utils.NetworkUtils;
import reale.francesco.com.popularmovie.data.ReviewAdapter;
import reale.francesco.com.popularmovie.data.MovieContract;
import reale.francesco.com.popularmovie.data.VideoAdapter;
import reale.francesco.com.popularmovie.databinding.ActivityDetailActivtyTabbedBinding;
import reale.francesco.com.popularmovie.databinding.FragmentDetailActivtyTabbedBinding;
import reale.francesco.com.popularmovie.databinding.FragmentReviewBinding;
import reale.francesco.com.popularmovie.databinding.FragmentVideoBinding;

public class DetailActivtyTabbed extends AppCompatActivity  {


    public static String MOVIE = "MOVIE_SELECTED";
    public Movie movieSelected;
    private ActivityDetailActivtyTabbedBinding bind;
    public static final String MOVIE_PARC = "movie";
    public static final String LIST_REV = "listRev";
    public static final String LIST_VIDEO = "listVideo";
    public static final int LOAD_REVIEW = 1002;
    public static final int LOAD_VIDEO = 1003;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bind = DataBindingUtil.setContentView(this, R.layout.activity_detail_activty_tabbed);
        setSupportActionBar(bind.toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));


        Intent startIntent = getIntent();
        if (startIntent != null)
            movieSelected = startIntent.getParcelableExtra(MOVIE);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle(null);

        Drawable drawable = getResources().getDrawable(android.R.drawable.btn_star);
        if (movieSelected.isPrefered())
            drawable.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);
        else
            drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        bind.floatingActionButton.setImageDrawable(drawable);
        fullActivityStart(movieSelected);

        bind.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable drawable = getResources().getDrawable(android.R.drawable.btn_star);
                if (movieSelected.isPrefered()) {
                    drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                    removeMovieFromPreferit(view);
                    movieSelected.setPrefered(false);
                } else {
                    drawable.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);
                    addMovieToPreferit(view);
                    movieSelected.setPrefered(true);
                }
                bind.floatingActionButton.setImageDrawable(drawable);
            }
        });

    }




    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements VideoAdapter.VideoClickListner,  LoaderManager.LoaderCallbacks {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final int GENERAL = 0;
        private static final int TRAILER = 1;
        private static final int REVIEW = 2;
        private static final String PAGE = "actual_page";


        ListOfReviews list_review;
        ListOfVideos list_video;
        int actualPage;
        private static final String ARG_SECTION_NUMBER = "section_number";
        FragmentDetailActivtyTabbedBinding binding = null;
        FragmentReviewBinding bind_rev = null;
        FragmentVideoBinding bind_video = null;
        public PlaceholderFragment() {

        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putParcelable(LIST_REV, list_review);
            outState.putParcelable(LIST_VIDEO, list_video);
            outState.putInt(PAGE,actualPage);
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(Movie movieSelected, int position) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, position);
            args.putParcelable(MOVIE_PARC, movieSelected);
            fragment.setArguments(args);
            return fragment;
        }


        protected boolean restoreInstance(Bundle savedInst) {

            if (savedInst == null) return false;
            if (!savedInst.containsKey(LIST_REV)) return false;
            if (!savedInst.containsKey(LIST_VIDEO)) return false;
            if (!savedInst.containsKey(PAGE)) return false;

            list_video = savedInst.getParcelable(LIST_VIDEO);

            list_review = savedInst.getParcelable(LIST_REV);

            actualPage = savedInst.getInt(PAGE);

            return true;

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            if(savedInstanceState!=null){
                restoreInstance(savedInstanceState);
            }
            Movie movieSelected = getArguments().getParcelable(MOVIE_PARC);
            View view = null;

            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {

                case GENERAL:
                    binding = DataBindingUtil.inflate(
                            inflater, R.layout.fragment_detail_activty_tabbed, container, false);
                    binding.descriptionTextview.setText(movieSelected.getOverView());
                    binding.releaseDateTextview.setText(movieSelected.getDateFromString());
                    binding.titleTextview.setText(movieSelected.getTitle());
                    binding.voteAverageTextview.setText(String.valueOf(movieSelected.getVote_avarage()) + " / 10");
                    view = binding.getRoot();
                   break;
                case REVIEW:
                    getLoaderManager().destroyLoader(LOAD_REVIEW);
                    actualPage = 1;
                    bind_rev = DataBindingUtil.inflate(
                            inflater, R.layout.fragment_review, container, false);
                    view = bind_rev.getRoot();
                    LinearLayoutManager linearManager = (LinearLayoutManager)
                            bind_rev.rcReview.getLayoutManager();
                    bind_rev.rcReview.setLayoutManager(linearManager);
                    bind_rev.rcReview.setHasFixedSize(true);
                    bind_rev.rcReview.addOnScrollListener(new RecyclerView.OnScrollListener() {

                        @Override
                        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                            // each time the recycle view is scrolled we need to check if we should perform a query
                            int totalOfItems = bind_rev.rcReview.getAdapter().getItemCount();
                            LinearLayoutManager layoutManager = (LinearLayoutManager)
                                    recyclerView.getLayoutManager();
                            int actualItem = layoutManager.findFirstVisibleItemPosition();
                            if(layoutManager.findLastCompletelyVisibleItemPosition() == totalOfItems -1) {
                                if(list_review.getTotalPagesNum()>actualPage) { // maybe we have finished the total pages to display
                                   actualPage ++; // increment number of page
                                    sendQueryToDb((Movie)(getArguments().getParcelable(MOVIE_PARC)),actualPage,LOAD_REVIEW); // send the query to the movieDbAPI
                                }
                            }
                        }
                    });
                    ReviewAdapter adapter = new ReviewAdapter();
                    bind_rev.rcReview.setAdapter(adapter);
                    bind_rev.textviewNOMOVIE.setVisibility(View.VISIBLE);
                    bind_rev.rcReview.setVisibility(View.GONE);
                    if(list_review == null || ( list_review != null &&  list_review.getResults().size()==0))
                        sendQueryToDb(movieSelected,actualPage,LOAD_REVIEW);
                    else
                        notifyRC(LOAD_REVIEW);
                    break;
                case TRAILER:
                    getLoaderManager().destroyLoader(LOAD_REVIEW);
                    bind_video = DataBindingUtil.inflate(
                            inflater, R.layout.fragment_video, container, false);
                    view = bind_video.getRoot();
                    LinearLayoutManager linearManagerVideo = (LinearLayoutManager)
                            bind_video.rcVideo.getLayoutManager();
                    bind_video.rcVideo.setLayoutManager(linearManagerVideo);
                    bind_video.rcVideo.setHasFixedSize(true);
                    VideoAdapter adapterVideo = new VideoAdapter(this);
                    bind_video.rcVideo.setAdapter(adapterVideo);
                    bind_video.textviewNOMOVIE.setVisibility(View.VISIBLE);
                    bind_video.rcVideo.setVisibility(View.GONE);

                    if(list_video == null || ( list_video != null &&  list_video.getResults().size()==0))
                        sendQueryToDb(movieSelected,actualPage,LOAD_VIDEO);
                    else
                        notifyRC(LOAD_VIDEO);
                    break;
            }
            return view;
        }

        @NonNull
        @Override
        public Loader onCreateLoader(int id, @Nullable Bundle args) {

            AsyncTaskLoader async = null;

            switch (id) {

                case LOAD_REVIEW:
                    final Movie selected = args.getParcelable(MOVIE_PARC);
                    final int actualpage = args.getInt(PAGE);
                    async = new AsyncTaskLoader(getContext()) {
                        URL url = null;
                        ListOfReviews list_rev = null;
                        @Nullable
                        @Override
                        public Object loadInBackground() {
                            url = NetworkUtils.buildUrlDetail(NetworkUtils.REVIEW,selected.getId(), actualpage);
                            String response = null;
                            try {
                                response = NetworkUtils.getResponseFromHttpUrl(url);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (response != null) {
                                Type type = new TypeToken<ListOfReviews>() {
                                }.getType();
                                list_rev =  new Gson().fromJson(response, type);
                            }

                           return list_rev;
                        }
                    };
                    break;
                case LOAD_VIDEO:
                    final Movie movieselected = args.getParcelable(MOVIE_PARC);
                    async = new AsyncTaskLoader(getContext()) {
                        URL url = null;
                        ListOfVideos list_video = null;

                        @Nullable
                        @Override
                        public Object loadInBackground() {
                            url = NetworkUtils.buildUrlDetail(NetworkUtils.TREILLER,movieselected.getId(), 0);
                            String response = null;
                            try {
                                response = NetworkUtils.getResponseFromHttpUrl(url);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (response != null) {
                                Type type = new TypeToken<ListOfVideos>() {
                                }.getType();
                                list_video =  new Gson().fromJson(response, type);
                            }
                            return list_video;
                        }
                    };

            }
            return async;
        }
        void notifyRC(int typeOfRC){
            switch(typeOfRC){
                case LOAD_REVIEW:
                    bind_rev.textviewNOMOVIE.setVisibility(View.GONE);
                    bind_rev.rcReview.setVisibility(View.VISIBLE);
                    ReviewAdapter adapter = (ReviewAdapter) bind_rev.rcReview.getAdapter();
                    adapter.setList(list_review.getResults());
                    adapter.notifyDataSetChanged();
                    break;
                case LOAD_VIDEO:
                    bind_video.textviewNOMOVIE.setVisibility(View.GONE);
                    bind_video.rcVideo.setVisibility(View.VISIBLE);
                    VideoAdapter videoAdapter = (VideoAdapter) bind_video.rcVideo.getAdapter();
                    videoAdapter.setList(list_video.getResults());
                    videoAdapter.notifyDataSetChanged();

                    break;
            };
        }

        @Override
        public void onLoadFinished(@NonNull Loader loader, Object data) {

           switch( loader.getId()){
               case LOAD_REVIEW:
                   if(data!=null){
                   ListOfReviews listReview = (ListOfReviews) data;
                  if (list_review != null && list_review.getResults().size() > 0) {
                      // list_review.setPage(listReview.getPage());
                       list_review.getResults().addAll(listReview.getResults());
                   } else // this is the first time i'm loading*/
                       list_review = listReview;
                       notifyRC(LOAD_REVIEW);
                   }
                   break;
               case LOAD_VIDEO:
                       if(data!=null) {
                           ListOfVideos listVideo = new ListOfVideos (((ListOfVideos) data).getResults());
                           for(int i =0; i<((ListOfVideos) data).getResults().size(); i ++){
                               if((((ListOfVideos) data).getResults().get(i).getType().equals("Trailer")))
                                  listVideo.getResults().add(((ListOfVideos) data).getResults().get(i));
                           }
                           list_video = listVideo;
                           notifyRC(LOAD_VIDEO);
                       }
                   break;
           }
        }

        @Override
        public void onLoaderReset(@NonNull Loader loader) {
            super.onResume();

        }



        void sendQueryToDb(final Movie movieSelected, final int actualPage,int typeOfQuery){

            final int type = typeOfQuery;
            Bundle args = new Bundle();
            args.putParcelable(MOVIE_PARC, movieSelected);
            args.putInt(PAGE,actualPage);
            if (NetworkUtils.isOnline(getContext()) ) {      // check if the device is Online
                switch(typeOfQuery){
                    case LOAD_REVIEW:
                         getLoaderManager().restartLoader(LOAD_REVIEW, args, PlaceholderFragment.this)
                        .forceLoad();
                         break;
                    case LOAD_VIDEO:
                        getLoaderManager().restartLoader(LOAD_VIDEO, args, PlaceholderFragment.this)
                                .forceLoad();
                        break;
                }
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("")
                        .setTitle(R.string.internet_error);
                builder.setPositiveButton(R.string.internet_error_detail, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        switch(type) {
                            case LOAD_REVIEW:
                                sendQueryToDb(movieSelected, actualPage,LOAD_REVIEW);
                                break;
                            case LOAD_VIDEO:
                                sendQueryToDb(movieSelected, actualPage,LOAD_VIDEO);
                                break;
                        }
                    }
                });

                builder.setNegativeButton(R.string.close_dialog, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }


        @Override
        public void onVideoClick(Video clickedVideo, int position) {

                Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + clickedVideo.getKey()));
                Intent webIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com/watch?v=" + clickedVideo.getKey()));
                try {
                    getContext().startActivity(appIntent);
                } catch (ActivityNotFoundException ex) {
                    getContext().startActivity(webIntent);
                }

        }
    }
        /**
         * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
         * one of the sections/tabs/pages.
         */
        public class SectionsPagerAdapter extends FragmentPagerAdapter {

            public SectionsPagerAdapter(FragmentManager fm) {
                super(fm);
            }

            @Override
            public Fragment getItem(int position) {
                // getItem is called to instantiate the fragment for the given page.
                // Return a PlaceholderFragment (defined as a static inner class below).
                return PlaceholderFragment.newInstance(movieSelected, position);

            }

            @Override
            public int getCount() {
                // Show 3 total pages.
                return 3;
            }
        }


        public void addMovieToPreferit(View view) {
            // Insert new task data via a ContentResolver
            // Create new empty ContentValues object
            ContentValues contentValues = new ContentValues();
            // Put the task description and selected mPriority into the ContentValues
            contentValues.put(MovieContract.MovieEntry.COLUMN_TITLE, movieSelected.getTitle());
            contentValues.put(MovieContract.MovieEntry.COLUMN_DATE, movieSelected.getDateFromString());
            contentValues.put(MovieContract.MovieEntry.COLUMN_DESC, movieSelected.getOverView());
            contentValues.put(MovieContract.MovieEntry.COLUMN_POSTER, movieSelected.getThmbnail());
            contentValues.put(MovieContract.MovieEntry.COLUMN_RATING, movieSelected.getVote_avarage());
            contentValues.put(MovieContract.MovieEntry.COLUMN_ID, movieSelected.getId());
            // Insert the content values via a ContentResolver
            Uri uri = getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, contentValues);
            if (uri != null) {

                Snackbar.make(view, "Movie added to preferit with ID " + movieSelected.getId(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

        }

        @Override
        public void onBackPressed() {

            Intent data = new Intent();
            Bundle bundle = new Bundle();

            bundle.putParcelable(MOVIE_PARC, movieSelected);
            data.putExtras(bundle);

            if (getParent() == null) {
                setResult(Activity.RESULT_OK, data);
            } else {
                getParent().setResult(Activity.RESULT_OK, data);
            }

            super.onBackPressed();
        }

        public void removeMovieFromPreferit(View view) {
            // Insert the content values via a ContentResolver

            // Build appropriate uri with String row id appended

            String stringId = Integer.toString(movieSelected.getId());
            Uri uri = MovieContract.MovieEntry.CONTENT_URI;
            uri = uri.buildUpon().appendPath(stringId).build();
            Log.println(Log.DEBUG, "removeMovieFromPreferit", uri.getPath());

            // COMPLETED (2) Delete a single row of data using a ContentResolver

            int id = getContentResolver().delete(uri, null, null);
            if (id != 0) {
                Snackbar.make(view, "Movie removed from preferit", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

        }


        public void fullActivityStart(Movie movieSelected) {

            Picasso.with(this)
                    .load(movieSelected.getFullImage())
                    .error(R.mipmap.ic_movie)
                    .placeholder(R.mipmap.ic_movie)
                    .into(bind.image);
            bind.toolbar.setTitle("");

        }



}