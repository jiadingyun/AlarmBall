package com.shdcec.alarmball.ball;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Ball implements Serializable
{
	private String balltel;		//手机号码
	private String ballpos;		//位置
	private String ballstate;	//状态
	private String balldistance;//距离
	public Ball(String balltel, String ballpos, String ballstate, String balldistance)
	{
		this.balltel = balltel;
		this.ballpos = ballpos;
		this.ballstate = ballstate;
		this.balldistance = balldistance;
	}
	public Ball()
	{
	}
	public void setballtel(String balltel)
	{
		this.balltel = balltel;
	}
	public String getballtel()
	{
		return balltel;
	}
	public void setballpos(String ballpos)
	{
		this.ballpos = ballpos;
	}
	public String getballpos()
	{
		return ballpos;
	}
	public void setballstate(String ballstate)
	{
		this.ballstate = ballstate;
	}
	public String getballstate()
	{
		return ballstate;
	}
	public void setballdistance(String balldistance)
	{
		this.balldistance = balldistance;
	}
	public String getballdistance()
	{
		return balldistance;
	}
}
