package org.redrock.web.chatroom.bean;


public class Message {
    private Integer id;
    private String send_user_name;
    private String info;
    private String receive_user_name;
    private String date;
    private int isAccept;

    @Override
    public String toString() {
        return "["+date+"]"+send_user_name+"---->"+receive_user_name+":"+info;
    }

    public String getReceive_user_name() {
        return receive_user_name;
    }

    public void setReceive_user_name(String receive_user_name) {
        this.receive_user_name = receive_user_name;
    }

    public String getSend_user_name() {
        return send_user_name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setSend_user_name(String send_user_name) {
        this.send_user_name = send_user_name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getIsAccept() {
        return isAccept;
    }

    public void setIsAccept(int isAccept) {
        this.isAccept = isAccept;
    }
}
