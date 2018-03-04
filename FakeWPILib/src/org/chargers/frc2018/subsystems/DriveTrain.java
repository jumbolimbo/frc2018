package org.chargers.frc2018.subsystems;

import com.ctre.phoenix.motorcontrol.NeutralMode;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SPI.Port;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import fake.wpilib.MetaRobot;
import edu.wpi.first.wpilibj.Timer;

import org.chargers.frc2018.OI;
import org.chargers.frc2018.RobotMap;
import org.usfirst.frc.team5160.utils.BasicPID;
import org.usfirst.frc.team5160.utils.RMath;

public class DriveTrain extends Subsystem {
	private MecanumDrive robotDrive;
	private double posX = 0, posY = 0, speed = 0;
	private double lastEncoderDistance;
	private static final double TICK_TO_INCH = 6.0*Math.PI/256.0;//256 ticks per rev, 6 inch diameter wheels
	private Timer timeSinceLastDrive = new Timer();
	private boolean fieldOriented = true;
	
	private double desiredRotation = 90; 
	private BasicPID turnPID = new BasicPID(0.05, 0.0, 0.5);
	
	public WPI_TalonSRX frontRight; 
	public WPI_TalonSRX backRight;
	public WPI_TalonSRX frontLeft;
	public WPI_TalonSRX backLeft;
	public Encoder leftEncoder;
	public Encoder rightEncoder;
	public ADXRS450_Gyro gyro;
	
	@Override
	public void robotInit() {
		//Initialize motors on the CAN bus 
		frontRight = new WPI_TalonSRX(RobotMap.FRONT_RIGHT_CIM);
		backRight = new WPI_TalonSRX(RobotMap.BACK_RIGHT_CIM);
		frontLeft = new WPI_TalonSRX(RobotMap.FRONT_LEFT_CIM);
		backLeft = new WPI_TalonSRX(RobotMap.BACK_LEFT_CIM);
		
		//Configure motors
		configureMotor(frontLeft);
		configureMotor(frontRight);
		configureMotor(backLeft);
		configureMotor(backRight);
		
		//Configure Greyhill encoders, plugged into the RIO
		leftEncoder = new Encoder(RobotMap.LEFT_ENCODER_CHANNEL_A, RobotMap.LEFT_ENCODER_CHANNEL_B, false, EncodingType.k4X);
		rightEncoder = new Encoder(RobotMap.RIGHT_ENCODER_CHANNEL_A, RobotMap.RIGHT_ENCODER_CHANNEL_B, false, EncodingType.k4X);
		
		//Configure the Analog Device Gyro, on the RIO
		gyro = new ADXRS450_Gyro();
	}

	@Override
	public void autoInit() {
		this.reset(); //Ensure that sensors are zeroed and ready for auto
	}

	@Override
	public void autoPeriodic() {
		//Motor safety feature, stops the robot if the motor output has not been updated
		if(this.timeSinceLastDrive.get()>0.075){
			this.mecanumDrive(0, 0, 0);
		}
	}

	@Override
	public void teleopPeriodic() {
		//if(this.timeSinceLastDrive.get()>0.1){
		//	this.mecanumDrive(0, 0, 0);
		//}
		if(fieldOriented){
			this.mecanumDriveField(OI.getJoystickY(), OI.getJoystickX(), OI.getJoystickAngle());
		}
		else{
			this.mecanumDrive(OI.getJoystickY(), OI.getJoystickX(), OI.getJoystickRotation());
		}
		
	}

	@Override
	public void stop() {

	}
	
	public double getPositionX(){
		return this.posX;
	}
	
	public double getPositionY(){
		return this.posY;
	}
	
	public double getAngle(){
		return -gyro.getAngle()+90;
	}
	
	public double getSpeed(){
		return speed;
	}
	
	public void reset(){
		//Reset the gyro, drive time, encoders and speed 
		timeSinceLastDrive.reset();
		timeSinceLastDrive.start();
		leftEncoder.reset();
		rightEncoder.reset();
		speed = 0;
		gyro.reset();
		
	}
	/***
	 * ChargerCanum field oriented drive and rotation
	 * @param forwards The amount of movement in the y-axis direction, relative to the field, input in the range of -1 to 1
	 * @param sideways The amount of movement in the x-axis direction, relative to the field, input in the range of -1 to 1
	 * @param desired_angle The desired angle in degrees to rotate to in degrees, relative to the field -- 90 is straight ahead
	 */
	public void mecanumDriveField(double forwards, double sideways, double desired_angle){
		System.out.println(this.getAngle()+", "+ desired_angle);
		
		//Finds the error in angle between the robot and the target. Returns a value in degrees, and finds whether to rotate clockwise or counter clockwise
		double angle_error = Math.toDegrees(Math.asin(Math.sin(Math.toRadians(this.getAngle())- Math.toRadians(desired_angle))));
		
		double rotation = -turnPID.runPID(angle_error, 0);//Plug the error into PID to get to the desired angle
		 
		//Field oriented drive power changing
		double temp    =  forwards*Math.sin(Math.toRadians(this.getAngle())) + sideways*Math.cos(Math.toRadians(this.getAngle())); 
		sideways   =  -forwards*Math.cos(Math.toRadians(this.getAngle())) + sideways*Math.sin(Math.toRadians(this.getAngle())); 
		forwards =  temp; 
		 
		//Normalize power so that no motor has more than 100% power. 
		double[] tmp = RMath.normalizeThree(forwards, sideways, rotation);
		forwards = tmp[0];
		sideways = tmp[1];
		rotation = tmp[2]; 
		
		frontLeft.set(forwards - sideways - rotation);
		backLeft.set(forwards + sideways - rotation);
		frontRight.set(forwards - sideways + rotation);
		backRight.set(forwards + sideways + rotation);
	}
	
	/***
	 * Robot oriented typical mecanum drive
	 * @param forwards The amount of movement in the y-axis direction, relative to the robot, input in the range of -1 to 1. Higher values will work but won't lead to faster speeds
	 * @param sideways The amount of movement in the x-axis direction, relative to the robot, input in the range of -1 to 1. Higher values will work but won't lead to faster speeds
	 * @param rotation The amount of power in rotation, relative to the robot, input in the range of -1 to 1. Higher values will work but won't lead to faster speeds
	 */
	public void mecanumDrive(double forwards, double sideways, double rotation){
		forwards = forwards*1;
		rotation = rotation * 0.6;
		
		double[] tmp = RMath.normalizeThree(forwards, sideways, rotation);
		forwards = tmp[0];
		sideways = tmp[1];
		rotation = tmp[2];
		
		frontLeft.set(forwards - sideways - rotation);
		backLeft.set(forwards + sideways - rotation);
		frontRight.set(forwards - sideways + rotation);
		backRight.set(forwards + sideways + rotation);
		
		//Update the x-y position of the robot for auto
		double deltaTime = timeSinceLastDrive.get();
		double encoderDistance = (leftEncoder.get() * TICK_TO_INCH + rightEncoder.get() * TICK_TO_INCH) / 2.0;
		double deltaDistance = encoderDistance - lastEncoderDistance;
		lastEncoderDistance = encoderDistance;
		
		double deltaX = deltaDistance * Math.cos(Math.toRadians(this.getAngle()));
		double deltaY = deltaDistance * Math.sin(Math.toRadians(this.getAngle()));
		this.posX = this.posX + deltaX;
		this.posY = this.posY + deltaY;
		this.speed = deltaDistance/deltaTime;
		timeSinceLastDrive.reset();
	}
	
	private void configureMotor(TalonSRX motor){
		//Configure the motor for driving and to limit excessive power draw. 
		motor.clearStickyFaults(0);
		motor.configOpenloopRamp(0.2, 100);
		motor.enableCurrentLimit(true);
		motor.configContinuousCurrentLimit(60, 100);
		motor.configPeakCurrentDuration(2000, 100);
		motor.configPeakCurrentLimit(85, 100);
		motor.setNeutralMode(NeutralMode.Brake);
	}

	public void setPosition(double x, double y) {
		this.posX = x;
		this.posY = y;
	}
	
}
