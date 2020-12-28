package io.github.turskyi.travellingpro.features.flags.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.databinding.ActivityFlagsBinding
import io.github.turskyi.travellingpro.extensions.openInfoDialog
import io.github.turskyi.travellingpro.features.flags.callback.OnFlagFragmentListener
import io.github.turskyi.travellingpro.features.flags.view.adapter.ScreenSlidePagerAdapter
import io.github.turskyi.travellingpro.features.flags.view.adapter.ZoomOutPageTransformer

class FlagsActivity : AppCompatActivity(R.layout.activity_flags), OnFlagFragmentListener {

    companion object {
        const val EXTRA_POSITION = "ua.turskyi.travelling.POSITION"
    }

    private lateinit var binding: ActivityFlagsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListeners()
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

    private fun initView() {
        binding = ActivityFlagsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorBlack)
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        binding.pager.adapter = pagerAdapter
        binding.pager.offscreenPageLimit = 4
        binding.pager.setPageTransformer(ZoomOutPageTransformer())
        val getBundle: Bundle? = this.intent.extras
        val startPosition = getBundle?.getInt(EXTRA_POSITION)
        startPosition?.let { position ->
            binding.pager.post {
                binding.pager.setCurrentItem(
                    position,
                    true
                )
            }
        }
        postponeEnterTransition()
    }

    private fun initListeners() = binding.toolbar.setNavigationOnClickListener { onBackPressed() }
}