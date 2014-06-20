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
		
		// 创建HttpSession对象
		HttpSession session = request.getSession();
		
		if ("status".equals(request.getParameter("c"))) {// 如果请求中c的值为status
			doStatus(session, response);// 调用doStatus方法
		} else {// 否则，调用doFileUpload方法
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
			// 创建UploadListener对象
			UploadListener listener = new UploadListener(myfile.size());
			listener.start();// 启动监听状态
			// 将监听器对象的状态保存在Session中
			session.setAttribute("FILE_UPLOAD_STATS", listener
					.getFileUploadStats());
			session.setAttribute("uploadedFile","0");
			// 停止使用监听器
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
			if (!hasError) {// 如果没有出现错误
				sendCompleteResponse(response, null);// 调用sendCompleteResponse方法
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
		// 设置该响应不在缓存中读取
		response.addHeader("Expires", "0");
		response.addHeader("Cache-Control",
				"no-store, no-cache, must-revalidate");
		response.addHeader("Cache-Control", "post-check=0, pre-check=0");
		response.addHeader("Pragma", "no-cache");
		// 获得保存在Session中的状态信息
		UploadListener.FileUploadStats fileUploadStats = (UploadListener.FileUploadStats) session
				.getAttribute("FILE_UPLOAD_STATS");
		if (fileUploadStats != null) {
			//long +fileUploadStats.getBytesRead();
			//System.out.println(bytesProcessed);
			int fileProcessed = fileUploadStats.getUploadedFile();// 获得已经上传的文件个数
			System.out.println(fileProcessed);
			int fileTotal = fileUploadStats.getTotalFile();// 获得上传文件的总大小
			// 计算上传完成的百分比
			long percentComplete = (long) Math
					.floor(((double) fileProcessed / (double) fileTotal) * 100.0);
			// 获得上传已用的时间
//			long timeInSeconds = fileUploadStats.getElapsedTimeInSeconds();
			// 计算平均上传速率
			//double uploadRate = bytesProcessed / (timeInSeconds + 0.00001);
			// 计算总共所需时间
			//double estimatedRuntime = sizeTotal / (uploadRate + 0.00001);
			// 将上传状态返回给客户端
			response.getWriter().println("<b>上传状态:</b><br/>");
//			if (fileProcessed != fileTotal) {
//				response.getWriter().println(
//						"<div class=\"prog-border\"><div class=\"prog-bar\" style=\"width: "
//								+ fileProcessed + "个;\"></div></div>");
//				response.getWriter().println(
//						"完成: " + fileProcessed + "个 总大小： " + fileTotal
//								+ " 个 ");
//				response.getWriter().println(
//						"用时: " + formatTime(timeInSeconds) + "<br/>");
//			} else {
//				response.getWriter().println(
//						"完成: " + fileProcessed + "个 总大小： " + fileTotal
//								+ " 个<br/>");
//				response.getWriter().println("上传完成.<br/>");
//			}
			//文件上传
			if (fileUploadStats.getUploadedFile() == fileUploadStats
							.getTotalFile()) {
				//写文件
				fileProcessed = Integer.parseInt((String)session.getAttribute("uploadedFile"));
				percentComplete = (long) Math
				.floor(((double) fileProcessed / (double) fileTotal) * 100.0);
				if (fileProcessed != fileTotal) {
					response.getWriter().println("<b>正在上传文件... ...</b><br/>");
					response.getWriter().println(
							"<div class=\"prog-border\"><div class=\"prog-bar\" style=\"width: "
							+ percentComplete + "%;\"></div></div>");
					response.getWriter().println(
							"已上传: " + fileProcessed + "个 总大小： " + fileTotal
							+ " 个 (" + percentComplete + "%) <br/>");
				} else {
					response.getWriter().println(
							"已上传: " + fileProcessed + "个 总大小： " + fileTotal
							+ " 个<br/>");
					response.getWriter().println("<b>上传完成.<b><br/>");
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
			System.out.println("文件名称不存在!");
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
