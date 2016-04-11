package mehagarg.android.gifygallery;

import android.support.v4.app.Fragment;

public class GiffyGalleryActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return GiffyGalleryFragment.newInstance();
    }
}
