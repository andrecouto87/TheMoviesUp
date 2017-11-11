package br.com.andrecouto.nextel.themoviesdbapp.ui.activity

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import br.com.andrecouto.nextel.themoviesdbapp.R
import br.com.andrecouto.nextel.themoviesdbapp.data.dao.DatabaseManager
import br.com.andrecouto.nextel.themoviesdbapp.data.model.Cast
import br.com.andrecouto.nextel.themoviesdbapp.data.model.Movie
import br.com.andrecouto.nextel.themoviesdbapp.extensions.loadUrl
import br.com.andrecouto.nextel.themoviesdbapp.extensions.setupToolbar
import br.com.andrecouto.nextel.themoviesdbapp.util.Constants
import br.com.andrecouto.nextel.themoviesdbapp.util.DateUtils
import br.com.andrecouto.nextel.themoviesdbapp.util.NetworkUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_details_movie.*
import kotlinx.android.synthetic.main.activity_details_movie_contents.*

class DetailsMovieActivity : AppCompatActivity() {
    val movie by lazy { intent.getParcelableExtra<Movie>("movie") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_movie)

        setupToolbar(R.id.toolbar, movie.title, true)

        initViews()
    }

    fun initViews() {

        movie.let { movie ->
            if (movie is Movie) {
                appBarImg.loadUrl(Constants.BASE_URL_IMG_500 + movie.backdropPath, null, NetworkUtils.isNetworkAvailable(this))
                imgVideo.loadUrl(Constants.BASE_URL_IMG_500 + movie.backdropPath, null, NetworkUtils.isNetworkAvailable(this))
                if (movie.overview.isNullOrEmpty())
                    cDescription.visibility = View.GONE
                else
                    tDesc.setText(movie.overview)

                if (movie.releaseDate != null)
                    tReleaseDate.setText(DateUtils.formatDateToString(movie.releaseDate))
                else
                    lReleaseDate.visibility = View.GONE

                if (movie.runtime != null && movie.runtime!! > 0)
                    tRunTime.setText(movie.runtime!!.toString())
                else
                    lRunTime.visibility = View.GONE

                if (movie.homepage.isNullOrEmpty())
                    lWebSite.visibility = View.GONE
                else
                    tWebsite.setText(movie.homepage)

                tVoteAverage.setText(movie.voteAverage.toString())
            }

        }

        DatabaseManager.getMovieDAO().getById(movie.id!!)?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { movieWith ->
                    if (movieWith.genres.size > 0)
                        tGenre.setText(movieWith.genres.first().name)
                    else
                        lGenre.visibility = View.GONE

                    if (movieWith.casts.size > 0) {
                        tCast.setText(movieWith.casts.map { cast: Cast -> cast.name }.toString().replace("[", "").replace("]", ""))
                    } else {
                        lCast.visibility = View.GONE
                    }

                    if (movieWith.videos.size > 0) {
                        imgVideo.setOnClickListener {
                            val url = Constants.BASE_URL_VIDEO + movieWith.videos.first().key
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setData(Uri.parse(url))
                            startActivity(intent)
                        }
                    } else {
                        cVideo.visibility = View.GONE
                    }
                }
    }
}
