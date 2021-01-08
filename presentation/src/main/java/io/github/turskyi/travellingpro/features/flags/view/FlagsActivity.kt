package io.github.turskyi.travellingpro.features.flags.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.databinding.ActivityFlagsBinding
import io.github.turskyi.travellingpro.extensions.openInfoDialog
import io.github.turskyi.travellingpro.features.flags.callbacks.FlagsActivityView
import io.github.turskyi.travellingpro.features.flags.callbacks.OnChangeFlagFragmentListener
import io.github.turskyi.travellingpro.features.flags.view.adapter.FlagsAdapter
import io.github.turskyi.travellingpro.features.flags.view.adapter.ZoomOutPageTransformer

class FlagsActivity : AppCompatActivity(R.layout.activity_flags), OnChangeFlagFragmentListener,
    FlagsActivityView {

    companion object {
        const val EXTRA_POSITION = "io.github.turskyi.travellingpro.POSITION"
        const val EXTRA_ITEM_COUNT = "io.github.turskyi.travellingpro.ITEM_COUNT"
    }
    private var getBundle: Bundle? = null
    private lateinit var binding: ActivityFlagsBinding
    private lateinit var flagsAdapter: FlagsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListener()
        initObserver()
    }

    override fun onChangeToolbarTitle(title: String?) {
        binding.tvToolbarTitle.text = title
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_info, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        openInfoDialog(R.string.txt_info_flags)
        return true
    }

    override fun getItemCount(): Int {
        val itemCount: Int? = getBundle?.getInt(EXTRA_ITEM_COUNT)
        return itemCount ?: 0
    }

    override fun setLoaderVisibility(currentVisibility: Int) {
        binding.pb.visibility = currentVisibility
    }

    private fun initView() {
        getBundle = this@FlagsActivity.intent.extras
        binding = ActivityFlagsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorBlack)
        initAdapter()
    }

    private fun initAdapter() {
        /* flagsAdapter cannot by implemented in koin modules
         since "view pager 2" required exact context */
        flagsAdapter = FlagsAdapter(this)
        binding.pager.apply {
            adapter = flagsAdapter
            offscreenPageLimit = 4
            setPageTransformer(ZoomOutPageTransformer())
            val startPosition = getBundle?.getInt(EXTRA_POSITION)
            startPosition?.let { position -> post { setCurrentItem(position, true) } }
        }
    }

    private fun initListener() = binding.toolbar.setNavigationOnClickListener { onBackPressed() }

    private fun initObserver() = lifecycle.addObserver(flagsAdapter)
}