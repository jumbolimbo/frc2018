package org.usfirst.frc.team5160.utils.path;
import java.awt.Color;
import java.util.ArrayList;

import org.usfirst.frc.team5160.utils.BasicPID;

public class PursuitController {
	private double Kp = 1;
	private double Lf = 8;
	private Path path; 
	private BasicPID pid;
	private double length = 0;
	private double speed = 0;
	public PursuitController(Path path, double robotLength, double speed){
		this.path = path;
		this.pid = pid;
		this.length = robotLength;
		this.speed = speed;
	}
	
	private Point getTargetPoint(double robot_distance){
		return path.getNearest(robot_distance+Lf);
	}
	
	private double[] update(Point robot, Point target){
		
		double alpha = Math.atan2(target.y - robot.y, target.x - robot.x) - robot.angle;
		
		double delta_angle = Math.atan2(2.0 * length * Math.sin(alpha) / Lf, 1.0);
		double delta_speed = Kp*(speed-robot.velocity); 
		return new double[]{delta_speed, delta_angle};
	}
	public double[] getDrive(Point robot, double distance){
		Point target = getTargetPoint(distance);
		return update(robot, target);
	}
	
	public static void main(String[] args){
		long time = System.currentTimeMillis();
		 Point[] ps = {
			new Point(0,0), new Point(50,50), new Point(200, 0), new Point(0,0), new Point(100,200)
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
	  	double ra = 0.5;
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
	}
	
}
