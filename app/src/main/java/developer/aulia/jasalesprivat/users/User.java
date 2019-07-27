package developer.aulia.jasalesprivat.users;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import developer.aulia.jasalesprivat.interfaces.Storable;
import developer.aulia.jasalesprivat.sessions.Session;
import developer.aulia.jasalesprivat.sessions.Status;
import developer.aulia.jasalesprivat.subjects.Subject;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.SortedSet;
import java.util.TreeSet;

public class User extends Observable implements Storable, Parcelable, IUser {
    //combination of FirebaseUser and user from the `users` collection in firestore
    private String username;
    private String email;
    private Role role;
    private String id;
    private Bitmap profile_picture;
    private Status status; // for tutors
    private Map<String,Subject> subjects = new HashMap<>();
    private List<Object> sessionIds = new ArrayList<>();// just use .toString()
    private List<Session> sessions = new ArrayList<>();


    public User(DocumentSnapshot user){
        id = user.getId();
        email = (String) user.getData().get("Email");
        username = (String) user.getData().get("Username");
        role = Role.valueOf((String)user.getData().get("Role"));
        if (role.equals(Role.TUTOR))
            status = Status.valueOf((String)user.getData().get("Status"));
        subjects = flatten2((Map<String, Map<String, Object>>) user.getData().get("Subjects"));
        sessionIds = user.contains("Sessions")? ((List<Object>) user.getData().get("Sessions")): new ArrayList<>();
    }

    public User(String username,String email, Role role,String id, Status status){
        this.username = username;
        this.email = email;
        this.id = id;
        this.role = role;
        this.status = status;
    }

    //Only used at signIn and signUp time
    public User(FirebaseUser user){
        id = user.getUid();
        email = user.getEmail();
        username = user.getDisplayName();
    }

    public void setEmail(String email) {
        this.email = email;
        setChanged();
    }

    public void setRole(Role role) {
        this.role = role;
        setChanged();
    }

    public void setUsername(String username) {
        this.username = username;
        setChanged();
    }

    public void setStatus(Status status) {
        this.status = status;
        setChanged();
    }

    public void setSubjects(Map<String, Subject> subjects) {
        this.subjects = subjects;
    }

    public void addSubject(Subject s){
        if (!subjects.containsKey(s.getId())){
            subjects.put(s.getId(),s);
            setChanged();
        }
    }

    public void removeSubject(Subject s){
        if (subjects.containsKey(s.getId())){
            subjects.remove(s.getId());
            setChanged();
        }
    }

    public void addSession(Session session){
        if (!sessions.contains(session)){
            if (!sessionIds.contains(session.getId()))
                sessionIds.add(session.getId());
            sessions.add(session);
            setChanged();
        }
    }

    public Role getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public String getAvatar() {
        return username;
    }

    public Bitmap getProfile_picture() {
        return profile_picture;
    }

    public void setProfile_picture(Bitmap profile_picture) {
        this.profile_picture = profile_picture;
    }

    public String getUsername() {
        return username;
    }

    public Status getStatus() {
        return status;
    }

    public Map<String, Subject> getSubjects() {
        return subjects;
    }

    public List<Object> getSessionIds() {
        return sessionIds;
    }

    public List<Session> getSessions() {
        return sessions;
    }

    /**
     * Generates the sorted list of subjects names associated with the user and the corresponding sorted alphabet list in a pair
     * @return the sorted list of subjects names associated with the user and the corresponding sorted alphabet list in a pair
     */
    public Pair<List<String>, String[]> getOrderedSubjects() {
        Pair<List<String>, String[]> res;
        List<String> sorted_subject_names = new ArrayList<>();
        SortedSet<String> ordered_subjects_alphabet = new TreeSet<>();
        String temp_subject_name;

        //Iterate over map values only (no need of the associated keys here)
        for (Subject subject_item: subjects.values()) {
            temp_subject_name = subject_item.getName();
            sorted_subject_names.add(temp_subject_name);
            ordered_subjects_alphabet.add(temp_subject_name.substring(0, 1).toUpperCase());
        }

        //Sorting the list of subjects' name by alphabetical order (case sensitive)
        Collections.sort(sorted_subject_names, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        });

        // Convert the ordered_subjects_alphabet tree set to an array (which will be sorted)
        // ordered_subjects_alphabet.toArray(new String[0]);

        res = new Pair<>(sorted_subject_names, ordered_subjects_alphabet.toArray(new String[0]));

        return res;
    }

    public List<String> getSubjectNames(){
        List<String> list = new ArrayList<>();
        if (subjects==null){
            return list;
        }
        for(Map.Entry<String, Subject> entry : subjects.entrySet()) {
            list.add(((Subject)entry.getValue()).getName());
        }
        return list;
    }

    public Map<String, Object> marshal(){
        //the Id must be fetch from the instance, in firestore document ids aren't in the map
        Map<String, Object> user = new HashMap<>();
        user.put("Username",username);
        user.put("Email",email);
        user.put("Role",role.toString());
        if (role.equals(Role.TUTOR))
            user.put("Status", status.toString());
        user.put("Subjects",flatten(subjects));
        user.put("Sessions", (sessionIds));

        return user;
    }

    private Map<String,Object> flatten(Map<String,Subject> map){
        //null check
        if (map==null){
            return new HashMap<>();
        }
        Map<String,Object> newMap = new HashMap<>();
        for(Map.Entry<String, Subject> entry : map.entrySet()) {
            newMap.put(entry.getKey(), ((Subject)entry.getValue()).marshal());
        }
        return newMap;
    }

    //Transform the map of maps to a single map
    //if arg is null returns empty HashMap
    public Map<String,Subject> flatten2(Map<String,Map<String,Object>> map){
        //null check
        if (map==null){
            return new HashMap<>();
        }
        Map<String,Subject> newMap = new HashMap<>();
        for(Map.Entry<String, Map<String,Object>> entry : map.entrySet()) {
            newMap.put(entry.getKey(), new Subject(entry.getKey(),(String) entry.getValue().get("Name")));
        }
        return newMap;
    }

    /**
     * describeContents method for a Parcelable class (in this project, such class(es) has(have) no
     * child classes)
     * @return 0. Parcelable class(es) in this project has(have) no child classes
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Write an object to a parcel
     * @param dest The Parcel in which the object should be written
     * @param flags Additional flags about how the object should be written
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.username);
        dest.writeString(this.email);
        dest.writeString(this.role.toString());
        dest.writeString(this.id);
        dest.writeMap(this.subjects);
        dest.writeList(this.sessionIds);
        dest.writeList(this.sessions);
        //dest.writeString(this.status.toString()); TODO instantiation to avoid null
    }

    /**
     * Regenerate the object from parcel
     */
    public static final Creator<User> CREATOR = new Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    /**
     * Constructor that takes a parcel and construct a populated user object
     * @param in parcel
     */
    private User(Parcel in) {
        username = in.readString();
        email = in.readString();
        role = Role.valueOf(in.readString());
        this.id = in.readString();
        in.readMap(subjects,Subject.class.getClassLoader());
        in.readList(sessionIds,String.class.getClassLoader());
        in.readList(sessions, Session.class.getClassLoader());
        //this.status = Status.valueOf(in.readString()); TODO instantiation to avoid null
    }

    /**
     * Retrieve the first next nb_sessions upcoming sessions' dates for a user
     * @param nb_session number of sessions' dates to retrieve
     * @return the first next nb_sessions upcoming sessions' dates for a user
     */
    public List<Date> getNUpcomingSessionDates(int nb_session) {
        List<Date> dates = new ArrayList<>();
        List<Date> nDates = new ArrayList<>();
        List<Date> session_dates;
        int counter = 0;

        //Transform list of sesions to list of dates occuring in the future (because multiple dates possible for each session)
        for (Session session:sessions) {

            if (session.getStatus().equals(Status.ACCEPTED)) {
                session_dates = session.getDates();
                Date today = GregorianCalendar.getInstance().getTime();

                for (Date date_item : session_dates) {
                    if (date_item.after(today)) {
                        dates.add(date_item);
                    }
                }
            }
        }

        // Order list of sessions' dates(in ascending order)
        Collections.sort(dates);

        //Retrive first N dates
        for (Date d:dates) {
            nDates.add(d);

            counter++;

            if (counter == nb_session-1) {
                break;
            }
        }

        return nDates;
    }

    //Returns list of accepted sessions
    public List<Session> getAcceptedSessions(){
        List<Session> acceptedSessions = new ArrayList<>();
        for (Session session : sessions){
            if (session.getStatus().equals(Status.ACCEPTED))
                acceptedSessions.add(session);
        }
        return acceptedSessions;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof User)
            return ((User) o).id.equals(this.id);
        return false;
    }
}
