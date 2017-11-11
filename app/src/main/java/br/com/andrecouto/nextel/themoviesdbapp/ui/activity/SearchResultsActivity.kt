package br.com.andrecouto.nextel.themoviesdbapp.ui.activity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.text.Html
import android.view.Menu
import br.com.andrecouto.nextel.themoviesdbapp.App
import br.com.andrecouto.nextel.themoviesdbapp.R
import br.com.andrecouto.nextel.themoviesdbapp.adapter.MovieAdapter
import br.com.andrecouto.nextel.themoviesdbapp.data.component.DaggerSearchScreenComponent
import br.com.andrecouto.nextel.themoviesdbapp.data.model.Movie
import br.com.andrecouto.nextel.themoviesdbapp.data.module.SearchScreenModule
import br.com.andrecouto.nextel.themoviesdbapp.ui.contract.SearchScreenContract
import br.com.andrecouto.nextel.themoviesdbapp.ui.presenter.SearchScreenPresenter
import br.com.andrecouto.nextel.themoviesdbapp.util.SearchUtils
import kotlinx.android.synthetic.main.activity_search.*
import org.jetbrains.anko.startActivity
import java.util.*
import javax.inject.Inject

class SearchResultsActivity : AppCompatActivity(), SearchScreenContract.searchView ,SearchView.OnQueryTextListener{

    internal lateinit var adapter: MovieAdapter
    @Inject
    internal lateinit var searchPresenter: SearchScreenPresenter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        DaggerSearchScreenComponent.builder()
                .netComponent((getApplicationContext() as App).netComponent)
                .searchScreenModule(SearchScreenModule(this) )
                .build().inject(this)
        initViews()
        handleIntent(getIntent())
    }

    fun initViews() {
        searchRecycler.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false))
        searchRecycler.setItemAnimator(DefaultItemAnimator())
        adapter = MovieAdapter(applicationContext, ArrayList()) { onClickMovie(it)}
        searchRecycler.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getMenuInflater().inflate(R.menu.menu_movies, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.action_search).getActionView() as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()))
        searchView.setOnQueryTextListener(this)
        searchView.setIconifiedByDefault(false)
        return true
    }

    fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchPresenter.getLocalDataMovieLike(intent.getStringExtra(SearchManager.QUERY))
        }
    }

    open fun onClickMovie(movie: Movie) {
        startActivity<DetailsMovieActivity>("movie" to movie)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        adapter.clear()
        searchPresenter.getLocalDataMovieLike(query)
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }

    override fun onShowMovieLile(movies: List<Movie>) {
        adapter.addAll(movies)
        tResultSearch.setText(Html.fromHtml(SearchUtils.getResultSearchString(this, movies.size)))
    }

}