package io.github.turskyi.travellingpro.features.flags.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.databinding.ActivityFlagsBinding
import io.github.turskyi.travellingpro.entities.Traveller
import io.github.turskyi.travellingpro.utils.extensions.openInfoDialog
import io.github.turskyi.travellingpro.features.flags.view.callbacks.FlagsActivityView
import io.github.turskyi.travellingpro.features.flags.view.callbacks.OnChangeFlagFragmentListener
import io.github.turskyi.travellingpro.features.flags.view.adapter.FlagsAdapter
import io.github.turskyi.travellingpro.features.flags.view.adapter.ZoomOutPageTransformer
import io.github.turskyi.travellingpro.utils.extensions.toast

class FlagsActivity : AppCompatActivity(R.layout.activity_flags), OnChangeFlagFragmentListener,
    FlagsActivityView {

    companion object {
        const val EXTRA_POSITION = "io.github.turskyi.travellingpro.POSITION"
        const val EXTRA_USER = "io.github.turskyi.travellingpro.USER_ID"
        const val EXTRA_ITEM_COUNT = "io.github.turskyi.travellingpro.ITEM_COUNT"
    }

    private var getBundle: Bundle? = null
    private lateinit var binding: ActivityFlagsBinding
    private lateinit var flagsAdapter: FlagsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getBundle = this@FlagsActivity.intent.extras
        if (getBundle != null) {
            initView()
            initObserver()
            initListener()
        } else {
            toast(R.string.msg_not_found)
            finish()
        }
    }

    override fun onChangeToolbarTitle(title: String?) {
        binding.tvToolbarTitle.text = title
    }

    override fun onCreatePanelMenu(featureId: Int, menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_info, menu)
        return super.onCreatePanelMenu(featureId, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (getBundle != null && getBundle!!.getParcelable<Traveller>(EXTRA_USER) != null) {
            val traveller: Traveller = getBundle!!.getParcelable(EXTRA_USER)!!
            openInfoDialog(
                R.string.txt_info_user_flags,
                traveller.name,
            )
        } else {
            openInfoDialog(R.string.txt_info_flags)
        }
        return true
    }

    override fun getItemCount(): Int = if (getBundle != null) {
        getBundle!!.getInt(EXTRA_ITEM_COUNT)
    } else {
        0
    }

    override fun setLoaderVisibility(currentVisibility: Int) {
        binding.pb.visibility = currentVisibility
    }

    private fun initView() {
        binding = ActivityFlagsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)
        initAdapter()
    }

    private fun initAdapter() {
        /* flagsAdapter cannot by implemented in dependency injection module
         * since "view pager 2" required exact context */
        flagsAdapter = FlagsAdapter(this)
        binding.pager.apply {
            adapter = flagsAdapter
            offscreenPageLimit = 4
            setPageTransformer(ZoomOutPageTransformer())
            if (getBundle != null) {
                val startPosition: Int = getBundle!!.getInt(EXTRA_POSITION)
                post { setCurrentItem(startPosition, true) }
            }
        }
    }

    private fun initListener() = binding.toolbar.setNavigationOnClickListener { onBackPressed() }

    private fun initObserver() = lifecycle.addObserver(flagsAdapter)
}