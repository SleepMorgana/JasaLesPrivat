package developer.aulia.jasalesprivat.subjects;

import android.os.Parcel;
import android.os.Parcelable;

import developer.aulia.jasalesprivat.interfaces.Storable;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

//Nama pelajaran
public class Subject implements Storable, Parcelable {
    private String id;
    private String name;

    public Subject(DocumentSnapshot subject){
        id = subject.getId();
        name = (String) subject.getData().get("Name");
    }

    public Subject(String name){
        this.name = name;
    }

    public Subject(String id, String name){
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<String, Object> marshal() {
        //Id harus diambil dari instance,
        //dalam ID dokumen firestore tidak ada di peta
        Map<String, Object> subject = new HashMap<>();
        subject.put("Name",name);
        return subject;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.id);
    }

    public static final Creator<Subject> CREATOR = new Creator<Subject>() {
        public Subject createFromParcel(Parcel in) {
            return new Subject(in);
        }

        public Subject[] newArray(int size) {
            return new Subject[size];
        }
    };

    /**
     * Constructor yang mengambil parcel dan membangun sebuah objek pengguna
     * @param in parcel
     */
    private Subject(Parcel in) {
       name = in.readString();
       this.id = in.readString();
    }
}
