package Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.save.OnboardingAdapter;
import com.example.save.OnboardingItem;
import com.example.save.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private OnboardingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);

        List<OnboardingItem> onboardingItems = new ArrayList<>();
        onboardingItems.add(new OnboardingItem(
                R.drawable.fs1,
                "Save Together, Grow Together",
                "Join or create savings circles with friends, \n" +
                        "family, or colleagues. Pool money monthly and \n" +
                        "receive payouts when it's your turn."
        ));
        onboardingItems.add(new OnboardingItem(
                R.drawable.fs2,
                "Simple 3-Step Process",
                "1. Contribute - Pay via Mobile Money automatically\n" +
                        "2. Pool Together - Track group progress in real-time\n" +
                        "3. Receive Payouts - Get your lump sum on schedule"
        ));
        onboardingItems.add(new OnboardingItem(
                R.drawable.fs3,
                "Everything You Need in One App",
                "✓ Automated Mobile Money payments\n" +
                        "✓ Real-time tracking & notifications\n" +
                        "✓ Emergency loan access from reserve\n" +
                        "✓ 100% transparent transaction history"
        ));

        adapter = new OnboardingAdapter(onboardingItems, new OnboardingAdapter.OnButtonClickListener() {
            @Override
            public void onNextClick(int position) {
                if (position < onboardingItems.size() - 1) {
                    // Go to next page
                    viewPager.setCurrentItem(position + 1);
                } else {
                    // Last page - mark onboarding as completed and go to LoginActivity
                    SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("isFirstTime", false);
                    editor.apply();

                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onBackClick(int position) {
                // Go to previous page
                if (position > 0) {
                    viewPager.setCurrentItem(position - 1);
                }
            }
        });

        viewPager.setAdapter(adapter);
    }
}