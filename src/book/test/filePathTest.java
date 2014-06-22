package book.test;

import java.io.File;

import org.apache.hadoop.fs.Path;

public class filePathTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File file = new File("D:\\");
		Path dst = new Path("D:\\");
		System.out.println(file.getAbsolutePath());
	}

}
