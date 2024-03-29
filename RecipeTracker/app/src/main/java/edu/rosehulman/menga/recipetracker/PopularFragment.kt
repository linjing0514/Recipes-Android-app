package edu.rosehulman.menga.recipetracker

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_popular.view.recycler_view

//private const val ARG_COLUMNS = "ARG_COLUMNS"

class PopularFragment : Fragment() {
    private lateinit var user: FirebaseUser
    private var columns: Int = 2
    private var listener: RecipeAdapter.OnRecipeSelectedListener? = null
    lateinit var adapter: PopularAdapter

    companion object {
        fun newInstance(user: FirebaseUser) =
            PopularFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(Constants.ARG_USER, user)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments.let {
            user = it?.getParcelable(Constants.ARG_USER)!!
        }
        adapter = PopularAdapter(context!!, listener!!,user)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is RecipeAdapter.OnRecipeSelectedListener) {
            listener = context
        } else {
            Log.e(Constants.TAG, "Should implement OnRecipeSelectedListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_popular, container, false)
        view.recycler_view.adapter = adapter
        adapter.getPopularRecipes()
        view.recycler_view.layoutManager = StaggeredGridLayoutManager(columns,StaggeredGridLayoutManager.VERTICAL)
        view.recycler_view.setHasFixedSize(true)
        return view
    }
}