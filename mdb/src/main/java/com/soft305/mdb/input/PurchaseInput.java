package com.soft305.mdb.input;


public class PurchaseInput {


    public enum Action {Purchase, Cancel}

    public Action action;

    public PurchaseInput(Action action) {
        this.action = action;
    }

}
