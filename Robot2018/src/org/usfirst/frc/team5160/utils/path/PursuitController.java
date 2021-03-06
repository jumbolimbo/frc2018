package org.usfirst.frc.team5160.utils.path;
import java.awt.Color;
import java.util.ArrayList;

import org.chargers.frc2018.Constants;
import org.usfirst.frc.team5160.utils.BasicPID;
import org.usfirst.frc.team5160.utils.TrajectoryPID;


public class PursuitController {
	//Proportional control factors
	public double Ka = Constants.kTrajectoryKalpha;
	public double Kb = Constants.kTrajectoryKbeta;
	public double Kp = Constants.kTrajectoryKp;
	public double Kvel = Constants.kTrajectoryKv;
	public double Kacc = Constants.kTrajectoryKa;
	public double Kd = Constants.kTrajectoryKd; 
	public double Lf = Constants.kTrajectoryLookAhead;
	public double La = Constants.kTrajectoryAccelerationDistance;
	private Path path;  //Path for the robot to follow
	private double wheelBase = Constants.kTrackWidth; //Length of the wheelbase
	private TrajectoryPID pid = new TrajectoryPID(Kp, 0.0, Kd, Kvel, Kacc, La); 
	public PursuitController(Path path){		
		this.path = path;
	}
	
	private Point getTargetPoint(double robot_distance){
		return path.getNearest(robot_distance+Lf);
	}
	
	private double[] update(Point robot, Point target){
		/*
		double targetVelocity = 0;
		if(path.getLength() > 2*La){
			//Accelerating
			if(target.distance < La){
				targetVelocity = target.distance / La;
			}
			//Holding top speed
			else if(target.distance > La && target.distance < path.getLength() - La){
				targetVelocity = 1;
			}
			//Deccelerating
			else{
				targetVelocity = (path.getLength() - target.distance) / La;
			}
		}
		else{
			double split = path.getLength() / 2.0;
			//Accelerating
			if(path.getLength() < split){
				targetVelocity = target.distance * (split/La) / split;
			}
			//Deccelerating
			else{
				targetVelocity = (split/La)  - (path.getLength() - target.distance) * (split/La) / La;
			}
		}*/
		double alpha = Path.AngleBetweenPoints(robot, target) - Math.toRadians(robot.angle);
		alpha = Ka * Math.atan2(2.0 * wheelBase * Math.sin(alpha) / Lf, 1.0);
		double beta = Kb * Math.atan2(2.0 * wheelBase * Math.sin(Math.toRadians(robot.angle - target.angle)) / Lf, 1.0);
		double delta_angle = alpha + beta;
		double delta_speed = pid.runPID(robot.distance, 0, path.getLength());//Kp*(Path.DistanceBetweenPoints(robot, target)) + Kv * (targetVelocity - robot.velocity/robotTopSpeed);
		System.out.println("Delta angle"+delta_angle);
		return new double[]{delta_speed, delta_angle};
	}
	
	/**
	 * 
	 * @param robot  A point containing the location, speed and angle of the robot
	 * @param distance  The distance that the robot has traversed so far
	 * @return An array containing the desired forward and angular power
	 */
	public double[] getDrive(Point robotPose){
		Point target = getTargetPoint(robotPose.distance);
		return update(robotPose, target);
	}
	
	/**
	 * 
	 * @param distance The distance the robot has traversed
	 * @return True if the robot has fully traversed the path. 
	 */
	public boolean isFinished(Point robotPose){
		return robotPose.distance >= path.getLength() || Path.DistanceBetweenPoints(robotPose, path.getNearest(path.getLength())) < 6;
	}
	
	/*public static void main(String[] args){
		long time = System.currentTimeMillis();
		 Point[] ps = {
			new Point(0,0),new Point(0,30), new Point(30,60), new Point(120, 60), new Point(120,120)
		 };
		 
	  	
	  	
	  	Path path = new Path();
	  	ps = Path.InjectPoints(ps, 5);
	  	ps = Path.SmoothPoints(ps);
	  	ps = Path.SmoothPoints(ps);
	  	ps = Path.InjectPoints(ps, 5);
	  	ps = Path.SmoothPoints(ps);
	  	ps = Path.SmoothPoints(ps);
	  	path.addPoints(ps);
	  	
	  	double rx = 0;
	  	double ry = 0;
	  	double rv = 0;
	  	double ra = 0;
	  	double rd = 0;
	  	double dt = 0.1;
	  	double rl = 24;
	  	double[][] points = new double[200][2]; 
	  	PursuitController pc = new PursuitController(path, rl, 30);
	  	for(int i = 0; i < 200; i++){
	  		points[i][0] = rx;
	  		points[i][1] = ry;
	  		double[] res = pc.getDrive(new Point(rx, ry, ra, rv), rd);
	  		
	  		double dx =  rv * Math.cos(ra) * dt;
	  		double dy =  rv * Math.sin(ra) * dt;
	  		
	  		rx = rx + dx;
	  		ry = ry + dy;
	  		ra = ra + rv / rl * Math.tan(res[1]) * dt;
	  		rv = rv + res[0] * dt;
	  		rd += Math.sqrt(dx*dx + dy*dy);
	  	}
	  	System.out.println(System.currentTimeMillis()-time);
	  	FalconLinePlot fig2 = new FalconLinePlot(Path.ToDoubleArray(ps), Color.RED, Color.RED);
	  	fig2.addData(points, Color.blue);
	}*/
	
}
