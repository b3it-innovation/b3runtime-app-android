package com.b3.development.b3runtime.ui.competition;

public interface ListItem {
    int TYPE_COMPETITION = 101;
    int TYPE_TRACK = 102;

    int getType();

    String getName();

    String getKey();
}
