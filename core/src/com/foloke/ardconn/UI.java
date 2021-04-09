package com.foloke.ardconn;

public interface UI {
    void output(String string);

    void showOnWall(String string);
    void block();
    void unblock();
    void askForHit();
}
