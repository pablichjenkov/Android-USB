package com.soft305.statemachine;

import java.util.List;

/**
 * Created by pablo on 5/14/17.
 */
public abstract class State {

    List<State> mNextStateList;

    public abstract void resolveNext();


}
