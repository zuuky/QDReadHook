package cn.xihan.qdds

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import cn.xihan.qdds.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.highcapable.yukihookapi.hook.xposed.prefs.ui.ModulePreferenceFragment

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window?.statusBarColor = Color.TRANSPARENT
        // ViewPager2 Adapter for the fragments
        val viewPager2Adapter =
            ViewPager2Adapter(supportFragmentManager, lifecycle)
        binding.viewPager2.adapter = viewPager2Adapter
        // Tablayout 和 ViewPager2 绑定
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.main_title)
                1 -> getString(R.string.ads_title)
                2 -> getString(R.string.splash_title)
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }.attach()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

}

class SettingsFragment : ModulePreferenceFragment() {

    companion object {
        fun newInstance(index: Int): SettingsFragment {
            val args = Bundle()
            args.putInt("index", index)
            val fragment = SettingsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreatePreferencesInModuleApp(savedInstanceState: Bundle?, rootKey: String?) {
        when (arguments?.getInt("index") ?: 0) {
            0 -> setPreferencesFromResource(R.xml.root_preferences, rootKey)
            1 -> setPreferencesFromResource(R.xml.ads_preferences, rootKey)
            2 -> setPreferencesFromResource(R.xml.splash__preferences, rootKey)
        }
    }
}

class ViewPager2Adapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return SettingsFragment.newInstance(position)
    }
}