package driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import logic.util.ImageUtil;

import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * @author 	Zachary Fox
 * @since	Version - 1.0.0
 * @version Version - 1.0.0
 */
public class Driver extends ApplicationWindow {

	
	private Combo combo_name;
	private Button btnAssign;
	private Label lbl_loadImagesIcon;
	private Label lbl_loadConfigIcon;
	private Label lblOutputsicon;
	private Label lblImage;
	
	private boolean imagesLoaded = false;
	private boolean configLoaded = false;
	private boolean outputsDirSelected = false;
	
	private Queue<MyImage> imageQueue = new LinkedList<>();
	private Map<String, Integer> nameCountMap = new HashMap<>();
	private List<String> configData = new ArrayList<>();
	
	private File imageDir;
	private File outputsDir;
	
	private MyImage currentImage = null;
	
	private final static String IDENTIFIER = "\\*";
	private final static String IDENTIFIER_SHORT = "*";
	
	private final int previewImageSize = 300;
	
	/**
	 * Create the application window,
	 */
	public Driver() {
		super(null);
		createActions();
		addMenuBar();
		addStatusLine();
	}

	/**
	 * Create contents of the application window.
	 * @author Zachary Fox
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		GridLayout gl_container = new GridLayout(4, false);
		gl_container.marginHeight = 0;
		container.setLayout(gl_container);
		
		Composite comp_setupBar = new Composite(container, SWT.NO_BACKGROUND);
		comp_setupBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 4, 1));
		comp_setupBar.setLayout(new GridLayout(8, false));
		
		lbl_loadImagesIcon = new Label(comp_setupBar, SWT.NONE);
		lbl_loadImagesIcon.setImage(SWTResourceManager.getImage(Driver.class, "/Images/failure.png"));
		
		Button btn_loadImages = new Button(comp_setupBar, SWT.FLAT);
		btn_loadImages.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				loadImages();
			}
		});
		btn_loadImages.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btn_loadImages.setText("Load Images");
		
		lbl_loadConfigIcon = new Label(comp_setupBar, SWT.NONE);
		lbl_loadConfigIcon.setImage(SWTResourceManager.getImage(Driver.class, "/Images/failure.png"));
		
		Button btn_loadConfig = new Button(comp_setupBar, SWT.FLAT);
		btn_loadConfig.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				loadConfig();
			}
		});
		btn_loadConfig.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btn_loadConfig.setText("Load Config");
		
		lblOutputsicon = new Label(comp_setupBar, SWT.NONE);
		lblOutputsicon.setImage(SWTResourceManager.getImage(Driver.class, "/Images/failure.png"));
		
		Button btnSelectOutputDir = new Button(comp_setupBar, SWT.FLAT);
		btnSelectOutputDir.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				selectOutputsDirectory();
			}
		});
		btnSelectOutputDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnSelectOutputDir.setText("Select Output Dir");
		
		Label lbl_validateIcon = new Label(comp_setupBar, SWT.NONE);
		lbl_validateIcon.setToolTipText("Checks how the output directory matches up to your config files and provides useful information.");
		lbl_validateIcon.setImage(SWTResourceManager.getImage(Driver.class, "/Images/info.png"));
		
		Button btn_validate = new Button(comp_setupBar, SWT.FLAT);
		btn_validate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				validateResults();
			}
		});
		btn_validate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btn_validate.setText("Validate");
		
		lblImage = new Label(container, SWT.NONE);
		GridData gd_lblImage = new GridData(SWT.CENTER, SWT.CENTER, true, true, 4, 1);
		gd_lblImage.minimumWidth = previewImageSize;
		gd_lblImage.minimumHeight = previewImageSize;
		lblImage.setLayoutData(gd_lblImage);
		
		combo_name = new Combo(container, SWT.NONE);
		combo_name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		btnAssign = new Button(container, SWT.NONE);
		btnAssign.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent event) {
				assignImageName();
			}
		});
		btnAssign.setText("Assign");
		btnAssign.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		Button btnSkip = new Button(container, SWT.NONE);
		btnSkip.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				nextImage();
			}
		});
		btnSkip.setText("Skip");
		new Label(container, SWT.NONE);
		
		return container;
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Create the menu manager.
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager("menu");
		return menuManager;
	}

	/**
	 * Create the status line manager.
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		return statusLineManager;
	}

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			Driver window = new Driver();
			window.setBlockOnOpen(true);
			window.open();
			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the shell.
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(new Point(600, 500));
		super.configureShell(newShell);
		newShell.setText("Image Directory Util");
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(650, 500);
	}
	
	/**
	 * Loads the images that will be renamed.
	 * 
	 * @author Zachary Fox
	 * @since	Version - 1.0.0
	 */
	public void loadImages(){
		try{
			DirectoryDialog dialog = new DirectoryDialog(this.getShell());
			String dirPath = dialog.open();
			if(dirPath != null){
				imageDir = new File(dirPath);
				imageQueue.clear();
				for(File f : imageDir.listFiles(getImageFileFilter())){
					if(f.isFile() && !f.isHidden()){
						Image image = SWTResourceManager.getImage(f.getAbsolutePath());
						Image scaledImage = ImageUtil.scaledResize(image, previewImageSize, previewImageSize);
						imageQueue.add(new MyImage(scaledImage, f.getName()));
					}
				}
				if(!imageQueue.isEmpty()){
					imagesLoaded = true;
					lbl_loadImagesIcon.setImage(SWTResourceManager.getImage(Driver.class, "/Images/success.png"));
					nextImage();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads the configuration file that contains the name patterns
	 * to use in the combo box.
	 * 
	 * @author Zachary Fox
	 * @since	Version - 1.0.0
	 */
	public void loadConfig(){
		try{
			FileDialog dialog = new FileDialog(this.getShell());
			String filePath = dialog.open();
			if(filePath != null && filePath.endsWith(".txt")){
				configData.clear();
				File configFile = new File(filePath);
				BufferedReader br = new BufferedReader(new FileReader(configFile));
				String line = "";
				while((line = br.readLine()) != null){
					combo_name.add(line);
					configData.add(line);
				}
				configLoaded = true;
				lbl_loadConfigIcon.setImage(SWTResourceManager.getImage(Driver.class, "/Images/success.png"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Selects the outputs directory for the
	 * renamed images.
	 * 
	 * @author Zachary Fox
	 * @since	Version - 1.0.0
	 */
	public void selectOutputsDirectory(){
		try{
			DirectoryDialog dialog = new DirectoryDialog(this.getShell());
			String dirPath = dialog.open();
			if(dirPath != null){
				File temp = new File(dirPath);
				if(temp.isDirectory()){
					outputsDirSelected = true;
					outputsDir = temp;
					lblOutputsicon.setImage(SWTResourceManager.getImage(Driver.class, "/Images/success.png"));
					initImageCounts(outputsDir, nameCountMap);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Runs some validation on the outputs directory
	 * comparing it to the configuration file and 
	 * prints some useful information.
	 * 
	 * @author 	Zachary Fox
	 * @since	Version - 1.0.0
	 */
	public void validateResults(){
		if(alertOnMissingOutputsDir() || alertOnMissingConfig()){
			return;
		}
		
		//Check for missing files that are listed in config
		StringBuilder resultStr = new StringBuilder("Missing Files:");
		for(String configLine : configData){
			String regexConfigLine = configLine.replaceFirst(IDENTIFIER, "[0-9]+");
			regexConfigLine = regexConfigLine + ".[a-zA-z]+";
			boolean match = false;
			for(File file : outputsDir.listFiles(getImageFileFilter())){
				String filename = file.getName();
				if(filename.matches(regexConfigLine)){
					match = true;
					break;
				}
			}
			if(!match){
				resultStr.append("\n\t"+configLine);
			}
		}
		
		//List occurrences of each type of file name
		resultStr.append("\nFile Count:");
		Map<String, Integer> countMap = new HashMap<>();
		for(File file : outputsDir.listFiles(getImageFileFilter())){
			String genericFilename = getGenericFilename(file.getName());
			if(countMap.containsKey(genericFilename)){
				int count = countMap.get(genericFilename);
				countMap.put(genericFilename, count+1);
			}else{
				countMap.put(genericFilename, 1);
			}
		}
		for(String key : countMap.keySet()){
			resultStr.append("\n\t"+key+" : "+ countMap.get(key));
		}
		
		MessageBox messageBox = new MessageBox(this.getShell(), SWT.ICON_INFORMATION | SWT.OK);
		messageBox.setMessage(resultStr.toString());
		messageBox.open();
	}
	
	/**
	 * Assigns a new name to the current image.
	 * 
	 * @author 	Zachary Fox
	 * @since	Version - 1.0.0
	 */
	public void assignImageName(){
		try{
			if(alertOnMissingImages() || alertOnMissingOutputsDir()){
				return;
			}
			String name = combo_name.getText();
			if(name.equals("")){
				MessageBox messageBox = new MessageBox(this.getShell(), SWT.ICON_ERROR | SWT.OK);
				messageBox.setMessage("Name cannot be blank");
				messageBox.open();
				return;
			}
			
			//Add number to name;
			if(name.contains(IDENTIFIER_SHORT)){
				int count = 0;
				if(nameCountMap.containsKey(name)){
					count = nameCountMap.get(name);
				}
				++count;
				nameCountMap.put(name, count);
				name = name.replaceFirst(IDENTIFIER, count+"");
			}
			
			//Check if file already exists
			String fileExt = currentImage.filename.substring(currentImage.filename.lastIndexOf("."));
			File resultFile = new File(outputsDir.getAbsolutePath()+"/"+name+fileExt);
			if(resultFile.exists()){
				MessageBox messageBox = new MessageBox(this.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				messageBox.setMessage("A file already exists with that name. Overwrite?");
				int responseCode = messageBox.open();
				if(responseCode == SWT.NO){
					return;
				}
			}
			
			//Copy file to output dir with new name
			Path sourcePath = Paths.get(imageDir.getAbsolutePath()+"/"+currentImage.filename);
			Path destPath = Paths.get(resultFile.getAbsolutePath());
			Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
			
			nextImage();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Moves to the next image in the queue.
	 * 
	 * @author 	Zachary Fox
	 * @since	Version - 1.0.0
	 */
	public void nextImage(){
		if(!imageQueue.isEmpty()){
			currentImage = imageQueue.poll();
			lblImage.setImage(currentImage.image);
			//TODO scale image
		}else{
			currentImage = null;
			imagesLoaded = false;
			lbl_loadImagesIcon.setImage(SWTResourceManager.getImage(Driver.class, "/Images/failure.png"));
			MessageBox messageBox = new MessageBox(this.getShell(), SWT.ICON_INFORMATION | SWT.OK);
			messageBox.setMessage("Done processing images");
			messageBox.open();
		}
	}
	
	/**
	 * Alerts the user that they have not
	 * selected an outputs directory.
	 * 
	 * @author Zachary Fox
	 * @since	Version - 1.0.0
	 */
	private boolean alertOnMissingOutputsDir(){
		if(!outputsDirSelected){
			MessageBox messageBox = new MessageBox(this.getShell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setMessage("No output directory selected");
			messageBox.open();
			return true;
		}
		return false;
	}
	
	/**
	 * Alerts the user that they have not
	 * selected a configuration file.
	 * 
	 * @author Zachary Fox
	 * @since	Version - 1.0.0
	 */
	private boolean alertOnMissingConfig(){
		if(!configLoaded){
			MessageBox messageBox = new MessageBox(this.getShell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setMessage("No configuration file loaded");
			messageBox.open();
			return true;
		}
		return false;
	}
	
	/**
	 * Alerts the user that no image is
	 * currently selected.
	 * 
	 * @author Zachary Fox
	 * @since	Version - 1.0.0
	 */
	private boolean alertOnMissingImages(){
		if(currentImage == null || !imagesLoaded){
			MessageBox messageBox = new MessageBox(this.getShell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setMessage("No images loaded");
			messageBox.open();
			return true;
		}
		return false;
	}
	
	/**
	 * A filename filter for the accepted
	 * image extensions.
	 * 
	 * @author Zachary Fox
	 * @since	Version - 1.0.0
	 */
	private FilenameFilter getImageFileFilter(){
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")
				  || name.endsWith("bmp") || name.endsWith("tiff") || name.endsWith("gif")){
					return true;
				}
				return false;
			}
		};
		return filter;
	}
	
	/**
	 * Checks the outputs directory for existing
	 * images and initialized the count for each
	 * image name. This allows the user to use
	 * the same outputs directory over multiple 
	 * sessions.
	 * 
	 * @author 	Zachary Fox
	 * @since	Version - 1.0.0
	 * @param	outputsDir The directory where image will be sent.
	 * @param	countMap the map being used to track the count for each filename
	 */
	private void initImageCounts(File outputsDir, Map<String, Integer> countMap){
		try{
			for(File file : outputsDir.listFiles(getImageFileFilter())){
				String filename = file.getName();
				int count = getCountFromName(filename);
				String genericFilename = getGenericFilename(filename);
				if(countMap.containsKey(filename)){
					int current = countMap.get(filename);
					if(count > current){
						countMap.put(genericFilename, count);
					}
				}else{
					countMap.put(genericFilename, count);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Attempts to retrieve the count from 
	 * a filename.
	 * 
	 * @author 	Zachary Fox
	 * @since	Version - 1.0.0
	 * @param	filename The filename to get a count from
	 */
	private int getCountFromName(String filename){
		try{
			String reverseStr = reverse(filename);
			String numberStr = filename.replaceFirst("[^0-9]*", ""); //delete everything before the first set of numbers
			numberStr = numberStr.replaceFirst("[^0-9].*", ""); //delete everything after the first set of numbers
			int result = Integer.parseInt(numberStr);
			return result;
		}catch(Exception e){
			return 0;
		}
	}
	
	/**
	 * Reverses a <code>String</code>.
	 * 
	 * @author Zachary Fox
	 * @since	Version - 1.0.0
	 * @param	str The <code>String</code> to reverse
	 */
	private String reverse(String str){
		String reverseStr = "";
		for(int i=str.length()-1; i >= 0; --i){
			reverseStr = reverseStr + str.charAt(i);
		}
		return reverseStr;
	}
	
	/**
	 * Converts a filename to its generic form
	 * by replacing the count with the identifier
	 * character.
	 * 
	 * @author 	Zachary Fox
	 * @since	Version - 1.0.0
	 * @param	filename The filename to make generic
	 */
	private String getGenericFilename(String filename){
		try{
			int count = getCountFromName(filename);
			//Make filename generic by replacing last occurence of count with *
			String genericFilename = reverse(filename);
			genericFilename = genericFilename.replaceFirst(count+"", "*");
			genericFilename = reverse(genericFilename);
			//Remove extension from filename
			genericFilename = genericFilename.substring(0, genericFilename.indexOf("."));
			
			return genericFilename;
		}catch(Exception e){
			e.printStackTrace();
			return filename;
		}
	}

	/**
	 * An inner class that stores the
	 * filename along with the image.
	 * 
	 * @author 	Zachary Fox
	 * @since	Version - 1.0.0
	 */
	private class MyImage{
		public Image image = null;
		public String filename = "";
		public MyImage(Image image, String filename){
			this.image = image;
			this.filename = filename;
		}
	}
}
