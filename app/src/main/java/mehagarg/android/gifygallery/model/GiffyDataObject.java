package mehagarg.android.gifygallery.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by meha on 4/2/16.
 */
public class GiffyDataObject implements Parcelable {
    private String url;
    private String slug;

    public GiffyDataObject() {
    }

    protected GiffyDataObject(Parcel in) {
        url = in.readString();
        slug = in.readString();
    }

    public static final Creator<GiffyDataObject> CREATOR = new Creator<GiffyDataObject>() {
        @Override
        public GiffyDataObject createFromParcel(Parcel in) {
            return new GiffyDataObject(in);
        }

        @Override
        public GiffyDataObject[] newArray(int size) {
            return new GiffyDataObject[size];
        }
    };

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(slug);
    }
}
