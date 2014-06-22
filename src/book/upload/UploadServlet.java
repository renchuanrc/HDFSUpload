package book.upload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;


public class UploadServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		request.setCharacterEncoding("gb2312");
		response.setCharacterEncoding("gb2312");
		response.setContentType("text/html; charset=gb2312");
		
		// ����HttpSession����
		HttpSession session = request.getSession();
		
		if ("status".equals(request.getParameter("c"))) {// ���������c��ֵΪstatus
			doStatus(session, response);// ����doStatus����
		} else {// ���򣬵���doFileUpload����
			String fileURL = request.getParameter("fileURL");
			File file = new File(fileURL);
			doFileUpload(file, session, request, response);
		}
	}
	private void doFileUpload(File file, HttpSession session, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		boolean hasError = false;
		Configuration conf = new Configuration();
		conf.set("fs.default.name", "hdfs://" + HDFSProperties.ip + ":"
				+ HDFSProperties.port);
		FileSystem fs = FileSystem.get(conf);
		List<File> myfile = new ArrayList<File>();	
		listDirectory(file, myfile);
		int uploadedFile = 0;
		try {
			// ����UploadListener����
			UploadListener listener = new UploadListener(myfile.size());
			listener.start();// ��������״̬
			// �������������״̬������Session��
			session.setAttribute("FILE_UPLOAD_STATS", listener
					.getFileUploadStats());
			session.setAttribute("uploadedFile","0");
			// ֹͣʹ�ü�����
			listener.done();
			Path src = null;
			Path dst = null;
			for(File upFile : myfile) {
				src = new Path(upFile.toString());
				dst = new Path(conf.get("fs.default.name") + "/user/root/test/" + upFile.getName());
				uploadedFile = Integer.parseInt((String) session.getAttribute("uploadedFile"));
				fs.copyFromLocalFile(src, dst);
				uploadedFile += 1;
				session.setAttribute("uploadedFile", String.valueOf(uploadedFile));
			}
				UploadListener.FileUploadStats fileUploadStats = (UploadListener.FileUploadStats) session
					.getAttribute("FILE_UPLOAD_STATS");
				int totalFile = fileUploadStats.getTotalFile();
			
				session.setAttribute("uploadedFile",String.valueOf(totalFile));
				if (fs != null)
					try {
						fs.close();
					} catch (IOException e) {
						;
			}
			System.out.println(hasError);
			if (!hasError) {// ���û�г��ִ���
				sendCompleteResponse(response, null);// ����sendCompleteResponse����
			} else {
				sendCompleteResponse(response,
						"Could not process uploaded file. Please see log for details.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			sendCompleteResponse(response, e.getMessage());
		}
	}

	private void doStatus(HttpSession session, HttpServletResponse response)
			throws IOException {
		// ���ø���Ӧ���ڻ����ж�ȡ
		response.addHeader("Expires", "0");
		response.addHeader("Cache-Control",
				"no-store, no-cache, must-revalidate");
		response.addHeader("Cache-Control", "post-check=0, pre-check=0");
		response.addHeader("Pragma", "no-cache");
		// ��ñ�����Session�е�״̬��Ϣ
		UploadListener.FileUploadStats fileUploadStats = (UploadListener.FileUploadStats) session
				.getAttribute("FILE_UPLOAD_STATS");
		if (fileUploadStats != null) {
			//long +fileUploadStats.getBytesRead();
			//System.out.println(bytesProcessed);
			int fileProcessed = fileUploadStats.getUploadedFile();// ����Ѿ��ϴ����ļ�����
			System.out.println(fileProcessed);
			int fileTotal = fileUploadStats.getTotalFile();// ����ϴ��ļ����ܴ�С
			// �����ϴ���ɵİٷֱ�
			long percentComplete = (long) Math
					.floor(((double) fileProcessed / (double) fileTotal) * 100.0);
			// ����ϴ����õ�ʱ��
//			long timeInSeconds = fileUploadStats.getElapsedTimeInSeconds();
			// ����ƽ���ϴ�����
			//double uploadRate = bytesProcessed / (timeInSeconds + 0.00001);
			// �����ܹ�����ʱ��
			//double estimatedRuntime = sizeTotal / (uploadRate + 0.00001);
			// ���ϴ�״̬���ظ��ͻ���
			response.getWriter().println("<b>�ϴ�״̬:</b><br/>");
//			if (fileProcessed != fileTotal) {
//				response.getWriter().println(
//						"<div class=\"prog-border\"><div class=\"prog-bar\" style=\"width: "
//								+ fileProcessed + "��;\"></div></div>");
//				response.getWriter().println(
//						"���: " + fileProcessed + "�� �ܴ�С�� " + fileTotal
//								+ " �� ");
//				response.getWriter().println(
//						"��ʱ: " + formatTime(timeInSeconds) + "<br/>");
//			} else {
//				response.getWriter().println(
//						"���: " + fileProcessed + "�� �ܴ�С�� " + fileTotal
//								+ " ��<br/>");
//				response.getWriter().println("�ϴ����.<br/>");
//			}
			//�ļ��ϴ�
			if (fileUploadStats.getUploadedFile() == fileUploadStats
							.getTotalFile()) {
				//д�ļ�
				fileProcessed = Integer.parseInt((String)session.getAttribute("uploadedFile"));
				percentComplete = (long) Math
				.floor(((double) fileProcessed / (double) fileTotal) * 100.0);
				if (fileProcessed != fileTotal) {
					response.getWriter().println("<b>�����ϴ��ļ�... ...</b><br/>");
					response.getWriter().println(
							"<div class=\"prog-border\"><div class=\"prog-bar\" style=\"width: "
							+ percentComplete + "%;\"></div></div>");
					response.getWriter().println(
							"���ϴ�: " + fileProcessed + "�� �ܴ�С�� " + fileTotal
							+ " �� (" + percentComplete + "%) <br/>");
				} else {
					response.getWriter().println(
							"���ϴ�: " + fileProcessed + "�� �ܴ�С�� " + fileTotal
							+ " ��<br/>");
					response.getWriter().println("<b>�ϴ����.<b><br/>");
				}
			}
		}	
		
	}

	private void sendCompleteResponse(HttpServletResponse response,
			String message) throws IOException {
		if (message == null) {
			response
					.getOutputStream()
					.print(
							"<html><head><script type='text/javascript'>function killUpdate() { window.parent.killUpdate(''); }</script></head><body onload='killUpdate()'></body></html>");
		} else {
			response
					.getOutputStream()
					.print(
							"<html><head><script type='text/javascript'>function killUpdate() { window.parent.killUpdate('"
									+ message
									+ "'); }</script></head><body onload='killUpdate()'></body></html>");
		}
	}

	public static void listDirectory(File path, List<File> myfile) {
		if (!path.exists()) {
			System.out.println("�ļ����Ʋ�����!");
		} else {
			if (path.isFile()) {
				myfile.add(path);
			} else {
				File[] files = path.listFiles();
				for (int i = 0; i < files.length; i++) {
					listDirectory(files[i], myfile);
				}
			}
		}
	}
}
