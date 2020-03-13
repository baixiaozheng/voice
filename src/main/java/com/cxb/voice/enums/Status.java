package com.cxb.voice.enums;

public enum Status {
    WAITING() {
        @Override
        public String toString() {
            return "待查询";
        }
    },
    PROGRESS() {
        @Override
        public String toString() {
            return "查询中";
        }
    },
    COMPLETE() {
        @Override
        public String toString() {
            return "已完成";
        }
    },
}
