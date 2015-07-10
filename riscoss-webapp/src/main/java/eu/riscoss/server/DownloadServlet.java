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
		RiscossDB db = DBConnector.openDB();
		try {
			String modelName = request.getParameter("name");

			// gets the description for the model
			String blobFileName = db.getModelDescFielname(modelName);

			byte[] blob = db.getModelDescBlob(modelName);
			response.setContentType("application/download");
			response.setHeader("Content-Disposition", "attachment; filename="+blobFileName+";");
			response.getOutputStream().write(blob);

			//System.out.println("File "+blobFileName+ " exported.");
			
		} catch (Exception e) {
			throw new ServletException(e);
		} finally {
			DBConnector.closeDB( db );
		}
	}
	
}