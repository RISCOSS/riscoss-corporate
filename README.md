# riscoss-corporate

Configure work environment – RISCOSS

1) Clone the following repositories in a same root directory: 

- https://github.com/RISCOSS/riscoss-data-collector

- https://github.com/RISCOSS/riscoss-analyser

- https://github.com/RISCOSS/riscoss-corporate 

2) In Eclipse, select File > Import > Existing Maven Project, and select the root directory where the repositories were cloned 

3) Install and proceed with all the required software Eclipse will ask to install while importing. 

4) Install GWT plugin in Eclipse in Help > Install New Software, introduce the following link: https://dl.google.com/eclipse/plugin/4.4 and select: 

- Google App Engine Tools for Maven (requires m2e-wtp 1.5+) 

- Google Plugin for Eclipse (required) 

- GWT Designer for GPE 

- SDKs 

5) Eclipse will detect some “Validation message” errors. These errors can be ignored: just right click on riscoss-webapp project, go to Properties > Validation, check “Enable project specific settings” and uncheck “Web (2.2 – 2.4) Validator”. Now no errors should appear. 

6) Now right click on riscoss-webapp project, go to Properties > Java Build Path > Source Tab, and configure the source folders to be like the images at the end of the document.

7) Then, in Libraries Tab, select Add Library and select the Google Web Toolkit to add. 

8) Right click on riscoss-webapp and go to Properties > Google > Web Toolkit and enable “Use Google Web Toolkit”.

9) Now go to Run > Run Configurations > Web Application 

In Main tab, change Main class to “com.google.gwt.dev.DevMode”

In GWT tab, check “Super Development Mode” 

In Arguments tab, you will need to specify Program Arguments: 

-superDevMode -startupUrl index.jsp -logLevel INFO -port 8888 -remoteUI "${gwt_remote_ui_server_port}:${unique_id}" -codeServerPort 9997 -war /path/to/RISCOSS/riscoss-corporate/riscoss-webapp/src/main/webapp eu.riscoss.RiscossWebApp eu.riscoss.analysis eu.riscoss.layers eu.riscoss.whatifanalysis eu.riscoss.entities eu.riscoss.models eu.riscoss.riskconfs eu.riscoss.report eu.riscoss.rdr eu.riscoss.riskanalysis eu.riscoss.ras eu.riscoss.admin eu.riscoss.auth

and VM Arguments:

-Xss2048k -Xmx2048m -XX:MaxPermSize=512m

10) Now, to check if everything is set up properly, right click on riscoss-webapp project,and go to Run As > Google Web Application (GWT Super Dev Mode). Select index.jsp and click on Run.
