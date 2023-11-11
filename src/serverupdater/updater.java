package serverupdater;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.border.Border;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.VerRsrc;
import com.sun.jna.platform.win32.Version;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import common.Layout;
import view.CustomProgressBar;


public class updater extends JFrame implements MouseMotionListener, MouseListener {

	private final String rootUpdate = "lately/";
	Thread worker;
	String current;
	String lately;

	JLabel logoEng;
	JLabel logoKor;
	JLabel logoMid;

	int perX;
	int perY;
	int currentX;
	int currentY;
	int startX;
	int startY;

	JLabel mainArea;
	JLabel text;
	Thread checkModule;
	JProgressBar progressBar;
	Border border;

	String pVersion;
	String cVersion;
	
	
	String chromeZip = "chromedriver_win32.zip";
	String updaterZip = "lately.zip";
//	String s3 http://sellprolab.s3.amazonaws.com/spl/lately.zip
//	String s3Api http://43.201.136.108:14685/version/lately/download
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					new updater();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	/**
	 * Create the application.
	 * 
	 * @throws IOException
	 */
	public updater() throws IOException {
		initialize();
		try {
		versionCheck();
		}catch (Exception e) {
			// TODO: handle exception

			JOptionPane.showMessageDialog(null, e.toString());
		}
	}

	public void chromeUpdate() throws IOException {

		text.setText("크롬 버젼 체크 중 ...");
		String tVersion =getCurrentChromeVersion();
		if (! cVersion.equals(tVersion)){
			cVersion=tVersion;
			System.out.println ("CheckIn");
		}
		
		
		unzip(chromeZip, tVersion);
		cleanupZip();
		
	}
	public void cleanupZip() {
		File f = new File(chromeZip);
		f.delete();
	}
	
	public boolean checkNowVersion(Element li) {
		boolean result = false;
		System.out.println(li.select("a").text()) ;
		System.out.println(li.select("a").attr("href").split("=")[1] );
		String version = li.select("a").attr("href").split("=")[1].substring(0,3);
		
		System.out.println(version.toString().split("."));
		if(version.equals(cVersion)) {
			result =true;
		}
		
		return result;
	}

	public String getCurrentChromeVersion() {
		System.out.println(searchVersion(getChromePaths()));
		String result = searchVersion(getChromePaths());

		if (result.length() > 0) {
			return result;
		}else {
			return "100";
		}
	}

	public String searchVersion(ArrayList<String> paths) {
		String version = "";
		for (String path : paths) {
			String tempVersion = getChromeVersion(path);
			if(tempVersion.length()>0) {
				System.out.println( path);
				System.out.println(tempVersion);
				version = tempVersion;
				
				return version;
			}
		}
		return version;
	}
	
	public String getChromeVersion(String path) {
		String version ="";
		System.out.println(path);
		File t = new File(path);
		short v = 0;
		int infoSize = Version.INSTANCE.GetFileVersionInfoSize(t.getAbsolutePath(), null);
		Pointer buffer = Kernel32.INSTANCE.LocalAlloc(WinBase.LMEM_ZEROINIT, infoSize);
	
		try {
			Version.INSTANCE.GetFileVersionInfo(t.getAbsolutePath(), 0, infoSize, buffer);
			IntByReference outputSize = new IntByReference();
			PointerByReference pointer = new PointerByReference();
			Version.INSTANCE.VerQueryValue(buffer, "\\", pointer, outputSize);
			VerRsrc.VS_FIXEDFILEINFO fileInfoStructure = new VerRsrc.VS_FIXEDFILEINFO(pointer.getValue());
	
			// file version
			v = (short) (fileInfoStructure.dwFileVersionMS.longValue() >> 16);
	
			if (v > 0) {
				version = Integer.toString(v);
				return version;
			}
		} finally {
			Kernel32.INSTANCE.GlobalFree(buffer);
		}
		return version;
	}

	public ArrayList<String> getChromePaths() {
		ArrayList<String> paths = new ArrayList<>();
		paths.add("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");
		paths.add("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
		paths.add("C:\\Users\\" + getUserName() + "\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe");
		return paths;
	}

	public String getUserName() {
		return System.getProperty("user.name");
	}

	public void versionCheck() throws IOException {

		worker = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (compareVersion()) {
						System.out.println("test");
							mainLaunch();
					} else {
						pVersion = lately;
						System.out.println("testf");
						text.setText("자동 업데이트 중...");
						download();
					}
				}catch (FileNotFoundException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "관리자 권한으로 실행하여 주세요.");
					System.exit(0);
				}catch (Exception e) {
					// TODO: handle exception
					String msg =e.getMessage()+"\n관리자에게 문의 해주세요.\n";
					for(int i = 0; i< e.getStackTrace().length;i++) {
						msg += e.getStackTrace()[i].getMethodName()+e.getStackTrace()[i].getLineNumber();
					}
					JOptionPane.showMessageDialog(null, msg);
				}
			}
		});
		worker.start();
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @throws IOException
	 */
	private void initialize() throws IOException {
		new Frame();

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

		perX = (int) (screen.getWidth() / 1920);
		perY = (int) (screen.getHeight() / 1080);

		setTitle("셀프로랩 (Seller Pro Lab)");
		List<Image> icons = new ArrayList<Image>();
        icons.add( new ImageIcon("resource/logo-mini.png").getImage()); 
        icons.add( new ImageIcon("resource/logo-mid.png").getImage());
		setIconImages(icons);

		setUndecorated(true);

		getContentPane().setLayout(null);
		currentX = (int) ((screen.getWidth() / 2) - 300 * perX);
		currentY = (int) ((screen.getHeight() / 2) - 187 * perY);

		setBounds(currentX, currentY, 500, 160);
		setUndecorated(true);
		setBackground(new Color(0xffffff));
		setLocationRelativeTo(null);
		
		setShape(new RoundRectangle2D.Double(0,0,  getWidth(),getHeight(), 27,27));
		
		setVisible(true);
		
		mainArea = new JLabel();
		mainArea.setIcon(resizeButtonImage(new Rectangle(0,0,500,160), Layout.UPDATER_FRAME));
		mainArea.setBounds(0,0,500,160);
		mainArea.addMouseListener(this);
		add(mainArea);
		
		progressBar = new JProgressBar();
		progressBar.setUI(new CustomProgressBar());
		progressBar.setBounds(10, 80, 480, 35);
		progressBar.setValue(20);
		progressBar.setOpaque(false);
		progressBar.setBorder(null);

		text = new JLabel("버전 체크중...");
		text.setForeground(new Color(0xFFFFFF));
		text.setFont(Layout.PRETENDARD_BOLD_FONT(18));
		text.setBounds(20, 15, 300, 20);

		mainArea.add(text);
		mainArea.add(progressBar);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void setProgress(int value) {
		progressBar.setValue(value);
	}

	private void download() {
				// TODO Auto-generated method stub
				try {
					downloadFile("http://43.201.136.108:14685/version/lately/download",updaterZip);
//						downloadFile("http://127.0.0.1:3010/version/lately/download/test");
					unzip(updaterZip ,rootUpdate);
					copyFiles(new File(rootUpdate), new File("").getAbsolutePath());
					cleanup(updaterZip,rootUpdate);
					mainLaunch();

				} catch (FileNotFoundException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "관리자 권한으로 실행하여 주세요.");
					System.exit(0);
				} catch (Exception ex) {
					ex.printStackTrace();
					// JOptionPane.showMessageDialog(null, "An error occured while preforming
					// update!");
					JOptionPane.showMessageDialog(null, ex.toString());
					System.exit(0);
				}

	}

	private void cleanup(String name, String root) {
		remove(new File(root));
		new File(root).delete();
		File f = new File(name);
		f.delete();
	}

	private void remove(File f) {
		File[] files = f.listFiles();
		int max = files.length;
		int curent = 0;
		for (File ff : files) {
			curent++;
			progressBar.setValue((curent * 100) / max);
			if (ff.isDirectory()) {
				remove(ff);
				ff.delete();
			} else {
				ff.delete();
			}
		}
	}

	public void copy(String srFile, String dtFile) throws FileNotFoundException, IOException {
		File f1 = new File(srFile);
		File f2 = new File(dtFile);
		InputStream in = new FileInputStream(f1);
		OutputStream out = new FileOutputStream(f2);

		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	private void copyFiles(File f, String dir) throws IOException {
		File[] files = f.listFiles();
		int max = files.length;
		int curent = 0;

		for (File ff : files) {
			curent++;
			setProgress((curent * 100) / max);

			if (ff.isDirectory()) {
				new File(dir + "/" + ff.getName()).mkdir();
				copyFiles(ff, dir + "/" + ff.getName());
			} else {
				copy(ff.getAbsolutePath(), dir + "/" + ff.getName());
			}
		}
	}

	private void unzip(String name, String root ) throws IOException {
		
		int BUFFER = 2048;
		BufferedOutputStream dest = null;
		BufferedInputStream is = null;
		ZipEntry entry;
		ZipFile zipfile = new ZipFile(name);
		Enumeration e = zipfile.entries();
		progressBar.setValue(0);
		int max = zipfile.size();
		int curent = 0;
		System.out.println(max);
		System.out.println(name);
		(new File(root)).mkdir();
		System.out.println("asdfasdfasfas");
		
		while (e.hasMoreElements()) {

			entry = (ZipEntry) e.nextElement();
			setProgress((curent * 100) / max);
			curent++;
			System.out.println(curent);
			System.out.println((curent * 100) / max);
			System.out.println(entry.getName());
			System.out.println(e.hasMoreElements());
			if (entry.isDirectory()) {
				System.out.println("asdfasdfsdaasfdfsdafasfsad");
				(new File(root + entry.getName())).mkdir();
			} else {
				if (entry.getName().indexOf("/") != -1) {
					for (int i = 0; i < entry.getName().split("/").length - 1; i++) {
						String temp = root;
						for (int j = 0; j < i; j++) {
							temp += entry.getName().split("/")[j] + "/";
						}
						temp += entry.getName().split("/")[i];
						(new File(temp)).mkdir();
					}
				}
				(new File(root + entry.getName())).createNewFile();
				is = new BufferedInputStream(zipfile.getInputStream(entry));
				int count;
				byte data[] = new byte[BUFFER];
				FileOutputStream fos = new FileOutputStream(root + entry.getName());
				dest = new BufferedOutputStream(fos, BUFFER);
				while ((count = is.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}

				System.out.println("asdfasdfsdaasfdfsdafaasfsaffsafafasfasfsaffsad");
				dest.flush();
				dest.close();
				is.close();
			}
		}
		zipfile.close();
	}

	public static URL getFinalURL(String link) throws IOException {

		URL url = new URL(link);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setConnectTimeout(300000);
		con.setReadTimeout(300000);
		con.setInstanceFollowRedirects(false);
		con.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36");
		con.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
		con.addRequestProperty("Referer", "https://www.google.com/");
		con.connect();

		int resCode = con.getResponseCode();
		if (resCode == HttpURLConnection.HTTP_SEE_OTHER || resCode == HttpURLConnection.HTTP_MOVED_PERM
				|| resCode == HttpURLConnection.HTTP_MOVED_TEMP) {
			String Location = con.getHeaderField("Location");
			if (Location.startsWith("/")) {
				Location = url.getProtocol() + "://" + url.getHost() + Location;
			}
			return getFinalURL(Location);
		}
		return url;

	}

	private void downloadFile(String link,String name) throws MalformedURLException, IOException {
		URL url = getFinalURL(link);
		URLConnection conn = url.openConnection();

		conn.setConnectTimeout(300000);
		conn.setReadTimeout(300000);
		InputStream is = conn.getInputStream();
		long max = conn.getContentLengthLong();
		File temp = new File(name);
		temp.setReadable(true);
		temp.setWritable(true);
		temp.setExecutable(true);
		FileOutputStream temp2 = new FileOutputStream(temp);
		BufferedOutputStream fOut = new BufferedOutputStream(temp2);
		byte[] buffer = new byte[32 * 1024];
		int byteRead = 0;
		int in = 0;

		System.out.println(conn.getContentLengthLong());
		setProgress(0);
		int currentDown = 0;
		while ((byteRead = is.read(buffer)) != -1) {
			currentDown += buffer.length;

			setProgress((int) (currentDown / conn.getContentLengthLong() * 100));
			in += byteRead;
			fOut.write(buffer, 0, byteRead);
		}
		fOut.flush();
		fOut.close();

		is.close();

	}

	public void mainLaunch() throws IOException {

		saveLatelyVersion();
		
		Process proc = Runtime.getRuntime().exec("support.exe  -jar sellerprolab.jar");
	
		dispose();
		System.exit(0);
	}

	public boolean compareVersion() {
		pVersion = currentVersion();
		lately = latelyVersion();
		System.out.println(cVersion);
		System.out.println(latelyVersion());
		return pVersion.equals(lately);
	}

	public String latelyVersion() {
		try {
			URL url = new URL("http://15.165.175.121:3010/version/lately");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setConnectTimeout(60000);
			con.setReadTimeout(60000);
			con.setDoInput(true);
			con.setDoOutput(true);

			con.addRequestProperty("Content-Type", "application/json");

			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				StringBuilder sb = new StringBuilder();
				BufferedReader br;
				br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line).append("\n");
				}
				br.close();

				JsonParser parser = new JsonParser();
				JsonObject respone = (JsonObject) parser.parse(sb.toString());

				return respone.get("version").getAsString();
			} else {
				System.out.println("test");
				return null;
			}

		} catch (Exception e) {
			System.err.println(e.toString());
			return null;
		}
	}

	public void saveLatelyVersion() {
		try {
			OutputStream output = new FileOutputStream("readme.txt");
			JsonObject out = new JsonObject();
			
			out.addProperty("version", pVersion);
			out.addProperty("chrome", cVersion);
			System.out.println(out);
			byte[] by = out.toString().getBytes();
			output.write(by);
			output.close();
		} catch (Exception e) {

			e.getStackTrace();
		}
	}

	public String currentVersion() {

		try {
			// set the properties value
			// ���� ��ü ����
			File file = new File("readme.txt");
			// �Է� ��Ʈ�� ����
			FileReader filereader = new FileReader(file);
			int singleCh = 0;
			String temp = "";
			while ((singleCh = filereader.read()) != -1) {
				temp += (char) singleCh;
			}
			filereader.close();
			JsonParser parser = new JsonParser();
			JsonObject respone = (JsonObject) parser.parse(temp);
			
			cVersion =respone.get("chrome").isJsonNull() ? "100" : respone.get("chrome").getAsString() ;
			
			return (respone.get("version").isJsonNull()?"0.00001" :  respone.get("version").getAsString() );

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			// TODO: handle exception
			return "0.00001";
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e);
			return "0.00001";
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
			return "0.00001";
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {

		startX = e.getXOnScreen();
		startY = e.getYOnScreen();

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		currentX = currentX - (startX - e.getXOnScreen());
		currentY = currentY - (startY - e.getYOnScreen());
		startX = e.getXOnScreen();
		startY = e.getYOnScreen();
		setLocation(currentX, currentY);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub

	}
	
	public static ImageIcon resizeButtonImage(Rectangle rec, String buttonUrl) {
		ImageIcon temImg = new ImageIcon(buttonUrl);

		Image dimg = temImg.getImage();
		Image img = dimg.getScaledInstance((int) rec.getWidth(), (int) rec.getHeight(), Image.SCALE_SMOOTH);
		ImageIcon image = new ImageIcon(img);
		return image;
	}

}
