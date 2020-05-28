package ch.ethz.semdwhsearch.prototyp1.tools.request;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import ch.ethz.semdwhsearch.prototyp1.tools.ServletTools;

/**
 * A class to handle POST requests.
 * 
 * @author Lukas Blunschi
 * 
 */
public class PostRequest implements Request {

	private Map<String, String> formFields;

	public PostRequest() {
		formFields = new HashMap<String, String>();
	}

	public PostRequest(HttpServletRequest req) throws Exception {
		this();
		parse(req, null, false);
	}

	public String getParameter(String name) {
		return getFormField(name);
	}

	// --------------------------------------------------------- helper methods

	@SuppressWarnings("unchecked")
	public void parse(HttpServletRequest request, String uploadPath, boolean allowUpload) throws Exception {

		// Check that we have a file upload request
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (isMultipart) {

			// Create a factory for disk-based file items
			FileItemFactory factory = new DiskFileItemFactory();

			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);

			// Parse the request
			List<FileItem> items = upload.parseRequest(request);

			// Process the uploaded items
			Iterator<FileItem> iter = items.iterator();
			while (iter.hasNext()) {
				FileItem item = iter.next();
				if (item.isFormField()) {

					// process form field
					String name = item.getFieldName();
					String value = item.getString("utf-8");
					formFields.put(name, value);
				} else {

					// process file upload
					String fieldName = item.getFieldName();
					String fileName = item.getName();
					// String contentType = item.getContentType();
					// boolean isInMemory = item.isInMemory();
					long sizeInBytes = item.getSize();

					// checks
					boolean fileSelected = fileName != null && fileName.length() > 0 && sizeInBytes > 0;

					// if everything is okay
					if (uploadPath != null && allowUpload && fileSelected) {

						// remove any path information
						fileName = ServletTools.removePathInformation(fileName);
						fileName = fileName.replaceAll(" ", "_");

						// put into upload directory
						new File(uploadPath).mkdirs();
						File uploadedFile = new File(uploadPath + fileName);
						// String cPath = uploadedFile.getCanonicalPath();
						item.write(uploadedFile);

						// store filename in form fields
						formFields.put(fieldName, fileName);
					}
				}
			}

		} else {
			throw new Exception("Not a multipart upload.");
		}
	}

	public String getFormField(String name) {
		return formFields.get(name);
	}

}
