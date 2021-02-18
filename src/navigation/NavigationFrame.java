package navigation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import navigation.Graph.Path;

/**
 * This class implements the GUI for the navigation of the graph and handles the listeners
 * 
 * @author snyderc1
 *
 */
public class NavigationFrame extends JFrame {
	private Graph<String> graph;
	private BufferedImage indiana;
	private Dimension screenSize;
	private double xScale; //The original program was based on a 1080 x 1920 screen
	private double yScale; //The content needs to be scaled to fit any screen
	private String currentDest;
	private String currentSrc;
	

	/** Auto Generated serialVersionUID */
	private static final long serialVersionUID = 1185712731956834898L;

	public NavigationFrame() throws IOException {
		currentDest = "Rose-Hulman";
		currentSrc = "Rose-Hulman";
		
		//Get the Screen Size and make scale variables to handle screen scaling
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setMinimumSize(screenSize);
		xScale = screenSize.getWidth()/1920.0;
		yScale = screenSize.getHeight()/1080.0;
		getContentPane().setBackground(Color.GRAY);
		
		//Draw image of Indiana
		indiana = ImageIO.read(new File("pics/indiana.png"));
		
		//Make the graph and import the colleges from the csv
		graph = new Graph<String>();
		ArrayList<String> names = importColleges();
		
		//Info Panel Setup
		JPanel infoPanel = new JPanel();
		this.add(infoPanel,BorderLayout.EAST);
		
		infoPanel.setBackground(Color.GRAY);
		infoPanel.setLayout(new FlowLayout());
		infoPanel.setPreferredSize(new Dimension(600,5000));
		
		JPanel title = new JPanel();
		title.setBackground(Color.GRAY);
		
		JLabel label0 = new JLabel("Indiana College Navigator");
		label0.setFont(new Font("Cooper Black",1,40));
		title.add(label0);
		infoPanel.add(title);
		
		JPanel selection = new JPanel();
		selection.setBackground(Color.GRAY);
		
		JLabel label1 = new JLabel("Source College");
		JLabel label2 = new JLabel("Destination College");
		
		JComboBox src = new JComboBox(names.toArray());
		src.setSelectedItem(currentSrc);
		src.setToolTipText("Select the college you are coming from");
		src.setMaximumSize(new Dimension(1000,20));
		src.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentSrc = String.valueOf(src.getSelectedItem());
			}
		});
		JComboBox dest = new JComboBox(names.toArray());
		dest.setSelectedItem(currentDest);
		dest.setToolTipText("Select the college you are going to");
		dest.setMaximumSize(new Dimension(1000,20));
		dest.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentDest = String.valueOf(dest.getSelectedItem());
			}
		});
		
		GroupLayout layout = new GroupLayout(selection);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(label1)
						.addComponent(src))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(label2)
						.addComponent(dest)));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(label1)
						.addComponent(label2))
				.addGroup(
						layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(src)
						.addComponent(dest)));
		selection.setLayout(layout);
		infoPanel.add(selection);
		
		JPanel GOButton = new JPanel();
		JButton GO = new JButton("GO!");
		GOButton.setMaximumSize(new Dimension(100,50));
		GOButton.add(GO);
		//GO.setPreferredSize(new Dimension(10,50));
		GO.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		infoPanel.add(GOButton);
		
		//Add the mouse listener to handle clicking on colleges
		this.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Object clicked = graph.getClicked(e.getX(), e.getY(), xScale, yScale);
				if(clicked != null) {
					if(SwingUtilities.isLeftMouseButton(e)) {
						currentSrc = clicked.toString();
						src.setSelectedItem(currentSrc);
					} else if(SwingUtilities.isRightMouseButton(e)) {
						currentDest = clicked.toString();
						dest.setSelectedItem(currentDest);
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseReleased(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
		});
		
//		this.pack();
//		this.setVisible(true);

		testAStar("Trine University", "USI", false);
	}
	
	private ArrayList<String> importColleges() throws FileNotFoundException {
		Scanner s = new Scanner(new File("lib/colleges.csv"));
		ArrayList<String> lines = new ArrayList<String>();
		ArrayList<String> namesList = new ArrayList<String>();
		
		while(s.hasNext()) lines.add(s.nextLine());
		
		for(int i = 0;i<lines.size();i++) {
			String str[] = lines.get(i).split(",");
			String name = str[0];
			namesList.add(name);
			int x = Integer.parseInt(str[1]);
			int y = Integer.parseInt(str[2]);
			ArrayList<String> connections = new ArrayList<String>();
			for(int j = 3; j< str.length; j++) {
				connections.add(str[j]);
			}
			graph.addCollege(name, x, y,connections);
		}
		graph.synthesizeEdges();
		s.close();
		return namesList;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		//This is where we draw all the images for the nodes and draw all the edges if they are visible
		
		//Color the background gray
		//g2d.setColor(Color.GRAY);
		//g2d.fill(new Rectangle((int)screenSize.getWidth()+20,(int)screenSize.getHeight()+20));
		
		//Scale the image of Indiana to the window size and draw it
		g2d.drawImage(indiana, 0, 20, 
				(int) ((screenSize.getHeight()/indiana.getHeight())*indiana.getWidth()), 
				(int) (screenSize.getHeight()), null);
		
		//Paint the graph starting at the middle of the indiana image
		g2d.translate(350*xScale, 20 + 530*yScale);
		graph.paint(g2d,xScale,yScale);
		
	}
	
//	@Override
//	public void repaint() {
//		this.pack();
//		this.requestFocus();
//		super.repaint();
//	}
	
	public void testAStar(String start, String finish, boolean speedConsidered) {
		// TODO when all edges are added to graph
		LinkedList<Graph<String>.Path> path = graph.shortestPath(start, finish, speedConsidered);
		for (Path p : path) {
			System.out.println(p.getCollegeName());
		}
	}
}
