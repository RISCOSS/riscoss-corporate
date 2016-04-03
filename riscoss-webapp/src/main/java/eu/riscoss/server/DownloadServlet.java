package eu.riscoss.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.riscoss.db.RiscossDB;

public class DownloadServlet extends HttpServlet {

	private static final long serialVersionUID = -4857621467686120020L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String domain = request.getParameter("domain");
		String token = request.getParameter("token");
		RiscossDB db = null;
		try {
			
			db = DBConnector.openDB( domain, token );
			
			String modelName = request.getParameter("name");
			String type = request.getParameter("type");
			
			String blobFileName = "";
			byte[] blob;
			
			switch (type) {
			case "desc":
				// gets the description for the model
				blobFileName = db.getModelDescFielname(modelName);
				blob = db.getModelDescBlob(modelName);
				break;
			case "model":
				// gets the model
				blobFileName = db.getModelFilename(modelName);
				blob = db.getModelBlob(modelName).getBytes();
				break;
			case "ras":
				//gets the ras report
				blobFileName = modelName + ".xml";
				blob = db.getXMLReport(request.getParameter("rasId")).getBytes();
				break;
			default:
				return;
			}

			response.setContentType("application/download");
			response.setHeader("Content-Disposition", "attachment; filename="+blobFileName+";");
			response.getOutputStream().write(blob);

			System.out.println("File "+blobFileName+ " sent.");
			
		} catch (Exception e) {
			throw new ServletException(e);
		} finally {
			DBConnector.closeDB( db );
		}
	}
	
}