package com.ibm.gts.dfs.appdparser;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class AppdparserApplication implements ApplicationRunner {

	public static void main(final String[] args) {
		SpringApplication.run(AppdparserApplication.class, args);
	}

	private static final int APP_NAME = 1; // B
	private static final int TIER_NAME = 2; // C

	Map<String, Set<String>> deps = new HashMap<String, Set<String>>();

	@Override
	public void run(final ApplicationArguments args) throws Exception {

		if (args.getOptionNames().isEmpty()) {
			log.info("Missing 'inputfile' argument");
			syntax();
		}

		final Set<String> argList = args.getOptionNames();
		for (final String arg : argList) {
			log.info("arg: {}={}", arg, args.getOptionValues(arg));
		}

		List<String> inputFileList = args.getOptionValues("inputfile");
		int argc = inputFileList.size();
		if (argc <= 0 || argc > 1) {
			log.info("Wrong number of arguments.");
			syntax();
		}

		String inputFileName = inputFileList.get(0);
		File inputFile = new File(inputFileName);
		if (!inputFile.exists()) {
			log.info("Provided file, {}, doesn't exist.", inputFile.getAbsolutePath());
			syntax();
		}

		log.info("Reading excel file {}...", inputFile.getAbsolutePath());

		final Workbook workbook = new XSSFWorkbook(new FileInputStream(inputFile));
		final Sheet sheet = workbook.getSheetAt(0);

		int i = 0;
		for (final Row row : sheet) {

			// skipping first row (headers)
			if (i == 0) {
				i++;
				continue;
			}

			final String appName = row.getCell(APP_NAME).getStringCellValue();
			final String tierName = row.getCell(TIER_NAME).getStringCellValue();

			Set<String> tiers = deps.get(appName);
			if (tiers == null) {
				tiers = new HashSet<String>();
				deps.put(appName, tiers);
			}

			tiers.add(tierName);
			i++;

			System.out.print("\rProcessing line " + i);

		} // Rows

		System.out.println();

		File outputFile = new File(inputFile.getName() + "_output.csv");
		log.info("Writing output file '{}' ...", outputFile.getAbsolutePath());

		try (PrintWriter pw = new PrintWriter(new FileWriter(outputFile))) {
			pw.println("app_name, app_tier, count");
			for (Map.Entry<String, Set<String>> entry : deps.entrySet()) {
				pw.printf("%s,\"", entry.getKey());
				i = 0;
				for (String tier : entry.getValue()) {
					if (i > 0) {
						pw.print(',');
					}
					pw.print(tier);
					i++;
				}
				pw.printf("\",%d\n",i);
			}
		}

	} // run

	private void syntax() {
		System.out.println("\nappdparser --inputfile <FILE>");
		System.out.println("\nwhere:");
		System.out.println("\t<FILE> input file to process");
		System.out.println("\nThe output will be an csv file with the input file name '<FILE>_output.csv'\n");
		System.exit(1);
	}

}
