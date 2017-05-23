package com.soft305.statemachine;

import java.util.List;

/**
 * Created by pablo on 5/14/17.
 */
public class StateMachine {


    private State mCurState;
    private List<Input> mInputList;
    private Output mOutput;

    public Input getInput() {
        return new Input();
    }

    public Input getInputByClass(Class<? extends Input> inputClass) {
        return new Input();
    }



}
