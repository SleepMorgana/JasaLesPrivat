package developer.aulia.jasalesprivat.chat;

import android.os.Parcel;
import android.os.Parcelable;

public class Chatroom implements Parcelable {

    private String email;
    private String chatroom_id;


    public Chatroom(String email, String chatroom_id) {
        this.email = email;
        this.chatroom_id = chatroom_id;
    }

    public Chatroom() {

    }

    protected Chatroom(Parcel in) {
        email = in.readString();
        chatroom_id = in.readString();
    }

    public static final Creator<Chatroom> CREATOR = new Creator<Chatroom>() {
        @Override
        public Chatroom createFromParcel(Parcel in) {
            return new Chatroom(in);
        }

        @Override
        public Chatroom[] newArray(int size) {
            return new Chatroom[size];
        }
    };

    public String getTitle() {
        return email;
    }

    public void setTitle(String email) {
        this.email = email;
    }

    public String getChatroom_id() {
        return chatroom_id;
    }

    public void setChatroom_id(String chatroom_id) {
        this.chatroom_id = chatroom_id;
    }

    @Override
    public String toString() {
        return "Chatroom{" +
                "email='" + email + '\'' +
                ", chatroom_id='" + chatroom_id + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(chatroom_id);
    }
}
