package com.ctre.phoenix.motorcontrol.can;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.SensorCollection;

public class TalonSRX {
	double encoder = 0;
	public TalonSRX(int eLEVATOR_LEFT_775) {
		// TODO Auto-generated constructor stub
	}
	public void setEncoder(double d){
		encoder = d;
	}
	public SensorCollection getSensorCollection() {
		// TODO Auto-generated method stub
		return new SensorCollection(encoder);
	}

	public void configOpenloopRamp(double d, int i) {
		// TODO Auto-generated method stub
		
	}

	public void enableCurrentLimit(boolean b) {
		// TODO Auto-generated method stub
		
	}

	public void configContinuousCurrentLimit(int i, int j) {
		// TODO Auto-generated method stub
		
	}

	public void setNeutralMode(NeutralMode brake) {
		// TODO Auto-generated method stub
		
	}

	public void configPeakCurrentLimit(int i, int j) {
		// TODO Auto-generated method stub
		
	}

	public void configPeakCurrentDuration(int i, int j) {
		// TODO Auto-generated method stub
		
	}

	public void clearStickyFaults(int i) {
		// TODO Auto-generated method stub
		
	}

}
