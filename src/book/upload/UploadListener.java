 package book.upload;

public class UploadListener{
	public static final int totalSize = 0;
	// ����״̬���ڲ������
	private FileUploadStats fileUploadStats = new FileUploadStats();
	// ���췽��
	public UploadListener(int totalFile) {
		fileUploadStats.setTotalFile(totalFile);
	}
	public void start() {
		// ���õ�ǰ״̬Ϊ��ʼ
		fileUploadStats.setCurrentStatus("start");
	}
	public void uploadedFile(int fileCount) {
		// ���Ѷ�ȡ�����ݱ��浽״̬������
		fileUploadStats.incrementUploadedFile(fileCount);
		// ���õ�ǰ��״̬Ϊ��ȡ������
		fileUploadStats.setCurrentStatus("reading");
	}
	public void error(String s) {
		// ���õ�ǰ״̬Ϊ����
		fileUploadStats.setCurrentStatus("error");
	}
	public void done() {
		// ���õ�ǰ�Ѷ�ȡ���ݵ��������ݴ�С
		fileUploadStats.setUploadedFile(fileUploadStats.getTotalFile());
		// ���õ�ǰ״̬Ϊ���
		fileUploadStats.setCurrentStatus("done");
	}
	public FileUploadStats getFileUploadStats() {
		// ���ص�ǰ״̬����
		return fileUploadStats;
	}
	// ����״̬��
	public static class FileUploadStats {
		private int totalFile = 0;// �����ݵĴ�С
		private int uploadedFile = 0;// �Ѷ����ݴ�С
		private long startTime = System.currentTimeMillis();// ��ʼ��ȡ��ʱ��
		private String currentStatus = "none";// Ĭ�ϵ�״̬
		
		public int getTotalFile() {
			return totalFile;
		}
		public void setTotalFile(int totalFile) {
			this.totalFile = totalFile;
		}
		public int getUploadedFile() {
			return uploadedFile;
		}
		public void setUploadedFile(int uploadedFile) {
			this.uploadedFile = uploadedFile;
		}
		public long getElapsedTimeInSeconds()// ����Ѿ��ϴ���ʱ��
		{
			return (System.currentTimeMillis() - startTime) / 1000;
		}
		public String getCurrentStatus()// ����currentStatus��get����
		{
			return currentStatus;
		}
		public void setCurrentStatus(String currentStatus) {
			this.currentStatus = currentStatus;
		}
		public void incrementUploadedFile(int FileCount) {
			this.uploadedFile += FileCount;
		}
	}
}
