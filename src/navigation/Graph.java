package navigation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class Graph<T extends Comparable<? super T>> implements Iterable<Graph.College>{
	private static double maxSpeed = 100; //max speed of any edge
	private static double minSpeed = 10; //min speed of any edge
	private HashMap<T,College> colleges;
	private ArrayList<Edge> edges;
	
	public Graph() {
		colleges = new HashMap<T,College>();
		edges = new ArrayList<Edge>();
	}
	
	public void addCollege(T name, int x, int y, ArrayList<String> connections) {
		colleges.put(name, new College(name,x,y, connections));
	}
	
	public void synthesizeEdges() {
		for(College c : colleges.values()) {
			c.synthesizeEdges();
		}
	}
	
	// add LinkedList<Path> path as a parameter- this will be returned from shortestPath method
	public void paint(Graphics2D g2d, double xScale, double yScale) {
		for(College c : colleges.values()) {
			c.paint(g2d,xScale,yScale);
			
		}
//		paintPath(g2d, path);
	}
	
	public void paintPath(Graphics2D g2d, LinkedList<Path> path) {
		// TODO
	}
	
	/**
	 * This method takes the location of a mouse click and finds the college that was clicked on
	 * @param x location that the mouse was clicked
	 * @param y location that the mouse was clicked
	 * @return name of the college that was clicked on
	 */
	public T getClicked(int x, int y, double xScale, double yScale) {
		double xAdjusted = (x-350.0*xScale)/xScale;
		double yAdjusted = (y-20.0-530.0*yScale)/yScale;
		for(College c : colleges.values()) {
			if(Math.sqrt((c.x-xAdjusted)*(c.x-xAdjusted)+(c.y-yAdjusted)*(c.y-yAdjusted))<5) {
				return c.name;
			}
		}
		return null;
	}
	
	@Override
	public Iterator<Graph.College> iterator() {
		//return new RandomIterator();
		return null;
	}
	
	/**
	 * College Class
	 * 
	 */
	public class College{
		private T name;
		private int x;
		private int y;
		private Image icon;
		private ArrayList<Edge> edges;
		private ArrayList<String> connections;

		public College(T name, int x, int y, ArrayList<String> connections){
			this.x = x;
			this.y = y;
			this.name = name;
			this.edges = new ArrayList<Edge>();
			this.connections = new ArrayList<String>();
			this.connections.addAll(connections);
		}
		
		public void synthesizeEdges() {
			for(String connection : connections) {
				if(!connection.isEmpty()) {
					College other = colleges.get(connection);
					
					//Arbitrary algorithm for making up a fictional speed
					int xDiff = Math.abs(this.x - other.x);
					int yDiff = Math.abs(this.y - other.y);
					double speedLimit = 50+50*((xDiff-yDiff)/(xDiff+yDiff));
					speedLimit = Math.min(speedLimit, maxSpeed);
					speedLimit = Math.max(speedLimit, minSpeed);
					
					edges.add(new Edge(other, speedLimit));
				}
			}
		}
		
		public int straightLineDistance(College otherCollege) {
			int x = this.x - otherCollege.x;
			int y = this.y - otherCollege.y;
			return (int)Math.sqrt(x*x + y*y);
		}
		
		private void paint(Graphics2D g2d, double xScale, double yScale) {
			g2d.setColor(Color.BLACK);
			g2d.fillOval((int) (x*xScale-5),(int) (y*yScale-5), 10, 10);
			g2d.drawString((String) this.name,(int) (x*xScale+3),(int) (y*yScale-3));
			for(Edge e : edges) {
				g2d.drawLine((int) (this.x*xScale), (int) (this.y*yScale), 
						(int) (e.otherCollege.x*xScale), (int) (e.otherCollege.y*yScale));
			}
		}
	}
	
	/**
	 * Edge Class
	 *
	 */
	public class Edge {
		private double speedLimit;
		private College otherCollege;
		
		public Edge(College otherCollege, double speedLimit) {
			this.otherCollege = otherCollege;
			this.speedLimit = speedLimit;
		}

		public double getSpeedLimit() {
			return this.speedLimit;
		}
		
		public College getDestination() {
			return this.otherCollege;
		}
	}
	
	public class Path implements Comparable<Path> {
		
		private College college;
		private College goal;
		private LinkedList<Path> pathTraveled;
		private int distanceTraveled;
		private int cost;
		
		public Path(College current, College goal, int distanceTraveled, LinkedList<Path> pathTraveled) {
			this.pathTraveled = pathTraveled;
			this.college = current;
			this.goal = goal;
			this.distanceTraveled = distanceTraveled;
			this.cost = current.straightLineDistance(goal) + this.distanceTraveled;
		}
		
		public LinkedList<Path> aStarSearch(PriorityQueue<Path> q) {
			College c;
			Path child;
			pathTraveled.add(this);
			for (Edge e : college.edges) {
				c = e.otherCollege;
				if (pathTraveled.isEmpty() || c!= pathTraveled.getLast().college) {
					int distanceToCollege = college.straightLineDistance(c);
					child = new Path(c, goal, distanceToCollege + distanceTraveled, pathTraveled);
					q.add(child);
				}
			}
			Path next = q.poll();
			if (next.college == goal) {
				pathTraveled.add(next);
				return pathTraveled;
			}
			return next.aStarSearch(q);
		}
		
		public T getCollegeName() {
			return college.name;
		}
		
		public int compareTo(Path other) {
			if (this.cost == other.cost) return 0;
			return (this.cost > other.cost) ? 1 : -1;
		}
	}
	
	/**
	 * A* search algorithm for shortest path
	 * 
	 * @param start
	 * @param finish
	 * @return
	 */
	public LinkedList<Path> shortestPath(T start, T finish) {
		PriorityQueue<Path> q = new PriorityQueue<>();
		Path begin = new Path(colleges.get(start), colleges.get(finish), 0, new LinkedList<Path>());
		return begin.aStarSearch(q);
	}
	
//	private class RandomIterator implements Iterator<Graph.College>{
//		ArrayList<College> collegeList;
//		
//		public RandomIterator() {
//			collegeList = new ArrayList<College>();
//			collegeList.addAll(colleges.values());
//		}
//
//		@Override
//		public boolean hasNext() {
//			return !collegeList.isEmpty();
//		}
//
//		@Override
//		public College next() {
//			College temp = collegeList.get(0);
//			collegeList.remove(0);
//			return temp;
//		}
//	}
}
