package eu.riscoss.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

import eu.riscoss.db.RiscossDB;

public class DownloadServlet extends HttpServlet {

	private static final long serialVersionUID = -4857621467686120020L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String domain = request.getParameter("domain");
		String token = request.getParameter("token");
		RiscossDB db = null;
		try {
			
			db = DBConnector.openDB( domain, token );
			
			String name = request.getParameter("name");
			String type = request.getParameter("type");
			
			String blobFileName = "";
			byte[] blob;
			
			switch (type) {
			case "desc":
				// gets the description for the model
				blobFileName = db.getModelDescFielname(name);
				blob = db.getModelDescBlob(name);
				break;
			case "model":
				// gets the model
				blobFileName = db.getModelFilename(name);
				blob = db.getModelBlob(name).getBytes();
				break;
			case "xlsxEnt":
				//gets the xlsx entities file
				blobFileName = "Supersede_IPR_Registry.xlsx";
				File ent = new File("resources/Supersede_IPR_Registry.xlsx");
				blob = FileUtils.readFileToByteArray(ent);
				break;
			case "xmlConf":
				//gets the xml conf import file
				blobFileName = "Supersede_Config_Stored.xml";
				File conf = new File("resources/Supersede_Config_Stored.xml");
				blob = FileUtils.readFileToByteArray(conf);
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