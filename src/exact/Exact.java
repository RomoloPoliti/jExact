package exact;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.*;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.Cursor;
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
//JSON
import org.json.simple.*;
import org.json.simple.parser.*;
// JFree
import org.jfree.chart.*;
import org.jfree.chart.title.*;
import org.jfree.ui.*;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.block.*;
import org.jfree.data.xy.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
//--------------------------------
public class Exact extends JFrame implements ActionListener {
private JLabel datasetLabel;
private JScrollPane pane, paneFile;
private static String host ="nazgul.iaps.inaf.it", hostpath="/platac/";
private DefaultTableModel model, fileModel;
private JTable datasetTable, fileTable;
private JPanel controlArea, preButtonBar, buttonBar, fileArea;
private JButton bt1,bt2, bt3, bt4;
private Box box, hBox, hBox2;
private Container content;
private JFreeChart chart;
private XYSeriesCollection dataset;
private int flag;
private String[] authVal;
private javax.swing.JComboBox jComboBox1;
private XYTitleAnnotation ta;
private XYPlot plot;
private LegendTitle lt;
//
	public static void main(String[] args) {
		new Exact();
	}
//
	public Exact(){
		super("Exact JAVA Interface");
		dataset = new XYSeriesCollection();
		WindowUtilities.setNimbusLookAndFeel();
		addWindowListener(new ExitListener());
		content = getContentPane();
		content.setBackground(Color.lightGray);
		hBox=new Box(BoxLayout.X_AXIS);
		hBox=Box.createHorizontalBox();
		box = new Box(BoxLayout.X_AXIS);
		box = Box.createVerticalBox();
		hBox2=new Box(BoxLayout.X_AXIS);
		hBox2=Box.createHorizontalBox();
		controlArea = new JPanel();
		controlArea.setLayout(new BoxLayout(controlArea, BoxLayout.PAGE_AXIS));
		String[] datasetTableHeader={"Datasets","idDataset"};
		//String[][] datasetTableData={{"11","12"},{"21","22"}};
		model = new DefaultTableModel(datasetTableHeader,0); 
		datasetTable = new JTable(model);
		datasetTable.setPreferredScrollableViewportSize(datasetTable.getPreferredSize());
		final TableColumnHider hider = new TableColumnHider(datasetTable);
		hider.hide("idDataset");
		datasetTable.setCellSelectionEnabled(true);
		datasetTable.getSelectionModel().addListSelectionListener(new RowListener());
//     cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pane=new JScrollPane(datasetTable);
		controlArea.add(pane,BorderLayout.CENTER);
		controlArea.setPreferredSize(new Dimension(225, 300));
		controlArea.setMaximumSize(new Dimension(220,25000));
		controlArea.setMinimumSize(new Dimension(220, 300));
		box.setMaximumSize(new Dimension(450,25000));
		preButtonBar=new JPanel();
		buttonBar= new JPanel();
		preButtonBar.setLayout(new GridLayout(1,1));
		preButtonBar.setMaximumSize(new Dimension(450,100));
		buttonBar.setLayout(new BoxLayout(buttonBar,BoxLayout.X_AXIS));
		bt1= new JButton("Connect");
		bt1.setMnemonic(KeyEvent.VK_C);
		bt1.setToolTipText("Connect To the EXACT Database");
		bt1.setActionCommand("connect");
		bt1.addActionListener(this);
		bt2= new JButton("About");
		bt2.setMnemonic(KeyEvent.VK_A);
		bt2.setToolTipText("Information about the program");
		bt2.setActionCommand("about");
		bt2.addActionListener(this);
		bt3= new JButton("Quit");
		bt3.setMnemonic(KeyEvent.VK_Q);
		bt3.setActionCommand("quit");
		bt3.addActionListener(this);
		bt3.setToolTipText("Quit from the program");
		buttonBar.add(bt1);
		buttonBar.add(bt2);
		buttonBar.add(bt3);
		hBox2.add(controlArea);
		box.add(hBox2);
		preButtonBar.add(buttonBar);
		box.add(preButtonBar);
		hBox.add(box);
		content.add(hBox);
		pack();
		setVisible(true);
	}
//
	private class RowListenerFile implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent event) {
			if (event.getValueIsAdjusting()) {
				return;
			}
			ListSelectionModel lsm = (ListSelectionModel)event.getSource();
			boolean isAdjusting = event.getValueIsAdjusting(); 
			if (lsm.isSelectionEmpty()) {
				System.out.println("No selection");
			} else {
				int minIndex = lsm.getMinSelectionIndex();
				int maxIndex = lsm.getMaxSelectionIndex();
				if(dataset.getSeriesCount()== 0){flag=0;}else{
					if(minIndex == maxIndex ){
						dataset.removeAllSeries();
					}
					flag=1;
				}
				for (int i = minIndex; i <= maxIndex; i++) {
					if (lsm.isSelectedIndex(i)) {
						String index =fileTable.getModel().getValueAt(i,1).toString();
						String a=getData(1,index.toString());
						JSONParser parser=new JSONParser();
						ContainerFactory containerFactory = new ContainerFactory(){
							public List creatArrayContainer() {
								return new LinkedList();
							}
							public Map createObjectContainer() {
								return new LinkedHashMap();
							}
						};
						try{
							Map json = (Map)parser.parse(a, containerFactory);
							if (json.get("error") == "false"){
								System.out.println(json.get("message"));
							}else{
								LinkedHashMap resp=(LinkedHashMap)(json.get("response"));
								LinkedHashMap spectrum=(LinkedHashMap)(resp.get("Spectrum"));
								String ttl=(String)spectrum.get("Label");
				// 			System.out.println(ttl);
								LinkedList data=(LinkedList)(spectrum.get("SpectrumData"));
								XYSeries series = new XYSeries(ttl);
								Iterator j = data.listIterator(); 
								while(j.hasNext()) {
									LinkedList elem=(LinkedList)j.next();
									series.add((Number)elem.get(0),(Number)elem.get(1));
								}
								if(dataset.indexOf(series)==-1){
									dataset.addSeries(series);
								}
								if( flag == 0){
									chart = ChartFactory.createXYLineChart(
										null, // Title 
										"Wavenumber (cm-1)", // x-axis Label
										"Absorption Coefficients (cm-1)", // y-axis Label
										dataset, // Dataset
										PlotOrientation.VERTICAL, // Plot Orientation
										false, // Show Legend
										true, // Use tooltips
										false // Configure chart to generate URLs?
									);
									ChartPanel chartPanel = new ChartPanel(chart);
									chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
									hBox.add(chartPanel);
									bt4= new JButton("Download");
									bt4.setMnemonic(KeyEvent.VK_D);
									bt4.setActionCommand("download");
									bt4.addActionListener(bt1.getActionListeners()[0]);
									bt4.setToolTipText("Download the data");
									buttonBar.add(bt4);
									jComboBox1 = new javax.swing.JComboBox();
									jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Legend top-right", "Legend top-left","Legend bottom-left", "Legend bottom-right", "Legend hide" }));
									jComboBox1.addActionListener(new java.awt.event.ActionListener() {
										public void actionPerformed(java.awt.event.ActionEvent evt) {
											jComboBox1ActionPerformed(evt);
										}
									});
									buttonBar.add(jComboBox1);
									pack();
									plot = (XYPlot) chart.getPlot();
									lt = new LegendTitle(plot);
									lt.setItemFont(new Font("Dialog", Font.PLAIN, 9));
									lt.setBackgroundPaint(new Color(200, 200, 255, 100));
									lt.setFrame(new BlockBorder(Color.white));
									lt.setPosition(RectangleEdge.TOP);
									ta = new XYTitleAnnotation(0.98, 0.90, lt,RectangleAnchor.TOP_RIGHT);
									ta.setMaxWidth(0.48);
									plot.addAnnotation(ta);
								}else{
									plot = (XYPlot) chart.getPlot();
									lt = new LegendTitle(plot);
									lt.setItemFont(new Font("Dialog", Font.PLAIN, 9));
									lt.setBackgroundPaint(new Color(200, 200, 255, 100));
									lt.setFrame(new BlockBorder(Color.white));
									lt.setPosition(RectangleEdge.TOP);
									plot.removeAnnotation(ta);
									ta = new XYTitleAnnotation(0.98, 0.90, lt,RectangleAnchor.TOP_RIGHT);
									ta.setMaxWidth(0.48);
									plot.addAnnotation(ta);
								}
							}
						}catch(ParseException pe){
							System.out.println("position: " + pe.getPosition());
							System.out.println(pe);
						}
					}
				}
			}
		}
	}
  private class RowListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            return;
	}
	if(event.getFirstIndex() == event.getLastIndex()){
            int row=event.getFirstIndex();
            String index =datasetTable.getModel().getValueAt(row,1).toString();
//          System.out.println(index);
            String a=getData(0,index);
            JSONParser parser=new JSONParser();
            ContainerFactory containerFactory = new ContainerFactory(){
                public List creatArrayContainer() {
                    return new LinkedList();
				}
				public Map createObjectContainer() {
							return new LinkedHashMap();
				}
            };
            try{
                Map json = (Map)parser.parse(a, containerFactory);
				if (json.get("error") == "false"){
					System.out.println(json.get("message"));
				}else{
					fileArea = new JPanel();
					fileArea.setLayout(new BoxLayout(fileArea, BoxLayout.PAGE_AXIS));
					String[] fileTableHeader={"Data","idData"};
					fileModel = new DefaultTableModel(fileTableHeader,0); 
					fileTable = new JTable(fileModel);
					//fileTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
					//fileTable.getColumnModel().getColumn(0).setPreferredWidth(200);
					ListSelectionModel listSelectionModel = fileTable.getSelectionModel();
					fileTable.setPreferredScrollableViewportSize(fileTable.getPreferredSize());
					final TableColumnHider hiderFile = new TableColumnHider(fileTable);
					hiderFile.hide("idData");
			// 		fileTable.setCellSelectionEnabled(true);
					listSelectionModel.addListSelectionListener(new RowListenerFile());
							listSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					fileTable.setSelectionModel(listSelectionModel);
					paneFile=new JScrollPane(fileTable);
					fileArea.add(paneFile,BorderLayout.CENTER);
					fileArea.setPreferredSize(new Dimension(230, 300));
					hBox2.add(fileArea);
					pack();
			// 		Class p=json.getClass();
			// 		System.out.println(p.getName());
					LinkedHashMap resp=(LinkedHashMap)(json.get("response"));
					LinkedHashMap list=(LinkedHashMap)(resp.get("dataSet"));
					LinkedList spectra=(LinkedList)(list.get("Spectra"));
					Iterator i = spectra.listIterator(); 
					while(i.hasNext()) {
						LinkedHashMap elem=(LinkedHashMap)i.next();
						fileModel.addRow(new Object[] {elem.get("Label"),elem.get("Id")});
					}
				}
            }catch(ParseException pe){
				System.out.println("position: " + pe.getPosition());
		System.out.println(pe);
            }
	}
       }
    }
  public void actionPerformed(ActionEvent e) {
    String fileName= "Test";
    if ("quit".equals(e.getActionCommand())) {
        System.exit(0);
    } else if ("connect".equals(e.getActionCommand())) {
			auth atval=new auth(this,true);
			atval.setVisible(true);
			authVal=atval.getValue();
			//System.out.println(authVal[1]);
			String a=getData(0,"all");
			JSONParser parser=new JSONParser();
			ContainerFactory containerFactory = new ContainerFactory(){
				public List creatArrayContainer() {
					return new LinkedList();
				}
				public Map createObjectContainer() {
					return new LinkedHashMap();
				}
			};
			try{
				Map json = (Map)parser.parse(a, containerFactory);
				if (json.get("error") == "false"){
					System.out.println(json.get("message"));
				}else{
					LinkedHashMap resp=(LinkedHashMap)(json.get("response"));
					LinkedList list=(LinkedList)(resp.get("datasetList"));
 				//Class p=resp.get("datasetList").getClass();
 				//System.out.println(p.getName());
					Iterator i = list.listIterator(); 
					while(i.hasNext()) {
						LinkedHashMap elem=(LinkedHashMap)i.next();
						model.addRow(new Object[] {elem.get("datasetName"),elem.get("datasetId")});
					}
				}
			}catch(ParseException pe){
		    System.out.println("position: " + pe.getPosition());
				System.out.println(pe);
			}
		} else if ("about".equals(e.getActionCommand())){
			about abt=new about(this,true);
			abt.setVisible(true);
		} else if ("download".equals(e.getActionCommand())){
			ListSelectionModel lsm = fileTable.getSelectionModel();
			List<String> idx=new ArrayList<String>();
			String text;
			text = null;
			
			int minIndex = lsm.getMinSelectionIndex();
			int maxIndex = lsm.getMaxSelectionIndex();
			for (int i = minIndex; i <= maxIndex; i++) {
				if (lsm.isSelectedIndex(i)) {
					idx.add(fileTable.getModel().getValueAt(i,1).toString());
				}
			}
			
				String a;
				JSONParser parser=new JSONParser();
				ContainerFactory containerFactory = new ContainerFactory(){
					public List creatArrayContainer() {
						return new LinkedList();
					}
					public Map createObjectContainer() {
						return new LinkedHashMap();
					}
				};
				if(idx.size()==1){
					String index;
					index = idx.get(0).toString();
					a = getData(3,index);
					fileName=fileTable.getModel().getValueAt(minIndex,0).toString();
					fileName=fileName.replaceAll(",","");
					fileName=fileName.replaceAll(" ","_");
				}else{
					String jsonText = JSONValue.toJSONString(idx);
					System.out.println(jsonText);
					a = getData(3,jsonText);
					fileName="DataSet";
				}
				// Create file 
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("TXT File","txt");
				chooser.setFileFilter(filter);
				chooser.setSelectedFile(new File(fileName+".txt"));
				int returnVal = chooser.showSaveDialog(content);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						Map json = (Map)parser.parse(a, containerFactory);
						text=(String)(json.get("response"));
					} catch (ParseException ex) {
						Logger.getLogger(Exact.class.getName()).log(Level.SEVERE, null, ex);
					}
					//System.out.println(text);
					try{
						//String filename=chooser.getSelectedFile();
						System.out.println(chooser.getSelectedFile());
						FileWriter fstream = new FileWriter(chooser.getSelectedFile());
						//BufferedWriter out = new BufferedWriter(fstream);
						//PrintWriter out = new PrintWriter(fstream);
						fstream.write(text);
						//out.flush();
						fstream.close();
						System.out.println("OK");
					}catch (IOException ee){//Catch exception if any
						System.err.println("Error: " + ee.getMessage());
					}
				}
// 				System.out.println(a);
			
	  } else {
		  System.out.println("default");
	  }
  }
	private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {
		JComboBox cb = (JComboBox)evt.getSource();
        String selName = (String)cb.getSelectedItem();
		Title ttl=ta.getTitle();
		double px=0.98;
		double py=0.90;
		RectangleAnchor newAnchor=RectangleAnchor.TOP_RIGHT;
		if ("Legend top-right".equals(selName)){
			newAnchor=RectangleAnchor.TOP_RIGHT;
		}else if("Legend top-left".equals(selName)){
			newAnchor=RectangleAnchor.TOP_LEFT;
			px=0.05;
			py=0.90;
		}else if("Legend bottom-left".equals(selName)){
			newAnchor=RectangleAnchor.BOTTOM_LEFT;
			px=0.05;
			py=0.05;
		}else if("Legend bottom-right".equals(selName)){
			newAnchor=RectangleAnchor.BOTTOM_RIGHT;
			px=0.98;
			py=0.05;
		}
		if("Legend hide".equals(selName)){
			plot.removeAnnotation(ta);
		}else{
			plot.removeAnnotation(ta);
			ta = new XYTitleAnnotation(px, py, lt,newAnchor);
			ta.setMaxWidth(0.48);
			plot.addAnnotation(ta);
		}
		//, "Legend top-left","Legend bottom-left", "Legend bottom-right", "Legend hide"
		//System.out.println(petName);
        // TODO add your handling code here:
    }    
	public String getData(int mode, String code){
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		URL connection = null;
		String a="Test";
		String s;
		String hostKey=hostpath+"tools/api.php?username="+authVal[0]+"&password="+authVal[1];
		try{
			if (mode ==0 ){
				hostKey=hostKey+"&QuerySet=Dataset&getDataSet="+code;
				if (!"all".equals(code)){
					hostKey=hostKey+"&getData=true";
				}
				connection = new URL("http",host,hostKey);
			}else if (mode == 1){
				hostKey=hostKey+"&QuerySet=Spectrum&getSpectrum="+code+"&getItemSpectrum=true";
				connection = new URL("http",host,hostKey);
			}else if (mode == 3){
				hostKey=hostKey+"&QuerySet=Download&getSpectrum="+code;
				connection = new URL("http",host,hostKey);
			}
		}catch (MalformedURLException e){
        System.err.println("New URL failed");
        System.err.println("exception thrown: " + e.getMessage());
    }
		try{
			InputStream is = connection.openStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			a=in.readLine();
			in.close();
		}catch(IOException e){
			System.out.println("Error:");
			e.printStackTrace();
		}
		//System.out.println(hostKey);
		this.setCursor(Cursor.getDefaultCursor());
	  return a;
  }
}