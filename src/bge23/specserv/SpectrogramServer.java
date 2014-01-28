package bge23.specserv;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.w3c.dom.Element;

import bge23.spectrogramandroid.CapturedBitmapAudio;

public class SpectrogramServer {

	private Socket socket;
	private CapturedBitmapAudio cba;
	private String FILEPATH = "C:/Users/Ben/test.bmp";
	private static int PORT = 5353;

	public SpectrogramServer(Socket socket) {
		this.socket = socket;
	}
	
	public void start() {
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
						cba = (CapturedBitmapAudio) ois.readObject();
						System.out.println("Object read from stream");
						saveBitmapToJPEG(cba, FILEPATH);
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


	private void saveBitmapToJPEG(CapturedBitmapAudio cba, String filepath) {
		int[] bitmapColours = cba.getBitmapRGBPixels();
		int width = cba.getBitmapWidth();
		int height = cba.getBitmapHeight();
		BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		bi.setRGB(0, 0, width, height, bitmapColours, 0, width);
		ImageIcon icon = new ImageIcon(bi);
		JLabel label = new JLabel(icon);
		JOptionPane.showMessageDialog(null, label);
		File outputFile = new File(FILEPATH);
		try {
			ImageIO.write(bi, "BMP", outputFile);
			//IIOImage iio = new IIOImage(bi,null, IIOMetadata metadata)
			
//			FileOutputStream fout = new FileOutputStream(outputFile);
//
//            fout.close();


		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}