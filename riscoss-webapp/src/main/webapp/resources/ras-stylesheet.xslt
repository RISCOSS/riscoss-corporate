<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- Recursive templates for arguments -->
	<xsl:template match="subArgs">
		<xsl:if test="*">
			<ul>
				<li><xsl:apply-templates select="argument" /></li>
			</ul>
		</xsl:if>
	</xsl:template>
	<xsl:template match="argument">
		<xsl:value-of select="summary"/><br/>
		<xsl:apply-templates select="subArgs"/>
	</xsl:template>

	<!-- Recursive templates for results -->
	<xsl:template match="event">
		<li>
			<strong><xsl:value-of select="label"/>: </strong>
			<xsl:value-of select="description"/><br/><br/>
			<strong><i>Exposure = </i></strong><xsl:value-of select="exposure"/>%<br/><br/>
			<xsl:if test="argument/summary">
				<xsl:apply-templates select="argument"/>
				<br/>
			</xsl:if>
		</li>
	</xsl:template>

	<xsl:template match="child">
		<h2><xsl:value-of select="entity"/> entity</h2>
		<hr/>
		<xsl:if test="res/event">
			<ul>
				<xsl:apply-templates select="res"/>
			</ul>
		</xsl:if>
		<xsl:if test="not(res/event)">
			<p class="comment">There are no risk or goals related with entity <xsl:value-of select="entity"/> for this risk analysis session.</p>
		</xsl:if>
	</xsl:template>

	<!-- Template for root-->
	<xsl:template match="/">
		<html>
			<head>
				<style media="screen" type="text/css">
					body {
						font-family: sans-serif;
						color: #686868;
						padding: 24px;
					}

					h1, h2 {
						color: #6E9C20;
						margin-bottom: 0;
					}

					h3, h4, h5, h6 {
						color: #7EAC30;
						margin-bottom: 0;
					}

					hr {
						color: #7EAC30;
					}

					table {
						margin-top: 24px;
						margin-bottom: 24px;
						border-collapse: collapse;
					}

					th {
						text-align: left;
						color: #686868;
						background-color: #e8e8e8;
						border: 1px solid #c8c8c8;
						padding: 6px;
					}

					td {
						border: 1px solid #c8c8c8;
						padding: 6px;
					}

					strong {
						color: #7EAC30;
					}

					.comment {
						font-style: italic;
						color: #787878;
					}

				</style>
			</head>
			<body>
				<xsl:for-each select="riscoss/risksession">
					<h1><xsl:value-of select="@label" /></h1>
					<hr/>
					<table>
						<tr>
							<th>Target</th>
							<td><xsl:value-of select="target"/></td>
						</tr>
						<tr>
							<th>RC</th>
							<td><xsl:value-of select="rc"/></td>
						</tr>
						<tr>
							<th>Last execution time</th>
							<td><xsl:value-of select="timestamp"/></td>
						</tr>
						<tr>
							<th>Models</th>
							<td>
								<xsl:for-each select="models/model">
									<xsl:value-of select="name"/><br/>
								</xsl:for-each>
							</td>
						</tr>
					</table>
					<xsl:if test="results/@type='evidence'">
						<h2><xsl:value-of select="results/entity"/> entity</h2>
						<hr/>
						<ul>
							<xsl:apply-templates select="results/res"/>
						</ul>
						<xsl:apply-templates select="results/children"/>
					</xsl:if>
					<xsl:if test="results/@type='distribution'">
						<h2><xsl:value-of select="results/entity"/> entity</h2>
						<hr/>
						<ul>
							<xsl:for-each select="results/res/event">
								<li>
									<strong><xsl:value-of select="@id"/></strong><br/><br/>
									Values:<br/>
									<ul>
										<xsl:for-each select="values/value">
											<li><xsl:value-of select="."/>%</li>
										</xsl:for-each>
									</ul>
									<br/>
								</li>
							</xsl:for-each>
						</ul>
					</xsl:if>
				</xsl:for-each>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>