/*
   (C) Copyright 2013-2016 The RISCOSS Project Consortium
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

/**
 * @author 	Alberto Siena
 **/

package eu.riscoss.server;

import eu.riscoss.db.RiscossDB;
import eu.riscoss.shared.RiscossUtil;
import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;
import gwtupload.shared.UConsts;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

public class UploadServiceImpl extends UploadAction {
	
	interface Action {
		public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) throws UploadActionException, Exception;
	}
	
	private static final long serialVersionUID = 1L;
	
	Map<String,Action> actions = new HashMap<String,Action>();
	
	class ModelUploader implements Action {
		
		@Override
		public String executeAction(
				HttpServletRequest request,
				List<FileItem> sessionFiles 
				) throws UploadActionException, Exception {
			String response = "";
			
			String domain = request.getParameter("domain");
			String token = request.getParameter("token");
			
			RiscossDB db = null;
			
			try {
				db = DBConnector.openDB( domain, token );
				
				for (FileItem item : sessionFiles) {
					if (false == item.isFormField()) {
						try {
							String name = request.getParameter( "name" );
							
							if( name == null ) {
								name = item.getName();
							}
							/// Create a temporary file placed in the default system temp folder
							//						File file = File.createTempFile("upload-", ".bin");
							//System.out.println("Storing the Model "+name);
							
							//attention:filename sanitation is not directly notified to the user
							name = RiscossUtil.sanitize(name);
							
							boolean duplicate = false;
							for (String storedmodel : db.getModelList()) {
								//System.out.println(storedmodel);
								if (storedmodel.equals(name)){
									duplicate = true;
									//response = "<response>\n" + "Duplicate model name. Please delete the stored model first."+ "</response>\n";
									response = "A model with this name was already uploaded. Please update this model on the model's properties page, change its name or delete it.";
									break;
									//throw new UploadActionException("Duplicate model name. Please delete the stored model first.");
								}
							}
							if (duplicate == false){
								db.storeModel( item.getString(), name );
								response = "Success";
							}
							
						} catch (Exception e) {
							throw new UploadActionException(e);
						}
					}
				}
				
				DBConnector.closeDB( db );
				
				/// Remove files from session because we have a copy of them
				removeSessionFileItems(request);
				
				/// Send information of the received files to the client.
				return response;
			} catch (Exception e1) {
				throw e1;
			}
			finally {
				DBConnector.closeDB( db );
			}
		}
		
	}
	
	class ModelDescUploader implements Action {
		
		@Override
		public String executeAction(
				HttpServletRequest request,
				List<FileItem> sessionFiles
				) throws Exception {
			
			String response = "";
			
			String domain = request.getParameter("domain");
			String token = request.getParameter("token");
			
			RiscossDB db = null;
			
			try {
				db = DBConnector.openDB( domain, token );
				
				String modelName = null;
				for (FileItem item : sessionFiles) { //reads the hidden field in which the model name is passed
					if (item.isFormField()){
						if (item.getFieldName().startsWith("Modelname")){
							modelName = item.getString();
							break;
						}
					} 
				}
				
				for (FileItem item : sessionFiles) {
					if (!item.isFormField()){
						try {
							String name = request.getParameter( "name" ); //the file name, used only to propose a filename when downloading
							if( name == null ) {
								name = item.getName();
							}
							//attention:filename sanitation is not directly notified to the user
							name = RiscossUtil.sanitize(name);
							modelName = RiscossUtil.sanitize(modelName);
							//store description for the model. Overwrites an existing description
							db.storeModelDesc(modelName, name, item.get());
							
							response = name + " uploaded.";
							
						} catch (Exception e) {
							throw new UploadActionException(e);
						}
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				throw e1;
			}
			finally {
				try {
					/// Remove files from session because we have a copy of them
					removeSessionFileItems(request);
				} catch( Exception ex ) {}
				
				DBConnector.closeDB( db );
			}
			
			return response;
		}
		
	}
	
	class ModelUpdateUploader implements Action {
		
		@Override
		public String executeAction(
				HttpServletRequest request,
				List<FileItem> sessionFiles
				) throws Exception {
			String response = "";
			
			String domain = request.getParameter("domain");
			String token = request.getParameter("token");
			
			RiscossDB db = null;
			
			try {
			db = DBConnector.openDB( domain, token );
			
			String modelName = null;
			for (FileItem item : sessionFiles) { //reads the hidden field in which the model name is passed
				if (item.isFormField()){
					if (item.getFieldName().startsWith("modelname")){
						modelName = item.getString();
						break;
					}
				} 
			}
			
			for (FileItem item : sessionFiles) {
				if (!item.isFormField()){
					try {
						String filename = request.getParameter( "name" ); //the file name, used only to propose a filename when downloading
						if( filename == null ) {
							filename = item.getName();
						}
						//attention:filename sanitation is not directly notified to the user
						filename = RiscossUtil.sanitize(filename);
						modelName = RiscossUtil.sanitize(modelName);
						//store description for the model. Overwrites an existing description
						
						db.updateModel(modelName, filename, item.getString());
						
						response = filename + " uploaded.";
						
					} catch (Exception e) {
						throw new UploadActionException(e);
					}
				}
			}
			} catch (Exception e1) {
				e1.printStackTrace();
				throw e1;
			}
			finally {
				/// Remove files from session because we have a copy of them
				try {
					removeSessionFileItems(request);
				} catch( Exception ex ) {}
				
				DBConnector.closeDB( db );
			}
			
			return response;
		}
		
	}
	
	
	public UploadServiceImpl() {
		actions.put( "modelblob", new ModelUploader() );
		actions.put( "modeldescblob", new ModelDescUploader() );
		actions.put( "modelupdateblob", new ModelUpdateUploader() );
	}
	
	/**
	 * Override executeAction to save the received files in a custom place
	 * and delete this items from session.  
	 */
	@Override
	public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) throws UploadActionException {
		String actionType = request.getParameter( "t" );
		if( actionType == null ) return null;
		
		Action action = actions.get( actionType );
		if( action != null ) {
			try {
				return action.executeAction(request, sessionFiles);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException( e );
			}
		}
		return "";
	}
	
	/**
	 * Get the content of an uploaded file. (mm: Not used(??))
	 */
	@Override
	public void getUploadedFile(
			HttpServletRequest request, 
			HttpServletResponse response
			) throws IOException {
		
		String fieldName = request.getParameter( UConsts.PARAM_SHOW );
		String domain = request.getParameter("domain");		
		String token = request.getParameter("token");
		
		RiscossDB db = null;
		try {
			db = DBConnector.openDB( domain, token );
			//only for checking if empty?
			String blob = db.getModelBlob( fieldName );
			if( blob == null ) blob = "";
			response.setContentType( "application/xml" );
			copyFromInputStreamToOutputStream( new ByteArrayInputStream( blob.getBytes() ), response.getOutputStream());
		} 
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException( e );
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	/**
	 * (mm: Not used(??) - note:called in doGet(?))
	 */
	@Override
	public void removeItem( HttpServletRequest request, String fieldName )  throws UploadActionException {
		String domain = request.getParameter("domain");		
		String token = request.getParameter("token");
		
		RiscossDB db = null;
		try {
			db = DBConnector.openDB( domain, token );
			db.removeModelBlob( fieldName );
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException( e );
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
}