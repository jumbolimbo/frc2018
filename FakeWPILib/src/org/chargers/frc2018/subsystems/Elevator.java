package org.chargers.frc2018.subsystems;

import org.chargers.frc2018.OI;
import org.chargers.frc2018.RobotMap;
import org.usfirst.frc.team5160.utils.TrajectoryPID;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;

public class Elevator extends Subsystem {

	public enum ELEVATOR_STATE {
		ZERO, SWITCH, SCALE, CLIMB, OTHER
	};

	private final double[] HEIGHTS = { 0, 18, 70, 70, 0 }; // Corresponds with
															// ELEVATOR STATES

	public WPI_TalonSRX leftMotor;
	public WPI_TalonSRX rightMotor;
	//public Encoder encoder;

	public TrajectoryPID pid;
	public static double p = 0.2, i = 0.0, d = 4, v = 0.0, a = 0.0;

	//private DigitalInput upperLimitSwitch;
	//private DigitalInput lowerLimitSwitch;
	private double targetHeight = 0;
	private double startHeight = 0;
	private static final double TICK_TO_INCH = 1.5 * Math.PI / 1024.0; // 1024
																		// ticks
																		// per
																		// revolution,
																		// 2"
																		// diameter
																		// wheel,
																		// no
																		// other
																		// reduction

	@Override
	public void robotInit() {
		leftMotor = new WPI_TalonSRX(RobotMap.ELEVATOR_LEFT_775);
		rightMotor = new WPI_TalonSRX(RobotMap.ELEVATOR_RIGHT_775);
		//encoder = new Encoder(RobotMap.ELEVATOR_ENCODER_CHANNEL_A, RobotMap.ELEVATOR_ENCODER_CHANNEL_B, false,
		//		EncodingType.k4X);

		configureMotor(leftMotor);
		configureMotor(rightMotor);

	//	upperLimitSwitch = new DigitalInput(RobotMap.UPPER_SWITCH_CHANNEL);
	//	lowerLimitSwitch = new DigitalInput(RobotMap.LOWER_SWITCH_CHANNEL);

		pid = new TrajectoryPID(p, i, d, v, a, 24);
	}

	@Override
	public void autoInit() {
	//	encoder.reset();
	}

	@Override
	public void autoPeriodic() {
		//setPower(pid.runPID(getHeightInches(), startHeight, targetHeight));
	}

	@Override
	public void teleopPeriodic() {
		/*
		if (OI.getElevatorMoveUp()) {
			//this.setTarget(this.nextStateUp(getTargetState()));
		} else if (OI.getElevatorMoveDown()) {
			//this.setTarget(this.nextStateDown(getTargetState()));
		}*/
		if (Math.abs(OI.getElevatorPower()) < 0.2) {
			setPower(0);
		//	setPowerSafe(pid.runPID(getHeightInches(), startHeight, targetHeight));
		} else {
			setPower(OI.getElevatorPower());
		}
	}

	public double getHeightInches() {
		return 15;//encoder.get() * TICK_TO_INCH;
	}

	public boolean atLowerLimit() {
		return //lowerLimitSwitch.get() || 
				getHeightInches() <= 0;
	}

	public boolean atUpperLimit() {
		return //upperLimitSwitch.get() || 
				getHeightInches() >= 100;
	}

	private void setPowerSafe(double power) {
		power *= 0.33;
		/*if (power < 0 && atLowerLimit()) {
			power = 0;
		}
		if (power > 0 && atUpperLimit()) {
			power = 0;
		}*/
		if(power < 0){
			power*=0.1;
			leftMotor.setNeutralMode(NeutralMode.Brake);
			rightMotor.setNeutralMode(NeutralMode.Brake);
		}
		else{
			leftMotor.setNeutralMode(NeutralMode.Brake);
			rightMotor.setNeutralMode(NeutralMode.Brake);
		}
		leftMotor.set(power);
		rightMotor.set(power);
	}

	private void setPower(double power) {
		power *= 0.8;
		leftMotor.set(power);
		rightMotor.set(power);
	}

	public ELEVATOR_STATE getCurrentState() {
		double height = getHeightInches();

		if (Math.abs(height - HEIGHTS[0]) < 3) {
			return ELEVATOR_STATE.ZERO;
		} else if (Math.abs(height - HEIGHTS[1]) < 5) {
			return ELEVATOR_STATE.SWITCH;
		} else if (Math.abs(height - HEIGHTS[2]) < 5) {
			return ELEVATOR_STATE.SCALE;
		} else if (Math.abs(height - HEIGHTS[3]) < 5) {
			return ELEVATOR_STATE.CLIMB;
		}

		return ELEVATOR_STATE.OTHER;
	}

	public ELEVATOR_STATE getTargetState() {
		double height = targetHeight;

		if (Math.abs(height - HEIGHTS[0]) < 3) {
			return ELEVATOR_STATE.ZERO;
		} else if (Math.abs(height - HEIGHTS[1]) < 5) {
			return ELEVATOR_STATE.SWITCH;
		} else if (Math.abs(height - HEIGHTS[2]) < 5) {
			return ELEVATOR_STATE.SCALE;
		} else if (Math.abs(height - HEIGHTS[3]) < 5) {
			return ELEVATOR_STATE.CLIMB;
		}

		return ELEVATOR_STATE.OTHER;
	}

	public void setTarget(double target) {
		startHeight = getHeightInches();
		if (target < 0) {
			this.targetHeight = 0;
		} else {
			this.targetHeight = target;
		}
	}

	public static ELEVATOR_STATE nextStateUp(ELEVATOR_STATE state) {
		if (state == ELEVATOR_STATE.ZERO) {
			return ELEVATOR_STATE.SWITCH;
		} else if (state == ELEVATOR_STATE.SWITCH) {
			return ELEVATOR_STATE.SCALE;
		} else if (state == ELEVATOR_STATE.SCALE) {
			return ELEVATOR_STATE.SCALE;
		}
		return ELEVATOR_STATE.ZERO; // Backup behavior is to set elevator to
									// zero
	}

	public static ELEVATOR_STATE nextStateDown(ELEVATOR_STATE state) {
		if (state == ELEVATOR_STATE.ZERO) {
			return ELEVATOR_STATE.ZERO;
		} else if (state == ELEVATOR_STATE.SWITCH) {
			return ELEVATOR_STATE.ZERO;
		} else if (state == ELEVATOR_STATE.SCALE) {
			return ELEVATOR_STATE.SWITCH;
		}
		return ELEVATOR_STATE.ZERO;
	}

	public void setTarget(ELEVATOR_STATE target) {
		if (target == ELEVATOR_STATE.ZERO) {
			setTarget(HEIGHTS[0]);
		} else if (target == ELEVATOR_STATE.SWITCH) {
			setTarget(HEIGHTS[1]);
		} else if (target == ELEVATOR_STATE.SCALE) {
			setTarget(HEIGHTS[2]);
		} else if (target == ELEVATOR_STATE.CLIMB) {
			setTarget(HEIGHTS[3]);
		} else {
			setTarget(HEIGHTS[0]); // Set to Zero by default
		}
	}

	private void configureMotor(TalonSRX motor) {
		motor.configOpenloopRamp(0.2, 100);
		motor.enableCurrentLimit(true);
		motor.configContinuousCurrentLimit(40, 100);
		motor.configPeakCurrentDuration(300, 100);
		motor.configPeakCurrentLimit(60, 100);
		motor.setNeutralMode(NeutralMode.Brake);
	}

}
