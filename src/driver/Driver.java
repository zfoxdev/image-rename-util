package driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

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
	
	private File imageDir;
	private File outputsDir;
	
	private MyImage currentImage = null;
	
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
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		GridLayout gl_container = new GridLayout(3, false);
		gl_container.marginHeight = 0;
		container.setLayout(gl_container);
		
		Composite comp_setupBar = new Composite(container, SWT.NO_BACKGROUND);
		comp_setupBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
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
		GridData gd_lblImage = new GridData(SWT.CENTER, SWT.CENTER, true, true, 3, 1);
		gd_lblImage.minimumWidth = 300;
		gd_lblImage.minimumHeight = 300;
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
	 * Create the coolbar manager.
	 * @return the coolbar manager
	 */
	@Override
	protected CoolBarManager createCoolBarManager(int style) {
		CoolBarManager coolBarManager = new CoolBarManager(style);
		return coolBarManager;
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
	
	public void loadImages(){
		try{
			DirectoryDialog dialog = new DirectoryDialog(this.getShell());
			String dirPath = dialog.open();
			System.out.println(dirPath);
			if(dirPath != null){
				imageDir = new File(dirPath);
				imageQueue.clear();
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
				for(File f : imageDir.listFiles(filter)){
					if(f.isFile() && !f.isHidden()){
						Image image = SWTResourceManager.getImage(f.getAbsolutePath());
						imageQueue.add(new MyImage(image, f.getName()));
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
	
	public void loadConfig(){
		try{
			FileDialog dialog = new FileDialog(this.getShell());
			String filePath = dialog.open();
			if(filePath != null && filePath.endsWith(".txt")){
				File configFile = new File(filePath);
				BufferedReader br = new BufferedReader(new FileReader(configFile));
				String line = "";
				while((line = br.readLine()) != null){
					combo_name.add(line);
				}
				configLoaded = true;
				lbl_loadConfigIcon.setImage(SWTResourceManager.getImage(Driver.class, "/Images/success.png"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
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
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void validateResults(){
		//TODO
		//Check for missing files that are listed in config
		//Check for extra files that are not listed in config
		//List occurrences of each type of file name
	}
	
	public void assignImageName(){
		try{
			if(currentImage == null){
				MessageBox messageBox = new MessageBox(this.getShell(), SWT.ICON_ERROR | SWT.OK);
				messageBox.setMessage("No image to assign name to");
				messageBox.open();
				return;
			}
			if(!outputsDirSelected){
				MessageBox messageBox = new MessageBox(this.getShell(), SWT.ICON_ERROR | SWT.OK);
				messageBox.setMessage("No output directory selected");
				messageBox.open();
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
			if(name.contains("*")){
				int count = 0;
				if(nameCountMap.containsKey(name)){
					count = nameCountMap.get(name);
				}
				++count;
				nameCountMap.put(name, count);
				name = name.replaceFirst("*", count+"");
			}
			
			//Check if file already exists
			String fileExt = currentImage.filename.substring(currentImage.filename.lastIndexOf("."));
			File resultFile = new File(outputsDir.getAbsolutePath()+"/"+name+fileExt);
			if(resultFile.exists()){
				MessageBox messageBox = new MessageBox(this.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				messageBox.setMessage("A file already exists with that name. Overwrite?");
				int responseCode = messageBox.open();
				if(responseCode == SWT.YES){
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

	private class MyImage{
		public Image image = null;
		public String filename = "";
		public MyImage(Image image, String filename){
			this.image = image;
			this.filename = filename;
		}
	}
}
