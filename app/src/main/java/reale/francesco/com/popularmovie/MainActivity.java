package reale.francesco.com.popularmovie;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import reale.francesco.com.popularmovie.Model.ListOfMovies;
import reale.francesco.com.popularmovie.Utils.NetworkUtils;
import reale.francesco.com.popularmovie.data.MovieAdapter;
import reale.francesco.com.popularmovie.data.MovieContract;
import reale.francesco.com.popularmovie.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieClickListner, LoaderManager.LoaderCallbacks<ListOfMovies> {

    ActivityMainBinding bind;
    private Toast toast = null;
    private static final int MOVIE_LOADER = 22;
    private int actualItem = 0 ;
    private int actualPage = 1 ;
    private static final String LAST_PAGE_SAVED =  "LASTPAGE";
    private static final String LAST_LIST =  "LIST_OF_MOVIE";
    private static final String POPULAR = "POPULAR";
    private static final String OLD_POPULAR = "OLD_POPULAR";
    private static final String PAGE = "PAGE";
    private static final int ACTUAL_POPULAR = 0;
    private static final int POSITION_ID_MOVIE_IN_DB = 0;
    private static final int POSITION_TITLE_MOVIE_IN_DB = 1;
    private static final int POSITION_POSTER_MOVIE_IN_DB = 2;
    private static final int POSITION_RATING_MOVIE_IN_DB = 3;
    private static final int POSITION_DATE_MOVIE_IN_DB = 4;
    private static final int POSITION_DESCR_MOVIE_IN_DB = 5;
    private static final int ACTUAL_TOP_RATED = 1;
    private static final int ACTUAL_PREFERD = 2;
    private int actualStatus = ACTUAL_POPULAR;
    private int oldStatus ;
    private Spinner spinner = null;
    private  ArrayAdapter<CharSequence> spinnerAdapter = null;
    ListOfMovies list;
    private int lastItemClickedPositon ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        oldStatus = actualStatus;
        bind = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(bind.toolbar);

        GridLayoutManager gridManager = (GridLayoutManager)
                bind.recyclerViewMovies.getLayoutManager();

        bind.recyclerViewMovies.setLayoutManager(gridManager);
        bind.recyclerViewMovies.setHasFixedSize(true);
        final MovieAdapter adapter = new MovieAdapter(this);
        bind.recyclerViewMovies.setAdapter(adapter);
        bind.recyclerViewMovies.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                // each time the recycle view is scrolled we need to check if we should perform a query
                int totalOfItems = bind.recyclerViewMovies.getAdapter().getItemCount();
                GridLayoutManager layoutManager = (GridLayoutManager)
                        recyclerView.getLayoutManager();
                actualItem = layoutManager.findFirstVisibleItemPosition();
                if(layoutManager.findLastCompletelyVisibleItemPosition() == totalOfItems -1) {
                    if(list.getTotalPagesNum()>actualPage && actualStatus != ACTUAL_PREFERD) { // maybe we have finished the total pages to display
                        incrementPage(); // increment number of page
                        sendQueryToDb(); // send the query to the movieDbAPI
                    }
                }
            }
        });

        bind.textviewNOMOVIE.setVisibility(View.VISIBLE);
        bind.recyclerViewMovies.setVisibility(View.GONE);

        if(restoreInstance(savedInstanceState)){            // check if there is something to restore
            updateMovies();                                 // update movies with data restored
            bind.textviewNOMOVIE.setVisibility(View.GONE);
            bind.recyclerViewMovies.scrollToPosition(actualItem);   // scroll to the last Item displayed in the old session
        }

       getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }


    public int getActualPage() {
        return actualPage;
    }

    public int incrementPage() {
        return actualPage++;
    }

    public void setActualPage(int actualPage) {
        this.actualPage = actualPage;
    }

    void sendQueryToDb(){

        if (NetworkUtils.isOnline(this) || actualStatus == ACTUAL_PREFERD) {      // check if the device is Online

            switch(actualStatus){
                case ACTUAL_POPULAR :
                    bind.toolbar.setTitle(R.string.PopularMovie);
                    break;
                case ACTUAL_TOP_RATED:
                    bind.toolbar.setTitle(R.string.TopRated);
                    break;
            }

            getSupportLoaderManager()
                    .restartLoader(MOVIE_LOADER, null, MainActivity.this)
                    .forceLoad();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("")
                    .setTitle(R.string.internet_error);

            builder.setPositiveButton(R.string.retry_connection, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    sendQueryToDb();
                }
            });

            builder.setNegativeButton(R.string.close_dialog, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    actualStatus = oldStatus;
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem spinnerMenuItem = menu.findItem(R.id.spinner);
        spinner = (Spinner)spinnerMenuItem.getActionView();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                ((TextView)view).setText(null);
                switch(position){

                    case ACTUAL_POPULAR :
                        if(list == null || actualStatus != ACTUAL_POPULAR){
                            oldStatus = actualStatus;
                            setActualPage(1);
                            actualStatus = ACTUAL_POPULAR;
                            sendQueryToDb();
                        }
                        break;
                    case ACTUAL_TOP_RATED:
                        if(list == null || actualStatus != ACTUAL_TOP_RATED){
                            setActualPage(1);
                            oldStatus = actualStatus;
                            actualStatus = ACTUAL_TOP_RATED;
                            sendQueryToDb();
                        }
                        break;
                    case ACTUAL_PREFERD:
                        if(list == null || actualStatus != ACTUAL_PREFERD){
                            setActualPage(1);
                            oldStatus = actualStatus;
                            actualStatus = ACTUAL_PREFERD;
                            sendQueryToDb();
                            bind.toolbar.setTitle(R.string.PreferitMovies);
                        }
                        break;

                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerAdapter = ArrayAdapter.
                createFromResource(this, R.array.order_array, android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        setSpinnerSelection();
        return true;
    }


    private void setSpinnerSelection(){
        int spinnerPosition = 0;
        switch(actualStatus){
            case ACTUAL_POPULAR:
                spinnerPosition = spinnerAdapter.getPosition("Popular Movie");
                break;
            case ACTUAL_TOP_RATED:
                spinnerPosition = spinnerAdapter.getPosition("Top Rated");
                break;
            case ACTUAL_PREFERD:
                spinnerPosition = spinnerAdapter.getPosition("Your Preferit Movie");
                break;
        }
        spinner.setSelection(spinnerPosition);

    }

    protected boolean restoreInstance(Bundle savedInst) {

        if (savedInst == null) return false;
        if (!savedInst.containsKey(LAST_PAGE_SAVED)) return false;
        if (!savedInst.containsKey(LAST_LIST)) return false;
        if (!savedInst.containsKey(POPULAR)) return false;
        if (!savedInst.containsKey(OLD_POPULAR)) return false;

        actualStatus = savedInst.getInt(POPULAR);
        oldStatus = savedInst.getInt(OLD_POPULAR);

        list = savedInst.getParcelable(LAST_LIST);

        actualItem = savedInst.getInt(LAST_PAGE_SAVED);

        actualPage = savedInst.getInt(PAGE);

        return true;

    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
            state.putInt(POPULAR, actualStatus);
            state.putInt(OLD_POPULAR, oldStatus);
            state.putParcelable(LAST_LIST, list);
            state.putInt(PAGE, actualPage);
            state.putInt(LAST_PAGE_SAVED,actualItem);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.spinner) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onGridMovieClick(Movie clickedFilm , int position) {

        Intent intent = new Intent(this, DetailActivtyTabbed.class);
        intent.putExtra(DetailActivtyTabbed.MOVIE, clickedFilm);
        startActivityForResult(intent,actualStatus);
        lastItemClickedPositon = position;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case(ACTUAL_PREFERD):
                if(resultCode == Activity.RESULT_OK) {
                    list.getResults().clear();
                    sendQueryToDb();
                    Log.d("result","activty back got it");
                }
                break;
            default:
                Movie tmp = (Movie)data.getExtras().getParcelable(DetailActivtyTabbed.MOVIE_PARC);
                list.getResults().get(lastItemClickedPositon).setPrefered(tmp.isPrefered());
                MovieAdapter adapter = (MovieAdapter) bind.recyclerViewMovies.getAdapter();
                adapter.setList(list.getResults());
                adapter.notifyItemChanged(lastItemClickedPositon);
        }

    }

    public ListOfMovies buildListForPrefered(){

        ListOfMovies preferitList = new ListOfMovies();
        List<Movie> listOfMovie = new ArrayList<Movie>();
       Cursor cursor =  getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                MovieContract.MovieEntry.COLUMN_ID);

        while(cursor.moveToNext()) {

            Movie movie = new Movie();
            movie.setPrefered(true);
            movie.setId(cursor.getInt(POSITION_ID_MOVIE_IN_DB));
            movie.setOverView(cursor.getString(POSITION_DESCR_MOVIE_IN_DB));
            movie.setDateFromString(cursor.getString(POSITION_DATE_MOVIE_IN_DB));
            movie.setVote_avarage(cursor.getFloat(POSITION_RATING_MOVIE_IN_DB));
            movie.setThmbnail(cursor.getString(POSITION_POSTER_MOVIE_IN_DB));
            movie.setTitle(cursor.getString(POSITION_TITLE_MOVIE_IN_DB));
            listOfMovie.add(movie);
        }
        preferitList.setResults(listOfMovie);
        preferitList.setTotalResultsNum(listOfMovie.size());
        preferitList.setTotalPagesNum(1);
        preferitList.setPage(1);

        return preferitList;
    }

    @NonNull
    @Override
    public Loader<ListOfMovies> onCreateLoader(int id, @Nullable Bundle args) {

        return new AsyncTaskLoader<ListOfMovies>(this) {

            @Override
            public ListOfMovies loadInBackground() {
                URL url = null;
                try {
                    switch(actualStatus){

                        case ACTUAL_POPULAR:
                            url = NetworkUtils.buildUrl(true,getActualPage());
                            if (url == null)
                                return null;
                            break;

                        case ACTUAL_TOP_RATED:
                            url = NetworkUtils.buildUrl(false,getActualPage());
                            if (url == null)
                                return null;
                            break;
                        case ACTUAL_PREFERD:
                            return buildListForPrefered();
                            default:
                    }
                    String response = NetworkUtils.getResponseFromHttpUrl(url);
                    if (response != null) {
                        Type type = new TypeToken<ListOfMovies>(){}.getType();
                        ListOfMovies lm = new Gson().fromJson(response, type);
                        for( Movie m : lm.getResults() ){
                            String idOfFIlm = String.valueOf(m.getId());
                            Cursor cursor =  getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                                    null,
                                    "movieID=" + idOfFIlm.toString(),
                                    null,
                                    null);
                            while(cursor.moveToNext())
                            {
                                m.setPrefered(true);
                            }
                        }

                        return lm;
                    }
                    else
                        return null;

                } catch (Exception e) {
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ListOfMovies> loader, ListOfMovies data) {

        ListOfMovies listFinished =  data;

        if(oldStatus == actualStatus) {
            if (list != null && list.getResults().size() > 0) {
                list.setPage(listFinished.getPage());
                list.getResults().addAll(data.getResults());
            } else // this is the first time i'm loading
                list = listFinished;
        }else{
            bind.recyclerViewMovies.scrollToPosition(0);
            list = listFinished;
            oldStatus = actualStatus;
        }
        updateMovies();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ListOfMovies> loader) {

    }


    private void updateMovies() {
        if (list != null) {
            if(list.getResults().size()>0) {
                bind.textviewNOMOVIE.setVisibility(View.GONE);
                bind.recyclerViewMovies.setVisibility(View.VISIBLE);
            }else{
                bind.textviewNOMOVIE.setVisibility(View.VISIBLE);
                bind.recyclerViewMovies.setVisibility(View.GONE);
            }
            MovieAdapter adapter = (MovieAdapter) bind.recyclerViewMovies.getAdapter();
            adapter.setList(list.getResults());
            adapter.notifyDataSetChanged();
        }else{
            bind.textviewNOMOVIE.setVisibility(View.VISIBLE);

        }

    }


}
