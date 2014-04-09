package uk.co.benjaminelliott.specserv;

import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import uk.co.benjaminelliott.spectrogramandroid.CapturedBitmapAudio;

public class SpectrogramServer {

	private Socket socket;
	private CapturedBitmapAudio cba;
	private String FILEPATH = "C:/wamp/www/specserv/captures";
	private static int PORT = 5353;
	
	private Connection connection;
	private PreparedStatement statement;
	private ResultSet resultSet;

	public SpectrogramServer(Socket socket) {
		this.socket = socket;
	}
	
	public void start() {
		connectDatabase();
		File storeDir = new File(FILEPATH);
		if (!storeDir.mkdirs()) { //create directory if it doesn't exist already
			System.err.println("Directory "+FILEPATH+" not created.");
		}
		objectReceiver().start();
	}

	public static void main(String[] args) {
		ServerSocket ss;
		try {
			ss = new ServerSocket(PORT);
			while (true) {
				Socket sock = ss.accept();
				System.out.println("Connection received!");
				SpectrogramServer spec = new SpectrogramServer(sock);
				spec.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Thread objectReceiver() {
		Thread ret = new Thread() {
			@Override
			public void run() {
				try {
					InputStream is = socket.getInputStream();
					ObjectInputStream ois = new ObjectInputStream(is);
					while (true) {
						try {
							cba = (CapturedBitmapAudio) ois.readObject();
							System.out.println("Object read from stream");
							insertIntoDatabase(cba);
						} catch (EOFException e) {
							System.out.println("No more to read from stream");
							is.close();
							ois.close();
							break;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}


			}
		};
		return ret;
	}
	
	private void connectDatabase() {
		System.out.println("Connecting to database...");
		try {
			//load MySQL driver:
			Class.forName("com.mysql.jdbc.Driver");
			//connect to database:
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/specschema","specserv","specserv");
			System.out.println("Successfully connected to database");
		} catch (ClassNotFoundException e) {
			System.err.println("Class not found!");
			e.printStackTrace();
		} catch (SQLException e) {
			System.err.println("SQL exception!");
			e.printStackTrace();
		}
	}
	
	private void insertIntoDatabase(CapturedBitmapAudio cba) {
		//insert the relevant information from the CapturedBitmapAudio object into the database of captures
		try {
			String species = cba.filename.toLowerCase(); //keep in lower case to make queries simpler
			double latitude = cba.decLatitude;
			double longitude = cba.decLongitude;
			
			//create a statement in which to package the insert query
			statement = connection.prepareStatement("insert into CAPTURES(species, latitude, longitude) values (?,?,?)",Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, species);
			statement.setDouble(2, latitude);
			statement.setDouble(3, longitude);

			statement.executeUpdate();
			System.out.println("Object information successfully inserted into database");

			//catch the ID assigned to the information in a result set:
			resultSet = statement.getGeneratedKeys();

			//once information inserted into database, store data into files with filename decided by unique id in table
			if (resultSet.next()) {
				int id = resultSet.getInt(1);
				System.out.println("Last ID: "+id);
				saveBitmapToJPEG(cba, Integer.toString(id));
				saveAudioToWAV(cba, Integer.toString(id));
				System.out.println("Object information successfully saved to files");
			}
			else System.err.println("No ID value returned from insert operation, can't store WAV and JPEG files!");

		} catch (SQLException e) {
			System.err.println("SQL exception!");
			e.printStackTrace();
		}
	}


	private void saveBitmapToJPEG(CapturedBitmapAudio cba, String filename) {
		int[] bitmapColours = cba.getBitmapRGBPixels();
		int width = cba.bitmapWidth;
		int height = cba.bitmapHeight;
		BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		bi.setRGB(0, 0, width, height, bitmapColours, 0, width);
// 	    Following code just displays spectrogram when received
//		ImageIcon icon = new ImageIcon(bi);
//		JLabel label = new JLabel(icon);
//		JOptionPane.showMessageDialog(null, label);
		File outputFile = new File(FILEPATH+"\\"+filename+".jpg");
		try {
			ImageIO.write(bi, "BMP", outputFile);
			System.out.println("JPEG file stored successfully.");
		} catch (IOException e) {
			System.err.println("Failed to store JPEG file.");
			e.printStackTrace();
		}
	}
	
	private void saveAudioToWAV(CapturedBitmapAudio cba, String filename) {
		try {
			File outputFile = new File(FILEPATH+"\\"+filename+".wav");
			FileOutputStream fos = new FileOutputStream(outputFile);
			fos.write(cba.wavAsByteArray);
			fos.close();
			System.out.println("WAV file stored successfully.");
		}  catch (IOException e) {
			System.err.println("Failed to store WAV file.");
			e.printStackTrace();
		}
	}
}