package com.shdcec.alarmball.ball;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Ball implements Serializable {
    private String ballTel;        //手机号码
    private String ballPos;        //位置
    private String ballState;    //状态
    private String ballDistance;//距离

    public Ball(String ballTel, String ballPos, String ballState, String ballDistance) {
        this.ballTel = ballTel;
        this.ballPos = ballPos;
        this.ballState = ballState;
        this.ballDistance = ballDistance;
    }

    public Ball() {
    }

    public void setBallTel(String ballTel) {
        this.ballTel = ballTel;
    }

    public String getBallTel() {
        return ballTel;
    }

    public void setBallPos(String ballPos) {
        this.ballPos = ballPos;
    }

    public String getBallPos() {
        return ballPos;
    }

    public void setBallState(String ballState) {
        this.ballState = ballState;
    }

    public String getBallState() {
        return ballState;
    }

    public void setBallDistance(String ballDistance) {
        this.ballDistance = ballDistance;
    }

    public String getBallDistance() {
        return ballDistance;
    }
}
