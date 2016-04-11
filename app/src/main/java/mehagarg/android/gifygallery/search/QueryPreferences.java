package mehagarg.android.gifygallery.search;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by meha on 4/11/16.
 */
public class QueryPreferences {
    public static final String PREF_SEARCH_QUERY = "searchQuery";

    public static String getStoredQuery(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_SEARCH_QUERY, null);
    }

    public static void setStoredQuery(Context context, String prefSearchQuery) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, prefSearchQuery)
                .apply();
    }
}
