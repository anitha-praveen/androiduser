package com.cloneUser.client.drawer.favorites.listFavorite

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cloneUser.client.BR
import com.cloneUser.client.R
import com.cloneUser.client.base.BaseFragment
import com.cloneUser.client.connection.FavPlace
import com.cloneUser.client.databinding.FragmentFavoriteBinding
import com.cloneUser.client.drawer.adapters.fav_adapter.FavoritesAdapter
import java.util.ArrayList
import javax.inject.Inject

class FavoriteFragment: BaseFragment<FragmentFavoriteBinding, FavoriteVM>(),
    FavoriteNavigator {
    companion object {
        const val TAG = "FavoriteFragment"
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: FragmentFavoriteBinding
    var adapter: FavoritesAdapter? = null

    @Inject
    lateinit var vmProviderFactory: ViewModelProvider.Factory
    private val vm by lazy {
        ViewModelProvider(this, vmProviderFactory).get(FavoriteVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getmBinding()
        vm.setNavigator(this)
        setUpAdapter()
    }


    override fun getLayoutId() = R.layout.fragment_favorite

    override fun getBR() = BR.viewModel

    override fun getVMClass() = vm

    private fun setUpAdapter(){
        adapter = FavoritesAdapter(ArrayList<FavPlace.Favourite>(), this,0)
        val mLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerFavPlaces.layoutManager = mLayoutManager
        binding.recyclerFavPlaces.itemAnimator = DefaultItemAnimator()
        binding.recyclerFavPlaces.adapter = adapter
        vm.getFavoritePlaces()
    }

    override fun addList(favPlace: List<FavPlace.Favourite>?) {
        adapter!!.addList(favPlace!!)
    }

    override fun gotoAddFavorite() {
        findNavController().navigate(R.id.favorite_to_add_favorite)
    }

    override fun closeFragment() {
        findNavController().popBackStack()
    }

    override fun deleteFavorite(favPlace: FavPlace.Favourite) {
        vm.deleteFavoritePlace(favPlace.slug!!)
    }
}