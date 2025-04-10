package com.marvinformatics.easyjacoco.jacoco;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.csv.CSVFormatter;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;

/**
 * Configurable output formats for the report goals.
 */
public enum ReportFormat {

	/**
	 * Multi-page html report.
	 */
	HTML() {
		@Override
		public IReportVisitor createVisitor(File outputDirectory, String outputEncoding, final Locale locale,
				String footer) throws IOException {
			final HTMLFormatter htmlFormatter = new HTMLFormatter();
			htmlFormatter.setOutputEncoding(outputEncoding);
			htmlFormatter.setLocale(locale);
			if (footer != null) {
				htmlFormatter.setFooterText(footer);
			}
			return htmlFormatter.createVisitor(new FileMultiReportOutput(outputDirectory));
		}
	},

	/**
	 * Single-file XML report.
	 */
	XML() {
		@Override
		public IReportVisitor createVisitor(File outputDirectory, String outputEncoding, final Locale locale,
				String footer) throws IOException {
			final XMLFormatter xml = new XMLFormatter();
			xml.setOutputEncoding(outputEncoding);
			return xml.createVisitor(new FileOutputStream(new File(outputDirectory, "jacoco.xml")));
		}
	},

	/**
	 * Single-file CSV report.
	 */
	CSV() {
		@Override
		public IReportVisitor createVisitor(File outputDirectory, String outputEncoding, final Locale locale,
				String footer) throws IOException {
			final CSVFormatter csv = new CSVFormatter();
			csv.setOutputEncoding(outputEncoding);
			return csv.createVisitor(new FileOutputStream(new File(outputDirectory, "jacoco.csv")));
		}
	};

	public abstract IReportVisitor createVisitor(File outputDirectory, String outputEncoding, final Locale locale,
			String footer) throws IOException;

}
