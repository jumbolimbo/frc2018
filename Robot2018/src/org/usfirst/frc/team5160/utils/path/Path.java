package org.usfirst.frc.team5160.utils.path;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

public class Path {
	private Map<Double, Object> distanceMap = new TreeMap<Double, Object>();
	private double pathLength;
	
	
	public void addPoints(Point... ps){
		double distance = 0;
		Point last = ps[0];
		for(Point p : ps){
			distanceMap.put(distance, p);
			distance = distance + DistanceBetweenPoints(p, last);
			last = p;
		}
		pathLength = distance;
	}
	
	public static double[][] ToDoubleArray(Point[] ps){
		double[][] tmp = new double[ps.length][2];
		for(int i = 0; i < ps.length; i++){
			tmp[i][0] = ps[i].x;
			tmp[i][1] = ps[i].y;
		}
		return tmp;
	}
	
	public static Point[] InjectPoints(Point[] ps, int pointsPerLine){
		Point[] points = new Point[(ps.length-1)*pointsPerLine+1];
		double distance = 0;
		for (int i = 1; i < ps.length; i++){
			Point pA = ps[i-1];
			Point pB = ps[i];
			double step = 1.0 / pointsPerLine; 
			double addX = step*(pB.x-pA.x);
			double addY = step*(pB.y-pA.y);
			for(int j = 0; j < pointsPerLine; j++){
				Point tmp = new Point(j*addX+pA.x, j*addY+pA.y);
				points[pointsPerLine*(i-1)+j] = tmp;
			}
		}
		points[points.length - 1] = new Point(ps[ps.length - 1]);
		return points;
	}
	
	
	
	public static Point[] SmoothPoints(Point[] ps){
		Point[] tmp = new Point[ps.length];
		tmp[0] = ps[0];
		tmp[tmp.length-1] = ps[tmp.length-1];
		for(int i = 1; i < tmp.length - 1; i++){
			tmp[i] = new Point((ps[i-1].x+ps[i+1].x)/2.0, (ps[i-1].y+ps[i+1].y)/2.0);
			
		}
		return tmp;
	}
	
	public Point getNearest(double distance){
		return (Point) findNearest(distanceMap, distance);
	}
	
	private static double DistanceBetweenPoints(Point a, Point b){
		return Math.sqrt(Math.pow(a.x-b.x, 2) + Math.pow(a.y-b.y, 2));
	}
	
	private static Object findNearest(Map<Double, Object> map, double value) {
	    Map.Entry<Double, Object> previousEntry = null;
	    
	    for (Entry<Double, Object> e : map.entrySet()) {
	        if (e.getKey() >= value) {
	            if (previousEntry == null) {
	                return e.getValue();
	            } else {
	                if (e.getKey() - value >= value - previousEntry.getKey()) {
	                    return previousEntry.getValue();
	                } else {
	                    return e.getValue();
	                }
	            }
	        }
	        previousEntry = e;
	    }
	    return previousEntry.getValue();
	}
	
	public double getLength(){
		return pathLength;
	}
	
}
