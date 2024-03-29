package developer.aulia.jasalesprivat.sessions;

import android.os.Parcel;
import android.os.Parcelable;

import developer.aulia.jasalesprivat.interfaces.Storable;
import developer.aulia.jasalesprivat.users.Role;
import developer.aulia.jasalesprivat.utils.Util;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

public class Session extends Observable implements Storable , Parcelable {
    private String subject;//jenis pelajaran
    private String sender;//siapa pengirim si murid?
    private String target;//id
    private Status status;//periksa apakah akun dapat diterima atau tidak (pending berarti menunggu)
    private String id;//mendapatkan id dari session(jadwal les)
    private List<Object> dates = new ArrayList<>();// daftar timestamps, yang juga berarti mengatur waktu kegiatan


    public Session(DocumentSnapshot session){
        id = session.getId();
        subject = (String) session.getData().get("Subject");
        sender = (String) session.getData().get("Sender");
        target = (String) session.getData().get("Target");
        status = Status.valueOf((String)session.getData().get("Status"));
        dates = session.contains("Dates")? ((List<Object>) session.getData().get("Dates")): new ArrayList<>();
    }

    public Session(String subject,String sender,String target, Status status, String id){
        this.subject = subject;
        this.sender = sender;
        this.id = id;
        this.target = target;
        this.status = status;
    }

    public Session(String subject,String sender,String target, Status status){
        this.subject = subject;
        this.sender = sender;
        this.target = target;
        this.status = status;
    }

    public void updateStatus(Status status){
        this.status = status;
        setChanged();
    }

    public void addDate(String timestamp){
        dates.add((Object)timestamp);//menambahkan objek timestamp
        setChanged();
    }

    public void setDates(List<Object> dates) {
        this.dates = dates;
        setChanged();
    }

    public void setId(String id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public String getSender() {
        return sender;
    }

    public String getTarget() {
        return target;
    }

    public String getSubject() {
        return subject;
    }

    public List<Object> getTimestamps() {
        return dates;
    }

    public List<Date> getDates(){
        List<Date> tmp = new ArrayList<>();
        for (Object day : this.dates){
            Calendar dayCalendar = GregorianCalendar.getInstance();
            dayCalendar.setTimeInMillis(Long.parseLong((String)day));
            tmp.add(dayCalendar.getTime());
        }
        return tmp;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Map<String, Object> marshal() {
        //Id harus diambil dari instansi, di firestore document ids tidak berada pada map
        Map<String, Object> session = new HashMap<>();
        session.put("Subject",subject);
        session.put("Sender",sender);
        session.put("Target",target);
        session.put("Status",status.toString());
        session.put("Dates", (dates));

        return session;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Session)
            return ((Session) o).id.equals(this.id);
        return false;
    }

    /**
     * metode yang telah diuraikan untuk kelas Parcelable (dalam proyek ini, seperti kelas (es) telah (memiliki) tidak ada
     * child classes)
     * @return 0. Kelas parcelable (es) dalam proyek ini tidak ada child classes
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Tulis objek ke sebuah parcel
     * @param dest Parcel di mana objek harus ditulis
     * @param flags flags tambahan tentang bagaimana objek harus ditulis
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.target);
        dest.writeString(this.sender);
        dest.writeString(this.subject);
        dest.writeString(this.id);
        dest.writeList(this.dates);
        if (status!=null)
            dest.writeString(this.status.toString());
        else
            dest.writeString(Status.PENDING.toString());
        //dest.writeString(this.status.toString()); TODO instantiation to avoid null
    }

    /**
     * Regenerasi objek dari parcel
     */
    public static final Creator<Session> CREATOR = new Creator<Session>() {
        public Session createFromParcel(Parcel in) {
            return new Session(in);
        }

        public Session[] newArray(int size) {
            return new Session[size];
        }
    };

    /**
     * Constructor yang mengambil parcel dan membangun sebuah objek pengguna
     * @param in parcel
     */
    private Session(Parcel in) {
        target = in.readString();
        sender = in.readString();
        this.subject = in.readString();
        this.id = in.readString();
        in.readList(dates,String.class.getClassLoader());
        this.status = Status.valueOf(in.readString()); //TODO instantiation to avoid null
    }
}
