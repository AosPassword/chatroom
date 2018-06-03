package org.redrock.web.chatroom.bean;

public class Relation {
    private int id;
    private int send_invitation_id;
    private int accept_invitation_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSend_invitation_id() {
        return send_invitation_id;
    }

    public void setSend_invitation_id(int send_invitation_id) {
        this.send_invitation_id = send_invitation_id;
    }

    public int getAccept_invitation_id() {
        return accept_invitation_id;
    }

    public void setAccept_invitation_id(int accept_invitation_id) {
        this.accept_invitation_id = accept_invitation_id;
    }

    @Override
    public String toString() {
        return "Relation{" +
                "id=" + id +
                ", send_invitation_id=" + send_invitation_id +
                ", accept_invitation_id=" + accept_invitation_id +
                '}';
    }
}
