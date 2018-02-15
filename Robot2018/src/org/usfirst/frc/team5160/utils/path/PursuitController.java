package org.usfirst.frc.team5160.utils.path;
import java.awt.Color;
import java.util.ArrayList;

import org.usfirst.frc.team5160.utils.BasicPID;

public class PursuitController {
	public static double Kp = 0.06, Ka = 0.0, Kb = 0.2, Kv = 0.35; //Proportional control factors
	public static double Lf = 10, La = 64;
	private Path path;  //Path for the robot to follow
	private double robotLength = 30;
	private double robotTopSpeed = 0;
	public PursuitController(Path path, double robotLength, double robotTopSpeed){
		this.path = path;
		this.robotLength = robotLength;
		this.robotTopSpeed = robotTopSpeed;
	}
	
	private Point getTargetPoint(double robot_distance){
		return path.getNearest(robot_distance+Lf);
	}
	
	private double[] update(Point robot, Point target){
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
		}
		double alpha = Path.AngleBetweenPoints(robot, target) - Math.toRadians(robot.angle);
		alpha = Ka * Math.atan2(2.0 * robotLength * Math.sin(alpha) / Lf, 1.0);
		double beta = Kb * Math.atan2(2.0 * robotLength * Math.sin(Math.toRadians(robot.angle - target.angle)) / Lf, 1.0);
		double delta_angle = alpha + beta;
		double delta_speed = Kp*(Path.DistanceBetweenPoints(robot, target)) + Kv * (targetVelocity - robot.velocity/robotTopSpeed);
		//System.out.println(Path.DistanceBetweenPoints(robot, target)+", "+Kv * (targetVelocity - robot.velocity/robotTopSpeed));
		//System.out.println(Path.DistanceBetweenPoints(robot, target)+", "+ (robot.angle) + ", " + target.angle + ", " + robot.distance + ", "+ target.distance +", "+robot.velocity);
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
		//System.out.println("target : " + target.x + ", " + target.y);
		//System.out.println("robot  : " + robotPose.x + ", " + robotPose.y);
		//System.out.println("error  : " + (target.x-robotPose.x) + ", " + (target.y - robotPose.y));
		//System.out.println();
		return update(robotPose, target);
	}
	
	/**
	 * 
	 * @param distance The distance the robot has traversed
	 * @return True if the robot has fully traversed the path. 
	 */
	public boolean isFinished(double distance){
		return distance >= path.getLength();
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