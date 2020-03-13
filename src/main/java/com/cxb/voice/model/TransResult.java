package com.cxb.voice.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class TransResult implements Serializable {
    private static final long serialVersionUID = -5226375480700568499L;

    @Getter @Setter
    private String startTime;

    @Getter @Setter
    private String endTime;

    @Getter @Setter
    private String result;
}
